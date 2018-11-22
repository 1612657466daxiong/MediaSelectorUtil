package yqhd.com.galleymediautil.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by chenpengxiang on 2018/9/27
 */
public class CustomDialog extends Dialog {

    public CustomDialog(Context context, int width, int height, int layout,
                        int style) {
        super(context, style);
        setContentView(layout);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = width;
        params.height = height;
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }

}
