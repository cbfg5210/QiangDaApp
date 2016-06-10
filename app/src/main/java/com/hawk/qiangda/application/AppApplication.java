package com.hawk.qiangda.application;

import android.app.Application;

import cn.jpush.android.api.JPushInterface;

public class AppApplication extends Application {
	private static AppApplication mInstance;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mInstance=this;

		JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
		JPushInterface.init(this);     		// 初始化 JPush
	}
	
	public static AppApplication getInstance(){
		return mInstance;
	}
}
