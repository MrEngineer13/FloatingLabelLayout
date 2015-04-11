/*
 * Copyright (C) 2014 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mrengineer13.fll;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * Layout which an {@link android.widget.EditText} to show a floating label when the hint is hidden
 * due to the user inputting text.
 *
 * @see <a href="https://dribbble.com/shots/1254439--GIF-Mobile-Form-Interaction">Matt D. Smith on Dribble</a>
 * @see <a href="http://bradfrostweb.com/blog/post/float-label-pattern/">Brad Frost's blog post</a>
 */
class FloatingLabelLayout extends FrameLayout {

    private static final long ANIMATION_DURATION = 150;
    private static final float DEFAULT_PADDING_LEFT_RIGHT_DP = 12f;

    private static final String SAVED_SUPER_STATE = "SAVED_SUPER_STATE";
    private static final String SAVED_LABEL_VISIBILITY = "SAVED_LABEL_VISIBILITY";
    private static final String SAVED_HINT = "SAVED_HINT";
    public static final String SAVED_TRIGGER = "SAVED_TRIGGER";
    public static final String SAVED_FOCUS = "SAVED_FOCUS";

    private EditText mEditText;
    private TextView mLabel;
    private Trigger mTrigger;
    private CharSequence mHint;

    public FloatingLabelLayout(Context context) {
        this(context, null);
    }

    public FloatingLabelLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingLabelLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray a = context
                .obtainStyledAttributes(attrs, R.styleable.FloatingLabelLayout);

        final int sidePadding = a.getDimensionPixelSize(
                R.styleable.FloatingLabelLayout_floatLabelSidePadding,
                dipsToPix(DEFAULT_PADDING_LEFT_RIGHT_DP));
        mLabel = new TextView(context);
        mLabel.setPadding(sidePadding, 0, sidePadding, 0);
        mLabel.setVisibility(INVISIBLE);

        mLabel.setTextAppearance(context,
                a.getResourceId(R.styleable.FloatingLabelLayout_floatLabelTextAppearance,
                        android.R.style.TextAppearance_Small)
        );

        int triggerInt = a.getInt(R.styleable.FloatingLabelLayout_floatLabelTrigger, Trigger.TYPE.getValue());
        mTrigger = Trigger.fromValue(triggerInt);

