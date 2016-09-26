package com.sam.hex.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.hex.core.Point;

/**
 * @author Will Harmon
 **/
public class SelectorLayout extends View implements OnTouchListener {
    private SelectorLayout.Button[] mButtons;
    private ShapeDrawable[] mButtonDrawable;
    private ShapeDrawable[] mMirrorButtonDrawable;
    private Paint mButtonTextPaint;
    private int mDisabledColor;
    private int mFocusedButton = -1;

    private int mWidth;
    private int mIndentHeight;
    private int mMargin;
    private float mRotation;
    private int mAnimationLength;
    private int mAnimationTick;
    private int mAnimationDelta;
    private int mMinAnimationDelta;
    private Rect[] mOldRect;
    private Rect[] mOldMirrorRect;
    private Point[] mOldTextPos;

    public SelectorLayout(Context context) {
        super(context);
        setUp();
    }

    public SelectorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp();
    }

    public SelectorLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setUp();
    }

    public void setUp() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        setOnTouchListener(this);
        mButtons = new Button[3];
        for(int i = 0; i < mButtons.length; i++) {
            mButtons[i] = new Button(getContext());
        }
        mDisabledColor = getDarkerColor(Color.LTGRAY);
        mButtonTextPaint = new Paint();
        mButtonTextPaint.setColor(Color.WHITE);
        mButtonTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24, dm));

        mWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90, dm);
        mIndentHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, dm);
        mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, dm);
        mRotation = 45f;
        mAnimationTick = 30;
        mAnimationLength = 240;
        mMinAnimationDelta = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55, dm);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for(Button b : mButtons) {
                    if(b.isSelected() || b.isPressed()) {
                        b.setAnimate(true);
                    }
                }
                invalidate();
            }
        });
        setFocusable(true);
        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    mFocusedButton = 0;
                    mButtons[0].setSelected(true);
                    invalidate();
                }
                else {
                    if(mFocusedButton != -1) {
                        mButtons[mFocusedButton].setSelected(false);
                        invalidate();
                    }
                }
            }
        });
    }

    @Override
    public View focusSearch(int direction) {
        mButtons[mFocusedButton].setSelected(false);
        switch(direction) {
        case View.FOCUS_RIGHT:
            switch(mFocusedButton) {
            case 0:
                mFocusedButton = 1;
                mButtons[mFocusedButton].setSelected(true);
                invalidate();
                return this;
            case 1:
                mFocusedButton = 2;
                mButtons[mFocusedButton].setSelected(true);
                invalidate();
                return this;
            }
            break;
        case View.FOCUS_LEFT:
            switch(mFocusedButton) {
            case 1:
                mFocusedButton = 0;
                mButtons[mFocusedButton].setSelected(true);
                invalidate();
                return this;
            case 2:
                mFocusedButton = 1;
                mButtons[mFocusedButton].setSelected(true);
                invalidate();
                return this;
            }
            break;
        case View.FOCUS_UP:
            break;
        case View.FOCUS_DOWN:
            break;
        case View.FOCUS_FORWARD:
            break;
        case View.FOCUS_BACKWARD:
            break;
        }
        return super.focusSearch(direction);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.rotate(mRotation);
        for(int i = 0; i < mButtons.length; i++) {
            if(!mButtons[i].isEnabled()) {
                mButtonDrawable[i].getPaint().setColor(mDisabledColor);
                mMirrorButtonDrawable[i].getPaint().setColor(mDisabledColor);
            }
            else if(mButtons[i].isPressed()) {
                mButtonDrawable[i].getPaint().setColor(getDarkerColor(mButtons[i].getColor()));
                mMirrorButtonDrawable[i].getPaint().setColor(getDarkerColor(mButtons[i].getColor()));
            }
            else if(mButtons[i].isSelected()) {
                mButtonDrawable[i].getPaint().setColor(getDarkerColor(mButtons[i].getColor()));
                mMirrorButtonDrawable[i].getPaint().setColor(getDarkerColor(mButtons[i].getColor()));
            }
            else {
                mButtonDrawable[i].getPaint().setColor(mButtons[i].getColor());
                mMirrorButtonDrawable[i].getPaint().setColor(mButtons[i].getColor());
            }

            if(mButtons[i].isAnimate()) {
                if(mButtonDrawable[i].getBounds().bottom + 3 * mIndentHeight > 0) {
                    mButtonDrawable[i].setBounds(mButtonDrawable[i].getBounds().left, mButtonDrawable[i].getBounds().top - mAnimationDelta,
                            mButtonDrawable[i].getBounds().right, mButtonDrawable[i].getBounds().bottom - mAnimationDelta);
                    mMirrorButtonDrawable[i].setBounds(mMirrorButtonDrawable[i].getBounds().left, mMirrorButtonDrawable[i].getBounds().top + mAnimationDelta,
                            mMirrorButtonDrawable[i].getBounds().right, mMirrorButtonDrawable[i].getBounds().bottom + mAnimationDelta);
                    mButtons[i].textX += mAnimationDelta;
                    postInvalidateDelayed(mAnimationTick);
                }
                else {
                    mButtons[i].setAnimate(false);
                    mButtons[i].performClick();
                }
            }
            mButtonDrawable[i].draw(canvas);
            mMirrorButtonDrawable[i].draw(canvas);
            canvas.save();
            canvas.rotate(-90f, mButtons[i].getHexagon().b.x, mButtons[i].getHexagon().d.y / 2);
            canvas.drawText(mButtons[i].getText(), mButtons[i].textX, mButtons[i].textY, mButtonTextPaint);
            canvas.restore();
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        int diagonal = (int) Math.sqrt(w * w + h * h);
        int margin = (w - mWidth * 3) / 4;
        int offset = margin;
        // Create the buttons
        mButtonDrawable = new ShapeDrawable[mButtons.length];
        mMirrorButtonDrawable = new ShapeDrawable[mButtons.length];
        mOldRect = new Rect[mButtons.length];
        mOldMirrorRect = new Rect[mButtons.length];
        mOldTextPos = new Point[mButtons.length];

        for(int i = 0; i < mButtons.length; i++) {
            Hexagon hex = new Hexagon(new Point(offset, mIndentHeight - 3 * offset), new Point(mWidth / 2 + offset, -3 * offset), new Point(mWidth + offset,
                    mIndentHeight - 3 * offset), new Point(mWidth + offset, h - offset), new Point(mWidth / 2 + offset, h - mIndentHeight - offset), new Point(
                    offset, h - offset));

            // Shape of a pressed state
            Path buttonPath = new Path();
            buttonPath.moveTo(hex.a.x, Math.max(hex.a.y, -diagonal));
            buttonPath.lineTo(hex.b.x, Math.max(hex.b.y, -diagonal));
            buttonPath.lineTo(hex.c.x, Math.max(hex.c.y, -diagonal));
            buttonPath.lineTo(hex.d.x, hex.d.y);
            buttonPath.lineTo(hex.e.x, hex.e.y);
            buttonPath.lineTo(hex.f.x, hex.f.y);
            buttonPath.close();

            Hexagon mirrorHex = new Hexagon(new Point(offset, mIndentHeight - offset), new Point(mWidth / 2 + offset, 0 - offset), new Point(mWidth + offset,
                    mIndentHeight - offset), new Point(mWidth + offset, h - offset), new Point(mWidth / 2 + offset, h - mIndentHeight - offset), new Point(
                    offset, h - offset));

            // Shape of a pressed state
            Path mirrorButtonPath = new Path();
            mirrorButtonPath.moveTo(mirrorHex.a.x, mirrorHex.a.y);
            mirrorButtonPath.lineTo(mirrorHex.b.x, mirrorHex.b.y);
            mirrorButtonPath.lineTo(mirrorHex.c.x, mirrorHex.c.y);
            mirrorButtonPath.lineTo(mirrorHex.d.x, mirrorHex.d.y);
            mirrorButtonPath.lineTo(mirrorHex.e.x, mirrorHex.e.y);
            mirrorButtonPath.lineTo(mirrorHex.f.x, mirrorHex.f.y);
            mirrorButtonPath.close();

            int heightOffset = (int) (3.6 * mIndentHeight);
            mButtonDrawable[i] = new ShapeDrawable(new PathShape(buttonPath, w, h));
            mButtonDrawable[i].setBounds(0, -heightOffset + h / 2, w, h - heightOffset + h / 2);
            mMirrorButtonDrawable[i] = new ShapeDrawable(new PathShape(mirrorButtonPath, w, h));
            mMirrorButtonDrawable[i].setBounds(0, (h - mIndentHeight) + mMargin - heightOffset + h / 2, w, (2 * h - mIndentHeight) + mMargin - heightOffset + h
                    / 2);

            mButtons[i].setHexagon(hex);

            mButtons[i].textX = mButtons[i].getHexagon().b.x * 2 - mButtonTextPaint.measureText(mButtons[i].getText()) / 2 - (int) (1.7 * i * margin);
            mButtons[i].textY = mButtons[i].getHexagon().d.y / 2 + mButtonTextPaint.getTextSize() / 4;

            mOldRect[i] = mButtonDrawable[i].copyBounds();
            mOldMirrorRect[i] = mMirrorButtonDrawable[i].copyBounds();
            mOldTextPos[i] = new Point((int) mButtons[i].textX, (int) mButtons[i].textY);

            offset += margin + mWidth;
        }

        mAnimationDelta = h / mAnimationLength * mAnimationTick;
        mAnimationDelta = Math.max(mMinAnimationDelta, mAnimationDelta);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            for(Button b : mButtons) {
                if(b.getHexagon().contains(new Point((int) event.getX(), (int) event.getY()))) {
                    b.setPressed(b.isEnabled());
                }
                else {
                    b.setPressed(false);
                }
            }
        }
        else if(event.getAction() == MotionEvent.ACTION_UP) {
            for(Button b : mButtons) {
                if(b.isPressed()) {
                    performClick();
                }
                b.setPressed(false);
            }
        }
        else {
            for(Button b : mButtons) {
                if(b.isPressed()) {
                    if(!b.getHexagon().contains(new Point((int) event.getX(), (int) event.getY()))) {
                        b.setPressed(false);
                    }
                }
            }
        }

        invalidate();
        return true;
    }

    private int getDarkerColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    public Button[] getButtons() {
        return mButtons;
    }

    public void reset() {
        if(mOldRect != null) {
            for(int i = 0; i < mButtons.length; i++) {
                mButtonDrawable[i].setBounds(mOldRect[i]);
                mMirrorButtonDrawable[i].setBounds(mOldMirrorRect[i]);
                mButtons[i].textX = mOldTextPos[i].x;
                mButtons[i].textY = mOldTextPos[i].y;
            }
            invalidate();
        }
    }

    private class Hexagon {
        private final Point a, b, c, d, e, f;
        private final Matrix m;
        private final float[] points;

        private Hexagon(Point a, Point b, Point c, Point d, Point e, Point f) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.e = e;
            this.f = f;
            points = new float[2];
            m = new Matrix();
            m.postRotate(-mRotation);
        }

        public boolean contains(Point p) {
            points[0] = p.x;
            points[1] = p.y;
            m.mapPoints(points);
            p.x = (int) points[0];
            p.y = (int) points[1];

            return p.x > a.x && p.x < c.x;
        }
    }

    public static class Button {
        private final Context context;
        private SelectorLayout.Button.OnClickListener onClickListener;
        private String text;
        private int color;
        private Hexagon hexagon;
        private boolean pressed = false;
        private boolean enabled = true;
        private boolean animate = false;
        private float textX;
        private float textY;
        private boolean selected;

        public Button(Context context) {
            this.context = context;
        }

        public static interface OnClickListener {
            public void onClick();
        }

        public void setOnClickListener(SelectorLayout.Button.OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
        }

        public OnClickListener getOnClickListener() {
            return onClickListener;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setText(int resId) {
            setText(context.getString(resId));
        }

        public String getText() {
            return text;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }

        private void setHexagon(Hexagon hexagon) {
            this.hexagon = hexagon;
        }

        private Hexagon getHexagon() {
            return hexagon;
        }

        public void performClick() {
            if(onClickListener != null) onClickListener.onClick();
        }

        protected boolean isPressed() {
            return pressed;
        }

        protected void setPressed(boolean pressed) {
            this.pressed = pressed;
        }

        protected boolean isSelected() {
            return selected;
        }

        protected void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isAnimate() {
            return animate;
        }

        public void setAnimate(boolean animate) {
            this.animate = animate;
        }
    }
}
