package yqhd.com.galleymediautil.ui;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.yalantis.ucrop.UCrop;


import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import yqhd.com.galleymediautil.MediaSelectedHelper;
import yqhd.com.galleymediautil.R;
import yqhd.com.galleymediautil.adapter.AlbumDirectoryAdapter;
import yqhd.com.galleymediautil.adapter.DiyMediaAdapter;
import yqhd.com.galleymediautil.base.BaseSelectActivity;
import yqhd.com.galleymediautil.config.ConfigKey;
import yqhd.com.galleymediautil.config.PictureMimeType;
import yqhd.com.galleymediautil.entity.EventEntity;
import yqhd.com.galleymediautil.entity.LocalMedia;
import yqhd.com.galleymediautil.entity.LocalMediaFolder;
import yqhd.com.galleymediautil.model.LocalMediaLoader;
import yqhd.com.galleymediautil.rxbus2.RxBus;
import yqhd.com.galleymediautil.rxbus2.Subscribe;
import yqhd.com.galleymediautil.rxbus2.ThreadMode;
import yqhd.com.galleymediautil.utils.DateUtils;
import yqhd.com.galleymediautil.utils.DoubleUtils;
import yqhd.com.galleymediautil.utils.PictureFileUtils;
import yqhd.com.galleymediautil.utils.ScreenUtils;
import yqhd.com.galleymediautil.utils.StringUtils;
import yqhd.com.galleymediautil.view.CustomDialog;
import yqhd.com.galleymediautil.view.FolderWindow;
import yqhd.com.galleymediautil.view.GridSpacingItemDecoration;
import yqhd.com.galleymediautil.view.TakePhotoOrAudioPopWindow;

