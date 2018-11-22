# MediaSelectorUtil
#### 这是一个多媒体文件选择库 调用简单 采用回调的方式
#### 说明下，本库主要是基于 [多图片选择器](https://github.com/LuckSiege/PictureSelector) 进行了些许修改以及扩展补充，主要是为了符合我项目的需求 感谢开源
#### 这个库还有许多不足 以及bug  欢迎各位大佬指正以及借鉴


### 使用方式 
```java
SelectionConfig config = SelectionConfig.getDefaultInstance();//配置类 主要是对获取多媒体的方式结果进行限制
  config.mimeType = PictureMimeType.ofAudio();//选择类型
  config.selectionMedias = new ArrayList<>();//选择列表
  config.isVisibleCamera = true;//摄像机、录音按钮显示与否（选择列表的第一位）
  config.openCamera = false;//直接打开摄像机 (未做)
  config.maxSelectNum = 1;//最多数量
  config.minSelectNum = 1;//最小数量
  config.enablePreview = true;//可否预览-试听 播放 预览
  MediaSelectedHelper.selectMutiplePicByDiy(this, config, new SelectPicCallback() {//以回调的方式进行返回结果 方便代码维护
     @Override
     public void selectPic(List<String> images) {
        showImage(images.get(0));
     }
  });
```
已经上传到jcenter
引用：
```java
compile 'com.yiqi:gallerymediautil:1.0.0'
```
#### 1.0.0 功能点

1. 针对选择多媒体库 多选 单选
2. 进行选择或者录制
3. 预览（音频视频播放，图片查看）
4. 回调方式返回结果 方便维护

#### 后续还要进行维护 
* 添加原本多图片选择器的 图片压缩 图片裁剪 功能
* 添加UI元素的扩展修改接口

