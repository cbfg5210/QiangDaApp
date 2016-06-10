package com.hawk.qiangda.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.hawk.qiangda.R;
import com.hawk.qiangda.adapter.HisRecAdapter;
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

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.listener.CloudCodeListener;

public class HistoryRecFragment extends BaseFragment {
    private static final String TAG="HistoryRecFragment";
    private Button fhrec_more;
    private int skip=0;
    private HisRecAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mListener.setCurrentFragmentTag(AppFragmentManager.SCORESFRAGMENT);
        View layoutView=inflater.inflate(R.layout.fragment_history_rec, container, false);
        ListView fhrec_list= (ListView) layoutView.findViewById(R.id.fhrec_list);
        fhrec_more= (Button) layoutView.findViewById(R.id.fhrec_more);
        adapter=new HisRecAdapter(getContext(),null);
        fhrec_list.setAdapter(adapter);

        fhrec_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchHistoryRec();
            }
        });

        return layoutView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetchHistoryRec();
    }

    @Override
    public void processJPushMsg(String extras) {

    }

    private void fetchHistoryRec(){
        final String objectId= SharedPreUtil.getInstance().read(SharedPreUtil.OBJECTID);
        if(TextUtils.isEmpty(objectId)){
            ToastUtil.show("暂无历史游戏数据");
            return;
        }
        ProgressUtil.showProgress(getContext(),"正在获取历史数据...");
        JSONObject params=new JSONObject();
        try {
            params.put("skip",skip);
            params.put("objectId",objectId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AsyncCustomEndpoints ace=new AsyncCustomEndpoints();
        ace.callEndpoint(getContext(), "Code_HistoryRec",params, new CloudCodeListener() {
            @Override
            public void onSuccess(Object objResult) {
                ProgressUtil.dismissProgress();
                if(null==objResult){
                    ToastUtil.show("暂无历史游戏数据");
                    return;
                }
                String strResult= (String) objResult;
                Log.i(TAG,"history records,strResult="+strResult);
                try {
                    JSONObject jsonResult=new JSONObject(strResult);
                    int code=jsonResult.optInt("code");
                    if(code==119){
                        ToastUtil.show(jsonResult.optString("msg"));
                        fhrec_more.setVisibility(View.GONE);
                        return;
                    }
                    /*{
                        "code": 200,
                            "msg": "获取数据成功",
                            "data": [
                        {
                            "02ee2446d8": {
                            "status": 1,
                                    "score": 0,
                                    "nickName": "bb"
                        },
                            "cb2d0f5919": {
                            "status": 1,
                                    "score": 0,
                                    "nickName": "aa"
                        }
                        },
                        {
                            "02ee2446d8": {
                            "status": 1,
                                    "score": 0,
                                    "nickName": "bb"
                        },
                            "cb2d0f5919": {
                            "status": 1,
                                    "score": 0,
                                    "nickName": "aa"
                        }
                        }
                        ]
                    }*/
                    JSONArray records=jsonResult.optJSONArray("data");
                    int len=records.length();
                    skip+=len;
                    List<String>recList=new ArrayList<>(len);
                    for(int i=0;i<len;i++){
                        JSONObject record=records.optJSONObject(i);
                        Iterator<String>iterator=record.keys();
                        StringBuffer stringBuffer=new StringBuffer();
                        while(iterator.hasNext()){
                            String key=iterator.next();
                            JSONObject perRecord=record.optJSONObject(key);
                            stringBuffer.append("玩家：")
                                        .append(perRecord.optString("nickName"))
                                        .append("        ")
                                        .append("分数：")
                                        .append(perRecord.optInt("score"))
                                        .append("\n\n");
                        }
                        recList.add(stringBuffer.toString());
                    }
                    adapter.addAll(recList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int code, String msg) {
                ProgressUtil.dismissProgress();
                ToastUtil.show("无法获取到游戏数据："+msg);
            }
        });
    }
}
