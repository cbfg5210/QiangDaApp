package com.hawk.qiangda.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.hawk.qiangda.ui.MainActivity;

public abstract class BaseFragment extends Fragment {
    public OnFragmentInteractionListener mListener;
    private MainActivity mMainActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        mMainActivity= (MainActivity) context;
    }

    public MainActivity getMainActivity(){
        return mMainActivity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void runOnMainThread(Runnable runnable){
        mMainActivity.runOnUiThread(runnable);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void switchToFragment(String fragmentTag,Bundle arguments,boolean addToStack);
        void setCurrentFragmentTag(String tag);
        void setNickName(String nickName);
    }

    public abstract void processJPushMsg(String extras);
}
