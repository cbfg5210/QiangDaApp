package com.hawk.qiangda.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hawk.qiangda.R;
import com.hawk.qiangda.model.UserStatus;

import java.util.List;

/**
 * 在此写用途
 * Created by hawk on 2016/4/6.
 */
public class UserStatusAdapter extends BaseListAdapter<UserStatus>{
    public UserStatusAdapter(Context ctx, List<UserStatus> list) {
        super(ctx, list);
    }
    @Override
    public View bindView(int position, View layoutView, ViewGroup viewGroup) {
        if(null==layoutView){
            layoutView=mInflater.inflate(R.layout.item_user_status,null);
        }
        TextView iusr_nick= (TextView) layoutView.findViewById(R.id.iusr_nick);
        ImageView iusr_status= (ImageView) layoutView.findViewById(R.id.iusr_status);
        UserStatus item=mList.get(position);
        iusr_nick.setText(item.getNickName());
        if(item.getStatus()==2){
            iusr_status.setImageResource(R.mipmap.buld_on);
        }else{
            iusr_status.setImageResource(R.mipmap.buld_off);
        }
        return layoutView;
    }

    public void addUpdate(UserStatus newItem){
        int len=mList.size();
        boolean isNew=true;
        for(int i=0;i<len;i++){
            if(newItem.getObjectId().equals(mList.get(i).getObjectId())){
                mList.get(i).setStatus(newItem.getStatus());
                notifyDataSetChanged();
                isNew=false;
            }
        }
        if(isNew){
            add(newItem);
        }
    }

    public void removeExit(String objectId){
        int len=mList.size();
        for(int i=0;i<len;i++){
            if(objectId.equals(mList.get(i).getObjectId())){
                mList.remove(i);
                notifyDataSetChanged();
                break;
            }
        }
    }
}
