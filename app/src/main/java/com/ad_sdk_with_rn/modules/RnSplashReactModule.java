package com.ad_sdk_with_rn.modules;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.ad_sdk_with_rn.MainActivity;
import com.ad_sdk_with_rn.R;
import com.ad_sdk_with_rn.manager.TTAdManagerHolder;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.bytedance.sdk.openadsdk.TTSplashAd;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.lang.ref.WeakReference;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RnSplashReactModule extends ReactContextBaseJavaModule {
    private final String REACT_MODULE_NAME = "ReactSplash";
    private final TTAdNative mTTAdNative;
    private TTSplashAd ttSplashAd;
    private ReactApplicationContext context;
    private boolean mHasShowDownloadActive = false;
    private int orientation = 1; //1为竖屏 2 为横屏
    private String test_id_ver = "801121648";
    private String test_id_hor = "901121430";
    private boolean isComplete = false;
    private static final int AD_TIME_OUT = 3000;
    private Activity showActivity;
    private Dialog mSplashDialog;
    private View splashView;
    private FrameLayout splashContainer;

    public RnSplashReactModule(@NonNull ReactApplicationContext reactContext) {
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

    /**
     * 初始化广告参数,rn 传入
     *
     * @param reward_name
     * @param reward_count
     * @param ad_code_id
     * @param user_id
     * @param orientation
     */
    @ReactMethod
    public void initConfig(String reward_name, int reward_count, String ad_code_id, String user_id, int orientation) {
        this.orientation = orientation;
    }

    /**
     * 打开启动屏
     */
    @ReactMethod
    public  void showSplashAd() {
        loadRewardAd();

    }


    /**
     * 加载广告
     */
    private void loadRewardAd() {
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(test_id_ver)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(1080, 1920)
                .build();
        //step4:请求广告，调用开屏广告异步请求接口，对请求回调的广告作渲染处理
        mTTAdNative.loadSplashAd(adSlot, new TTAdNative.SplashAdListener() {
            @Override
            @MainThread
            public void onError(int code, String message) {
                showToast(message);
            }

            @Override
            @MainThread
            public void onTimeout() {
                showToast("开屏广告加载超时");
            }

            @Override
            @MainThread
            public void onSplashAdLoad(TTSplashAd ad) {
                if (ad == null) {
                    return;
                }
                //获取SplashView
                splashView = ad.getSplashView();
                showActivity=MainActivity.mainActivity;
                showActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!showActivity.isFinishing()) {
                            mSplashDialog = new Dialog(showActivity, R.style.SplashScreen_Fullscreen);
                            mSplashDialog.setContentView(R.layout.launch_screen);
                            splashContainer=mSplashDialog.findViewById(R.id.splash_container);
                            mSplashDialog.setCancelable(false);
                            splashContainer.addView(splashView);
                            if (!mSplashDialog.isShowing()) {
                                mSplashDialog.show();
                            }
                        }
                    }
                });
                //设置SplashView的交互监听器
                ad.setSplashInteractionListener(new TTSplashAd.AdInteractionListener() {
                    @Override
                    public void onAdClicked(View view, int type) {
                        showToast("开屏广告点击");
                    }

                    @Override
                    public void onAdShow(View view, int type) {
                        showToast("开屏广告展示");
                    }

                    @Override
                    public void onAdSkip() {
                        showToast("开屏广告跳过");

                    }

                    @Override
                    public void onAdTimeOver() {
                        showToast("开屏广告倒计时结束");
                        WritableMap event = Arguments.createMap();
                        event.putString("isComplete", "ok");
                        sendEventToRn("splashComplete", event);
                    }
                });
                if(ad.getInteractionType() == TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
                    ad.setDownloadListener(new TTAppDownloadListener() {
                        boolean hasShow = false;

                        @Override
                        public void onIdle() {

                        }

                        @Override
                        public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                            if (!hasShow) {
                                showToast("下载中...");
                                hasShow = true;
                            }
                        }

                        @Override
                        public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                            showToast("下载暂停...");

                        }

                        @Override
                        public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                            showToast("下载失败...");

                        }

                        @Override
                        public void onDownloadFinished(long totalBytes, String fileName, String appName) {

                        }

                        @Override
                        public void onInstalled(String fileName, String appName) {

                        }
                    });
                }
            }
        }, AD_TIME_OUT);
    }

    public void sendEventToRn(String eventName, @Nullable WritableMap paramss) {

        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, paramss);

    }

    private void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
