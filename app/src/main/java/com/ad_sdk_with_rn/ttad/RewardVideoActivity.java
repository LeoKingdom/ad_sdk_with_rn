package com.ad_sdk_with_rn.ttad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ad_sdk_with_rn.R;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.facebook.react.ReactActivity;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import androidx.annotation.Nullable;

/**
 * Created by bytedance on 2018/2/1.
 */

public class RewardVideoActivity extends ReactActivity {
    private Button mLoadAd;
    private Button mLoadAdVertical;
    private Button mShowAd;
    private TTAdNative mTTAdNative;
    private TTRewardVideoAd mttRewardVideoAd;
    private String mHorizontalCodeId;
    private String mVerticalCodeId;


    @SuppressWarnings("RedundantCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_reward_video);
        //step1:初始化sdk
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        //step2:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdManagerHolder.get().requestPermissionIfNecessary(this);
        //step3:创建TTAdNative对象,用于调用广告请求接口
        mTTAdNative = ttAdManager.createAdNative(getApplicationContext());
        getCodeId();
    }

    private void getCodeId() {
        Intent intent = getIntent();
        if(intent == null) {
            return;
        }
        mHorizontalCodeId = intent.getStringExtra("codeId");
        loadAd(mHorizontalCodeId);

    }


    private boolean mHasShowDownloadActive = false;

    private void loadAd(String codeId) {
        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(1080, 1920)
                .setRewardName("金币") //奖励的名称
                .setRewardAmount(3)  //奖励的数量
                .setUserID("user123")//用户id,必传参数
                .setMediaExtra("media_extra") //附加参数，可选
                .setOrientation(TTAdConstant.VERTICAL) //必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
                .build();
        //step5:请求广告
        mTTAdNative.loadRewardVideoAd(adSlot, new TTAdNative.RewardVideoAdListener() {
            @Override
            public void onError(int code, String message) {
            }

            //视频广告加载后，视频资源缓存到本地的回调，在此回调后，播放本地视频，流畅不阻塞。
            @Override
            public void onRewardVideoCached() {
            }

            //视频广告的素材加载完毕，比如视频url等，在此回调后，可以播放在线视频，网络不好可能出现加载缓冲，影响体验。
            @Override
            public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
                mttRewardVideoAd = ad;
                mttRewardVideoAd.showRewardVideoAd(RewardVideoActivity.this);
                mttRewardVideoAd.setRewardAdInteractionListener(new TTRewardVideoAd.RewardAdInteractionListener() {

                    @Override
                    public void onAdShow() {
                    }

                    @Override
                    public void onAdVideoBarClick() {
                    }

                    @Override
                    public void onAdClose() {
                        WritableMap event = Arguments.createMap();
                        event.putInt("code",1);
                        sendEventToRn("rewardCallback",event);
                        Log.e("close---","ok");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                RewardVideoActivity.this.onBackPressed();
                                RewardVideoActivity.this.finish();
                            }
                        });

                    }

                    //视频播放完成回调
                    @Override
                    public void onVideoComplete() {
                        RewardVideoActivity.this.finish();
                    }

                    @Override
                    public void onVideoError() {
                    }

                    //视频播放完成后，奖励验证回调，rewardVerify：是否有效，rewardAmount：奖励梳理，rewardName：奖励名称
                    @Override
                    public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName) {
                    }

                    @Override
                    public void onSkippedVideo() {
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
                        }
                    }

                    @Override
                    public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                    }

                    @Override
                    public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                    }

                    @Override
                    public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                    }

                    @Override
                    public void onInstalled(String fileName, String appName) {
                    }
                });
            }
        });
    }

    public  void sendEventToRn(String eventName, @Nullable WritableMap paramss)
    {

        RewardModule.context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, paramss);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
