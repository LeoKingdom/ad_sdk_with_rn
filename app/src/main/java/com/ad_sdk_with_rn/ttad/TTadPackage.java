package com.ad_sdk_with_rn.ttad;

import java.util.List;
import java.util.Arrays;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.uimanager.ViewManager;
import com.facebook.react.bridge.ReactApplicationContext;

public class TTadPackage implements ReactPackage {
    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        return Arrays.<NativeModule>asList(new TTadModule(reactContext),new RnRewardVideoReactModule(reactContext),new RewardModule(reactContext));
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Arrays.<ViewManager>asList(
                new TTBannerView(),
                new TTInteractionView(),
                new TTSplashView()
        );
    }
}
