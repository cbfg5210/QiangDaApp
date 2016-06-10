package com.hawk.qiangda.util;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * 在此写用途
 * Created by hawk on 2016/4/1.
 */
public class Utils {
    /**
     * Gets the device unique id called IMEI. Sometimes, this returns 00000000000000000 for the
     * rooted devices.
     * **
     */
    public static String getDeviceImei(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }
}
