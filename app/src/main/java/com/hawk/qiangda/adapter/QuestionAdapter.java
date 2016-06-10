package com.hawk.qiangda.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hawk.qiangda.R;
import com.hawk.qiangda.model.Question;

import java.util.List;

/**
 * 在此写用途
 * Created by hawk on 2016/3/29.
 */
public class QuestionAdapter extends BaseListAdapter<Question>{
    public QuestionAdapter(Context ctx, List<Question> list) {
        super(ctx, list);
    }
    @Override
    public View bindView(int position, View layoutView, ViewGroup viewGroup) {
        if(null==layoutView){
            layoutView=mInflater.inflate(R.layout.item_question,null);
        }
        TextView iqun_question=ViewHolder.getView(layoutView,R.id.iqun_question);
        TextView iqun_options=ViewHolder.getView(layoutView,R.id.iqun_options);
        TextView iqun_answer=ViewHolder.getView(layoutView,R.id.iqun_answer);

        Question item=mList.get(position);
        iqun_question.setText(item.getQuestion());
        iqun_options.setText(item.getOptions());
        iqun_answer.setText(item.getAnswer());

        return layoutView;
    }
}
