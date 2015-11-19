package pl.trebor.instanttranslator;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import pl.trebor.freegoogletranslate.Language;
import pl.trebor.freegoogletranslate.TranslateResult;
import pl.trebor.instanttranslator.asynctask.AsyncTaskResult;
import pl.trebor.instanttranslator.asynctask.TranslateTask;
import pl.trebor.instanttranslator.views.InterceptFrameLayout;
import pl.trebor.instanttranslator.views.LinkTouchMovementMethod;
import pl.trebor.instanttranslator.views.TouchableSpan;

public class InstantTranslateService extends Service implements TranslateTask.TranslateTaskCallback {

    public static final String TAG = InstantTranslateService.class.getSimpleName();

    public static final String WHITESPACES_REGEX = "\\s+";
    public static final String SPECIFIC_SIGNS_REGEX = "[\\.,]";

    public InstantTranslateService() {
    }

    private WindowManager windowManager;
    private ClipboardManager clipboardManager;
    private TranslateWindow translateWindow;
    private TextToSpeech textToSpeech;


    private final ClipboardManager.OnPrimaryClipChangedListener clipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
        @Override
        public void onPrimaryClipChanged() {
            clipboardChange();
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(clipChangedListener);
        translateWindow = new TranslateWindow();
        initTextToSpeech();
    }

    private class TranslateWindow implements PopupMenu.OnMenuItemClickListener {
        public static final int DP_Y_VALUE = 48 + 10;
        public static final int WORD_LIMIT = 7;
        public static final int SIMILAR_TRANSLATED_WORD_LIMIT = 3;
        private InterceptFrameLayout mParentView;
        private TextView translateView;

        private FrameLayout translateLayout;
        private ProgressBar progressBar;
        private boolean isLayoutDisplay;
        private PopupWindow similarWordsPopupWindow;
        private TranslateResult translateResult;

        private Animation animationClose;
        private Animation animationOpen;
        private Animation animationShowResult;

        private final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        TranslateWindow() {
            createTranslatePopupLayout();
        }

        private void setInitialParams() {
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            params.y = (int) applyDimension(DP_Y_VALUE);
        }

        private void initAnimations() {
            animationOpen = AnimationUtils.loadAnimation(InstantTranslateService.this, R.anim.popin);
            animationShowResult = AnimationUtils.loadAnimation(InstantTranslateService.this, R.anim.popout);
            animationShowResult.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    translateView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    clipboardManager.addPrimaryClipChangedListener(clipChangedListener);
                    translateLayout.startAnimation(animationOpen);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            animationClose = AnimationUtils.loadAnimation(InstantTranslateService.this, R.anim.popout);
            animationClose.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    if (similarWordsPopupWindow != null && similarWordsPopupWindow.isShowing()) {
                        similarWordsPopupWindow.dismiss();
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    windowManager.removeView(mParentView);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }

        private void createTranslatePopupLayout() {
            mParentView = new InterceptFrameLayout(InstantTranslateService.this);
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            translateLayout = (FrameLayout) layoutInflater.inflate(R.layout.translate_view, mParentView, false);
            translateView = (TextView) translateLayout.findViewById(R.id.translated_text_tv);
            translateView.setMovementMethod(new LinkTouchMovementMethod());
            translateView.setHighlightColor(getResources().getColor(android.R.color.transparent));
            translateView.setLinkTextColor(getResources().getColor(android.R.color.white));
            progressBar = (ProgressBar) translateLayout.findViewById(R.id.progressBar);

            initAnimations();
            initCloseButton();
            setInitialParams();

            mParentView.addView(translateLayout);
            mParentView.setOnTouchListener(new View.OnTouchListener() {
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (!mParentView.mIsDownEventDispatched) {
                        event = mParentView.motionEventDown;
                        mParentView.mIsDownEventDispatched = true;
                    }
                    final int actionMasked = event.getActionMasked();
                    switch (actionMasked) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            return true;
                        case MotionEvent.ACTION_UP:
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(mParentView, params);
                            return true;
                    }
                    return false;
                }
            });
        }

