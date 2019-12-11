package com.ad_sdk_with_rn.ttad;

import androidx.annotation.NonNull;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

public class TTSplashView extends TTadViewManager {

    @Override
    public @NonNull String getName() {
        return "RNTTSplashView";
    }

    @Override
    protected @NonNull TTadView createViewInstance(@NonNull ThemedReactContext context) {
        return new TTadView(context, TTadType.SPLASH);
    }

    // 加载超时时长
    @ReactProp(name = "timeout")
    public void setTimeout(TTadView view, int timeout) {
        view.setTimeout(timeout);
    }
}