package com.hawk.qiangda.util;

import android.content.Context;

import com.hawk.qiangda.view.AppProgressDialog;

/**
 * 在此写用途
 * Created by hawk on 2016/4/4.
 */
public class ProgressUtil {
    private static Context mContext;
    private static AppProgressDialog progressDialog;
    public static void showProgress(Context context,String msg){
        if(mContext!=context){
            progressDialog=AppProgressDialog.createDialog(context);
            mContext=context;
        }
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    public static void dismissProgress(){
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }
}
