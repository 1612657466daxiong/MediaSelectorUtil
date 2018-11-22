package yqhd.com.galleymediautil.adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import yqhd.com.galleymediautil.R;
import yqhd.com.galleymediautil.anim.OptAnimationLoader;
import yqhd.com.galleymediautil.config.ConfigKey;
import yqhd.com.galleymediautil.config.PictureMimeType;
import yqhd.com.galleymediautil.config.SelectionConfig;
import yqhd.com.galleymediautil.entity.LocalMedia;
import yqhd.com.galleymediautil.utils.DateUtils;
import yqhd.com.galleymediautil.utils.StringUtils;

/**
 * Created by chenpengxiang on 2018/9/18
 */
public class DiyMediaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    private final  static int DURATION  = 450;
    private Context context;
    private boolean isVisibleCamera = true;
    private int maxSelectNum;
    private OnPhotoSelectChangedListener imageSelectChangedListener;
    private List<LocalMedia> images = new ArrayList<LocalMedia>();
    private List<LocalMedia> selectImages = new ArrayList<LocalMedia>();
    private boolean enablePreview;
    private int mimeType;
    private SelectionConfig config;
    private Animation animation;

    /**
     * 单选图片
     */
    private boolean isGo;

    public DiyMediaAdapter(Context context, SelectionConfig config) {
        this.context = context;
        this.config = config;
        this.enablePreview = config.enablePreview;
        this.isVisibleCamera = config.isVisibleCamera;
        this.maxSelectNum = config.maxSelectNum;
        this.mimeType =config.mimeType;
        animation = OptAnimationLoader.loadAnimation(context, R.anim.modal_in);
    }

    public void setVisibleCamera(boolean visibleCamera) {
        isVisibleCamera = visibleCamera;
    }


    public void bindData(List<LocalMedia> localMedia){
        this.images = localMedia;
        notifyDataSetChanged();
    }

    public void bindSelectImages(List<LocalMedia> images) {
        // 这里重新构构造一个新集合，不然会产生已选集合一变，结果集合也会添加的问题
        List<LocalMedia> selection = new ArrayList<>();
        for (LocalMedia media : images) {
            selection.add(media);
        }
        this.selectImages = selection;
        if (imageSelectChangedListener != null) {
            imageSelectChangedListener.onChange(selectImages);
        }
    }

    public List<LocalMedia> getSelectImages() {
        if (selectImages==null){
            selectImages = new ArrayList<>();
        }
        return selectImages;
    }

    public List<LocalMedia> getImages() {
        if (images == null) {
            images = new ArrayList<>();
        }
        return images;
    }


    @Override
    public int getItemViewType(int position) {
        if (isVisibleCamera && position == 0){
            return ConfigKey.TYPE_CAMERA;
        }else {
            return ConfigKey.TYPE_PICTURE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ConfigKey.TYPE_CAMERA) {
            View view = LayoutInflater.from(context).inflate(R.layout.picture_item_camera, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.picture_image_grid_item, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == ConfigKey.TYPE_CAMERA){
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.headerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (imageSelectChangedListener!=null){
                        imageSelectChangedListener.onOpenCamera();
                    }
                }
            });
        }else {
            final int adapterposition = position;
            final ViewHolder contentHolder = (ViewHolder) holder;
            final LocalMedia localMedia = images.get(isVisibleCamera?position-1:position);
            localMedia.position = contentHolder.getAdapterPosition();
            final String path = localMedia.getPath();
            String name = localMedia.getName();
            String pictureType = localMedia.getPictureType();
            final int mediaMimeType = PictureMimeType.isPictureType(pictureType);
            contentHolder.tv_name.setText(name);
            selectImage(contentHolder,isSelected(localMedia),false);

            boolean gif = PictureMimeType.isGif(pictureType);
            contentHolder.tv_gif.setVisibility(gif? View.VISIBLE: View.GONE);
            boolean longImage = PictureMimeType.isLongImg(localMedia);
            contentHolder.tv_longImage.setVisibility(longImage? View.VISIBLE: View.GONE);
            if (mediaMimeType == ConfigKey.TYPE_VIDEO){
                contentHolder.tv_type.setVisibility(View.VISIBLE);
                Drawable drawable = context.getResources().getDrawable(R.drawable.video_icon);
                StringUtils.modifyTextViewDrawable(contentHolder.tv_type, drawable, 0);
                long duration = localMedia.getDuration();
                contentHolder.tv_type.setText(DateUtils.timeParse(duration));
            }else if (mediaMimeType == ConfigKey.TYPE_AUDIO){
                contentHolder.tv_type.setVisibility(View.VISIBLE);
                Drawable drawable = context.getResources().getDrawable(R.drawable.picture_audio);
                StringUtils.modifyTextViewDrawable(contentHolder.tv_type, drawable, 0);
                long duration = localMedia.getDuration();
                contentHolder.tv_type.setText(DateUtils.timeParse(duration));
            }else if (mediaMimeType == ConfigKey.TYPE_IMAGE){
                contentHolder.tv_type.setVisibility(View.INVISIBLE);
            }
            if (PictureMimeType.isAudio(localMedia.getPictureType())){
                contentHolder.iv_picture.setImageResource(R.drawable.audio_placeholder);
            }else {
                Glide.with(context)
                        .load(path)
                        .asBitmap()
                        .centerCrop()
                        .placeholder(R.drawable.zhanweitu)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)//低版本使用All策略 会导致视频缩略图无法显示
                        .into(contentHolder.iv_picture);
            }
            if (enablePreview){
                contentHolder.ll_check.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!new File(path).exists()){
                            Toast.makeText(context, PictureMimeType.s(context, mediaMimeType), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        changCheckBoxState(contentHolder,localMedia);
                    }
                });
            }
            contentHolder.contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!new File(path).exists()) {
                        Toast.makeText(context, PictureMimeType.s(context, mediaMimeType), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int index = isVisibleCamera ? adapterposition - 1 : adapterposition;
                    boolean eqResult =
                            mediaMimeType == ConfigKey.TYPE_IMAGE && enablePreview
                                    || mediaMimeType == ConfigKey.TYPE_VIDEO && (enablePreview || maxSelectNum ==1)
                                    || mediaMimeType == ConfigKey.TYPE_AUDIO && (enablePreview || maxSelectNum ==1);
                    if (eqResult) {
                        imageSelectChangedListener.onPictureClick(localMedia, index);
                    } else {
                        changCheckBoxState(contentHolder, localMedia);
                    }
                }
            });
        }
    }


    public boolean isSelected(LocalMedia image) {
        for (LocalMedia media : selectImages) {
            if (media.getPath().equals(image.getPath())) {
                return true;
            }
        }
        return false;
    }
    private void changCheckBoxState(ViewHolder contentHolder, LocalMedia localMedia) {
        boolean isChecked =contentHolder.check.isSelected();
        String pictureType = selectImages.size() > 0 ? selectImages.get(0).getPictureType() : "";
//        if (!TextUtils.isEmpty(pictureType)) {
//            return;
//        }
        if (selectImages.size() >= maxSelectNum && !isChecked) {
            boolean eqImg = pictureType.startsWith(ConfigKey.IMAGE);
            String str = eqImg ? context.getString(R.string.picture_message_max_num, maxSelectNum)
                    : context.getString(R.string.picture_message_video_max_num, maxSelectNum);
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
            return;
        }
        if (isChecked) {
            if (maxSelectNum == 1){
                selectImages.remove(localMedia);
                disZoom(contentHolder.iv_picture);
            }
            for (LocalMedia media : selectImages) {
                if (media.getPath().equals(localMedia.getPath())) {
                    selectImages.remove(media);
//                    subSelectPosition();
                    disZoom(contentHolder.iv_picture);
                    break;
                }
            }
        } else {
            // 如果是单选，则清空已选中的并刷新列表(作单一选择)
            if (maxSelectNum == 1) {
                singleRadioMediaImage();
            }
            selectImages.add(localMedia);
            localMedia.setNum(selectImages.size());
//            VoiceUtils.playVoice(context, enablePreview);
            zoom(contentHolder.iv_picture);
        }
        //通知点击项发生了改变
        notifyItemChanged(contentHolder.getAdapterPosition());
        selectImage(contentHolder, !isChecked, true);
        if (imageSelectChangedListener != null) {
            imageSelectChangedListener.onChange(selectImages);
        }
    }


    /**
     * 单选模式
     */
    private void singleRadioMediaImage() {
        if (selectImages != null
                && selectImages.size() > 0) {
            isGo = true;
            LocalMedia media = selectImages.get(0);
            notifyItemChanged(config.isVisibleCamera ? media.position :
                    isGo ? media.position : media.position > 0 ? media.position - 1 : 0);
            selectImages.clear();
        }
    }

    @Override
    public int getItemCount() {
        return isVisibleCamera ? images.size() + 1 : images.size();
    }

    public void setOnPhotoSelectChangedListener(OnPhotoSelectChangedListener
                                                        imageSelectChangedListener) {
        this.imageSelectChangedListener = imageSelectChangedListener;
    }

    public interface OnPhotoSelectChangedListener {
        /**
         * 拍照回调
         */
        void onOpenCamera();

        /**
         * 已选Media回调
         *
         * @param selectImages
         */
        void onChange(List<LocalMedia> selectImages);

        /**
         * 图片预览回调
         *
         * @param media
         * @param position
         */
        void onPictureClick(LocalMedia media, int position);
    }


    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        View headerView;
        TextView tv_title_camera;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            headerView = itemView;
            tv_title_camera = (TextView) itemView.findViewById(R.id.tv_title_camera);
            String title = mimeType == PictureMimeType.ofAudio() ?
                    context.getString(R.string.picture_tape)
                    : context.getString(R.string.picture_take_picture);
            tv_title_camera.setText(title);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_picture;
        TextView check;
        TextView tv_name,tv_type,tv_gif,tv_longImage;
        View contentView;
        RelativeLayout ll_check;
        RelativeLayout layout_bottom;

        public ViewHolder(View itemView) {
            super(itemView);
            contentView = itemView;
            tv_gif = (TextView) itemView.findViewById(R.id.tv_isGif);
            tv_longImage = (TextView)itemView.findViewById(R.id.tv_long_chart);
            iv_picture = (ImageView) itemView.findViewById(R.id.iv_picture);
            check = (TextView) itemView.findViewById(R.id.check);
            ll_check = (RelativeLayout) itemView.findViewById(R.id.ll_check);
            tv_name = (TextView) itemView.findViewById(R.id.tv_media_name);
            tv_type = (TextView) itemView.findViewById(R.id.tv_media_type);
            layout_bottom = (RelativeLayout) itemView.findViewById(R.id.bottom_layout);
        }
    }

    private void zoom(ImageView iv_img) {

        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(iv_img, "scaleX", 1f, 1.12f),
                ObjectAnimator.ofFloat(iv_img, "scaleY", 1f, 1.12f)
        );
        set.setDuration(DURATION);
        set.start();

    }

    private void disZoom(ImageView iv_img) {

        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(iv_img, "scaleX", 1.12f, 1f),
                ObjectAnimator.ofFloat(iv_img, "scaleY", 1.12f, 1f)
        );
        set.setDuration(DURATION);
        set.start();

    }

    /**
     * 选中的图片并执行动画
     *
     * @param holder
     * @param isChecked
     * @param isAnim
     */
    public void selectImage(ViewHolder holder, boolean isChecked, boolean isAnim) {
        holder.check.setSelected(isChecked);
        if (isChecked) {
            if (isAnim) {
                if (animation != null) {
                    holder.check.startAnimation(animation);
                }
            }
            holder.iv_picture.setColorFilter(context.getResources().getColor(R.color.image_overlay_true), PorterDuff.Mode.SRC_ATOP);
        } else {
            holder.iv_picture.setColorFilter(context.getResources().getColor(R.color.image_overlay_false), PorterDuff.Mode.SRC_ATOP);
        }
    }
}
