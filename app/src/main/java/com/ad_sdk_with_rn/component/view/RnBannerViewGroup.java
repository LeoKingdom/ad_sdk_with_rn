package com.ad_sdk_with_rn.component.view;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


import com.ad_sdk_with_rn.MainActivity;
import com.ad_sdk_with_rn.component.manager.RnBannerView;
import com.ad_sdk_with_rn.component.manager.TTAdManagerHolder;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTImage;
import com.bytedance.sdk.openadsdk.TTNativeAd;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.views.view.ReactViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class RnBannerViewGroup extends ReactViewGroup {
    private final Runnable measureAndLayout = new Runnable() {
        @Override
        public void run() {
            measure(
                    MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
            layout(getLeft(), getTop(), getRight(), getBottom());
        }
    };
    private final int width;
    private final int height;
    TTNativeAd nativeAd;
    private View view;
    private Button mCreativeButton;
    private ReactContext context;
//    private FrameLayout frameLayout;
    private RnBannerView bannerView;
    private ImageView imgDislike;
    private TextView titleTv;
    private TextView desTv;

    public RnBannerViewGroup(ReactContext context) {
        super(context);
        this.context = context;
        TTAdNative mTTAdNative = TTAdManagerHolder.get().createAdNative(context);
        //step3:可选，申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdManagerHolder.get().requestPermissionIfNecessary(context);
        width= MainActivity.mainActivity.getWindowManager().getDefaultDisplay().getWidth();
        height=MainActivity.mainActivity.getWindowManager().getDefaultDisplay().getHeight();

        loadBannerAd(mTTAdNative, "901121423");
    }

    public void show(){
        this.bannerView.show();
    }

    @SuppressWarnings({"ALL", "SameParameterValue"})
    private void loadBannerAd(TTAdNative mTTAdNative, String codeId) {
//        frameLayout = new FrameLayout(context);
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
//        frameLayout.setLayoutParams(params);
        bannerView = new RnBannerView(context);
//        this.bannerView.hide();
        imgDislike = bannerView.getImgDislike();
        mCreativeButton = bannerView.getmCreativeButton();
        //step4:创建广告请求参数AdSlot,注意其中的setNativeAdtype方法，具体参数含义参考文档
        final AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(600, 150)
                .setNativeAdType(AdSlot.TYPE_BANNER) //请求原生广告时候，请务必调用该方法，设置参数为TYPE_BANNER或TYPE_INTERACTION_AD
                .setAdCount(1)
                .build();

        //step5:请求广告，对请求回调的广告作渲染处理
        mTTAdNative.loadNativeAd(adSlot, new TTAdNative.NativeAdListener() {


            @Override
            public void onError(int code, String message) {
            }

            @Override
            public void onNativeAdLoad(List<TTNativeAd> ads) {
                if (ads.get(0) == null) {
                    return;
                }
                nativeAd = ads.get(0);
                setAdData();

            }
        });
        this.addView(bannerView);
        bannerView.layout(-width/2,0,width/2,height);
        bannerView.requestFocus();
        requestFocus();

    }

    @SuppressWarnings("RedundantCast")
    public void setAdData() {
        this.bannerView.setTitle(nativeAd.getTitle());
        this.bannerView.setDes(nativeAd.getDescription());
        if (nativeAd.getImageList() != null && !nativeAd.getImageList().isEmpty()) {
            TTImage image = nativeAd.getImageList().get(0);
            if (image != null && image.isValid()) {
                this.bannerView.setNimSource(image.getImageUrl());
            }
        }
        TTImage icon = nativeAd.getIcon();
        if (icon != null && icon.isValid()) {
            ImageView im = bannerView.getImc();
            this.bannerView.setimcSource(icon.getImageUrl());
        }

        //可根据广告类型，为交互区域设置不同提示信息
        switch (nativeAd.getInteractionType()) {
            case TTAdConstant.INTERACTION_TYPE_DOWNLOAD:
                //如果初始化ttAdManager.createAdNative(getApplicationContext())没有传入activity 则需要在此传activity，否则影响使用Dislike逻辑
                nativeAd.setActivityForDownloadApp(MainActivity.mainActivity);
                this.bannerView.showBtn();
//                nativeAd.setDownloadListener(mDownloadListener); // 注册下载监听器
                break;
            case TTAdConstant.INTERACTION_TYPE_DIAL:
                this.bannerView.showBtn();
                this.bannerView.setBtnText("立即拨打");
                break;
            case TTAdConstant.INTERACTION_TYPE_LANDING_PAGE:
            case TTAdConstant.INTERACTION_TYPE_BROWSER:
                this.bannerView.showBtn();
                this.bannerView.setBtnText("查看详情");
                break;
            default:
                this.bannerView.hideBtn();
        }

        //可以被点击的view, 也可以把nativeView放进来意味整个广告区域可被点击
        List<View> clickViewList = new ArrayList<>();
        clickViewList.add(bannerView);

        //触发创意广告的view（点击下载或拨打电话）
        List<View> creativeViewList = new ArrayList<>();
        //如果需要点击图文区域也能进行下载或者拨打电话动作，请将图文区域的view传入
        //creativeViewList.add(nativeView);
        creativeViewList.add(mCreativeButton);

        //重要! 这个涉及到广告计费，必须正确调用。convertView必须使用ViewGroup。
//        nativeAd.registerViewForInteraction((ViewGroup) bannerView, clickViewList, creativeViewList, imgDislike, new TTNativeAd.AdInteractionListener() {
//            @Override
//            public void onAdClicked(View view, TTNativeAd ad) {
//                if (ad != null) {
//                    WritableMap event = Arguments.createMap();
//                    event.putInt("code", 0);
//                    sendEventToRn("bannerCallback", event);
//                }
//            }
//
//            @Override
//            public void onAdCreativeClick(View view, TTNativeAd ad) {
//                if (ad != null) {
//                    WritableMap event = Arguments.createMap();
//                    event.putInt("code", 0);
//                    sendEventToRn("bannerCallback", event);
//                }
//            }
//
//            @Override
//            public void onAdShow(TTNativeAd ad) {
//                if (ad != null) {
//                }
//            }
//        });
        this.bannerView.show();


    }

    //接入网盟的dislike 逻辑，有助于提示广告精准投放度
    public void bindDislikeAction() {
        imgDislike.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("click---","ok");
                bannerView.hide();
                WritableMap event = Arguments.createMap();
                event.putInt("code", 1);
                sendEventToRn("bannerCallback", event);
            }
        });

    }

    public void sendEventToRn(String eventName, @Nullable WritableMap paramss) {

        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, paramss);

    }
}
