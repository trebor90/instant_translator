package pl.trebor.instanttranslator.views;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

/**
 * Created by trebor on 12/31/2014.
 */
public class InterceptFrameLayout extends FrameLayout {
    private int mTouchSlop;
    private float yInitial;
    private float xInitial;
    public MotionEvent motionEventDown;
    public boolean mIsDownEventDispatched;

    {
        ViewConfiguration vc = ViewConfiguration.get(getContext());
        mTouchSlop = vc.getScaledTouchSlop() / 2;
    }

    public InterceptFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public InterceptFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InterceptFrameLayout(Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                yInitial = ev.getY();
                xInitial = ev.getX();
                motionEventDown = MotionEvent.obtainNoHistory(ev);
                mIsDownEventDispatched = false;
                break;
            }
            case MotionEvent.ACTION_MOVE: {

                int yDiff = calculateDistanceY(ev);
                int xDiff = calculateDistanceX(ev);
                // Touch slop should be calculated using ViewConfiguration
                // constants.
                if (yDiff > mTouchSlop || xDiff > mTouchSlop) {
                    // intercept
                    return true;
                }
                break;
            }

        }
        return super.onInterceptTouchEvent(ev);
    }

    private int calculateDistanceY(MotionEvent ev) {
        return (int) Math.abs(ev.getY() - yInitial);
    }

    private int calculateDistanceX(MotionEvent ev) {
        return (int) Math.abs(ev.getX() - xInitial);
    }

}
