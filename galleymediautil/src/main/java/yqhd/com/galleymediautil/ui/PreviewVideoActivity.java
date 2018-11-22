package yqhd.com.galleymediautil.ui;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;



import yqhd.com.galleymediautil.R;
import yqhd.com.galleymediautil.entity.LocalMedia;

public class PreviewVideoActivity extends AppCompatActivity implements MediaPlayer.OnErrorListener,MediaPlayer.OnPreparedListener,MediaPlayer.OnCompletionListener, View.OnClickListener{
    LocalMedia media = null;
    private ImageView iv_back,iv_play;
    private VideoView mVideoView;
    private MediaController mMediaController;
    private TextView mTvTitle;
    private int mPositionWhenPaused = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_video);
        media =  getIntent().getParcelableExtra("video");
        initView();
    }

    private void initView() {
        iv_back = (ImageView) findViewById(R.id.picture_left_back);
        iv_play = (ImageView) findViewById(R.id.iv_play);
        mVideoView = (VideoView) findViewById(R.id.video_view);
        mMediaController = new MediaController(this);
        mTvTitle = (TextView) findViewById(R.id.tv_media_name);
        mVideoView.setMediaController(mMediaController);
        iv_play.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        if (media!=null){
            mTvTitle.setText(media.getName());
        }
    }

    @Override
    public void onStart() {
        mVideoView.setVideoPath(media.getPath());
        mVideoView.start();
        super.onStart();
    }


    @Override
    public void onPause() {
        mPositionWhenPaused = mVideoView.getCurrentPosition();
        mVideoView.stopPlayback();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMediaController = null;
        mVideoView = null;
        iv_play = null;
        super.onDestroy();
    }

    @Override
    public void onResume() {
        // Resume video player
        if (mPositionWhenPaused >= 0) {
            mVideoView.seekTo(mPositionWhenPaused);
            mPositionWhenPaused = -1;
        }
        super.onResume();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (null != iv_play) {
            iv_play.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new ContextWrapper(newBase) {
            @Override
            public Object getSystemService(String name) {
                if (Context.AUDIO_SERVICE.equals(name)) {
                    return getApplicationContext().getSystemService(name);
                }
                return super.getSystemService(name);
            }
        });
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    // video started
                    mVideoView.setBackgroundColor(Color.TRANSPARENT);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.picture_left_back) {
            finish();
        } else if (id == R.id.iv_play) {
            mVideoView.start();
            iv_play.setVisibility(View.INVISIBLE);
        }
    }
}
