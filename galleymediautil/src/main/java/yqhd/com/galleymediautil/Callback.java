package yqhd.com.galleymediautil;

/**
 * Created by tanghongbin on 2017/3/29.
 */

interface Callback<T> {

    void onSuccess(T t);

    void onFailed(String message);

}
