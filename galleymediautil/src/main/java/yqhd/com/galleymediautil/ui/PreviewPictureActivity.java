package yqhd.com.galleymediautil.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;


import yqhd.com.galleymediautil.R;
import yqhd.com.galleymediautil.adapter.ViewPagerFragmentAdapter;
import yqhd.com.galleymediautil.anim.OptAnimationLoader;
import yqhd.com.galleymediautil.base.BaseSelectActivity;
import yqhd.com.galleymediautil.config.ConfigKey;
import yqhd.com.galleymediautil.entity.EventEntity;
import yqhd.com.galleymediautil.entity.LocalMedia;
import yqhd.com.galleymediautil.rxbus2.RxBus;
import yqhd.com.galleymediautil.utils.DateUtils;
import yqhd.com.galleymediautil.utils.ScreenUtils;
import yqhd.com.galleymediautil.view.CustomDialog;
import yqhd.com.galleymediautil.view.PreviewViewPager;

public class PreviewPictureActivity extends BaseSelectActivity implements ViewPagerFragmentAdapter.OnCallBackActivity,
        ViewPagerFragmentAdapter.OnPreviewClickListener,Animation.AnimationListener, View.OnClickListener{
    private TextView mTvSure,mTvTitle,check;
    private LinearLayout llCheck;
    private ImageView mIvBack;
    private PreviewViewPager viewPager;
    private ViewPagerFragmentAdapter adapter;
    private int position;
    private List<LocalMedia> images = new ArrayList<>();
    private List<LocalMedia> selectImages = new ArrayList<>();
    private Animation animation;
    private int index;
    private boolean refresh;

    //preview audio dialog
    private CustomDialog audioDialog;
    private TextView mTvAudioName,mTvTime,mTvAudioTotalTime;
    private SeekBar mTvAudioSeekBar;
    private ImageView mIvAudioClose,mIvAudioControl;
    private MediaPlayer mediaPlayer;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preview_picture);
        if (!RxBus.getDefault().isRegistered(this)) {
            RxBus.getDefault().register(this);
        }
        initView();
    }

    private void initView() {
        animation = OptAnimationLoader.loadAnimation(this, R.anim.modal_in);
        animation.setAnimationListener(this);
        check =(TextView)findViewById(R.id.check);
        mTvSure = (TextView) findViewById(R.id.tv_ok);
        mTvTitle = (TextView) findViewById(R.id.picture_title);
        llCheck =(LinearLayout) findViewById(R.id.ll_check);
        mIvBack = (ImageView) findViewById(R.id.picture_left_back);
        viewPager = (PreviewViewPager)findViewById(R.id.view_pager);
        mIvBack.setOnClickListener(this);
        mTvSure.setOnClickListener(this);
        selectImages = (List<LocalMedia>) getIntent().
                getSerializableExtra(ConfigKey.EXTRA_SELECT_MEDIA_LIST);
        images = (List<LocalMedia>) getIntent().getSerializableExtra(ConfigKey.EXTRA_ALL_LIST);
        position = getIntent().getIntExtra(ConfigKey.EXTRA_SELECT_POSITION, 0);
        initPagerAdapterData();
        llCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (images != null && images.size() > 0) {
                    LocalMedia image = images.get(viewPager.getCurrentItem());
//                    String pictureType = selectImages.size() > 0 ?
//                            selectImages.get(0).getPictureType() : "";
//                    if (!TextUtils.isEmpty(pictureType)) {
//                        boolean toEqual = PictureMimeType.
//                                mimeToEqual(pictureType, image.getPictureType());
//                        if (!toEqual) {
//                            Toast.makeText((mContext,getString(R.string.picture_rule),Toast.LENGTH_SHORT);
//                            return;
//                        }
//                    }
                    // 刷新图片列表中图片状态
                    boolean isChecked;
                    if (!check.isSelected()) {
                        isChecked = true;
                        check.setSelected(true);
                        check.startAnimation(animation);
                    } else {
                        isChecked = false;
                        check.setSelected(false);
                    }
                    if (selectImages.size() >= config.maxSelectNum && isChecked) {
                        Toast.makeText(mContext, getString(R.string.picture_message_max_num, config.maxSelectNum), Toast.LENGTH_LONG);
                        check.setSelected(false);
                        return;
                    }
                    if (isChecked) {
//                        VoiceUtils.playVoice(mContext, config.openClickSound);
                        // 如果是单选，则清空已选中的并刷新列表(作单一选择)
                        if (config.maxSelectNum == 1) {
                            singleRadioMediaImage();
                        }
                        selectImages.add(image);
                        image.setNum(selectImages.size());
                    } else {
                        for (LocalMedia media : selectImages) {
                            if (media.getPath().equals(image.getPath())) {
                                selectImages.remove(media);
                                break;
                            }
                        }
                    }
                    onSelectNumChange(true);
                }
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                isPreviewEggs(config.previewEggs, position, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int i) {
                position = i;
                mTvTitle.setText(position + 1 + "/" + images.size());
                LocalMedia media = images.get(position);
                index = media.getPosition();
                onImageChecked(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void onSelectNumChange(boolean isRefresh) {
        this.refresh = isRefresh;
        boolean enable = selectImages.size() != 0;
        if(enable){
            mTvSure.setSelected(true);
            mTvSure.setEnabled(true);
            mTvSure.setText(getString(R.string.picture_done_front_num,selectImages.size(),config.maxSelectNum));
            if (refresh){
                mTvSure.startAnimation(animation);
            }
        }else {
            mTvSure.setEnabled(false);
            mTvSure.setSelected(false);
            mTvSure.setText(getString(R.string.picture_done_front_num,0,config.maxSelectNum));
        }
        updateSelector(refresh);
    }
    /**
     * 更新图片列表选中效果
     *
     * @param isRefresh
     */
    private void updateSelector(boolean isRefresh) {
        if (isRefresh) {
            EventEntity obj = new EventEntity(ConfigKey.UPDATE_FLAG, selectImages, index);
            RxBus.getDefault().post(obj);
        }
    }

    /**
     * 判断当前图片是否选中
     *
     * @param position
     */
    public void onImageChecked(int position) {
        if (images != null && images.size() > 0) {
            LocalMedia media = images.get(position);
            check.setSelected(isSelected(media));
        } else {
            check.setSelected(false);
        }
    }

    /**
     * 当前图片是否选中
     *
     * @param image
     * @return
     */
    public boolean isSelected(LocalMedia image) {
        for (LocalMedia media : selectImages) {
            if (media.getPath().equals(image.getPath())) {
                return true;
            }
        }
        return false;
    }

//    /**
//     * 选择按钮更新
//     */
//    private void notifyCheckChanged(LocalMedia imageBean) {
////        if (config.checkNumMode) {
////            check.setText("");
//            for (LocalMedia media : selectImages) {
//                if (media.getPath().equals(imageBean.getPath())) {
//                    imageBean.setNum(media.getNum());
//                    check.setText(String.valueOf(imageBean.getNum()));
//                    check.setSelected(true);
//                }
//            }
////        }
//    }

    private void singleRadioMediaImage() {
        if (selectImages != null
                && selectImages.size() > 0) {
            LocalMedia media = selectImages.get(0);
            RxBus.getDefault()
                    .post(new EventEntity(ConfigKey.UPDATE_FLAG,
                            selectImages, media.getPosition()));
            selectImages.clear();
        }
    }

    private void initPagerAdapterData() {
        mTvTitle.setText(position + 1+"/"+images.size());
        adapter = new ViewPagerFragmentAdapter(images,this,this);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);

        adapter.setOnPreviewClickListener(this);
        onSelectNumChange(false);
        onImageChecked(position);
        if (images.size() > 0) {
            LocalMedia media = images.get(position);
            index = media.getPosition();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (RxBus.getDefault().isRegistered(this)) {
            RxBus.getDefault().unregister(this);
        }
        if (animation != null) {
            animation.cancel();
            animation = null;
        }
    }


    /**
     * 播放音频点击事件
     */
    public class audioOnClick implements View.OnClickListener {
        private LocalMedia path;

        public audioOnClick(LocalMedia path) {
            super();
            this.path = path;
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.picture_audio_control) {
                playAudio();
            }

            if (id == R.id.audio_preview_cloase) {
                handler.removeCallbacks(runnable);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stop(path.getPath());
                    }
                }, 30);
                try {
                    if (audioDialog != null
                            && audioDialog.isShowing()) {
                        audioDialog.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 初始化音频预览播放组件
     * 并开始播放
     * */
    private void initPlayer(LocalMedia path) {
        mediaPlayer = new MediaPlayer();
        try{
            mediaPlayer.setDataSource(path.getPath());
            mediaPlayer.prepare();
            mediaPlayer.setLooping(false);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mIvAudioControl.setImageDrawable(getResources().getDrawable(R.drawable.start_audio));
                }
            });
            playAudio();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private boolean isPlayAudio = false;//是否在播放的标志
    private void playAudio() {
        if (mediaPlayer!=null){
            mTvAudioSeekBar.setProgress(mediaPlayer.getCurrentPosition());
            mTvAudioSeekBar.setMax(mediaPlayer.getDuration());
        }
        if (mediaPlayer.isPlaying()){
            mIvAudioControl.setImageDrawable(getResources().getDrawable(R.drawable.start_audio));
            playOrPause();
        }else {

            mIvAudioControl.setImageDrawable(getResources().getDrawable(R.drawable.stop_audio));
            playOrPause();
        }
        if (isPlayAudio == false) {
            handler.post(runnable);
            isPlayAudio = true;
        }

    }


    /**
     * 停止播放
     *
     * @param path
     */
    public void stop(String path) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 暂停播放
     */
    public void playOrPause() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * mediaPlayer与dialog界面交互 通过handler
     * */
    public Handler handler = new Handler();
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try{
                if (mediaPlayer!=null){
                    mTvTime.setText(DateUtils.timeParse(mediaPlayer.getCurrentPosition()));
                    mTvAudioSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                    mTvAudioSeekBar.setMax(mediaPlayer.getDuration());
                    mTvAudioTotalTime.setText(DateUtils.timeParse(mediaPlayer.getDuration()));
                    handler.postDelayed(runnable, 200);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onPreviewAudioClick(final LocalMedia media) {
            int audioH = ScreenUtils.getScreenHeight(mContext)+ScreenUtils.getStatusBarHeight(mContext);
            audioDialog = new CustomDialog(mContext, LinearLayout.LayoutParams.MATCH_PARENT,audioH,R.layout.preview_audio_layout,R.style.Theme_dialog);
            mTvAudioName = audioDialog.findViewById(R.id.tv_music_name);
            mTvTime = audioDialog.findViewById(R.id.tv_musicTime);
            mTvAudioTotalTime = audioDialog.findViewById(R.id.tv_musicTotal);
            mTvAudioSeekBar = audioDialog.findViewById(R.id.music_seekbar);
            mIvAudioClose = audioDialog.findViewById(R.id.audio_preview_cloase);
            mIvAudioControl = audioDialog.findViewById(R.id.picture_audio_control);
            mTvAudioName.setText(media.getName());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initPlayer(media);
                }
            },30);
            mIvAudioControl.setOnClickListener(new audioOnClick(media));
            mIvAudioClose.setOnClickListener(new audioOnClick(media));
            mTvAudioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser == true) {
                        mediaPlayer.seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            audioDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    handler.removeCallbacks(runnable);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            stop(media.getPath());
                        }
                    }, 30);
                    try {
                        if (audioDialog != null
                                && audioDialog.isShowing()) {
                            audioDialog.dismiss();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            handler.post(runnable);
            audioDialog.show();

    }

    @Override
    public void onPreviewVideoClick(LocalMedia media) {
        Intent intent = new Intent(this,PreviewVideoActivity.class);
        intent.putExtra("video",media);
        startActivity(intent);
    }

    @Override
    public void onActivityBackPressed() {
        onBackPressed();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id ==  R.id.picture_left_back){
            onBackPressed();
        }
        if (id == R.id.tv_ok) {
            // 如果设置了图片最小选择数量，则判断是否满足条件
            int size = selectImages.size();
            LocalMedia image = selectImages.size() > 0 ? selectImages.get(0) : null;
            String pictureType = image != null ? image.getPictureType() : "";
            if (config.minSelectNum > 0) {
                if (size < config.minSelectNum && config.maxSelectNum >1) {
                    boolean eqImg = pictureType.startsWith(ConfigKey.IMAGE);
                    String str = eqImg ? getString(R.string.picture_min_img_num, config.minSelectNum)
                            : getString(R.string.picture_min_video_num, config.minSelectNum);
                    Toast.makeText(mContext,str, Toast.LENGTH_LONG);
                    return;
                }
            }
//            if (config.enableCrop && pictureType.startsWith(PictureConfig.IMAGE)) {
//                if (config.maxSelectNum == 1) {
//                    originalPath = image.getPath();
//                    startCrop(originalPath);
//                } else {
//                    // 是图片和选择压缩并且是多张，调用批量压缩
//                    ArrayList<String> cuts = new ArrayList<>();
//                    for (LocalMedia media : selectImages) {
//                        cuts.add(media.getPath());
//                    }
//                    startCrop(cuts);
//                }
//            } else {
                onResult(selectImages);
//            }
        }
    }

    @Override
    public void onBackPressed() {
        closeActivity();
    }

    private void closeActivity() {
        finish();
        if (config.openCamera) {
            overridePendingTransition(0, R.anim.fade_out);
        } else {
            overridePendingTransition(0, R.anim.a3);
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        updateSelector(refresh);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
