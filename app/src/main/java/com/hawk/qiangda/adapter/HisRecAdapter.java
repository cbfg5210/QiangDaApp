package com.hawk.qiangda.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hawk.qiangda.R;

import java.util.List;

/**
 * 在此写用途
 * Created by hawk on 2016/4/7.
 */
public class HisRecAdapter extends BaseListAdapter<String>{
    public HisRecAdapter(Context ctx, List<String> list) {
        super(ctx, list);
    }
    @Override
    public View bindView(int position, View layoutView, ViewGroup viewGroup) {
        if(null==layoutView){
            layoutView=mInflater.inflate(R.layout.item_hisrec,null);
        }
        TextView ihic_record=ViewHolder.getView(layoutView,R.id.ihic_record);
        String item=mList.get(position);
        ihic_record.setText(item);

        return layoutView;
    }
}
