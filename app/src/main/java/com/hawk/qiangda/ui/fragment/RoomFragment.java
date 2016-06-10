package com.hawk.qiangda.ui.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hawk.qiangda.R;
import com.hawk.qiangda.adapter.UserStatusAdapter;
import com.hawk.qiangda.model.Question;
import com.hawk.qiangda.model.UserStatus;
import com.hawk.qiangda.util.AppFragmentManager;
import com.hawk.qiangda.util.ProgressUtil;
import com.hawk.qiangda.util.SharedPreUtil;
import com.hawk.qiangda.util.ToastUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.CloudCodeListener;
import cn.bmob.v3.listener.FindListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class RoomFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = "RoomFragment";
    private FloatingActionButton from_actions;

    private UserStatusAdapter adapter;
    private String extras;
    private Timer timer;
    private int daojishi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mListener.setCurrentFragmentTag(AppFragmentManager.ROOMFRAGMENT);
        View layoutView = inflater.inflate(R.layout.fragment_room, container, false);
        ListView from_users = (ListView) layoutView.findViewById(R.id.from_users);
        adapter = new UserStatusAdapter(getContext(), null);
        from_users.setAdapter(adapter);
        from_actions = (FloatingActionButton) layoutView.findViewById(R.id.from_actions);
        from_actions.setOnClickListener(this);

        return layoutView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetchRoomUsers();
    }

    @Override
    public void processJPushMsg(String extras) {
        Log.i(TAG, "extras=" + extras);
        try {
            JSONObject jsonExtras = new JSONObject(extras);
            String flag = jsonExtras.optString("flag");

            if (flag.equals("UserStart")) {
                String nickName = jsonExtras.optString("nickName");
                String objectId = jsonExtras.optString("objectId");
                UserStatus item = new UserStatus();
                item.setNickName(nickName);
                item.setStatus(2);//-1:exit,1:wait,2:start
                item.setObjectId(objectId);
                adapter.addUpdate(item);
            } else if (flag.equals("NewUserEnter")) {
                String nickName = jsonExtras.optString("nickName");
                String objectId = jsonExtras.optString("objectId");
                UserStatus item = new UserStatus();
                item.setNickName(nickName);
                item.setStatus(1);
                item.setObjectId(objectId);
                adapter.addUpdate(item);
            } else if (flag.equals("UserExit")) {
                String objectId = jsonExtras.optString("objectId");
                adapter.removeExit(objectId);
            } else if (flag.equals("DaoJiShi")) {
                String roomId = jsonExtras.optString("roomId");
                Log.i(TAG, "daojishi,roomId=" + roomId);
                //如果倒计时的不是当前房间
                if (!roomId.equals(SharedPreUtil.getInstance().read(SharedPreUtil.ROOMID))) {
                    return;
                }
                //如果倒计时的是当前房间
//                extras : {"questions":["1","2","9","4","5","7","6","11","3","8"],"time":"10","flag":"DaoJiShi"}
                JSONArray questions = jsonExtras.optJSONArray("questions");
                int len = questions.length();
                List<Integer> list = new ArrayList<>(len);
                for (int i = 0; i < len; i++) {
                    list.add(questions.optInt(i));
                }
//                Log.i(TAG,"list="+list.toString());
                daojishi = jsonExtras.optInt("time");
                Log.i(TAG, "daojishi time=" + daojishi);
                ProgressUtil.showProgress(getContext(), "开始载入题目...");

                BmobQuery<Question> questionBmobQuery = new BmobQuery<>();
                questionBmobQuery.addWhereContainedIn("index", list);
                questionBmobQuery.addQueryKeys("question,options,index");
                questionBmobQuery.findObjects(getContext(), new FindListener<Question>() {
                    @Override
                    public void onSuccess(final List<Question> questionList) {
                        if (null == questionList || questionList.size() == 0) {
                            ProgressUtil.dismissProgress();
                            ToastUtil.show("没有获取到题目数据");
                            return;
                        }
                        Log.i(TAG, "question list=" + questionList.size());
                        ArrayList<Question> questionsList = (ArrayList<Question>) questionList;
                        startGameDaoJiShi(questionsList);
                    }

                    @Override
                    public void onError(int code, String msg) {
                        ProgressUtil.dismissProgress();
                        ToastUtil.show("载入题目出错:" + msg);
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        onStartClick();
    }

    private void onStartClick() {
        from_actions.setEnabled(false);
        JSONObject params = null;
        try {
            params = new JSONObject();
            params.put("roomId", SharedPreUtil.getInstance().read(SharedPreUtil.ROOMID));
            params.put("objectId", SharedPreUtil.getInstance().read(SharedPreUtil.OBJECTID));
            params.put("nickName", SharedPreUtil.getInstance().read(SharedPreUtil.NICKNAME));
            params.put("recordObjectId", SharedPreUtil.getInstance().read(SharedPreUtil.RECORDOBJECTID));
        } catch (JSONException e) {
            e.printStackTrace();
            from_actions.setEnabled(true);
        }
        ProgressUtil.showProgress(getContext(), "正在处理开始请求");
        AsyncCustomEndpoints ace = new AsyncCustomEndpoints();
        ace.callEndpoint(getContext(), "Code_IStart", params, new CloudCodeListener() {
            @Override
            public void onSuccess(Object objResult) {
                ProgressUtil.dismissProgress();
                //{"code":200,"flag":"UserStart"}
                if (null == objResult) {
                    from_actions.setEnabled(true);
                    return;
                }
                String strResult = (String) objResult;
                Log.i(TAG, "onStartClick,strResult=" + strResult);
                try {
                    JSONObject jsonResult = new JSONObject(strResult);
                    ToastUtil.show(jsonResult.optString("msg"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                UserStatus newStatus = new UserStatus();
                newStatus.setObjectId(SharedPreUtil.getInstance().read(SharedPreUtil.OBJECTID));
                newStatus.setNickName(SharedPreUtil.getInstance().read(SharedPreUtil.NICKNAME));
                newStatus.setStatus(2);
                adapter.addUpdate(newStatus);
                from_actions.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int code, String msg) {
                ProgressUtil.dismissProgress();
                ToastUtil.show("开始游戏失败:" + msg);
                from_actions.setEnabled(true);
            }
        });
    }

    private void fetchRoomUsers() {
        ProgressUtil.showProgress(getContext(), "正在获取房间内的数据,请稍等...");

        JSONObject params = new JSONObject();
        try {
            params.put("recordObjectId", SharedPreUtil.getInstance().read(SharedPreUtil.RECORDOBJECTID));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AsyncCustomEndpoints ace = new AsyncCustomEndpoints();
        ace.callEndpoint(getContext(), "Code_GetScore", params, new CloudCodeListener() {
            @Override
            public void onSuccess(Object objResult) {
                ProgressUtil.dismissProgress();
                if (null == objResult) {
                    ToastUtil.show("请求用户数据失败");
                    return;
                }
                String strResult = (String) objResult;
                Log.i(TAG, "strResult=" + strResult);
                try {
                    JSONObject jsonResult = new JSONObject(strResult);
                    /*{
  "15f5397165": {
    "status": 1,
    "score": 0,
    "nickName": "000000000000000"
  },
  "JM57dKKW": {
    "status": 1,
    "score": 0,
    "nickName": "aa"
  }
}*/
                    Iterator<String> iterator = jsonResult.keys();
                    List<UserStatus> statusList = new ArrayList<>();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        JSONObject infos = jsonResult.optJSONObject(key);
                        UserStatus item = new UserStatus();
                        item.setObjectId(key);
                        item.setNickName(infos.optString("nickName"));
                        item.setStatus(infos.optInt("status"));
                        statusList.add(item);
                    }
                    adapter.setList(statusList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int code, String msg) {
                ProgressUtil.dismissProgress();
                ToastUtil.show("获取不到房间内的数据:" + msg);
                Log.i(TAG, "code=" + code + ";msg=" + msg);
            }
        });
    }

    private void startGameDaoJiShi(final ArrayList<Question> questionList) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                    ProgressUtil.showProgress(getContext(), daojishi + "秒后进入抢答游戏...");
                    daojishi--;
                    if (daojishi <= 0) {
                        cancel();
                        Bundle arguments = new Bundle();
                        arguments.putParcelableArrayList("questionList", questionList);
                        mListener.switchToFragment(AppFragmentManager.QIANGDAFRAGMENT, arguments, false);
                        ProgressUtil.dismissProgress();
                    }
                    }
                });
            }
        }, 0, 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != timer) {
            timer.cancel();
            timer = null;
        }
    }
}
