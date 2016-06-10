package com.hawk.qiangda.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hawk.qiangda.R;
import com.hawk.qiangda.model.Score;

import java.util.List;

/**
 * 在此写用途
 * Created by hawk on 2016/4/2.
 */
public class ScoreAdapter extends BaseListAdapter<Score>{
    public ScoreAdapter(Context ctx, List<Score> list) {
        super(ctx, list);
    }
    @Override
    public View bindView(int position, View layoutView, ViewGroup viewGroup) {
        if(null==layoutView){
            layoutView=mInflater.inflate(R.layout.item_score,null);
        }
        TextView isce_nickname=ViewHolder.getView(layoutView,R.id.isce_nickname);
        TextView isce_score=ViewHolder.getView(layoutView,R.id.isce_score);
        Score item=mList.get(position);
        isce_nickname.setText("玩家："+item.getNickName());
        isce_score.setText("分数："+item.getScore());
        return layoutView;
    }
}
