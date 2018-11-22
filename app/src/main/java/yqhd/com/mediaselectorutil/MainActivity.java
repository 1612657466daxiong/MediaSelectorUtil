package yqhd.com.mediaselectorutil;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import yqhd.com.galleymediautil.MediaSelectedHelper;
import yqhd.com.galleymediautil.callback.SelectPicCallback;
import yqhd.com.galleymediautil.config.PictureMimeType;
import yqhd.com.galleymediautil.config.SelectionConfig;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.iv_image);
    }

    public void onTakePhoto(View view) {
        SelectionConfig config = SelectionConfig.getDefaultInstance();
        config.mimeType = PictureMimeType.ofImage();
        config.selectionMedias = new ArrayList<>();
        config.isVisibleCamera = true;
        config.openCamera = false;
        config.maxSelectNum = 1;
        config.minSelectNum = 1;
        config.enablePreview = true;
        MediaSelectedHelper.selectMutiplePicByDiy(this, config, new SelectPicCallback() {
            @Override
            public void selectPic(List<String> images) {
                Toast.makeText(MainActivity.this, "启动成功", Toast.LENGTH_SHORT).show();
                showImage(images.get(0));
            }
        });
    }

    private void showImage(String s) {
//        Glide.with(getApplication())
//                .load(new File(s))
//                .into(imageView);
        imageView.setImageBitmap(BitmapFactory.decodeFile(s));
    }

    public void onTakeAll(View view) {
        SelectionConfig config = SelectionConfig.getDefaultInstance();
        config.mimeType = PictureMimeType.ofAll();
        config.selectionMedias = new ArrayList<>();
        config.isVisibleCamera = true;
        config.openCamera = false;
        config.maxSelectNum = 1;
        config.minSelectNum = 1;
        config.enablePreview = true;
        MediaSelectedHelper.selectMutiplePicByDiy(this, config, new SelectPicCallback() {
            @Override
            public void selectPic(List<String> images) {
                Toast.makeText(MainActivity.this, "启动成功", Toast.LENGTH_SHORT).show();
                showImage(images.get(0));
            }
        });
    }

    public void onTakeAudio(View view) {
        SelectionConfig config = SelectionConfig.getDefaultInstance();
        config.mimeType = PictureMimeType.ofAudio();
        config.selectionMedias = new ArrayList<>();
        config.isVisibleCamera = true;
        config.openCamera = false;
        config.maxSelectNum = 1;
        config.minSelectNum = 1;
        config.enablePreview = true;
        MediaSelectedHelper.selectMutiplePicByDiy(this, config, new SelectPicCallback() {
            @Override
            public void selectPic(List<String> images) {
                Toast.makeText(MainActivity.this, "启动成功", Toast.LENGTH_SHORT).show();
                showImage(images.get(0));
            }
        });
    }

    public void onTakeVideo(View view) {
        SelectionConfig config = SelectionConfig.getDefaultInstance();
        config.mimeType = PictureMimeType.ofVideo();
        config.selectionMedias = new ArrayList<>();
        config.isVisibleCamera = true;
        config.openCamera = false;
        config.maxSelectNum = 1;
        config.minSelectNum = 1;
        config.enablePreview = true;
        MediaSelectedHelper.selectMutiplePicByDiy(this, config, new SelectPicCallback() {
            @Override
            public void selectPic(List<String> images) {
                Toast.makeText(MainActivity.this, "启动成功", Toast.LENGTH_SHORT).show();
                showImage(images.get(0));
            }
        });
    }

    public void onCreama(View view) {
        SelectionConfig config = SelectionConfig.getDefaultInstance();
        config.mimeType = PictureMimeType.ofAudio();
        config.selectionMedias = new ArrayList<>();
        config.isVisibleCamera = true;
        config.openCamera = true;
        config.maxSelectNum = 1;
        config.minSelectNum = 1;
        config.enablePreview = true;
        MediaSelectedHelper.selectMutiplePicByDiy(this, config, new SelectPicCallback() {
            @Override
            public void selectPic(List<String> images) {
                Toast.makeText(MainActivity.this, "启动成功", Toast.LENGTH_SHORT).show();
                showImage(images.get(0));
            }
        });
    }
}
