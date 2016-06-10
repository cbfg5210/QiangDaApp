package com.hawk.qiangda.util;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.hawk.qiangda.R;
import com.hawk.qiangda.ui.MainActivity;
import com.hawk.qiangda.ui.fragment.BaseFragment;
import com.hawk.qiangda.ui.fragment.HistoryRecFragment;
import com.hawk.qiangda.ui.fragment.LoginFragment;
import com.hawk.qiangda.ui.fragment.MainFragment;
import com.hawk.qiangda.ui.fragment.QiangDaFragment;
import com.hawk.qiangda.ui.fragment.RoomFragment;
import com.hawk.qiangda.ui.fragment.ScoresFragment;

public class AppFragmentManager {
	private static final String TAG="AppFragmentManager";
	
	public static final String LOGINFRAGMENT="LoginFragment";
	public static final String MAINFRAGMENT="MainFragment";
	public static final String ROOMFRAGMENT="RoomFragment";
	public static final String QIANGDAFRAGMENT="QiangDaFragment";
	public static final String SCORESFRAGMENT="ScoresFragment";
	public static final String HISTORYRECFRAGMENT="HistoryRecFragment";

	private FragmentManager mFragmentManager;
	private FragmentTransaction mFragmentTransaction;
	private String currentFragmentTag="";
	
	public AppFragmentManager(MainActivity mainActivity){
		mFragmentManager=mainActivity.getSupportFragmentManager();
	}
	
	public void setCurrentFragmentTag(String tag){
		currentFragmentTag=tag;
	}
	
	public String getCurrentFragmentTag(){
		return currentFragmentTag;
	}
	
	/**
	 *  弹出堆栈中的一个并且显示，也就是代码模拟按下返回键的操作。
	 */
	public void popBackStack(){
		mFragmentManager.popBackStack();
	}
	
	public void switchToFragment(String tag,Bundle bundle,boolean addToStack){
		Log.i(TAG,"tag="+tag);
		
		if(tag.equals(currentFragmentTag)){
			return;
		}
//		currentFragmentTag = tag;//因为涉及到消息推送，所以设置currentFragmentTag应该在每个fragment里
		Fragment fragment = mFragmentManager.findFragmentByTag(tag);

		boolean isExist = true;
		
		if(null==fragment){
			isExist = false;

			if(tag.equals(LOGINFRAGMENT)){
				fragment = new LoginFragment();
			}else if(tag.equals(ROOMFRAGMENT)){
				fragment = new RoomFragment();
			}else if(tag.equals(QIANGDAFRAGMENT)){
				fragment=new QiangDaFragment();
			}else if(tag.equals(SCORESFRAGMENT)){
				fragment=new ScoresFragment();
			}else if(tag.equals(MAINFRAGMENT)){
				fragment=new MainFragment();
			}else if(tag.equals(HISTORYRECFRAGMENT)){
				fragment=new HistoryRecFragment();
			}
			
			fragment.setArguments(bundle);
		}
			
			//Log.i(TAG,"Fragment"+tag+":"+mFragmentManager.getFragments().contains(fragment));
			if (fragment.isAdded()) {
				Log.i(TAG, tag+"------------------isAdded");
				//ensureTransaction().show(fragment);
				return;
			}
			if(isExist){
				ensureTransaction().replace(R.id.aman_container, fragment);
				//调用replace(int containerViewId, Fragment fragment, String tag)，但是tag为null
			}else{
				ensureTransaction().replace(R.id.aman_container, fragment, tag);
				//替换一个已经存在了的Fragment（先remove，在add）
			}
			if(addToStack){
				Log.i(TAG,"addToStack:" + tag);
				mFragmentTransaction.addToBackStack(tag);
			}
//			else{
//				mFragmentTransaction.addToBackStack(null);
//			}
			if(bundle!=null && !bundle.isEmpty()){
				fragment.getArguments().putAll(bundle);
			}
			commitTransactions();
	}
	
	protected FragmentTransaction ensureTransaction() {
		if (mFragmentTransaction == null) {
			mFragmentTransaction = mFragmentManager.beginTransaction();

			//mFragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//			mFragmentTransaction.setTransition(FragmentTransaction.TRANSIT_NONE);
//			mFragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//			mFragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
		}
		return mFragmentTransaction;
	}

	protected void commitTransactions() {
		if (mFragmentTransaction != null && !mFragmentTransaction.isEmpty()) {
			mFragmentTransaction.commitAllowingStateLoss();
			mFragmentTransaction = null;
		}
	}
	public void processCustomMsg(String extras){
		BaseFragment targetFragment= (BaseFragment) mFragmentManager.findFragmentByTag(currentFragmentTag);
		targetFragment.processJPushMsg(extras);
	}
}