package com.hawk.qiangda.ui.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.hawk.qiangda.R;
import com.hawk.qiangda.adapter.ScoreAdapter;
import com.hawk.qiangda.model.Score;
import com.hawk.qiangda.util.AppFragmentManager;
import com.hawk.qiangda.util.ProgressUtil;
import com.hawk.qiangda.util.SharedPreUtil;
import com.hawk.qiangda.util.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.listener.CloudCodeListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScoresFragment extends BaseFragment implements View.OnClickListener{
    private static final String TAG="ScoresFragment";
    private ScoreAdapter adapter;
    private ArrayList<Score>scoresList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        mListener.setCurrentFragmentTag(AppFragmentManager.SCORESFRAGMENT);
        View layoutView=inflater.inflate(R.layout.fragment_scores, container, false);
        ListView fscs_list= (ListView) layoutView.findViewById(R.id.fscs_list);
        Button fscs_back= (Button) layoutView.findViewById(R.id.fscs_back);
        adapter=new ScoreAdapter(getContext(),scoresList);
        fscs_list.setAdapter(adapter);
        fscs_back.setOnClickListener(this);

        return layoutView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetchScores();
    }

    @Override
    public void processJPushMsg(String extras) {
    }

    @Override
    public void onClick(View v) {
        mListener.switchToFragment(AppFragmentManager.MAINFRAGMENT,null,false);
    }

    private void fetchScores(){
        ProgressUtil.showProgress(getContext(),"正在获取分数数据...");

        JSONObject params=new JSONObject();
        try {
            params.put("recordObjectId", SharedPreUtil.getInstance().read(SharedPreUtil.RECORDOBJECTID));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AsyncCustomEndpoints ace = new AsyncCustomEndpoints();
        ace.callEndpoint(getContext(),"Code_GetScore", params, new CloudCodeListener() {
            @Override
            public void onSuccess(Object objResult) {
                ProgressUtil.dismissProgress();
                if(null==objResult){
                    ToastUtil.show("请求分数数据失败");
                    return;
                }
                String strResult= (String) objResult;
                Log.i(TAG,"分数结果:"+strResult);
                try {
                    JSONObject jsonResult=new JSONObject(strResult);
                    /*{
                        "a4a552f4ab": {
                        "userNick": "867246022405667",
                                "start": true,
                                "score": 0,
                                "nickName": "123"
                    },
                        "0f79a39cb0": {
                        "userNick": "868715026970735",
                                "start": true,
                                "score": 1,
                                "nickName": "是我"
                    }
                    }*/
                    Iterator<String> iterator=jsonResult.keys();
                    List<Score>scoresList=new ArrayList<>();
                    while(iterator.hasNext()){
                        String  key=iterator.next();
                        Score item=new Score();
                        JSONObject jItem=jsonResult.optJSONObject(key);
                        item.setNickName(jItem.optString("nickName"));
                        item.setScore(jItem.optInt("score"));
                        scoresList.add(item);
                    }
                    adapter.setList(scoresList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int code, String msg) {
                ProgressUtil.dismissProgress();
                ToastUtil.show("获取不到分数数据:"+msg);
                Log.i(TAG, "code=" + code + ";msg=" + msg);
            }
        });
    }
}
