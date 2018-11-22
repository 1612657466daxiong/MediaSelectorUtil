package yqhd.com.galleymediautil.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;


import java.util.List;

import yqhd.com.galleymediautil.R;
import yqhd.com.galleymediautil.adapter.AlbumDirectoryAdapter;
import yqhd.com.galleymediautil.entity.LocalMedia;
import yqhd.com.galleymediautil.entity.LocalMediaFolder;
import yqhd.com.galleymediautil.utils.ScreenUtils;
import yqhd.com.galleymediautil.utils.StringUtils;

/**
 * Created by chenpengxiang on 2018/9/13
 */
public class FolderWindow extends PopupWindow implements View.OnClickListener {
    private Context context;
    private View window;
    private RecyclerView recyclerView;
    private AlbumDirectoryAdapter adapter;
    private Animation animationIn,animationOut;
    private boolean isDismiss =false;
    private LinearLayout id_ll_root;
    private TextView picture_title;
    private Drawable drawableUp, drawableDown;
    private int mimeType;

    public FolderWindow(Context context, int mimeType) {
        this.context = context;
        this.mimeType = mimeType;
        window = LayoutInflater.from(context).inflate(R.layout.picture_window_folder,null);
        this.setContentView(window);
        this.setWidth(ScreenUtils.getScreenWidth(context));
        this.setHeight(ScreenUtils.getScreenWidth(context));
        this.setAnimationStyle(R.style.WindowStyle);
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.update();
        this.setBackgroundDrawable(new ColorDrawable(Color.argb(123, 0, 0, 0)));
//        drawableUp = AttrsUtils.getTypeValuePopWindowImg(context, R.attr.picture_arrow_up_icon);
//        drawableDown = AttrsUtils.getTypeValuePopWindowImg(context, R.attr.picture_arrow_down_icon);
        drawableDown = context.getResources().getDrawable(R.drawable.orange_arrow_down);
        drawableUp =context.getResources().getDrawable(R.drawable.orange_arrow_up);
        animationIn = AnimationUtils.loadAnimation(context, R.anim.photo_album_show);
        animationOut = AnimationUtils.loadAnimation(context, R.anim.photo_album_dismiss);
        initView();
    }

    private void initView() {
        id_ll_root = (LinearLayout) window.findViewById(R.id.id_ll_root);
        adapter = new AlbumDirectoryAdapter(context);
        recyclerView = (RecyclerView) window.findViewById(R.id.folder_list);
        recyclerView.getLayoutParams().height = (int) (ScreenUtils.getScreenHeight(context) * 0.6);
        recyclerView.addItemDecoration(new RecycleViewDivider(
                context, LinearLayoutManager.HORIZONTAL, ScreenUtils.dip2px(context, 0), ContextCompat.getColor(context, R.color.transparent)));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
        id_ll_root.setOnClickListener(this);
    }


    public void bindFolder(List<LocalMediaFolder> folders) {
        adapter.setMimeType(mimeType);
        adapter.bindFolderData(folders);
    }

    public void setPictureTitleView(TextView picture_title) {
        this.picture_title = picture_title;
    }



    @Override
    public void showAsDropDown(View anchor) {
        //这里编译版本如果低于24 不知道会不会出问题
        try {
            if (Build.VERSION.SDK_INT>=24){
                Rect rect = new Rect();
                anchor.getGlobalVisibleRect(rect);
                int h = anchor.getResources().getDisplayMetrics().heightPixels - rect.bottom;
                setHeight(h);
            }else {
                Rect rect = new Rect();
                anchor.getGlobalVisibleRect(rect);
                int h = anchor.getResources().getDisplayMetrics().heightPixels - rect.bottom;
                setHeight(h);
            }
            super.showAsDropDown(anchor);

            isDismiss = false;
            recyclerView.startAnimation(animationIn);
            StringUtils.modifyTextViewDrawable(picture_title, drawableUp, 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOnItemClickListener(AlbumDirectoryAdapter.OnItemClickListener onItemClickListener) {
        adapter.setOnItemClickListener(onItemClickListener);
    }

    @Override
    public void dismiss() {
        if (isDismiss) {
            return;
        }
        StringUtils.modifyTextViewDrawable(picture_title, drawableDown, 2);
        isDismiss = true;
        recyclerView.startAnimation(animationOut);
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
                    FolderWindow.super.dismiss();
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
                FolderWindow.super.dismiss();
            }
        });
    }


    /**
     * 设置选中状态
     */
    public void notifyDataCheckedStatus(List<LocalMedia> medias) {
        try {
            // 获取选中图片
            List<LocalMediaFolder> folders = adapter.getFolders();
            for (LocalMediaFolder folder : folders) {
                folder.setCheckedNum(0);
            }
            if (medias.size() > 0) {
                for (LocalMediaFolder folder : folders) {
                    int num = 0;// 记录当前相册下有多少张是选中的
                    List<LocalMedia> images = folder.getImages();
                    for (LocalMedia media : images) {
                        String path = media.getPath();
                        for (LocalMedia m : medias) {
                            if (path.equals(m.getPath())) {
                                num++;
                                folder.setCheckedNum(num);
                            }
                        }
                    }
                }
            }
            adapter.bindFolderData(folders);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.id_ll_root) {
            dismiss();
        }
    }


}
