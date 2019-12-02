package com.ad_sdk_with_rn.packages;

import com.ad_sdk_with_rn.modules.RnFullScreenReactModule;
import com.ad_sdk_with_rn.modules.RnRewardVideoReactModule;
import com.ad_sdk_with_rn.modules.RnSplashReactModule;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;

public class RnCustomPackage implements ReactPackage {
    @NonNull
    @Override
    public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
        return Arrays.<NativeModule>asList(new RnRewardVideoReactModule(reactContext),
                new RnFullScreenReactModule(reactContext),
                new RnSplashReactModule(reactContext));
    }

    @NonNull
    @Override
    public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }


}
