package com.ad_sdk_with_rn.ttad;

import android.content.Intent;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import androidx.annotation.NonNull;

public class RewardModule extends ReactContextBaseJavaModule {
    public static ReactApplicationContext context;
    public RewardModule(@NonNull ReactApplicationContext reactContext) {
        super(reactContext);
        context=reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return "RewardModule";
    }

    @ReactMethod
    public void startReward(String codeId){
        Intent intent=new Intent(getCurrentActivity(),RewardVideoActivity.class);
        intent.putExtra("codeId",codeId);
        getCurrentActivity().startActivityForResult(intent,1001);
    }

    @ReactMethod
    public void closeReward(){
        getCurrentActivity().finishActivity(1001);
    }
}