public class SelectResActivity extends BaseSelectActivity implements View.OnClickListener,AlbumDirectoryAdapter.OnItemClickListener,
        DiyMediaAdapter.OnPhotoSelectChangedListener,TakePhotoOrAudioPopWindow.OnTakeClickListenner{


    private ImageView iv_back;
    private TextView tv_title,tv_sure,tv_chose_count,tv_empty;
    private RelativeLayout rl_picture_title;
    private FolderWindow folderWindow;
    private TakePhotoOrAudioPopWindow takeResPopWindow;
    private RecyclerView picture_recycler;

    private Acp instance;

    private LocalMediaLoader mediaLoader;
    private List<LocalMedia> images = new ArrayList<>();
    private List<LocalMediaFolder> foldersList = new ArrayList<>();

    private DiyMediaAdapter adapter;

    private CustomDialog audioDialog;
    private TextView mTvAudioName,mTvTime,mTvAudioTotalTime;
    private SeekBar mTvAudioSeekBar;
    private ImageView mIvAudioClose,mIvAudioControl;
    private MediaPlayer mediaPlayer;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_res);
        if (!RxBus.getDefault().isRegistered(this)) {
            RxBus.getDefault().register(this);
        }
        instance = Acp.getInstance(this);

        initView(savedInstanceState);
    }

    private void initView(Bundle savedInstanceState) {
        tv_empty = (TextView)findViewById(R.id.tv_empty);
        iv_back = (ImageView) findViewById(R.id.picture_left_back);
        tv_title = (TextView) findViewById(R.id.picture_title);
        tv_sure = (TextView) findViewById(R.id.picture_tv_ok);
        tv_chose_count = (TextView) findViewById(R.id.picture_tv_img_num);
        rl_picture_title = (RelativeLayout) findViewById(R.id.head_layout);
        picture_recycler =(RecyclerView) findViewById(R.id.picture_recycler);
        tv_title.setOnClickListener(this);
        tv_sure.setOnClickListener(this);
        String title = config.mimeType == PictureMimeType.ofAudio() ?
                getString(R.string.picture_all_audio)
                : getString(R.string.picture_camera_roll);
        tv_title.setText(title);
        if (config.mimeType == PictureMimeType.ofAll()){
            takeResPopWindow = new TakePhotoOrAudioPopWindow(this);
            takeResPopWindow.setOnTakeClickListenner(this);
        }
        folderWindow = new FolderWindow(this,config.mimeType);
        folderWindow.setPictureTitleView(tv_title);
        folderWindow.setOnItemClickListener(this);
        adapter = new DiyMediaAdapter(mContext,config);
        adapter.bindSelectImages(selectionMedias);
        adapter.setOnPhotoSelectChangedListener(this);
        mediaLoader = new LocalMediaLoader(this, config.mimeType, true, 0, 0);
        AcpOptions options = new AcpOptions.Builder()
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .setDeniedMessage("读取内存权限被拒绝").build();
        instance.request(options, new AcpListener() {
            @Override
            public void onGranted() {
                readLoaclMedia();
            }

            @Override
            public void onDenied(List<String> permissions) {
                Toast.makeText(SelectResActivity.this, "读取内存权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        });

        picture_recycler.setHasFixedSize(true);
        picture_recycler.addItemDecoration(new GridSpacingItemDecoration(2,
                ScreenUtils.dip2px(this, 2), false));
        picture_recycler.setLayoutManager(new GridLayoutManager(this,2));
        picture_recycler.setAdapter(adapter);
        if (savedInstanceState != null) {
            // 防止拍照内存不足时activity被回收，导致拍照后的图片未选中
            selectionMedias = MediaSelectedHelper.obtainSelectorList(savedInstanceState);
        }
    }

    /**
     * EventBus 3.0 回调
     *
     * @param obj
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventBus(EventEntity obj) {
        switch (obj.what) {
            case ConfigKey.UPDATE_FLAG:
                // 预览时勾选图片更新回调
                List<LocalMedia> selectImages = obj.medias;
//                anim = selectImages.size() > 0 ? true : false;
                int position = obj.position;
                Log.e("刷新下标:", String.valueOf(position));
                adapter.bindSelectImages(selectImages);
                adapter.notifyItemChanged(position);
                break;
        }
    }

    /**
     * 通过CursorLoader 获取多媒体文件夹 以及文件的 二级列表
     * */
    private void readLoaclMedia() {
        mediaLoader.loadAllMedia(new LocalMediaLoader.LocalMediaLoadListener() {
            @Override
            public void loadComplete(List<LocalMediaFolder> folders) {
                if (folders.size() > 0) {
                    foldersList = folders;
                    LocalMediaFolder folder = folders.get(0);
                    folder.setChecked(true);
                    List<LocalMedia> localImg = folder.getImages();
                    // 这里解决有些机型会出现拍照完，相册列表不及时刷新问题
                    // 因为onActivityResult里手动添加拍照后的照片，
                    // 如果查询出来的图片大于或等于当前adapter集合的图片则取更新后的，否则就取本地的
                    if (localImg.size() >= images.size()) {
                        images = localImg;
                        folderWindow.bindFolder(folders);
                    }
                }
                if (adapter != null) {
                    if (images == null) {
                        images = new ArrayList<>();
                    }
                    adapter.bindData(images);
//                    tv_sure.setVisibility(images.size() > 0
//                            ? View.INVISIBLE : View.VISIBLE);
                }
            }
        });
    }


    /**
     * 部分按钮的点击事件
     * 顶部taitle
     * 顶部确定按钮
     * */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        //顶部title按钮 显示文件夹选择
        if (id == R.id.picture_title){
            if (folderWindow.isShowing()) {
                folderWindow.dismiss();
            } else {
                folderWindow.showAsDropDown(rl_picture_title);
                if (images != null && images.size() > 0) {
                    List<LocalMedia> selectedImages = adapter.getSelectImages();
                    folderWindow.notifyDataCheckedStatus(selectedImages);
                }
            }
        }
        if (id == R.id.picture_tv_ok){
            List<LocalMedia> images = adapter.getSelectImages();
            LocalMedia image = images.size()>0?images.get(0):null;
            String pictureType = image != null ? image.getPictureType() : "";
            boolean eqImg = pictureType.startsWith(ConfigKey.IMAGE);
            int size = images.size();
            if (config.minSelectNum>0 && config.minSelectNum>size){
                String str = eqImg ? (getString(R.string.picture_min_img_num, config.minSelectNum))
                        : (getString(R.string.picture_min_video_num, config.minSelectNum));
                Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
                return;
            }
            onResult(images);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (adapter != null) {
            List<LocalMedia> selectedImages = adapter.getSelectImages();
            MediaSelectedHelper.saveSelectorList(outState, selectedImages);
        }
    }

    /**
     * 文件夹popwindow的item节点点击事件
     * 依据文件夹更新多媒体文件显示列表
     * */
    @Override
    public void onItemClick(String folderName, List<LocalMedia> images) {
        boolean camera = StringUtils.isCamera(folderName);
        camera = config.isVisibleCamera ? camera : false;
        adapter.setVisibleCamera(camera);
        tv_title.setText(folderName);
        adapter.bindData(images);
        folderWindow.dismiss();
    }


    /**
     * 多媒体文件选择第一项 配置如果显示拍摄/录音按钮
     * 相应该事件
     * 四种情况：
     *  1.适配所有多媒体文件（图片，视频，音频）则点击底部出现TakePhotoOrAudioPopWindow
     *  2.拍照
     *  3.拍摄视频
     *  4.录音
     * */
    @Override
    public void onOpenCamera() {
        if (!DoubleUtils.isFastDoubleClick() || config.openCamera){
            switch (config.mimeType){
                case ConfigKey.TYPE_ALL:
                    if (takeResPopWindow !=null){
                        if (takeResPopWindow.isShowing()){
                            takeResPopWindow.dismiss();
                        }
                        takeResPopWindow.showAsDropDown(rl_picture_title);
                    }else {
                        //打开照相机 如果是不选择 直接打开摄像头 不用管是什么文件类型 直接打开拍照
                        startIntentOpenCamera();
                    }
                    break;
                case ConfigKey.TYPE_IMAGE:
                    //直接拍照
                    startIntentOpenCamera();
                    break;
                case ConfigKey.TYPE_VIDEO:
                    // 录视频
                    startIntentVideo();
                    break;
                case ConfigKey.TYPE_AUDIO:
                    // 录音
                    startIntentAudio();
                    break;
            }
        }
    }

    /**
     * 开启录音
     * 首先判断是否有录音权限
     * */
    private void startIntentAudio() {
        AcpOptions options = new AcpOptions.Builder()
                .setPermissions(Manifest.permission.RECORD_AUDIO)
                .setDeniedMessage("录音权限被拒绝").build();
        instance.request(options, new AcpListener() {
            @Override
            public void onGranted() {
                Intent cameraIntent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(cameraIntent, ConfigKey.REQUEST_CAMERA_AUDIO);
                }
            }

            @Override
            public void onDenied(List<String> permissions) {
                Toast.makeText(SelectResActivity.this, "录音存权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 开启拍摄视频
     * */
    private void startIntentVideo() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File cameraFile = PictureFileUtils.createCameraFile(this, config.mimeType ==
                            ConfigKey.TYPE_ALL ? ConfigKey.TYPE_VIDEO : config.mimeType,
                    outputCameraPath, config.suffixType);
            cameraPath = cameraFile.getAbsolutePath();
            Uri imageUri = parUri(cameraFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 0);
            cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            startActivityForResult(cameraIntent, ConfigKey.REQUEST_CAMERA_VIDEO);
        }
    }

    /**
     * 开启拍照
     * */
    private void startIntentOpenCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //使用这个Action  如果没有申明使用camera权限 则不需要 如果申明了camera 则需要对camera进行询问 否则会报错
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            int type = config.mimeType == ConfigKey.TYPE_ALL ? ConfigKey.TYPE_IMAGE : config.mimeType;
            File cameraFile = PictureFileUtils.createCameraFile(this,
                    type,
                    outputCameraPath,config.suffixType);
            cameraPath = cameraFile.getAbsolutePath();
            Uri imageUri = parUri(cameraFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(cameraIntent, ConfigKey.REQUEST_CAMERA_IMAGE);
        }
    }


    /**
     * 选择多媒体文件列表有变更触发
     * */
    @Override
    public void onChange(List<LocalMedia> selectImages) {
        changeImageNumber(selectImages);
    }


    /**
     * 多媒体文件 列表中每个Item显示的图片点击事件
     * 触发预览事件
     * */
    @Override
    public void onPictureClick(LocalMedia media, int position) {
        List<LocalMedia> images = adapter.getImages();
        startPreview(images, position);
    }

    /**
     * 预览跳转
     * 针对不同的文件进行预览
     * */
    private void startPreview(List<LocalMedia> images, int position) {
        LocalMedia media = images.get(position);
        String pictureType = media.getPictureType();
        Bundle bundle = new Bundle();
        List<LocalMedia> result = new ArrayList<>();
        int mediaType = PictureMimeType.isPictureType(pictureType);
        switch (mediaType){
            case ConfigKey.TYPE_AUDIO:
                previewAudio(media);
                break;
            case  ConfigKey.TYPE_VIDEO:
                previewVideo(media);
                break;
            case ConfigKey.TYPE_IMAGE:
                previewImage(media,position);
                break;
        }
    }

    private void previewImage(LocalMedia media,int position) {
        Intent intent = new Intent(this,PreviewPictureActivity.class);
        Bundle bundle = new Bundle();
        List<LocalMedia> selectedImages = adapter.getSelectImages();
        List<LocalMedia> images = adapter.getImages();
        bundle.putSerializable(ConfigKey.EXTRA_SELECT_MEDIA_LIST, (Serializable) selectedImages);
        bundle.putInt(ConfigKey.EXTRA_SELECT_POSITION, position);
        bundle.putSerializable(ConfigKey.EXTRA_ALL_LIST,(Serializable)images);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void previewVideo(LocalMedia media) {
        Intent intent = new Intent(this,PreviewVideoActivity.class);
        intent.putExtra("video",media);
        startActivity(intent);
    }

    /**
     * 添加音频文件预览
     * 试听
     * */
    private void previewAudio(final LocalMedia path) {
        int audioH = ScreenUtils.getScreenHeight(mContext)+ScreenUtils.getStatusBarHeight(mContext);
        audioDialog = new CustomDialog(mContext, LinearLayout.LayoutParams.MATCH_PARENT,audioH,R.layout.preview_audio_layout,R.style.Theme_dialog);
        mTvAudioName = audioDialog.findViewById(R.id.tv_music_name);
        mTvTime = audioDialog.findViewById(R.id.tv_musicTime);
        mTvAudioTotalTime = audioDialog.findViewById(R.id.tv_musicTotal);
        mTvAudioSeekBar = audioDialog.findViewById(R.id.music_seekbar);
        mIvAudioClose = audioDialog.findViewById(R.id.audio_preview_cloase);
        mIvAudioControl = audioDialog.findViewById(R.id.picture_audio_control);
        mTvAudioName.setText(path.getName());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initPlayer(path);
            }
        },30);
        mIvAudioControl.setOnClickListener(new audioOnClick(path));
        mIvAudioClose.setOnClickListener(new audioOnClick(path));
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
        });
        handler.post(runnable);
        audioDialog.show();
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


    private void changeImageNumber(List<LocalMedia> selectImages) {
        boolean enable = selectImages.size() != 0;
        if (enable) {
            tv_sure.setEnabled(true);
            tv_sure.setText(getString(R.string.picture_done_front_num,selectImages.size(),config.maxSelectNum));
        } else {
            tv_sure.setEnabled(false);
            tv_sure.setText(getString(R.string.picture_done_front_num,0,config.maxSelectNum));
        }
    }


    /**
     * 生成uri
     *
     * @param cameraFile
     * @return
     */
    private Uri parUri(File cameraFile) {
        Uri imageUri;
        String authority = getPackageName() + ".provider";
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            //通过FileProvider创建一个content类型的Uri
            imageUri = FileProvider.getUriForFile(mContext, authority, cameraFile);
        } else {
            imageUri = Uri.fromFile(cameraFile);
        }
        return imageUri;
    }



    /**
     * 在type为All的时候 点击拍摄 则会谈起TakePhotoOrAudioPopWindow
     * 以下是选择不同item的回调
     * */
    @Override
    public void onTakePicture() {
        startIntentOpenCamera();
    }

    @Override
    public void onTakeAudio() {
        startIntentAudio();
    }

    @Override
    public void onTakeVideo() {
        startIntentVideo();
    }

    /**
     * 针对拍照/录屏/录音/预览界面返回当前页面进行结果处理
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){

            switch (requestCode){
                case ConfigKey.REQUEST_CAMERA_AUDIO:
                    cameraPath = getAudioPath(data, ConfigKey.REQUEST_CAMERA_AUDIO);
//                    duration = PictureMimeType.getLocalVideoDuration(cameraPath);
                    onCameraBack(cameraPath,ConfigKey.REQUEST_CAMERA_AUDIO);
                    break;
                case ConfigKey.REQUEST_CAMERA_IMAGE:
                    onCameraBack(cameraPath,ConfigKey.REQUEST_CAMERA_IMAGE);
                    break;
                case ConfigKey.REQUEST_CAMERA_VIDEO:
                    onCameraBack(cameraPath,ConfigKey.REQUEST_CAMERA_VIDEO);
                    break;
            }
        }else if (resultCode == RESULT_CANCELED) {
            if (config.openCamera) {
                closeActivity();
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable throwable = (Throwable) data.getSerializableExtra(UCrop.EXTRA_ERROR);
            Toast.makeText(mContext, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 录屏 录音 拍照 确定有结果返回 更新界面
     * */
    private void onCameraBack(String cameraPath, int requestCode) {
        List<LocalMedia> medias = new ArrayList<>();
        LocalMedia media;
        String imageType;
        String pictureType = "";
        int duration = 0;
        File file = new File(cameraPath);
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        String toType = PictureMimeType.fileToType(file);
        if (requestCode ==ConfigKey.REQUEST_CAMERA_IMAGE) {
            int degree = PictureFileUtils.readPictureDegree(file.getAbsolutePath());
            rotateImage(degree, file);
        }
        // 生成新拍照片或视频对象
        media = new LocalMedia();
        media.setPath(cameraPath);
        boolean eqVideo = toType.startsWith(ConfigKey.VIDEO);
        if (requestCode == ConfigKey.REQUEST_CAMERA_AUDIO && toType.startsWith(ConfigKey.AUDIO)){
            pictureType = PictureMimeType.createAudioType(cameraPath);
            duration = PictureMimeType.getLocalVideoDuration(cameraPath);
        }
        if (requestCode == ConfigKey.REQUEST_CAMERA_VIDEO && toType.startsWith(ConfigKey.VIDEO)){
            duration = PictureMimeType.getLocalVideoDuration(cameraPath);
            pictureType = PictureMimeType.createVideoType(cameraPath);
        }
        if (requestCode == ConfigKey.REQUEST_CAMERA_IMAGE && toType.startsWith(ConfigKey.IMAGE)){
            pictureType =  PictureMimeType.createImageType(cameraPath);
        }
        media.setPictureType(pictureType);
        media.setDuration(duration);
        media.setMimeType(config.mimeType);
        if (config.maxSelectNum==1){
            //单选模式下 拍照直接返回
            medias.add(media);
            onResult(medias);
        }
        if (config.openCamera) {
            Log.e("openCamera","直接拍照返回");
            // 如果是拍照后直接返回
            boolean eqImg = toType.startsWith(ConfigKey.IMAGE);
//                        if (config.enableCrop && eqImg) {
//                            // 去裁剪
//                            originalPath = cameraPath;
//                            startCrop(cameraPath);
//                        } else if (config.isCompress && eqImg) {
//                            // 去压缩
//                            medias.add(media);
//                            compressImage(medias);
//                            if (adapter != null) {
//                                images.add(0, media);
//                                adapter.notifyDataSetChanged();
//                            }
//                        } else {
            // 不裁剪 不压缩 直接返回结果
            medias.add(media);
            onResult(medias);
//                        }
        } else {
            // 多选 返回列表并选中当前拍照的
            images.add(0, media);
            if (adapter != null) {
                List<LocalMedia> selectedImages = adapter.getSelectImages();
                // 没有到最大选择量 才做默认选中刚拍好的
                if (selectedImages.size() < config.maxSelectNum ) {

                    pictureType = selectedImages.size() > 0 ? selectedImages.get(0).getPictureType() : "";
                    boolean toEqual = PictureMimeType.mimeToEqual(pictureType, media.getPictureType());
                    // 类型相同或还没有选中才加进选中集合中

                    if (toEqual || selectedImages.size() == 0) {
                        if (selectedImages.size() < config.maxSelectNum) {

                            // 如果是单选，则清空已选中的并刷新列表(作单一选择)
                            if (config.maxSelectNum == 1) {
                                singleRadioMediaImage();

                            }
                            selectedImages.add(media);
                            adapter.bindSelectImages(selectedImages);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        }
        if (adapter != null) {
            // 解决部分手机拍照完Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
            // 不及时刷新问题手动添加
            manualSaveFolder(media);
            tv_empty.setVisibility(images.size() > 0
                    ? View.INVISIBLE : View.VISIBLE);
        }

        if (requestCode != ConfigKey.REQUEST_CAMERA_AUDIO) {
            int lastImageId = getLastImageId(eqVideo);
            if (lastImageId != -1) {
                removeImage(lastImageId, eqVideo);
            }
        }

    }


    /**
     * 将图片插入到相机文件夹中
     *
     * @param path
     * @param imageFolders
     * @return
     */
    protected LocalMediaFolder getImageFolder(String path, List<LocalMediaFolder> imageFolders) {
        File imageFile = new File(path);
        File folderFile = imageFile.getParentFile();

        for (LocalMediaFolder folder : imageFolders) {
            if (folder.getName().equals(folderFile.getName())) {
                return folder;
            }
        }
        LocalMediaFolder newFolder = new LocalMediaFolder();
        newFolder.setName(folderFile.getName());
        newFolder.setPath(folderFile.getAbsolutePath());
        newFolder.setFirstImagePath(path);
        imageFolders.add(newFolder);
        return newFolder;
    }

    /**
     * Close Activity
     */
    protected void closeActivity() {
        finish();
        if (config.openCamera) {
            overridePendingTransition(0, R.anim.fade_out);
        } else {
            overridePendingTransition(0, R.anim.a3);
        }
    }

    /**
     * 获取DCIM文件下最新一条拍照记录
     *
     * @return
     */
    protected int getLastImageId(boolean eqVideo) {
        try {
            //selection: 指定查询条件
            String absolutePath = PictureFileUtils.getDCIMCameraPath();
            String ORDER_BY = MediaStore.Files.FileColumns._ID + " DESC";
            String selection = eqVideo ? MediaStore.Video.Media.DATA + " like ?" :
                    MediaStore.Images.Media.DATA + " like ?";
            //定义selectionArgs：
            String[] selectionArgs = {absolutePath + "%"};
            Cursor imageCursor = this.getContentResolver().query(eqVideo ?
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            : MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                    selection, selectionArgs, ORDER_BY);
            if (imageCursor.moveToFirst()) {
                int id = imageCursor.getInt(eqVideo ?
                        imageCursor.getColumnIndex(MediaStore.Video.Media._ID)
                        : imageCursor.getColumnIndex(MediaStore.Images.Media._ID));
                long date = imageCursor.getLong(eqVideo ?
                        imageCursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                        : imageCursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                int duration = DateUtils.dateDiffer(date);
                imageCursor.close();
                // DCIM文件下最近时间30s以内的图片，可以判定是最新生成的重复照片
                return duration <= 30 ? id : -1;
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    /**
     * 删除部分手机 拍照在DCIM也生成一张的问题
     *
     * @param id
     * @param eqVideo
     */
    protected void removeImage(int id, boolean eqVideo) {
        try {
            ContentResolver cr = getContentResolver();
            Uri uri = eqVideo ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String selection = eqVideo ? MediaStore.Video.Media._ID + "=?"
                    : MediaStore.Images.Media._ID + "=?";
            cr.delete(uri,
                    selection,
                    new String[]{Long.toString(id)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 手动添加拍照后的相片到图片列表，并设为选中
     *
     * @param media
     */
    private void manualSaveFolder(LocalMedia media) {
        try {
            createNewFolder(foldersList);
            LocalMediaFolder folder = getImageFolder(media.getPath(), foldersList);
            LocalMediaFolder cameraFolder = foldersList.size() > 0 ? foldersList.get(0) : null;
            if (cameraFolder != null && folder != null) {
                // 相机胶卷
                cameraFolder.setFirstImagePath(media.getPath());
                cameraFolder.setImages(images);
                cameraFolder.setImageNum(cameraFolder.getImageNum() + 1);
                // 拍照相册
                int num = folder.getImageNum() + 1;
                folder.setImageNum(num);
                folder.getImages().add(0, media);
                folder.setFirstImagePath(cameraPath);
                folderWindow.bindFolder(foldersList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 如果没有任何相册，先创建一个最近相册出来
     *
     * @param folders
     */
    protected void createNewFolder(List<LocalMediaFolder> folders) {
        if (folders.size() == 0) {
            // 没有相册 先创建一个最近相册出来
            LocalMediaFolder newFolder = new LocalMediaFolder();
            String folderName = config.mimeType == PictureMimeType.ofAudio() ?
                    getString(R.string.picture_all_audio) : getString(R.string.picture_camera_roll);
            newFolder.setName(folderName);
            newFolder.setPath("");
            newFolder.setFirstImagePath("");
            folders.add(newFolder);
        }
    }


    /**
     * 单选图片
     */
    private void singleRadioMediaImage() {
        if (adapter != null) {
            List<LocalMedia> selectImages = adapter.getSelectImages();
            if (selectImages != null
                    && selectImages.size() > 0) {
                selectImages.clear();
            }
        }
    }

    /**
     * 录音
     *
     * @param data
     */
    protected String getAudioPath(Intent data, int requestCode) {
        boolean compare_SDK_19 = Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT;
        if (data != null && requestCode == ConfigKey.REQUEST_CAMERA_AUDIO) {
            try {
                Uri uri = data.getData();
                final String audioPath;
                if (compare_SDK_19) {
                    audioPath = uri.getPath();
                } else {
                    audioPath = getAudioFilePathFromUri(uri);
                }
                return audioPath;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 获取刚录取的音频文件
     *
     * @param uri
     * @return
     */
    protected String getAudioFilePathFromUri(Uri uri) {
        String path = "";
        try {
            Cursor cursor = getContentResolver()
                    .query(uri, null, null, null, null);
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
            path = cursor.getString(index);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }


    /**
     * 判断拍照 图片是否旋转
     *
     * @param degree
     * @param file
     */
    protected void rotateImage(int degree, File file) {
        if (degree > 0) {
            // 针对相片有旋转问题的处理方式
            try {
                BitmapFactory.Options opts = new BitmapFactory.Options();//获取缩略图显示到屏幕上
                opts.inSampleSize = 2;
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
                Bitmap bmp = PictureFileUtils.rotaingImageView(degree, bitmap);
                PictureFileUtils.saveBitmapFile(bmp, file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (RxBus.getDefault().isRegistered(this)) {
            RxBus.getDefault().unregister(this);
        }
        if (mediaPlayer != null && handler != null) {
            handler.removeCallbacks(runnable);
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
