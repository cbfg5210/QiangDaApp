package com.hawk.qiangda.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hawk.qiangda.R;
import com.hawk.qiangda.model.Room;

import java.util.List;

/**
 * 在此写用途
 * Created by hawk on 2016/4/5.
 */
public class RoomAdapter extends BaseListAdapter<Room>{
    public RoomAdapter(Context ctx, List<Room> list) {
        super(ctx, list);
    }
    @Override
    public View bindView(int position, View layoutView, ViewGroup viewGroup) {
        if(null==layoutView){
            layoutView=mInflater.inflate(R.layout.item_room,null);
        }
        TextView irom_roomname=ViewHolder.getView(layoutView, R.id.irom_roomname);
        TextView irom_roomstat=ViewHolder.getView(layoutView,R.id.irom_roomstat);
        Room item=mList.get(position);
        irom_roomname.setText(item.getName());
        if(item.isStarted()){
            irom_roomstat.setText("抢答中,不可加入...");
            irom_roomstat.setEnabled(false);
        }else{
            irom_roomstat.setText("可加入,点击加入...");
            irom_roomstat.setEnabled(true);
        }
        return layoutView;
    }

    public void addUpdate(Room newRoom){
        int len=mList.size();
        for(int i=0;i<len;i++){
            if(newRoom.getObjectId().equals(mList.get(i).getObjectId())){
                mList.get(i).setStarted(newRoom.isStarted());
                notifyDataSetChanged();
                break;
            }
        }
    }
}
