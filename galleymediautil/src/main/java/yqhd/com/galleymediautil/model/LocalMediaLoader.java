package yqhd.com.galleymediautil.model;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import yqhd.com.galleymediautil.R;
import yqhd.com.galleymediautil.config.ConfigKey;
import yqhd.com.galleymediautil.config.PictureMimeType;
import yqhd.com.galleymediautil.entity.LocalMedia;
import yqhd.com.galleymediautil.entity.LocalMediaFolder;


/**
 * author：luck
 * project：LocalMediaLoader
 * package：com.luck.picture.ui
 * email：893855882@qq.com
 * data：16/12/31
 */

public class LocalMediaLoader {
    private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");
    private static final String ORDER_BY = MediaStore.Files.FileColumns._ID + " DESC";
    private static final String DURATION = "duration";
    private static final String NOT_GIF = "!='image/gif'";
    private static final int AUDIO_DURATION = 500;// 过滤掉小于500毫秒的录音
    private int type = ConfigKey.TYPE_IMAGE;
//    private int type;
    private FragmentActivity activity;
    private boolean isGif;
    private long videoMaxS = 0;
    private long videoMinS = 0;

    // 媒体文件数据库字段
    private static final String[] PROJECTION = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            DURATION,
            MediaStore.MediaColumns.DISPLAY_NAME};

    // 图片
    private static final String SELECTION = MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
            + " AND " + MediaStore.MediaColumns.SIZE + ">0";

    private static final String SELECTION_NOT_GIF = MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
            + " AND " + MediaStore.MediaColumns.SIZE + ">0"
            + " AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF;

    // 查询条件(音视频)
    private static String getSelectionArgsForSingleMediaCondition(String time_condition) {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0"
                + " AND " + time_condition;
    }

    // 全部模式下条件
    private static String getSelectionArgsForAllMediaCondition(String time_condition, String time_audio, boolean isGif) {
        String condition = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + (isGif ? "" : " AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF)
                + " OR "
                + (MediaStore.Files.FileColumns.MEDIA_TYPE + "=? AND " + time_condition)
                + " OR "
                + (MediaStore.Files.FileColumns.MEDIA_TYPE + "=? AND "+ time_audio)
                + ")"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0";

        return condition;
    }

    // 获取图片or视频
    private static final String[] SELECTION_ALL_ARGS = {
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO),
    };

    /**
     * 获取指定类型的文件
     *
     * @param mediaType
     * @return
     */
    private static String[] getSelectionArgsForSingleMediaType(int mediaType) {
        return new String[]{String.valueOf(mediaType)};
    }

    public LocalMediaLoader(FragmentActivity activity, int type, boolean isGif, long videoMaxS, long videoMinS) {
        this.activity = activity;
        this.type = type;
        this.isGif = isGif;
        this.videoMaxS = videoMaxS;
        this.videoMinS = videoMinS;
    }

    public void loadAllMedia(final LocalMediaLoadListener imageLoadListener) {
        activity.getSupportLoaderManager().initLoader(type, null,
                new LoaderManager.LoaderCallbacks<Cursor>() {
                    @Override
                    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                        CursorLoader cursorLoader = null;
                        switch (id) {
                            case ConfigKey.TYPE_ALL:

                                String all_condition = getSelectionArgsForAllMediaCondition(getDurationCondition(0, 0),
                                        getDurationCondition(0,0), isGif);
                                cursorLoader = new CursorLoader(
                                        activity, QUERY_URI,
                                        PROJECTION, all_condition,
                                        SELECTION_ALL_ARGS, ORDER_BY);
                                break;
                            case ConfigKey.TYPE_IMAGE:

                                String[] MEDIA_TYPE_IMAGE = getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
                                cursorLoader = new CursorLoader(
                                        activity, QUERY_URI,
                                        PROJECTION, isGif ? SELECTION : SELECTION_NOT_GIF, MEDIA_TYPE_IMAGE
                                        , ORDER_BY);
                                break;
                            case ConfigKey.TYPE_VIDEO:

                                String video_condition = getSelectionArgsForSingleMediaCondition(getDurationCondition(0, 0));
                                String[] MEDIA_TYPE_VIDEO = getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO);
                                cursorLoader = new CursorLoader(
                                        activity, QUERY_URI, PROJECTION, video_condition, MEDIA_TYPE_VIDEO
                                        , ORDER_BY);
                                break;
                            case ConfigKey.TYPE_AUDIO:

                                String audio_condition = getSelectionArgsForSingleMediaCondition(getDurationCondition(0, AUDIO_DURATION));
                                String[] MEDIA_TYPE_AUDIO = getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO);
                                cursorLoader = new CursorLoader(
                                        activity, QUERY_URI, PROJECTION, audio_condition, MEDIA_TYPE_AUDIO
                                        , ORDER_BY);
                                break;
                        }
                        return cursorLoader;
                    }

                    @Override
                    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                        try {
                            List<LocalMediaFolder> imageFolders = new ArrayList<>();
                            LocalMediaFolder allImageFolder = new LocalMediaFolder();
                            List<LocalMedia> latelyImages = new ArrayList<>();
                            if (data != null) {
                                int count = data.getCount();
                                if (count > 0) {
                                    data.moveToFirst();
                                    do {
                                        String path = data.getString
                                                (data.getColumnIndexOrThrow(PROJECTION[1]));

                                        String pictureType = data.getString
                                                (data.getColumnIndexOrThrow(PROJECTION[2]));

                                        int w = data.getInt
                                                (data.getColumnIndexOrThrow(PROJECTION[3]));

                                        int h = data.getInt
                                                (data.getColumnIndexOrThrow(PROJECTION[4]));

                                        int duration = data.getInt
                                                (data.getColumnIndexOrThrow(PROJECTION[5]));
                                        String name = data.getString
                                                (data.getColumnIndexOrThrow(PROJECTION[6]));

                                        LocalMedia image = new LocalMedia
                                                (path, duration, type, pictureType, w, h,name);

                                        LocalMediaFolder folder = getImageFolder(path, imageFolders);
                                        List<LocalMedia> images = folder.getImages();
                                        images.add(image);
                                        folder.setImageNum(folder.getImageNum() + 1);
                                        latelyImages.add(image);
                                        int imageNum = allImageFolder.getImageNum();
                                        allImageFolder.setImageNum(imageNum + 1);
                                    } while (data.moveToNext());

                                    if (latelyImages.size() > 0) {

                                        sortFolder(imageFolders);
                                        imageFolders.add(0, allImageFolder);
                                        allImageFolder.setFirstImagePath
                                                (latelyImages.get(0).getPath());
                                        String title = type == PictureMimeType.ofAudio() ?
                                                activity.getString(R.string.picture_all_audio)
                                                : activity.getString(R.string.picture_camera_roll);
                                        allImageFolder.setName(title);
                                        allImageFolder.setImages(latelyImages);
                                    }
                                    imageLoadListener.loadComplete(imageFolders);
                                } else {
                                    // 如果没有相册
                                    imageLoadListener.loadComplete(imageFolders);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onLoaderReset(Loader<Cursor> loader) {
                    }
                });
    }

    /**
     * 文件夹数量进行排序
     *
     * @param imageFolders
     */
    private void sortFolder(List<LocalMediaFolder> imageFolders) {
        // 文件夹按图片数量排序
        Collections.sort(imageFolders, new Comparator<LocalMediaFolder>() {
            @Override
            public int compare(LocalMediaFolder lhs, LocalMediaFolder rhs) {
                if (lhs.getImages() == null || rhs.getImages() == null) {
                    return 0;
                }
                int lsize = lhs.getImageNum();
                int rsize = rhs.getImageNum();
                return lsize == rsize ? 0 : (lsize < rsize ? 1 : -1);
            }
        });
    }

    /**
     * 创建相应文件夹
     *
     * @param path
     * @param imageFolders
     * @return
     */
    private LocalMediaFolder getImageFolder(String path, List<LocalMediaFolder> imageFolders) {
        File imageFile = new File(path);
        File folderFile = imageFile.getParentFile();
        for (LocalMediaFolder folder : imageFolders) {
            // 同一个文件夹下，返回自己，否则创建新文件夹
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
     * 获取视频(最长或最小时间)
     *
     * @param exMaxLimit
     * @param exMinLimit
     * @return
     */
    private String getDurationCondition(long exMaxLimit, long exMinLimit) {
        long maxS = videoMaxS == 0 ? Long.MAX_VALUE : videoMaxS;
        if (exMaxLimit != 0) maxS = Math.min(maxS, exMaxLimit);

        return String.format(Locale.CHINA, "%d <%s duration and duration <= %d",
                Math.max(exMinLimit, videoMinS),
                Math.max(exMinLimit, videoMinS) == 0 ? "" : "=",
                maxS);
    }


    public interface LocalMediaLoadListener {
        void loadComplete(List<LocalMediaFolder> folders);
    }
}
