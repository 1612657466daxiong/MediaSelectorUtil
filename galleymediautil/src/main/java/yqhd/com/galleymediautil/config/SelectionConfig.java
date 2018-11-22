package yqhd.com.galleymediautil.config;

import android.os.Parcel;
import android.os.Parcelable;



import java.util.ArrayList;
import java.util.List;

import yqhd.com.galleymediautil.entity.LocalMedia;
import yqhd.com.galleymediautil.utils.PictureFileUtils;

/**
 * Created by chenpengxiang on 2018/9/17
 */
public final class SelectionConfig implements Parcelable {
    public int mimeType;//选择文件类别
    public boolean openCamera;//直接打开录音 录屏 照相机 不通过选择文件
    public boolean isVisibleCamera;//是否显拍照选项
    public int maxSelectNum;//最大选择数 这里不设置单选多选 直接以多选 设置1或者N
    public int minSelectNum;//最小选择数 >=1 默认
    public String suffixType;//文件后缀 用于拍摄 录音后的文件
    public boolean enablePreview;//预览与否
    public boolean enableGif;//是否扫描动图
    public boolean cropImage;//
    public List<LocalMedia> selectionMedias;

    public static SelectionConfig getInstance(){
        return InstaceHolder.INSTANCE;
    }

    private static final class InstaceHolder{
        private static final SelectionConfig INSTANCE = new SelectionConfig();
    }

    public static SelectionConfig getDefaultInstance(){
        SelectionConfig selectionConfig = getInstance();
        selectionConfig.defaultVaule();
        return selectionConfig;
    }

    private void defaultVaule() {
        this.maxSelectNum = 1;
        this.minSelectNum = 1;
        this.mimeType = ConfigKey.TYPE_IMAGE;
        this.openCamera = false;
        this.isVisibleCamera = false;
        this.cropImage = false;
        this.enableGif = true;
        this.suffixType =  PictureFileUtils.POSTFIX;
        this.enablePreview = true;
        this.selectionMedias = new ArrayList<>();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mimeType);
        dest.writeByte((byte) (this.openCamera?1:0));
        dest.writeByte((byte) (this.isVisibleCamera?1:0));
        dest.writeInt(maxSelectNum);
        dest.writeInt(minSelectNum);
        dest.writeByte((byte) (this.enablePreview?1:0));
        dest.writeString(this.suffixType);
        dest.writeByte((byte) (this.cropImage?1:0));
        dest.writeByte((byte)(this.enableGif?1:0));
        dest.writeTypedList(this.selectionMedias);
    }

    public SelectionConfig() {
    }

    protected SelectionConfig (Parcel in){
        this.mimeType =in.readInt();
        this.openCamera = in.readByte()!=0;
        this.isVisibleCamera = in.readByte()!=0;
        this.maxSelectNum = in.readInt();
        this.minSelectNum = in.readInt();
        this.enablePreview = in.readByte()!=0;
        this.suffixType = in.readString();
        this.cropImage = in.readByte()!=0;
        this.enableGif = in.readByte()!=0;
        this.selectionMedias = in.createTypedArrayList(LocalMedia.CREATOR);
    }

    public static final Parcelable.Creator<SelectionConfig> CREATOR = new Parcelable.Creator<SelectionConfig>(){

        @Override
        public SelectionConfig createFromParcel(Parcel source) {
            return new SelectionConfig(source);
        }

        @Override
        public SelectionConfig[] newArray(int size) {
            return new SelectionConfig[0];
        }
    };
}
