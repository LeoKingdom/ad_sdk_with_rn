package com.ad_sdk_with_rn.component.modules;

import android.widget.Toast;

import com.ad_sdk_with_rn.MainActivity;
import com.ad_sdk_with_rn.component.manager.TTAdManagerHolder;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RnRewardVideoReactModule extends ReactContextBaseJavaModule {
    private final String REACT_MODULE_NAME = "ReactReward";
    private final TTAdNative mTTAdNative;
    private TTRewardVideoAd mttRewardVideoAd;
    private ReactApplicationContext context;
    private boolean mHasShowDownloadActive = false;
    private int orientation = 1; //1为竖屏 2 为横屏
    private String reward_name; //奖励的名称
    private int reward_count;//奖励数量
    private String user_id = "";
    private String ad_code_id;//广告 id
    private String test_id_ver = "901121365";
    private String test_id_hor = "901121430";
    private boolean isComplete = false;

    public RnRewardVideoReactModule(@NonNull ReactApplicationContext reactContext) {
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
        this.reward_name = reward_name;
        this.reward_count = reward_count;
        this.ad_code_id = ad_code_id;
        this.user_id = user_id;
        this.orientation = orientation;
    }

    /**
     * 展示激励视频广告
     */
    @ReactMethod
    public void showRewardAd() {
        loadRewardAd();
        MainActivity.mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mttRewardVideoAd.showRewardVideoAd(MainActivity.mainActivity, TTAdConstant.RitScenes.CUSTOMIZE_SCENES, "scenes_test");
            }
        });

    }

    /**
     * 展示激励视频广告
     */
    @ReactMethod
    public void showRewardAd(String codeId) {
        test_id_ver=codeId;
        loadRewardAd();
        MainActivity.mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mttRewardVideoAd.showRewardVideoAd(MainActivity.mainActivity, TTAdConstant.RitScenes.CUSTOMIZE_SCENES, "scenes_test");
            }
        });

    }

    @ReactMethod
    public void videoComplete(Callback callback) {
        callback.invoke(isComplete);
    }

    /**
     * 加载广告
     */
    private void loadRewardAd() {
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(test_id_ver)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(1080, 1920)
                .setRewardName("金币") //奖励的名称
                .setRewardAmount(3)  //奖励的数量
                .setUserID("user123")//用户id,必传参数
                .setMediaExtra("media_extra") //附加参数，可选
                .setOrientation(orientation) //必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
                .build();
        //step5:请求广告
        mTTAdNative.loadRewardVideoAd(adSlot, new TTAdNative.RewardVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }

            //视频广告加载后，视频资源缓存到本地的回调，在此回调后，播放本地视频，流畅不阻塞。
            @Override
            public void onRewardVideoCached() {
                showToast("rewardVideoAd video cached");
            }

            //视频广告的素材加载完毕，比如视频url等，在此回调后，可以播放在线视频，网络不好可能出现加载缓冲，影响体验。
            @Override
            public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
                showToast("rewardVideoAd loaded");
                mttRewardVideoAd = ad;
                mttRewardVideoAd.setRewardAdInteractionListener(new TTRewardVideoAd.RewardAdInteractionListener() {

                    @Override
                    public void onAdShow() {
                        showToast("rewardVideoAd show");
                    }

                    @Override
                    public void onAdVideoBarClick() {
                        showToast("rewardVideoAd bar click");
                    }

                    @Override
                    public void onAdClose() {
                        WritableMap event = Arguments.createMap();
                        event.putInt("code",1);
                        sendEventToRn("rewardCallback",event);
//                        showToast("rewardVideoAd close");
                    }

                    //视频播放完成回调
                    @Override
                    public void onVideoComplete() {
                        isComplete = true;
                        WritableMap event = Arguments.createMap();
                        event.putInt("code",0);
                        sendEventToRn("rewardCallback",event);
//                        showToast("rewardVideoAd complete");
                    }

                    @Override
                    public void onVideoError() {
                        showToast("rewardVideoAd error");
                    }

                    //视频播放完成后，奖励验证回调，rewardVerify：是否有效，rewardAmount：奖励梳理，rewardName：奖励名称
                    @Override
                    public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName) {
//                        showToast("verify:" + rewardVerify + " amount:" + rewardAmount +
//                                " name:" + rewardName);
                    }

                    @Override
                    public void onSkippedVideo() {
                        showToast("rewardVideoAd has onSkippedVideo");
                    }
                });
                mttRewardVideoAd.setDownloadListener(new TTAppDownloadListener() {
                    @Override
                    public void onIdle() {
                        mHasShowDownloadActive = false;
                    }

                    @Override
                    public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                        if (!mHasShowDownloadActive) {
                            mHasShowDownloadActive = true;
//                            showToast("下载中，点击下载区域暂停");
                        }
                    }

                    @Override
                    public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
//                        showToast("下载暂停，点击下载区域继续");
                    }

                    @Override
                    public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
//                        showToast("下载失败，点击下载区域重新下载");
                    }

                    @Override
                    public void onDownloadFinished(long totalBytes, String fileName, String appName) {
//                        showToast("下载完成，点击下载区域重新下载");
                    }

                    @Override
                    public void onInstalled(String fileName, String appName) {
//                        showToast("安装完成，点击下载区域打开");
                    }
                });

            }
        });
    }

    public  void sendEventToRn(String eventName, @Nullable WritableMap paramss)
    {

        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, paramss);

    }

    private void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
