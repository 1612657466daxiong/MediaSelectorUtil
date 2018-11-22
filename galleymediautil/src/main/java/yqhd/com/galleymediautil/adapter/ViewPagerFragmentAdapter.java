package yqhd.com.galleymediautil.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.List;

import yqhd.com.galleymediautil.R;
import yqhd.com.galleymediautil.config.ConfigKey;
import yqhd.com.galleymediautil.config.PictureMimeType;
import yqhd.com.galleymediautil.entity.LocalMedia;
import yqhd.com.galleymediautil.utils.DateUtils;
import yqhd.com.galleymediautil.view.longimage.ImageSource;
import yqhd.com.galleymediautil.view.longimage.ImageViewState;
import yqhd.com.galleymediautil.view.longimage.SubsamplingScaleImageView;
import yqhd.com.galleymediautil.view.photoview.OnViewTapListener;
import yqhd.com.galleymediautil.view.photoview.PhotoView;

/**
 * Created by chenpengxiang on 2018/9/29
 */
public class ViewPagerFragmentAdapter extends PagerAdapter {
    private List<LocalMedia> images;
    private Context mContext;
    private OnCallBackActivity onBackPressed;
    private OnPreviewClickListener onPreviewClickListener;

    public interface  OnPreviewClickListener{
       public void onPreviewAudioClick(LocalMedia media);
       public void onPreviewVideoClick(LocalMedia media);
    }


    public void setOnPreviewClickListener(OnPreviewClickListener onPreviewClickListener) {
        this.onPreviewClickListener = onPreviewClickListener;
    }

    public ViewPagerFragmentAdapter(List<LocalMedia> iamges, Context mContext, OnCallBackActivity onBackPressed) {
        super();
        this.images = iamges;
        this.mContext = mContext;
        this.onBackPressed = onBackPressed;
    }

    public interface OnCallBackActivity {
        /**
         * 关闭预览Activity
         */
        void onActivityBackPressed();
    }
    @Override
    public int getCount() {
        if (images != null) {
            return images.size();
        }
        return 0;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        (container).removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View contentView = LayoutInflater.from(container.getContext()).inflate(R.layout.pictrue_preview_item_layout,container,false);
        final PhotoView photoView =(PhotoView)contentView.findViewById(R.id.photo_view);
        final SubsamplingScaleImageView longImageView =(SubsamplingScaleImageView) contentView.findViewById(R.id.long_image_view);
        ImageView iv_play =(ImageView) contentView.findViewById(R.id.iv_video_play);
        ImageView iv_audio =(ImageView) contentView.findViewById(R.id.iv_audio_play);
        TextView tv_name =(TextView) contentView.findViewById(R.id.tv_file_name);
        TextView tv_duration = (TextView)contentView.findViewById(R.id.tv_duration);
        final LocalMedia media = images.get(position);
        boolean isAudio =false ;
        if (media!=null){
            String pictureType = media.getPictureType();

            if (pictureType.startsWith(ConfigKey.VIDEO)){
                iv_play.setVisibility(View.VISIBLE);
                iv_audio.setVisibility(View.GONE);
                tv_duration.setVisibility(View.VISIBLE);
                isAudio = false;
            }else if (pictureType.startsWith(ConfigKey.AUDIO)){
                isAudio = true;
                iv_audio.setVisibility(View.VISIBLE);
                iv_play.setVisibility(View.GONE);
                tv_duration.setVisibility(View.VISIBLE);
            }else if (pictureType.startsWith(ConfigKey.IMAGE)){
                iv_audio.setVisibility(View.GONE);
                iv_play.setVisibility(View.GONE);
                tv_duration.setVisibility(View.GONE);
                isAudio = false;
            }
            long duration = media.getDuration();
            tv_duration.setText(DateUtils.timeParse(duration));
            tv_name.setText(media.getName());
            String path = media.getPath();//后续这里添加压缩 以及裁剪 功能后 path获取裁剪压缩后的路径
            boolean isGif = PictureMimeType.isGif(pictureType);
            final boolean isLongImage = PictureMimeType.isLongImg(media);
            photoView.setVisibility(isLongImage? View.GONE: View.VISIBLE);
            longImageView.setVisibility(isLongImage && !isGif? View.VISIBLE: View.GONE);
            //图片加载 gif如果压缩过就不是gif了 后续注意
            if (isGif && !isAudio){
                Glide.with(contentView.getContext())
                        .load(path)
                        .asGif()
                        .override(480,800)
                        .priority(Priority.HIGH)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(photoView);
            }else if (!isGif && !isAudio ){
                Glide.with(contentView.getContext())
                        .load(path)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                if (isLongImage){
                                    longImageView.setQuickScaleEnabled(true);
                                    longImageView.setZoomEnabled(true);
                                    longImageView.setPanEnabled(true);
                                    longImageView.setDoubleTapZoomDuration(100);
                                    longImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
                                    longImageView.setDoubleTapZoomDpi(SubsamplingScaleImageView.ZOOM_FOCUS_CENTER);
                                    longImageView.setImage(ImageSource.cachedBitmap(resource),new ImageViewState(0,new PointF(0,0),0));
                                }else {
                                    photoView.setImageBitmap(resource);
                                }
                            }
                        });
            }else if (isAudio && !isLongImage){
               photoView.setImageResource(R.drawable.audio_placeholder);
            }
            photoView.setOnViewTapListener(new OnViewTapListener() {
                @Override
                public void onViewTap(View view, float x, float y) {
                    if (onBackPressed!=null){
                        onBackPressed.onActivityBackPressed();
                    }
                }
            });
            longImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onBackPressed != null) {
                        onBackPressed.onActivityBackPressed();
                    }
                }
            });
            iv_audio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onPreviewClickListener!=null){
                        onPreviewClickListener.onPreviewAudioClick(media);
                    }
                }
            });
            iv_play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (onPreviewClickListener!=null){
                        onPreviewClickListener.onPreviewVideoClick(media);
                    }
                }
            });
        }
        container.addView(contentView,0);
        return contentView;
    }
}