        private void initCloseButton() {
            ImageButton closeBtn = (ImageButton) translateLayout.findViewById(R.id.close_btn);
            closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isLayoutDisplay = false;
                    translateLayout.startAnimation(animationClose);
                }
            });
        }

        private void showSimilarTranslationPopup() {
            if (similarWordsPopupWindow != null && similarWordsPopupWindow.isShowing()) {
                similarWordsPopupWindow.dismiss();
            }
            LinearLayout mainView = new LinearLayout(InstantTranslateService.this);
            mainView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mainView.setOrientation(LinearLayout.VERTICAL);
            mainView.setBackgroundResource(R.drawable.popup_background);

            int pxPadding = (int) applyDimension(5);
            int dividerHeight = (int) applyDimension(1);

            mainView.setPadding(pxPadding, pxPadding, pxPadding, pxPadding);

            float maxTextWidth = 0;
            Set<Map.Entry<String, ArrayList<String>>> entrySet = this.translateResult.getSimilarTranslation().entrySet();
            Iterator<Map.Entry<String, ArrayList<String>>> iterator = entrySet.iterator();
            int wordLimitCounter = 0; // words limit, because to many word is unnecessary
            while (wordLimitCounter < WORD_LIMIT && iterator.hasNext()) {
                wordLimitCounter++;
                Map.Entry<String, ArrayList<String>> entry = iterator.next();
                LinearLayout row = new LinearLayout(InstantTranslateService.this);
                row.setOrientation(LinearLayout.VERTICAL);

                TextView similarTv = new TextView(InstantTranslateService.this);
                similarTv.setTextColor(getResources().getColor(R.color.white));
                similarTv.setTypeface(null, Typeface.BOLD);
                similarTv.setText(entry.getKey());
                row.addView(similarTv);

                ArrayList<String> words = entry.getValue();
                int length = (words.size() > SIMILAR_TRANSLATED_WORD_LIMIT) ? SIMILAR_TRANSLATED_WORD_LIMIT : words.size();
                StringBuilder nounsSb = new StringBuilder();
                for (int i = 0; i < length; i++) {
                    nounsSb.append(words.get(i)).append(", ");
                }
                nounsSb.delete(nounsSb.length() - 2, nounsSb.length() - 1);
                Log.d(TAG, "Raw similar words: " + nounsSb.toString());
                TextView nouns = new TextView(InstantTranslateService.this);
                nouns.setTextColor(getResources().getColor(R.color.white));
                nouns.setText(nounsSb.toString());

                float tWidth = nouns.getPaint().measureText(nounsSb.toString());
                Log.d(TAG, "Text width: " + Float.toString(tWidth));
                if (tWidth > maxTextWidth) {
                    maxTextWidth = tWidth;
                }

                row.addView(nouns);
                mainView.addView(row);
            }

            Log.d(TAG, "Text maxTextWidth: " + maxTextWidth);
            int dividerWidth = (int) (maxTextWidth + 10); // 10 is postfix margin, it prevent from trunc last word on display window
        /* add appropriate divider*/
            for (int i = 0, j = 1; i < wordLimitCounter - 1; i++, j = j + 2) {
                View divider = new View(InstantTranslateService.this);
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(dividerWidth, dividerHeight);
                layoutParams.width = dividerWidth;
                divider.setLayoutParams(layoutParams);
                divider.setBackgroundColor(getResources().getColor(R.color.lite_blue));
                mainView.addView(divider, j);
            }

            mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    similarWordsPopupWindow.dismiss();
                }
            });

            similarWordsPopupWindow = new PopupWindow(mainView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            similarWordsPopupWindow.setContentView(mainView);
            similarWordsPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            similarWordsPopupWindow.setOutsideTouchable(true);
            similarWordsPopupWindow.showAsDropDown(mParentView);
        }

        private void showPopupMenu() {
            PopupMenu popup = new PopupMenu(InstantTranslateService.this, mParentView);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.translate_word_menu, popup.getMenu());
            popup.getMenu().findItem(R.id.show_popup_window).setEnabled(this.translateResult.isSimilarTranslationExist());
            popup.setOnMenuItemClickListener(this);
            popup.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.speak:
                    runSpeech();
                    return true;
                case R.id.show_popup_window:
                    showSimilarTranslationPopup();
                    return true;
                case R.id.add_anki_flashcard:
                    addToAnkiDroid();
                    return true;
                default:
                    return false;
            }
        }

        public void showProgressState() {
            if (similarWordsPopupWindow != null && similarWordsPopupWindow.isShowing()) {
                similarWordsPopupWindow.dismiss();
            }
            translateView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            if (!isLayoutDisplay) {
                setInitialParams();
                windowManager.addView(mParentView, params);
                isLayoutDisplay = true;
                translateLayout.startAnimation(AnimationUtils.loadAnimation(InstantTranslateService.this, R.anim.popin));
            }
        }

        public void showResultState(AsyncTaskResult<TranslateResult> result) {
            final Exception exception = result.getException();
            if (exception != null) {
                if (exception instanceof UnknownHostException) {
                    setErrorMessage(R.string.error_check_your_internet_connection);
                } else {
                    setErrorMessage(R.string.error_while_translating);
                }
                Log.d(TAG, "Error occur: " + exception.getMessage(), exception);
            } else if (result.getResult() != null && !TextUtils.isEmpty(result.getResult().getTranslation())) {
                this.translateResult = result.getResult();
                setTranslation();
            } else {
                setErrorMessage(R.string.no_translation);
            }

            translateLayout.startAnimation(animationShowResult);
        }

        private void setErrorMessage(int messageId) {
            translateView.setText(getString(messageId));
        }

        private void setTranslation() {
            SpannableString text = new SpannableString(this.translateResult.getTextToTranslate() + " - " + this.translateResult.getTranslation());

            ClickableSpan touchableSpan = new TouchableSpan(getResources().getColor(R.color.white), getResources().getColor(R.color.lite_blue)) {
                @Override
                public void onClick(View widget) {
                    showPopupMenu();
                }
            };

            text.setSpan(touchableSpan, 0, this.translateResult.getTextToTranslate().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            translateView.setText(text);
        }

        public void releaseParentView() {
            if (mParentView != null && isLayoutDisplay) {
                windowManager.removeView(mParentView);
            }
        }

        public TranslateResult getTranslateResult() {
            return translateResult;
        }
    }

    private void initTextToSpeech() {
        final SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.option_file_name), Context.MODE_PRIVATE);
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(new Locale(sharedPreferences.getString(StartActivity.LANGUAGE_FROM, Language.ENGLISH)));
                } else {
                    Toast.makeText(InstantTranslateService.this, getString(R.string.texttospeech_initialization_error), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private float applyDimension(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }


    private void runSpeech() {
        String textToTranslateToShow = translateWindow.getTranslateResult().getTextToTranslate();
        if (textToTranslateToShow != null) {
            if (Build.VERSION.SDK_INT >= 21) {
                textToSpeech.speak(textToTranslateToShow, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                textToSpeech.speak(textToTranslateToShow, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }


    private void addToAnkiDroid() {
        TranslateResult translateResult = translateWindow.getTranslateResult();
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction("org.openintents.action.CREATE_FLASHCARD");
        intent.putExtra("SOURCE_LANGUAGE", translateResult.getTranslateFrom());
        intent.putExtra("TARGET_LANGUAGE", translateResult.getTranslateTo());
        intent.putExtra("SOURCE_TEXT", translateResult.getTextToTranslate());
        intent.putExtra("TARGET_TEXT", translateResult.getTranslation());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
            Toast.makeText(this, R.string.add_word_to_flashcard_app, Toast.LENGTH_LONG).show();
        } else {
            Intent installFlashCardsIntent = new Intent(getApplicationContext(), InstallFlashcardAppActivity.class);
            installFlashCardsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(installFlashCardsIntent);
        }
    }


    private void clipboardChange() {
        String textFromClip = null;

        // Gets the clipboard data from the clipboard
        ClipData clip = clipboardManager.getPrimaryClip();
        if (clip != null) {

            // Gets the first item from the clipboard data
            ClipData.Item item = clip.getItemAt(0);

            // If the contents of the clipboard wasn't a reference to a
            // note, then
            // this converts whatever it is to text.
            textFromClip = coerceToText(this, item).toString().trim();

        }
        if (!TextUtils.isEmpty(textFromClip)) {
            clipboardManager.removePrimaryClipChangedListener(clipChangedListener);
            String textToTranslate = textFromClip.replaceAll(WHITESPACES_REGEX, " ").replaceAll(SPECIFIC_SIGNS_REGEX, " ");
            Log.d(TAG, "Formatted text: \"" + textToTranslate + "\"");
            TranslateTask translateTask = new TranslateTask(this);
            translateTask.execute(textToTranslate);
        }
    }

    public CharSequence coerceToText(Context context, ClipData.Item item) {
        // If this Item has an explicit textual value, simply return that.
        CharSequence text = item.getText();
        if (text != null) {
            return text;
        }

        // If this Item has a URI value, try using that.
        Uri uri = item.getUri();
        if (uri != null) {

            // First see if the URI can be opened as a plain text stream
            // (of any sub-type). If so, this is the best textual
            // representation for it.
            FileInputStream stream = null;
            try {
                // Ask for a stream of the desired type.
                AssetFileDescriptor descr = context.getContentResolver()
                        .openTypedAssetFileDescriptor(uri, "text/*", null);
                stream = descr.createInputStream();
                InputStreamReader reader = new InputStreamReader(stream,
                        "UTF-8");

                // Got it... copy the stream into a local string and return it.
                StringBuilder builder = new StringBuilder(128);
                char[] buffer = new char[8192];
                int len;
                while ((len = reader.read(buffer)) > 0) {
                    builder.append(buffer, 0, len);
                }
                return builder.toString();

            } catch (FileNotFoundException e) {
                // Unable to open content URI as text... not really an
                // error, just something to ignore.

            } catch (IOException e) {
                // Something bad has happened.
                Log.w("ClippedData", "Failure loading text", e);
                return e.toString();

            } finally {
                if (stream != null) {

                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            // If we couldn't open the URI as a stream, then the URI itself
            // probably serves fairly well as a textual representation.
            return uri.toString();
        }

        // Finally, if all we have is an Intent, then we can just turn that
        // into text. Not the most user-friendly thing, but it's something.
        Intent intent = item.getIntent();
        if (intent != null) {
            return intent.toUri(Intent.URI_INTENT_SCHEME);
        }

        // Shouldn't get here, but just in case...
        return "";
    }


    @Override
    public void onPreTranslate() {
        translateWindow.showProgressState();
    }

    @Override
    public void onPostTranslate(AsyncTaskResult<TranslateResult> result) {
        translateWindow.showResultState(result);
    }

    @Override
    public void onDestroy() {
        if (translateWindow != null) {
            translateWindow.releaseParentView();
            translateWindow = null;
        }
        clipboardManager.removePrimaryClipChangedListener(clipChangedListener);
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
