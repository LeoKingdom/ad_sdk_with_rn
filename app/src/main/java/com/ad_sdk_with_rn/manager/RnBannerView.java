package com.ad_sdk_with_rn.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ad_sdk_with_rn.R;
import com.bumptech.glide.Glide;

public class RnBannerView extends RelativeLayout {
    private TextView titleText;
    private TextView desText;
    private ImageView imgDislike;
    private Button mCreativeButton;
    private ImageView nim;
    private ImageView imc;

    public RnBannerView(Context context) {
        this(context, null);
    }

    public RnBannerView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public RnBannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, -1);
    }

    @SuppressLint("NewApi")
    public RnBannerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    public ImageView getImc() {
        return imc;
    }

    public ImageView getNim() {
        return nim;
    }

    public TextView getTitleText() {
        return titleText;
    }

    public TextView getDesText() {
        return desText;
    }

    public ImageView getImgDislike() {
        return imgDislike;
    }

    public Button getmCreativeButton() {
        return mCreativeButton;
    }

    private void initView(Context context) {
        View contentView = View.inflate(context, R.layout.native_ad, this);
        titleText = ((TextView) contentView.findViewById(R.id.tv_native_ad_title));
        desText = ((TextView) contentView.findViewById(R.id.tv_native_ad_desc));
        imgDislike = contentView.findViewById(R.id.img_native_dislike);
        mCreativeButton = (Button) contentView.findViewById(R.id.btn_native_creative);
        nim = findViewById(R.id.iv_native_image);
        imc = findViewById(R.id.iv_native_icon);
    }

    public void setTitle(String title){
        titleText.setText(title);
    }

    public void setDes(String des){
        desText.setText(des);
    }

    public void setNimSource(String imgUrl){
        Glide.with(getContext()).load(imgUrl).into(nim);
    }

    public void setimcSource(String imgUrl){
        Glide.with(getContext()).load(imgUrl).into(imc);
    }

    public void setBtnText(String text){
        mCreativeButton.setText(text);
    }

    public void showBtn(){
        mCreativeButton.setVisibility(VISIBLE);
    }

    public void hideBtn(){
        mCreativeButton.setVisibility(GONE);
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        post(measureAndLayout);
    }

    private final Runnable measureAndLayout = new Runnable() {
        @Override
        public void run() {
            measure(
                    MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
            layout(getLeft(), getTop(), getRight(), getBottom());
        }
    };
}
