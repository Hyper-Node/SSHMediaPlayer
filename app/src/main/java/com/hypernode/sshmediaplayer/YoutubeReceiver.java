package com.hypernode.sshmediaplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by johannes on 11.07.14.
 * NYI
 */
public class YoutubeReceiver extends BroadcastReceiver{
    static final String TAG="YoutubeReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Youtube intent received");
        //MainActivity.instance
    }
}
