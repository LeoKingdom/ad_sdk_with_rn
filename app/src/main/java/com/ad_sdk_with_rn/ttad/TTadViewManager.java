package com.ad_sdk_with_rn.ttad;

import android.util.Log;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.common.MapBuilder;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ReactStylesDiffMap;
import com.facebook.react.uimanager.annotations.ReactProp;

public abstract class TTadViewManager extends SimpleViewManager<TTadView> {
    public static final String EVENT_NAME = "onTTadViewEvent";

    @Override
    public void onDropViewInstance(@NonNull TTadView view) {
        super.onDropViewInstance(view);
        view.destroyAdView();
    }

    @Override
    public void updateProperties(@NonNull TTadView view, ReactStylesDiffMap props) {
        super.updateProperties(view, props);
        for (Map.Entry entry:props.toMap().entrySet()){
            Log.e("proMap----",entry.getKey().toString()+"/"+entry.getValue());
        }
        view.updateAd(
                props.hasKey("codeId") || props.hasKey("uuid") || props.hasKey("deepLink"),
                props.hasKey("width") || props.hasKey("height")
        );
    }

    @ReactProp(name = "codeId")
    public void setCodeId(TTadView view, String codeId) {
        view.setCodeId(codeId);
    }

    @ReactProp(name = "deepLink")
    public void setDeepLink(TTadView view, boolean deepLink) {
        view.setDeepLink(deepLink);
    }

    @ReactProp(name = "listeners")
    public void setListeners(TTadView view, ReadableMap listeners) {
        view.setListeners(listeners);
    }

    @Override
    public @Nullable Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.<String, Object>builder()
                .put(EVENT_NAME, MapBuilder.of("registrationName", EVENT_NAME))
                .build();
    }
}
