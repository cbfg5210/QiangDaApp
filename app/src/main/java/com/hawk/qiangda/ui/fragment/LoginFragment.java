package com.hawk.qiangda.ui.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hawk.qiangda.R;
import com.hawk.qiangda.util.AppFragmentManager;
import com.hawk.qiangda.util.ProgressUtil;
import com.hawk.qiangda.util.SharedPreUtil;
import com.hawk.qiangda.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.listener.CloudCodeListener;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends BaseFragment implements View.OnClickListener{
    private static final String TAG="LoginFragment";
    private TextView flon_tip;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //如果本地保存有用户信息，跳转到主页面
        String nickName= SharedPreUtil.getInstance().read(SharedPreUtil.NICKNAME);
        if(!TextUtils.isEmpty(nickName)){
            mListener.setNickName(nickName);
            mListener.switchToFragment(AppFragmentManager.MAINFRAGMENT,null,false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mListener.setCurrentFragmentTag(AppFragmentManager.LOGINFRAGMENT);
        View layoutView=inflater.inflate(R.layout.fragment_login,null);
        flon_tip= (TextView)layoutView.findViewById(R.id.flon_tip);
        flon_tip.setOnClickListener(this);
        return layoutView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        login();
    }

    @Override
    public void processJPushMsg(String extras) {
        
    }

    @Override
    public void onClick(View v) {
        login();
    }

    private void login(){
        flon_tip.setVisibility(View.INVISIBLE);
        //注册bmob用户，设置jpush的alias为objectid
        JSONObject params=null;
        try {
            params=new JSONObject();
            params.put("deviceId", Utils.getDeviceImei(getContext()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ProgressUtil.showProgress(getContext(),"正在登录,请稍等...");
            //调用云端注册代码
            AsyncCustomEndpoints ace = new AsyncCustomEndpoints();
            //第一个参数是上下文对象，第二个参数是云端逻辑的方法名称，第三个参数是上传到云端逻辑的参数列表（JSONObject cloudCodeParams），第四个参数是回调类
            ace.callEndpoint(getContext(),"Code_Login", params, new CloudCodeListener() {
                @Override
                public void onSuccess(Object object) {
                    try {
                        String strResult= (String) object;
                        JSONObject jsonResult = new JSONObject(strResult);
                        if(jsonResult.optInt("code")!=200){
                            ProgressUtil.dismissProgress();
                            flon_tip.setText("登录失败,点击这里重新登录");
                            flon_tip.setVisibility(View.VISIBLE);
                        }else{
                            setJPushAlia(jsonResult);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(int code, String msg) {
                    // TODO Auto-generated method stub
                    ProgressUtil.dismissProgress();
                    flon_tip.setText("登录失败:" + msg+",点击这里重新登录");
                    flon_tip.setVisibility(View.VISIBLE);
                }
            });
    }

    private void setJPushAlia(final JSONObject jsonResult){
        String objectId=jsonResult.optString(SharedPreUtil.OBJECTID);
        JPushInterface.setAlias(getContext(),objectId, new TagAliasCallback() {
            @Override
            public void gotResult(int code, String s, Set<String> set) {
                ProgressUtil.dismissProgress();
                Log.i(TAG,"code="+code+";msg="+s);
                //code=0;msg=fe1763ed79
                if(code==0){
                //设置alias成功
                //把用户信息保存在activity中
                SharedPreUtil.getInstance().save(SharedPreUtil.OBJECTID,jsonResult.optString(SharedPreUtil.OBJECTID));
                String nickName=jsonResult.optString(SharedPreUtil.NICKNAME);
                SharedPreUtil.getInstance().save(SharedPreUtil.NICKNAME,nickName);
                mListener.setNickName(nickName);
                mListener.switchToFragment(AppFragmentManager.MAINFRAGMENT,null,false);
            }else{
                flon_tip.setText("登录失败,点击这里重新登录");
                flon_tip.setVisibility(View.VISIBLE);
            }
            }
        });
    }
}
