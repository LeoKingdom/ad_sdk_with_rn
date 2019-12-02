package com.ad_sdk_with_rn;

import android.app.Activity;
import android.os.Bundle;

import com.facebook.react.ReactActivity;

public class MainActivity extends ReactActivity {

    /**
     * Returns the name of the main component registered from JavaScript. This is used to schedule
     * rendering of the component.
     */

    public static Activity mainActivity;

    @Override
    protected String getMainComponentName() {
        return "ad_sdk_with_rn";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
    }
}