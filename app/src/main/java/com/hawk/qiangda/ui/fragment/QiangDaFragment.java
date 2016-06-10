package com.hawk.qiangda.ui.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.hawk.qiangda.R;
import com.hawk.qiangda.model.Question;
import com.hawk.qiangda.util.AppFragmentManager;
import com.hawk.qiangda.util.ProgressUtil;
import com.hawk.qiangda.util.SharedPreUtil;
import com.hawk.qiangda.util.ToastUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.listener.CloudCodeListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class QiangDaFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = "QiangDaFragment";
    private List<Question> questionList;
    private TextView fqda_index, fqda_question,fqda_time;
    private RadioGroup fqda_options;
    private Button fqda_qiangda;
    private int questionIndex;
    private String[] option = new String[]{"A", "B", "C", "D", "E", "F", "G", "H"};
//    private String extras;
    private String myNickName;
    private Timer timer;
    private int time;
    private String currentUserNick;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        questionList = getArguments().getParcelableArrayList("questionList");
        myNickName = SharedPreUtil.getInstance().read(SharedPreUtil.NICKNAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        mListener.setCurrentFragmentTag(AppFragmentManager.QIANGDAFRAGMENT);
        View layoutView = inflater.inflate(R.layout.fragment_qiang_da, container, false);
        fqda_index = (TextView) layoutView.findViewById(R.id.fqda_index);
        fqda_question = (TextView) layoutView.findViewById(R.id.fqda_question);
        fqda_time = (TextView) layoutView.findViewById(R.id.fqda_time);
        fqda_options = (RadioGroup) layoutView.findViewById(R.id.fqda_options);
        fqda_qiangda = (Button) layoutView.findViewById(R.id.fqda_qiangda);
        fqda_qiangda.setOnClickListener(this);

        showQuestion(0);

        return layoutView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*if (!TextUtils.isEmpty(extras)) {
            processJPushMsg(extras);
        }*/
    }

    /**
     * @param tihao
     */
    private void showQuestion(int tihao) {
        fqda_index.setText("题号：" + (tihao + 1));
        Question question = questionList.get(tihao);
        questionIndex = question.getIndex();
        fqda_question.setText(question.getQuestion());

        String options = question.getOptions();
        try {
            JSONArray jsonOptions = new JSONArray(options);
            Log.i(TAG, "jsonOptions=" + jsonOptions);
            int len = jsonOptions.length();
//            Log.i(TAG, "childs count=" + fqda_options.getChildCount());
            fqda_options.removeAllViews();

            for (int i = 0; i < len; i++) {
                RadioButton radioButton = new RadioButton(getContext());
                radioButton.setText(jsonOptions.optString(i));
                radioButton.setTag(i);
                fqda_options.addView(radioButton, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
        {
            "status": "failure",
                "msg": "当前正在答题状态,抢题失败",
                "code": 119
        }
        extras : {"nickName":"aaaaa","flag":"QiangDaSuccess"}
        */
    @Override
    public void processJPushMsg(String extras) {
        try {
            JSONObject jsonExtras = new JSONObject(extras);
            String flag = jsonExtras.optString("flag");
            if (flag.equals("QiangDaSuccess")) {
                String nickName = jsonExtras.optString("nickName");
                if (nickName.equals(myNickName)) {
                    ToastUtil.show("抢题成功");
                    fqda_qiangda.setEnabled(true);
                    fqda_qiangda.setText("答题");
                    showDaoJiShi();
                } else {
                    fqda_qiangda.setEnabled(false);
                    ToastUtil.show(nickName + " 抢题成功,请等待答题结束再进行抢题...");
                    startWaitJiShi();
                }
            } else if (flag.equals("AnswerWrong")) {
                String nickName = jsonExtras.optString("nickName");
                if (nickName.equals(myNickName)) {
                    ToastUtil.show("答案错误");
                } else {
                    ToastUtil.show(nickName + " 回答错误,开始抢答下一题...");
                    cancelDaoJiShi();
                }
//                Log.i(TAG, "answer wrong,radio nums=" + fqda_options.getChildCount());
                showQuestion(jsonExtras.optInt("nextNum"));
                fqda_qiangda.setEnabled(true);
                fqda_qiangda.setText("抢题");
            } else if (flag.equals("AnswerRight")) {
                String nickName = jsonExtras.optString("nickName");
                if (nickName.equals(myNickName)) {
                    ToastUtil.show("答案正确");
                } else {
                    ToastUtil.show(nickName + " 回答正确,开始抢答下一题...");
                    cancelDaoJiShi();
                }
                Log.i(TAG, "answer right,radio nums=" + fqda_options.getChildCount());
                showQuestion(jsonExtras.optInt("nextNum"));
                fqda_qiangda.setEnabled(true);
                fqda_qiangda.setText("抢题");
            } else if (flag.equals("GameOver")) {
                String roomId=jsonExtras.optString("roomId");
                if(roomId.equals(SharedPreUtil.getInstance().read(SharedPreUtil.ROOMID))){
                    cancelDaoJiShi();
                    mListener.switchToFragment(AppFragmentManager.SCORESFRAGMENT, null, false);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        String btnTxt = fqda_qiangda.getText().toString();
        if (btnTxt.equals("抢题")) {
            qiangTi();
        } else if (btnTxt.equals("答题")) {
            daTi();
        }
    }

    private void qiangTi() {
        cancelDaoJiShi();
        fqda_qiangda.setEnabled(false);
        JSONObject params = packParams(false);
        if (null == params){
            fqda_qiangda.setEnabled(true);
            return;
        }
        ProgressUtil.showProgress(getContext(), "抢题中...");
        AsyncCustomEndpoints ace = new AsyncCustomEndpoints();
        ace.callEndpoint(getContext(), "Code_QiangDa", params, new CloudCodeListener() {
            @Override
            public void onSuccess(Object objResult) {
                ProgressUtil.dismissProgress();
                if (null == objResult) return;
                String strResult = (String) objResult;
                try {
                    JSONObject jsonResult = new JSONObject(strResult);
                    ToastUtil.show(jsonResult.optString("msg"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int code, String msg) {
                ProgressUtil.dismissProgress();
                ToastUtil.show("抢题失败:" + msg);
                fqda_qiangda.setEnabled(true);
            }
        });
    }

    private void daTi() {
        fqda_qiangda.setEnabled(false);
        JSONObject params = packParams(true);
        if (null == params){
            fqda_qiangda.setEnabled(true);
            return;
        }
        ProgressUtil.showProgress(getContext(), "正在审核答案...");
        AsyncCustomEndpoints ace = new AsyncCustomEndpoints();
        ace.callEndpoint(getContext(), "Code_Answer", params, new CloudCodeListener() {
            @Override
            public void onSuccess(Object objResult) {
//                Log.i(TAG, "mListener=" + mListener);//mListener=null
//                Log.i(TAG, "getMainActivity=" + getMainActivity());
                ProgressUtil.dismissProgress();
                if (null == objResult) return;
                String strResult = (String) objResult;
                try {
                    JSONObject jsonResult = new JSONObject(strResult);
                    ToastUtil.show(jsonResult.optString("msg"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int code, String msg) {
                ProgressUtil.dismissProgress();
                ToastUtil.show("答题失败:" + msg);
                fqda_qiangda.setEnabled(true);
            }
        });
    }

    private JSONObject packParams(boolean isDaTi) {
        JSONObject params = null;
        try {
            params = new JSONObject();
            if (isDaTi) {
                params.put("index", questionIndex);
                //提交答案
                int radioId = fqda_options.getCheckedRadioButtonId();
                Log.i(TAG, "radioId=" + radioId);
                String answer;
                if (radioId == -1) {
                    answer = "NO";
                } else {
                    RadioButton radio = (RadioButton) fqda_options.findViewById(radioId);
                    int tag = (int) radio.getTag();
                    answer = option[tag];
                }
                params.put("answer", answer);
            }
            params.put("roomId", SharedPreUtil.getInstance().read(SharedPreUtil.ROOMID));
            params.put("objectId", SharedPreUtil.getInstance().read(SharedPreUtil.OBJECTID));
            params.put("nickName", SharedPreUtil.getInstance().read(SharedPreUtil.NICKNAME));
            Log.i(TAG, "params=null");
            return params;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i(TAG, "params=null");
            return null;
        }
    }

    private void showDaoJiShi(){
        time=5;
        fqda_time.setVisibility(View.VISIBLE);
        fqda_time.setText(""+time);
        if(null==timer)timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        time--;
                        fqda_time.setText(""+time);
                        if(time<=0){
                            cancel();
                            daTi();
                        }
                    }
                });
            }
        },0,1000);
    }
    private void cancelDaoJiShi(){
        if(null!=timer){
            timer.cancel();
        }
        fqda_time.setVisibility(View.INVISIBLE);
    }

    /**
     * 计算等待时间，如果超出10秒，通知服务器其它用户作答超时
     */
    private void startWaitJiShi(){
        time=0;
        if(null==timer)timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        time++;
                        if(time>=10){
                            cancel();
                            sendTimeOutMsgToServer();
                        }
                    }
                });
            }
        },0,1000);
    }

    /**
     * 如果其它用户10秒内还没答题完成，发送超时通知到服务器，服务器做相应处理
     */
    private void sendTimeOutMsgToServer(){
        cancelDaoJiShi();
        JSONObject params=new JSONObject();
        try {
            params.put("roomId",SharedPreUtil.getInstance().read(SharedPreUtil.ROOMID));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AsyncCustomEndpoints ace=new AsyncCustomEndpoints();
        ace.callEndpoint(getContext(), "Code_TimeOut", params, new CloudCodeListener() {
            @Override
            public void onSuccess(Object objResult) {
            }
            @Override
            public void onFailure(int code, String msg) {
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null!=timer){
            timer.cancel();
            timer=null;
        }
    }
}
