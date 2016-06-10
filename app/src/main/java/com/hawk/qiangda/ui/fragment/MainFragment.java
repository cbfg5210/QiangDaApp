package com.hawk.qiangda.ui.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hawk.qiangda.R;
import com.hawk.qiangda.adapter.RoomAdapter;
import com.hawk.qiangda.model.Room;
import com.hawk.qiangda.util.AppFragmentManager;
import com.hawk.qiangda.util.ProgressUtil;
import com.hawk.qiangda.util.SharedPreUtil;
import com.hawk.qiangda.util.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.CloudCodeListener;
import cn.bmob.v3.listener.FindListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends BaseFragment {
    private static final String TAG = "MainFragment";
    private RoomAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mListener.setCurrentFragmentTag(AppFragmentManager.MAINFRAGMENT);
        View layoutView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView fman_roomlist = (ListView) layoutView.findViewById(R.id.fman_roomlist);
        adapter = new RoomAdapter(getContext(), null);
        fman_roomlist.setAdapter(adapter);
        fman_roomlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Room item = adapter.getItem(position);
                if (item.isStarted()) {
                    ToastUtil.show("游戏正在进行,暂时无法加入该房间,请稍候...");
                } else {
                    joinRoom(item.getObjectId());
                }
            }
        });
        return layoutView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetchRooms();
    }

    @Override
    public void processJPushMsg(String extras) {
        try {
            JSONObject jsonExtras = new JSONObject(extras);
            String flag = jsonExtras.optString("flag");
            if (flag.equals("GameOver")) {
                Room newStatus = new Room();
                newStatus.setStarted(false);
                newStatus.setObjectId(jsonExtras.optString(SharedPreUtil.ROOMID));
                adapter.addUpdate(newStatus);
            }else if (flag.equals("DaoJiShi")) {
                Log.i(TAG,"daojishi,extras="+extras);
                Room newStatus = new Room();
                newStatus.setStarted(true);
                newStatus.setObjectId(jsonExtras.optString(SharedPreUtil.ROOMID));
                adapter.addUpdate(newStatus);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fetchRooms() {
        ProgressUtil.showProgress(getContext(), "正在获取房间信息...");
        BmobQuery<Room> roomBmobQuery = new BmobQuery<>();
        roomBmobQuery.addQueryKeys("name,isStarted");
        roomBmobQuery.findObjects(getContext(), new FindListener<Room>() {
            @Override
            public void onSuccess(List<Room> list) {
                ProgressUtil.dismissProgress();
                if (null != list) {
                    adapter.setList(list);
                }
            }

            @Override
            public void onError(int i, String msg) {
                ProgressUtil.dismissProgress();
                ToastUtil.show("获取房间信息失败:" + msg);
            }
        });
    }

    private void joinRoom(final String roomId) {
        JSONObject params = null;
        try {
            params = new JSONObject();
            params.put("roomId", roomId);
            params.put("objectId", SharedPreUtil.getInstance().read(SharedPreUtil.OBJECTID));
            params.put("nickName", SharedPreUtil.getInstance().read(SharedPreUtil.NICKNAME));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "params=" + params);
        ProgressUtil.showProgress(getContext(), "正在处理加入房间的请求...");
        AsyncCustomEndpoints ace = new AsyncCustomEndpoints();
        ace.callEndpoint(getContext(), "Code_EnterRoom", params, new CloudCodeListener() {
            @Override
            public void onSuccess(Object object) {
                ProgressUtil.dismissProgress();
                if (null == object) {
                    ToastUtil.show("加入房间失败");
                    return;
                }
                String strResult = (String) object;
                Log.i(TAG, "加入房间,strResult=" + strResult);
                try {
                    JSONObject jsonResult = new JSONObject(strResult);
                    int code = jsonResult.optInt("code");
                    if (code == 200) {
                        ToastUtil.show("加入房间成功");
                        //保存recordObjectId和roomId
                        SharedPreUtil.getInstance().save(SharedPreUtil.ROOMID, roomId);
                        SharedPreUtil.getInstance().save(SharedPreUtil.RECORDOBJECTID, jsonResult.optString(SharedPreUtil.RECORDOBJECTID));
                        mListener.switchToFragment(AppFragmentManager.ROOMFRAGMENT, null, false);
                    } else if (code == 119) {
                        ToastUtil.show(jsonResult.optString("msg"));
                        Room newStatus = new Room();
                        newStatus.setStarted(true);
                        newStatus.setObjectId(roomId);
                        adapter.addUpdate(newStatus);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int code, String msg) {
                // TODO Auto-generated method stub
                ProgressUtil.dismissProgress();
                ToastUtil.show("加入房间失败:" + msg);
            }
        });
    }
}
