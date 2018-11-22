package yqhd.com.galleymediautil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import yqhd.com.galleymediautil.callback.SelectPicCallback;
import yqhd.com.galleymediautil.config.ConfigKey;
import yqhd.com.galleymediautil.config.SelectionConfig;
import yqhd.com.galleymediautil.entity.LocalMedia;
import yqhd.com.galleymediautil.ui.SelectResActivity;


/**
 * Created by xiongqiwe on 2018/10/9.
 * 多媒体文件选择帮帮助类
 */

public class MediaSelectedHelper {

    /**
     * 从图库选择多张照片
     *
     * @param context           上下文环境对象
     *
     * @param selectPicCallback 照片选择后的回调
     */
    public static void selectMutiplePicByDiy(final Activity context, final SelectionConfig selectionConfig,
                                             final SelectPicCallback selectPicCallback) {
        requestPermission(context, new Callback() {
            @Override
            public void onSuccess(Object o) {

                SelectCallbackManager.getInstance().setSelectPicCallback(selectPicCallback);
                SelectionConfig config = selectionConfig;
                Intent intent = new Intent(context, SelectResActivity.class);
                intent.putExtra("config",config);
                context.startActivity(intent);

            }

            @Override
            public void onFailed(String message) {
//                if (selectPicCallback != null) {
//                    selectPicCallback.selectPic(null);
//                }
            }
        });
    }
                                                   
    private static void requestPermission(Context context, final Callback callback) {
        AcpOptions options = new AcpOptions.Builder()
                .setPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.CAMERA)
                .setDeniedMessage("选择图片权限被拒绝").build();
        Acp.getInstance(context)
                .request(options, new AcpListener() {
                    @Override
                    public void onGranted() {
                        if (callback != null) {
                            callback.onSuccess(null);
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                        if (callback != null) {
                            callback.onFailed(permissions == null ? "" : permissions.toString());
                        }
                    }
                });
    }


    /**
     * @param bundle
     * @return get Selector  LocalMedia
     */
    public static List<LocalMedia> obtainSelectorList(Bundle bundle) {
        List<LocalMedia> selectionMedias;
        if (bundle != null) {
            selectionMedias = (List<LocalMedia>) bundle
                    .getSerializable(ConfigKey.EXTRA_SELECT_LIST);
            return selectionMedias;
        }
        selectionMedias = new ArrayList<>();
        return selectionMedias;
    }

    /**
     * @param selectedImages
     * @return put Selector  LocalMedia
     */
    public static void saveSelectorList(Bundle outState, List<LocalMedia> selectedImages) {
        outState.putSerializable(ConfigKey.EXTRA_SELECT_LIST, (Serializable) selectedImages);
    }
}
