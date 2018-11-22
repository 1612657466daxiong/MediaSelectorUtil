package yqhd.com.galleymediautil;


import yqhd.com.galleymediautil.callback.SelectPicCallback;

/**
 * Created by tanghongbin on 2017/1/7.
 */

public class SelectCallbackManager {
    private SelectCallbackManager(){

    }
    private static SelectCallbackManager manager = new SelectCallbackManager();
    public static SelectCallbackManager getInstance(){
        return manager;
    }
    SelectPicCallback selectPicCallback;


    public SelectPicCallback getSelectPicCallback() {
        return selectPicCallback;
    }

    public void setSelectPicCallback(SelectPicCallback selectPicCallback) {
        this.selectPicCallback = selectPicCallback;
    }
    public void clearCallback(){
        selectPicCallback = null;
    }
}
