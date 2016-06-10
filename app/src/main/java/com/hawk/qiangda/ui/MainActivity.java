package com.hawk.qiangda.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.hawk.qiangda.R;
import com.hawk.qiangda.model.Question;
import com.hawk.qiangda.ui.fragment.BaseFragment;
import com.hawk.qiangda.util.AppFragmentManager;
import com.hawk.qiangda.util.ProgressUtil;
import com.hawk.qiangda.util.SharedPreUtil;
import com.hawk.qiangda.util.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.listener.CloudCodeListener;
import cn.jpush.android.api.JPushInterface;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        BaseFragment.OnFragmentInteractionListener,View.OnClickListener{
    private static final String TAG = "MainActivity";
    private EditText nhman_nickname;
    private ImageButton nhman_editnick;
    private AlertDialog exitAlert;

    public static boolean isForeground = false;
    private AppFragmentManager mAppFragmentManager;
    private List<Question> questionList;
    private String oldNick;
    private long firstTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化bmob
        Bmob.initialize(this, "2c7a1b59e6f2f65fb137474dcc2c9647");

        initViews();

        //注册极光推送广播
        registerMessageReceiver();
        //默认切换到登录页
        mAppFragmentManager = new AppFragmentManager(this);
        switchToFragment(AppFragmentManager.LOGINFRAGMENT, null, false);
    }

    @Override
    protected void onResume() {
        isForeground = true;
        super.onResume();
        JPushInterface.onResume(this);
    }


    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
        JPushInterface.onPause(this);
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mMessageReceiver);
//        SharedPreUtil.getInstance().save(SharedPreUtil.ROOMID,"");
//        SharedPreUtil.getInstance().save(SharedPreUtil.RECORDOBJECTID,"");
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(mAppFragmentManager.getCurrentFragmentTag().equals(AppFragmentManager.ROOMFRAGMENT)){
                showExitAlert("退出房间","游戏即将开始,确定要退出房间吗?");
            }else if(mAppFragmentManager.getCurrentFragmentTag().equals(AppFragmentManager.QIANGDAFRAGMENT)){
                showExitAlert("退出游戏","现在退出将无法继续这盘游戏,确定要退出吗?");
            }else if(mAppFragmentManager.getCurrentFragmentTag().equals(AppFragmentManager.SCORESFRAGMENT)){
                switchToFragment(AppFragmentManager.MAINFRAGMENT,null,false);
            }else if(mAppFragmentManager.getCurrentFragmentTag().equals(AppFragmentManager.MAINFRAGMENT)||mAppFragmentManager.getCurrentFragmentTag().equals(AppFragmentManager.LOGINFRAGMENT)){
                long secondTime=System.currentTimeMillis();
                Log.i(TAG,"firstTime="+firstTime+";secondTime="+secondTime);

                if((secondTime-firstTime)>1000){
                    ToastUtil.show("再按一次退出");
                    firstTime=secondTime;
                    return;
                }
                finish();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(mAppFragmentManager.getCurrentFragmentTag().equals(AppFragmentManager.ROOMFRAGMENT)||mAppFragmentManager.getCurrentFragmentTag().equals(AppFragmentManager.QIANGDAFRAGMENT)){
            ToastUtil.show("切换到相应页面需要先退出房间");
            item.setChecked(false);
            return true;
        }
        if (id == R.id.mdrr_home) {
            switchToFragment(AppFragmentManager.MAINFRAGMENT,null,false);
        }else if(id==R.id.mdrr_records){
            switchToFragment(AppFragmentManager.HISTORYRECFRAGMENT,null,false);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initViews(){
        //标题栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //侧滑菜单
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //headerview初始化
        View headView=navigationView.getHeaderView(0);
        nhman_nickname= (EditText)headView.findViewById(R.id.nhman_nickname);
        nhman_editnick= (ImageButton)headView.findViewById(R.id.nhman_editnick);
        nhman_editnick.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int vid=v.getId();
        if(vid==R.id.nhman_editnick){
            String nickName=nhman_nickname.getText().toString().trim();
            if(nickName.equals("未登录"))return;
            if(!nhman_nickname.isEnabled()){
                oldNick=nhman_nickname.getText().toString();
                nhman_nickname.setEnabled(true);
                nhman_editnick.setBackgroundResource(R.mipmap.ic_edit_ok);
            }else{
                String newNickName=nhman_nickname.getText().toString().trim();
                nhman_nickname.setEnabled(false);
                nhman_editnick.setBackgroundResource(R.mipmap.ic_menu_edit);
                if(!TextUtils.isEmpty(newNickName)){
                    if(oldNick.equals(newNickName)){
                        ToastUtil.show("昵称不变哦");
                        return;
                    }
                    if(newNickName.length()>10){
                        ToastUtil.show("昵称太长了,不要超过10个字为好·o_O·");
                        nhman_nickname.setText(oldNick);
                        return;
                    }
                    updateNickName(newNickName);
                }else{
                    ToastUtil.show("昵称不能为空");
                    nhman_nickname.setText(oldNick);
                }
            }
        }
    }
    private void updateNickName(final String newNickName){
        //需要调用云端代码，以确保用户的昵称唯一
        JSONObject params=null;
        try {
            params=new JSONObject();
            params.put("newNick",newNickName);
            params.put("objectId",SharedPreUtil.getInstance().read(SharedPreUtil.OBJECTID));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ProgressUtil.showProgress(MainActivity.this,"正在更新昵称,请稍等...");
        //调用云端注册代码
        AsyncCustomEndpoints ace = new AsyncCustomEndpoints();
        //第一个参数是上下文对象，第二个参数是云端逻辑的方法名称，第三个参数是上传到云端逻辑的参数列表（JSONObject cloudCodeParams），第四个参数是回调类
        ace.callEndpoint(MainActivity.this,"Code_UpdateNick", params, new CloudCodeListener() {
            @Override
            public void onSuccess(Object objResult) {
                ProgressUtil.dismissProgress();
                if(null!=objResult){
                    String strResult= (String) objResult;
                    try {
                        JSONObject jsonResult=new JSONObject(strResult);
                        ToastUtil.show(jsonResult.optString("msg"));
                        if(jsonResult.optInt("code")!=200){
                            nhman_nickname.setText(oldNick);
                        }else{
                            SharedPreUtil.getInstance().save(SharedPreUtil.NICKNAME,newNickName);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onFailure(int i, String msg) {
                ProgressUtil.dismissProgress();
                ToastUtil.show("昵称修改失败:"+msg);
                nhman_nickname.setText(oldNick);
            }
        });
    }

    public void showExitAlert(String title,String msg){
        if(null==exitAlert){
            exitAlert=new AlertDialog.Builder(MainActivity.this)
                    .setNegativeButton("不推出了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("继续退出", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            exitGame();
                        }
                    })
                    .create();
        }
        exitAlert.setTitle(title);
        exitAlert.setMessage(msg);
        exitAlert.show();
    }

    private void exitGame(){
        JSONObject params =null;
        try {
            params = new JSONObject();
            params.put("roomId", SharedPreUtil.getInstance().read("roomId"));
            params.put("objectId", SharedPreUtil.getInstance().read("objectId"));
            params.put("nickName", SharedPreUtil.getInstance().read("nickName"));
            params.put("recordObjectId", SharedPreUtil.getInstance().read("recordObjectId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ProgressUtil.showProgress(MainActivity.this, "正在处理退出房间请求");
        AsyncCustomEndpoints ace = new AsyncCustomEndpoints();
        ace.callEndpoint(MainActivity.this, "Code_ExitRoom", params, new CloudCodeListener() {
            @Override
            public void onSuccess(Object objResult) {
                ProgressUtil.dismissProgress();
                if (null != objResult) {
                    ToastUtil.show("您已退出房间");
                    switchToFragment(AppFragmentManager.MAINFRAGMENT, null, false);
                }
            }
            @Override
            public void onFailure(int code, String msg) {
                ProgressUtil.dismissProgress();
                ToastUtil.show("您已退出房间");
                switchToFragment(AppFragmentManager.MAINFRAGMENT, null, false);
            }
        });
    }

    //for receive customer msg from jpush server
    private MessageReceiver mMessageReceiver;
    public static final String MESSAGE_RECEIVED_ACTION = "com.hawk.qiangda.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EXTRAS = "extras";

    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(MESSAGE_RECEIVED_ACTION);
        registerReceiver(mMessageReceiver, filter);
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
                String messge = intent.getStringExtra(KEY_MESSAGE);
                final String extras = intent.getStringExtra(KEY_EXTRAS);
                StringBuilder showMsg = new StringBuilder();
                showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
                if (!TextUtils.isEmpty(extras)) {
                    showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
                }
//                tip.setText("接受到自定义消息："+showMsg.toString());
                Log.i(TAG, "接受到自定义消息：" + showMsg.toString());

                if (extras.contains("GameOver")) {
                    if (mAppFragmentManager.getCurrentFragmentTag().equals(AppFragmentManager.MAINFRAGMENT)) {
                        mAppFragmentManager.processCustomMsg(extras);
                    }else if (mAppFragmentManager.getCurrentFragmentTag().equals(AppFragmentManager.QIANGDAFRAGMENT)) {
                        mAppFragmentManager.processCustomMsg(extras);
                    }
                }else if(extras.contains("UserStart")||extras.contains("NewUserEnter") || extras.contains("UserExit")){
                    if (mAppFragmentManager.getCurrentFragmentTag().equals(AppFragmentManager.ROOMFRAGMENT)) {
                        mAppFragmentManager.processCustomMsg(extras);
                    }
                }else if(extras.contains("DaoJiShi")){
                    if (mAppFragmentManager.getCurrentFragmentTag().equals(AppFragmentManager.ROOMFRAGMENT)) {
                        mAppFragmentManager.processCustomMsg(extras);
                    }else if (mAppFragmentManager.getCurrentFragmentTag().equals(AppFragmentManager.MAINFRAGMENT)) {
                        mAppFragmentManager.processCustomMsg(extras);
                    }
                }else if(extras.contains("QiangDaSuccess")||extras.contains("AnswerWrong")||extras.contains("AnswerRight")){
                    if (mAppFragmentManager.getCurrentFragmentTag().equals(AppFragmentManager.QIANGDAFRAGMENT)) {
                        mAppFragmentManager.processCustomMsg(extras);
                    }
                }
            }
        }
    }

    //OnFragmentInteractionListener的接口方法
    @Override
    public void switchToFragment(String fragmentTag, Bundle arguments, boolean addToStack) {
        mAppFragmentManager.switchToFragment(fragmentTag, arguments, addToStack);
    }
    @Override
    public void setCurrentFragmentTag(String tag) {
        mAppFragmentManager.setCurrentFragmentTag(tag);
    }
    @Override
    public void setNickName(String nickName) {
        nhman_nickname.setText(nickName);
    }
}
