package com.mrengineer13.floating.sample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.mrengineer13.fll.FloatingLabelEditText;

public class DemoActivity extends Activity {

    public static final String FLL_KEY = "FLL";
    public static final String STATE_KEY = "State";
    public static final int MAX_FLLS = 10;

    private LinearLayout mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        mContainer = (LinearLayout) findViewById(R.id.ll_flls);

        for (int i = 0; i < MAX_FLLS; i++){
            FloatingLabelEditText floatingLabelEditText = new FloatingLabelEditText(DemoActivity.this);
            floatingLabelEditText.setTag(FLL_KEY + i);
            floatingLabelEditText.setHint(String.format("Message %d hint", i));
            mContainer.addView(floatingLabelEditText);

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        for (int i = 0; i < MAX_FLLS; i++){
            FloatingLabelEditText floatingLabelEditText = (FloatingLabelEditText) mContainer.findViewWithTag(FLL_KEY+i);
            outState.putBundle(STATE_KEY + i, (Bundle) floatingLabelEditText.onSaveInstanceState());
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle saveState){
        super.onRestoreInstanceState(saveState);
        for (int i = 0; i < MAX_FLLS; i++){
            FloatingLabelEditText floatingLabelEditText = (FloatingLabelEditText) mContainer.findViewWithTag(FLL_KEY +i);
            floatingLabelEditText.onRestoreInstanceState(saveState.getBundle(STATE_KEY + i));
        }
    }
}
