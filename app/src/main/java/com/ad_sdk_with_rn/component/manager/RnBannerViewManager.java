package com.ad_sdk_with_rn.component.manager;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.ad_sdk_with_rn.R;
import com.ad_sdk_with_rn.component.view.RnBannerViewGroup;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTNativeAd;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RnBannerViewManager extends ViewGroupManager<RnBannerViewGroup> {
    /**
     * 加载广告
     */

    @NonNull
    @Override
    public String getName() {

        return "RnBannerView";
    }

    @NonNull
    @Override
    protected RnBannerViewGroup createViewInstance(@NonNull ThemedReactContext reactContext) {
        RnBannerViewGroup bannerViewGroup = new RnBannerViewGroup(reactContext);
        bannerViewGroup.findViewById(R.id.img_native_dislike).setOnClickListener((v -> {
            Log.e("ok","ok");
        }));
        return bannerViewGroup;
    }

    @Override
    public void receiveCommand(@NonNull RnBannerViewGroup root, String commandId, @Nullable ReadableArray args) {
        super.receiveCommand(root, commandId, args);
//        root.setAdData();
//        root.show();
    }


}