        addView(mLabel, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        a.recycle();
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SAVED_SUPER_STATE, super.onSaveInstanceState());
        bundle.putInt(SAVED_LABEL_VISIBILITY, mLabel.getVisibility());
        bundle.putCharSequence(SAVED_HINT, mHint);
        bundle.putInt(SAVED_TRIGGER, mTrigger.getValue());
        bundle.putBoolean(SAVED_FOCUS, getEditText().isFocused());
        return bundle;
    }

    @SuppressWarnings("ResourceType")
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;

            mLabel.setVisibility(bundle.getInt(SAVED_LABEL_VISIBILITY));
            mHint = bundle.getCharSequence(SAVED_HINT);
            mTrigger = Trigger.fromValue(bundle.getInt(SAVED_TRIGGER));

            // if the trigger is on focus
            if (mTrigger == Trigger.FOCUS) {
                if (bundle.getBoolean(SAVED_FOCUS)) {
                    mEditText.requestFocus();
                } else if (!TextUtils.isEmpty(getEditText().getText())) {
                    showLabel();
                }
            } else if (mTrigger == Trigger.TYPE){
                if (TextUtils.isEmpty(getEditText().getText())) {
                    showLabel();
                } else {
                    hideLabel();
                }
            }

            // retrieve super state
            state = bundle.getParcelable(SAVED_SUPER_STATE);
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof EditText) {
            // If we already have an EditText, throw an exception
            if (mEditText != null) {
                throw new IllegalArgumentException("We already have an EditText, can only have one");
            }

            // Update the layout params so that the EditText is at the bottom, with enough top
            // margin to show the label
            final LayoutParams lp = new LayoutParams(params);
            lp.gravity = Gravity.BOTTOM;
            lp.topMargin = (int) mLabel.getTextSize();
            params = lp;

            setEditText((EditText) child);
        }

        // Carry on adding the View...
        super.addView(child, index, params);
    }

    protected void setEditText(EditText editText) {
        mEditText = editText;

        mLabel.setTypeface(editText.getTypeface());
        mLabel.setText(mEditText.getHint());

        if (mHint == null) {
            mHint = mEditText.getHint();
        }

        // Add a TextWatcher so that we know when the text input has changed
        mEditText.addTextChangedListener(mTextWatcher);

        // Add focus listener to the EditText so that we can notify the label that it is activated.
        // Allows the use of a ColorStateList for the text color on the label
        mEditText.setOnFocusChangeListener(mOnFocusChangeListener);

        // if view already had focus we need to manually call the listener
        if (mTrigger == Trigger.FOCUS && mEditText.isFocused()) {
            mOnFocusChangeListener.onFocusChange(mEditText, true);
        }

    }

    /**
     * @return the {@link android.widget.EditText} text input
     */
    public EditText getEditText() {
        return mEditText;
    }

    /**
     * @return the {@link android.widget.TextView} label
     */
    public TextView getLabel() {
        return mLabel;
    }

    /**
     * Show the label using an animation
     */
    protected void showLabel() {
        mLabel.setVisibility(View.VISIBLE);
        ViewHelper.setAlpha(mLabel, 0f);
        ViewHelper.setTranslationY(mLabel, mLabel.getHeight());
        ViewPropertyAnimator.animate(mLabel)
                .alpha(1f)
                .translationY(0f)
                .setDuration(ANIMATION_DURATION)
                .setListener(null).start();
    }

    /**
     * Hide the label using an animation
     */
    private void hideLabel() {
        ViewHelper.setAlpha(mLabel, 1f);
        ViewHelper.setTranslationY(mLabel, 0f);
        ViewPropertyAnimator.animate(mLabel)
                .alpha(0f)
                .translationY(mLabel.getHeight())
                .setDuration(ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLabel.setVisibility(View.GONE);
                    }
                }).start();

    }

    /**
     * sets hint on {@link #mEditText}
     *
     * @param hint to set
     */
    public void setHint(String hint){
        getEditText().setHint(hint);
        getLabel().setText(hint);
    }

    /**
     * Sets text on {@link #mEditText}
     *
     * @param text to set
     */
    public void setText(String text){
        getEditText().setText(text);
    }

    /**
     * @return {@link #mEditText} text
     */
    public String getText(){
        return getEditText().getText().toString();
    }

    /**
     * Helper method to convert dips to pixels.
     */
    private int dipsToPix(float dps) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps,
                getResources().getDisplayMetrics());
    }

    private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {

        @Override
        public void onFocusChange(View view, boolean focused) {
            if (android.os.Build.VERSION.SDK_INT>= android.os.Build.VERSION_CODES.HONEYCOMB) {
                mLabel.setActivated(focused); // only available after API 11
            }

            if (mTrigger == Trigger.FOCUS) {
                if (focused) {
                    mEditText.setHint("");
                    showLabel();
                } else {
                    if (TextUtils.isEmpty(mEditText.getText())) {
                        mEditText.setHint(mHint);
                        hideLabel();
                    }
                }
            }
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
            // only takes affect if mTrigger is set to TYPE
            if (mTrigger != Trigger.TYPE) {
                return;
            }

            if (TextUtils.isEmpty(s)) {
                hideLabel();
            } else {
                showLabel();
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

    };

    public static enum Trigger {
        TYPE(0),
        FOCUS(1);

        private final int mValue;

        private Trigger(int i) {
            mValue = i;
        }

        public int getValue() {
            return mValue;
        }

        public static Trigger fromValue(int value) {
            Trigger[] triggers = Trigger.values();
            for (int i = 0; i < triggers.length; i++) {
                if (triggers[i].getValue() == value) {
                    return triggers[i];
                }
            }

            throw new IllegalArgumentException(value + " is not a valid value for " + Trigger.class.getSimpleName());
        }
    }
}