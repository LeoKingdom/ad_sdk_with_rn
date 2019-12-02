package com.ad_sdk_with_rn.modules;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ad_sdk_with_rn.MainActivity;
import com.ad_sdk_with_rn.R;
import com.ad_sdk_with_rn.manager.TTAdManagerHolder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTImage;
import com.bytedance.sdk.openadsdk.TTNativeAd;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RnInterstitialReactModule extends ReactContextBaseJavaModule {
    private final String REACT_MODULE_NAME = "ReactInterstitial";
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
    private Dialog mAdDialog;
    private ViewGroup mRootView;
    private ImageView mAdImageView;
    private ImageView mCloseImageView;
    private TextView mDislikeView;
    private RequestManager mRequestManager;

    public RnInterstitialReactModule(@NonNull ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
        mRequestManager = Glide.with(reactContext);
        //step2:创建TTAdNative对象,用于调用广告请求接口
        mTTAdNative = TTAdManagerHolder.get().createAdNative(reactContext);
        //step3:可选，申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdManagerHolder.get().requestPermissionIfNecessary(reactContext);

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
    public void showInteristitalAd() {

        MainActivity.mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadInteractionAd("901121435");
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
    private void loadInteractionAd(String codeId) {
        //step4:创建广告请求参数AdSlot,注意其中的setNativeAdtype方法，具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(1080, 1920)
                .setNativeAdType(AdSlot.TYPE_INTERACTION_AD)//请求原生广告时候，请务必调用该方法，设置参数为TYPE_BANNER或TYPE_INTERACTION_AD
                .build();

        //step5:请求广告，对请求回调的广告作渲染处理
        mTTAdNative.loadNativeAd(adSlot, new TTAdNative.NativeAdListener() {
            @Override
            public void onError(int code, String message) {
                showToast("load error : " + code + ", " + message);
            }

            @Override
            public void onNativeAdLoad(List<TTNativeAd> ads) {
                if (ads.get(0) == null) {
                    return;
                }
                showAd(ads.get(0));
            }
        });
    }

    private void showAd(TTNativeAd ad) {
        mAdDialog = new Dialog(MainActivity.mainActivity, R.style.native_insert_dialog);
        mAdDialog.setCancelable(false);
        mAdDialog.setContentView(R.layout.native_insert_ad_layout);
        mRootView = mAdDialog.findViewById(R.id.native_insert_ad_root);
        mAdImageView = (ImageView) mAdDialog.findViewById(R.id.native_insert_ad_img);
        //限制dialog 的最大宽度不能超过屏幕，宽高最小为屏幕宽的 1/3
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int maxWidth = (dm == null) ? 0 : dm.widthPixels;
        int minWidth = maxWidth / 3;
        mAdImageView.setMaxWidth(maxWidth);
        mAdImageView.setMinimumWidth(minWidth);
        //noinspection SuspiciousNameCombination
        mAdImageView.setMinimumHeight(minWidth);
        mCloseImageView = (ImageView) mAdDialog.findViewById(R.id.native_insert_close_icon_img);
        mDislikeView = mAdDialog.findViewById(R.id.native_insert_dislike_text);

        ImageView iv = mAdDialog.findViewById(R.id.native_insert_ad_logo);

        //绑定关闭按钮
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ad.getAdLogo().compress(Bitmap.CompressFormat.PNG, 100, stream);
            mRequestManager
                    .load(stream.toByteArray())
                    .asBitmap()
                    .into(iv);
        }catch (Exception e){

        }finally {
            try {
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        bindCloseAction();
        //绑定网盟dislike逻辑，有助于精准投放
        bindDislikeAction(ad);
        //绑定广告view事件交互
        bindViewInteraction(ad);
        //加载ad 图片资源
        loadAdImage(ad);
    }

    private void bindCloseAction() {
        mCloseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdDialog.dismiss();
                WritableMap event = Arguments.createMap();
                event.putInt("interType", 1);
                sendEventToRn("interClick", event);
            }
        });
    }

    //接入网盟的dislike 逻辑，有助于提示广告精准投放度
    private void bindDislikeAction(TTNativeAd ad) {
        final TTAdDislike ttAdDislike = ad.getDislikeDialog(MainActivity.mainActivity);
        if (ttAdDislike != null) {
            ttAdDislike.setDislikeInteractionCallback(new TTAdDislike.DislikeInteractionCallback() {
                @Override
                public void onSelected(int position, String value) {
                    mAdDialog.dismiss();
                }

                @Override
                public void onCancel() {

                }
            });
        }
        mDislikeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ttAdDislike != null)
                    ttAdDislike.showDislikeDialog();
            }
        });
    }

    private void bindViewInteraction(TTNativeAd ad) {
        //可以被点击的view, 比如标题、icon等,点击后尝试打开落地页，也可以把nativeView放进来意味整个广告区域可被点击
        List<View> clickViewList = new ArrayList<>();
        clickViewList.add(mAdImageView);

        //触发创意广告的view（点击下载或拨打电话），比如可以设置为一个按钮，按钮上文案根据广告类型设定提示信息
        List<View> creativeViewList = new ArrayList<>();
        //如果需要点击图文区域也能进行下载或者拨打电话动作，请将图文区域的view传入
        //creativeViewList.add(nativeView);
        creativeViewList.add(mAdImageView);

        //重要! 这个涉及到广告计费，必须正确调用。convertView必须使用ViewGroup。
        ad.registerViewForInteraction(mRootView, clickViewList, creativeViewList, mDislikeView, new TTNativeAd.AdInteractionListener() {
            @Override
            public void onAdClicked(View view, TTNativeAd ad) {
                if (ad != null) {
                    WritableMap event = Arguments.createMap();
                    event.putInt("interType", 0);
                    event.putString("interTitle", ad.getTitle());
                    event.putString("interDes", ad.getDescription());
                    sendEventToRn("interClick", event);
                }
            }

            @Override
            public void onAdCreativeClick(View view, TTNativeAd ad) {
                if (ad != null) {
                    WritableMap event = Arguments.createMap();
                    event.putInt("interType", 0);
                    event.putString("interTitle", ad.getTitle());
                    event.putString("interDes", ad.getDescription());
                    sendEventToRn("interClick", event);
                }
            }

            @Override
            public void onAdShow(TTNativeAd ad) {
                if (ad != null) {
                }
            }
        });

    }

    private void loadAdImage(TTNativeAd ad) {
        if (ad.getImageList() != null && !ad.getImageList().isEmpty()) {
            TTImage image = ad.getImageList().get(0);
            if (image != null && image.isValid()) {
                mRequestManager.load(image.getImageUrl()).into(mAdImageView);
            }
        }

        TTImage image = ad.getImageList().get(0);
        int width = image.getWidth();
        String url = image.getImageUrl();
        mRequestManager.load(url).into(new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable glideDrawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
                if (mAdImageView != null) {
                    mAdImageView.setImageDrawable(glideDrawable);
                    showAd();
                }
            }
        });
    }

    private void showAd() {
        if (MainActivity.mainActivity.isFinishing()) {
            return;
        }
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalStateException("不能在子线程调用 TTInteractionAd.showInteractionAd");
        }
        mAdDialog.show();
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
