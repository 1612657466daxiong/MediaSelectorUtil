package yqhd.com.galleymediautil.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import yqhd.com.galleymediautil.R;


/**
 * Created by chenpengxiang on 2018/9/21
 */
public class TakePhotoOrAudioPopWindow extends PopupWindow implements View.OnClickListener{
    private TextView tv_take_picture,tv_take_video,tv_take_audio,tv_cancel;
    private LinearLayout ll_root;
    private FrameLayout fl_content;
    private Animation animationIn,animationOut;
    private boolean isDismiss = false;


    public TakePhotoOrAudioPopWindow(Context context) {
        super(context);
        View inflate = LayoutInflater.from(context).inflate(R.layout.take_photo_or_video_or_audio_layout,null);
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        this.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        this.setBackgroundDrawable(new ColorDrawable());
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.update();
        this.setBackgroundDrawable(new ColorDrawable());
        this.setContentView(inflate);
        animationIn = AnimationUtils.loadAnimation(context, R.anim.up_in);
        animationOut = AnimationUtils.loadAnimation(context, R.anim.down_out);
        initView(inflate);
    }

    private void initView(View inflate) {
        ll_root = (LinearLayout) inflate.findViewById(R.id.ll_content);
        fl_content = (FrameLayout) inflate.findViewById(R.id.fl_content);
        tv_take_audio = (TextView) inflate.findViewById(R.id.picture_tv_audio);
        tv_take_picture = (TextView)inflate.findViewById(R.id.picture_tv_photo);
        tv_take_video = (TextView)inflate.findViewById(R.id.picture_tv_video);
        tv_cancel = (TextView) inflate.findViewById(R.id.picture_tv_cancel);
        tv_cancel.setOnClickListener(this);
        tv_take_video.setOnClickListener(this);
        tv_take_picture.setOnClickListener(this);
        tv_take_audio.setOnClickListener(this);
        fl_content.setOnClickListener(this);

    }

    @Override
    public void showAsDropDown(View parent) {
        try {
            if (Build.VERSION.SDK_INT >= 24) {
                int[] location = new int[2];
                parent.getLocationOnScreen(location);
                int x = location[0];
                int y = location[1] + parent.getHeight();
                this.showAtLocation(parent, Gravity.BOTTOM, x, y);
            } else {
                this.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
            }

            isDismiss = false;
            ll_root.startAnimation(animationIn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void dismiss() {
        if (isDismiss) {
            return;
        }
        isDismiss = true;
        ll_root.startAnimation(animationOut);
        dismiss();
        animationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isDismiss = false;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                    dismiss4Pop();
                } else {
                    TakePhotoOrAudioPopWindow.super.dismiss();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    /**
     * 在android4.1.1和4.1.2版本关闭PopWindow
     */
    private void dismiss4Pop() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                TakePhotoOrAudioPopWindow.super.dismiss();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (onTakeClickListenner!=null){
            if(id == R.id.picture_tv_video){
                onTakeClickListenner.onTakeVideo();
            }
            if (id == R.id.picture_tv_photo){
                onTakeClickListenner.onTakePicture();
            }
            if (id == R.id.picture_tv_audio){
                onTakeClickListenner.onTakeAudio();
            }
        }
        dismiss();
    }

    private OnTakeClickListenner onTakeClickListenner;

    public void setOnTakeClickListenner(OnTakeClickListenner onTakeClickListenner) {
        this.onTakeClickListenner = onTakeClickListenner;
    }

    public interface OnTakeClickListenner{
        void onTakePicture();
        void onTakeAudio();
        void onTakeVideo();
    }
}
