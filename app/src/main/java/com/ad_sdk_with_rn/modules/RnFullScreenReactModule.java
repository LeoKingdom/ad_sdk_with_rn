package com.ad_sdk_with_rn.modules;

import android.widget.Toast;

import com.ad_sdk_with_rn.MainActivity;
import com.ad_sdk_with_rn.manager.TTAdManagerHolder;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RnFullScreenReactModule extends ReactContextBaseJavaModule {
    private final String REACT_MODULE_NAME = "ReactFull";
    private final TTAdNative mTTAdNative;
    private TTFullScreenVideoAd mttFullVideoAd;
    private ReactContext context;
    private int orientation = 1; //1为竖屏 2 为横屏
    private String test_id_ver = "901121375";
    private String test_id_hor = "901121430";

    public RnFullScreenReactModule(@NonNull ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
        //step1:初始化sdk
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        //step2:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdManagerHolder.get().requestPermissionIfNecessary(reactContext);
        mTTAdNative = ttAdManager.createAdNative(reactContext.getApplicationContext());

    }

    @NonNull
    @Override
    public String getName() {
        return REACT_MODULE_NAME;
    }

    @ReactMethod
    public void showFullAd() {
        loadFulldAd();


    }

    public void loadFulldAd() {
//step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(test_id_ver)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(1080, 1920)
                .setOrientation(TTAdConstant.VERTICAL) //必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
                .build();
        //step5:请求广告
        mTTAdNative.loadFullScreenVideoAd(adSlot, new TTAdNative.FullScreenVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                showToast(message);
            }

            @Override
            public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
                showToast("FullVideoAd loaded");
                mttFullVideoAd = ad;
                MainActivity.mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mttFullVideoAd.showFullScreenVideoAd(MainActivity.mainActivity, TTAdConstant.RitScenes.GAME_GIFT_BONUS, null);
                    }
                });
                mttFullVideoAd.setFullScreenVideoAdInteractionListener(new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {

                    @Override
                    public void onAdShow() {
                        showToast("FullVideoAd show");
                    }

                    @Override
                    public void onAdVideoBarClick() {
                        showToast("FullVideoAd bar click");
                    }

                    @Override
                    public void onAdClose() {
                        showToast("FullVideoAd close");
                    }

                    @Override
                    public void onVideoComplete() {
                        showToast("FullVideoAd complete");
                        WritableMap event = Arguments.createMap();
                        event.putString("isComplete", "ok");
                        sendEventToRn("fullComplete", event);
                    }

                    @Override
                    public void onSkippedVideo() {
                        showToast("FullVideoAd skipped");

                    }

                });
            }

            @Override
            public void onFullScreenVideoCached() {
                showToast("FullVideoAd video cached");
            }
        });
    }

    public void sendEventToRn(String eventName, @Nullable WritableMap paramss) {

        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, paramss);

    }

    @ReactMethod
    public void setVideoOrientation(int orientation) {
        this.orientation = orientation;
    }

    private void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
