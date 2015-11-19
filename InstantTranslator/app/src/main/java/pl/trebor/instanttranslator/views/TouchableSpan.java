package pl.trebor.instanttranslator.views;

import android.text.TextPaint;
import android.text.style.ClickableSpan;

/**
 * Created by trebor on 2/26/2015.
 */
public abstract class TouchableSpan extends ClickableSpan {
    private boolean mIsPressed;
    private int mPressedBackgroundColor;
    private int mBackgroundColor;
    private int mNormalTextColor;
    private int mPressedTextColor;

    public TouchableSpan(int normalTextColor, int pressedTextColor) {
        this(normalTextColor, pressedTextColor, 0x00000000, 0x00000000);
    }

    public TouchableSpan(int normalTextColor, int pressedTextColor, int pressedBackgroundColor, int backgroundColor) {
        mNormalTextColor = normalTextColor;
        mPressedTextColor = pressedTextColor;
        mPressedBackgroundColor = pressedBackgroundColor;
        mBackgroundColor = backgroundColor;
    }

    public void setPressed(boolean isSelected) {
        mIsPressed = isSelected;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(mIsPressed ? mPressedTextColor : mNormalTextColor);

        ds.bgColor = mIsPressed ? mPressedBackgroundColor : mBackgroundColor;
//        ds.setUnderlineText(true);
    }
}
