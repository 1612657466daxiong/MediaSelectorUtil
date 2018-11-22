package yqhd.com.galleymediautil.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;


import java.util.ArrayList;
import java.util.List;

import yqhd.com.galleymediautil.R;
import yqhd.com.galleymediautil.SelectCallbackManager;
import yqhd.com.galleymediautil.config.ConfigKey;
import yqhd.com.galleymediautil.config.SelectionConfig;
import yqhd.com.galleymediautil.entity.LocalMedia;
import yqhd.com.galleymediautil.immersive.ImmersiveManage;
import yqhd.com.galleymediautil.utils.AttrsUtils;

/**
 * Created by chenpengxiang on 2018/9/17
 */
public class BaseSelectActivity extends FragmentActivity {
    public static final String RESULT_LIST = "ResultList";
    protected Context mContext;
    protected SelectionConfig config;
    protected int colorPrimary, colorPrimaryDark;
    protected boolean openWhiteStatusBar;
    protected List<LocalMedia> selectionMedias;

    protected String cameraPath, outputCameraPath;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mContext = this;
        if (savedInstanceState!=null){
            config =savedInstanceState.getParcelable(ConfigKey.EXTRA_SELECT_CONFIG);
        }else {
            config = SelectionConfig.getInstance();
            if (config==null){
                initSelection();
            }
        }
        super.onCreate(savedInstanceState);
        initConfig();
        if (isImmersive()){
            ImmersiveManage.immersiveAboveAPI23(this,colorPrimaryDark
                    , colorPrimary,openWhiteStatusBar);
        }
    }

    private void initSelection() {
        SelectionConfig config = getIntent().getParcelableExtra("config");
        if (config!=null)
            this.config = config;
        else
            this.config = SelectionConfig.getDefaultInstance();
    }

    private void initConfig() {
        colorPrimary = AttrsUtils.getTypeValueColor(this, R.attr.colorPrimary);
        // 状态栏背景色
        colorPrimaryDark = AttrsUtils.getTypeValueColor(this, R.attr.colorPrimaryDark);
        openWhiteStatusBar = AttrsUtils.getTypeValueBoolean
                (this, R.attr.picture_statusFontColor);
        selectionMedias = config.selectionMedias;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ConfigKey.EXTRA_SELECT_CONFIG,config);
    }

    /**
     * 是否使用沉浸式，子类复写该方法来确定是否采用沉浸式
     *
     * @return 是否沉浸式，默认true
     */
    @Override
    public boolean isImmersive() {
        return true;
    }

    /**
     * @param images 返回的多媒体文件列表
     * 直接返回多媒体列表
     * */
    protected void onResult(List<LocalMedia> images) {
        ArrayList<String> strings = new ArrayList<>();
        if (images!=null){
            for (LocalMedia media:images) {
                strings.add(media.getPath());
            }
        }
        Intent intent = new Intent();
        intent.putStringArrayListExtra(RESULT_LIST, (ArrayList) images);
        setResult(RESULT_OK, intent);
        finish();
        if(SelectCallbackManager.getInstance().getSelectPicCallback() != null){
            SelectCallbackManager.getInstance().getSelectPicCallback().selectPic(strings);
            SelectCallbackManager.getInstance().clearCallback();
        }
    }
}
