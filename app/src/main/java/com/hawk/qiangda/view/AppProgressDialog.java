package com.hawk.qiangda.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import com.hawk.qiangda.R;

/**
 * 在此写用途
 * Created by hawk on 2016/4/5.
 */
public class AppProgressDialog extends Dialog {
    private Context mContext;
    private static AppProgressDialog progressDialog;

    public AppProgressDialog(Context context){
        super(context);
        this.mContext = context;
    }

    public AppProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    public void onWindowFocusChanged(boolean hasFocus){
        if (progressDialog == null){
            return;
        }
        ImageView imageView = (ImageView) progressDialog.findViewById(R.id.vprg_imgs);
        AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
        animationDrawable.start();
    }

    public static AppProgressDialog createDialog(Context context){
        progressDialog = new AppProgressDialog(context,R.style.AppProgressDialog);
        progressDialog.setContentView(R.layout.view_progressdialog);
        progressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        return progressDialog;
    }

    /**
     *
     * @param strTitle
     * @return
     */
    public AppProgressDialog setTitile(String strTitle){
        return progressDialog;
    }

    /**
     *
     * @param strMessage
     * @return
     */
    public AppProgressDialog setMessage(String strMessage){
        TextView tvMsg = (TextView)progressDialog.findViewById(R.id.id_tv_loadingmsg);
        tvMsg.setText(strMessage);
        return progressDialog;
    }
}
