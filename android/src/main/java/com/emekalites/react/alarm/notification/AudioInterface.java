/* THIS FILE WAS HEAVILY MODIFIED FOR WakeMeApp */
package com.emekalites.react.alarm.notification;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.media.AudioAttributes;
import android.media.AudioManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class AudioInterface {
    private static final String TAG = AudioInterface.class.getSimpleName();

    private static MediaPlayer player;
    private static AudioInterface ourInstance = new AudioInterface();
    private Context mContext;
    private Uri uri;

    private AudioInterface() {
    }

    private static Context get() {
        return getInstance().getContext();
    }

    static synchronized AudioInterface getInstance() {
        return ourInstance;
    }

    void init(Context context) {
        uri = Settings.System.DEFAULT_ALARM_ALERT_URI;

        if (mContext == null) {
            this.mContext = context;
        }
    }

    private Context getContext() {
        return mContext;
    }

    MediaPlayer getSingletonMedia(String soundName, String soundNames) {
        Log.e(TAG, "player: " + soundName + ", names: " + soundNames);
        if (player == null) {
            List<Integer> resIds = new ArrayList<Integer>();
            if (soundNames != null && !soundNames.equals("")) {
                String[] names = soundNames.split(",");
                for (String item : names) {
                    int _resId;
                    if (mContext.getResources().getIdentifier(item, "raw", mContext.getPackageName()) != 0) {
                        _resId = mContext.getResources().getIdentifier(item, "raw", mContext.getPackageName());
                    } else {
                        String _item = item.substring(0, item.lastIndexOf('.'));
                        _resId = mContext.getResources().getIdentifier(_item, "raw", mContext.getPackageName());
                    }

                    resIds.add(_resId);
                }
            }

            int resId;
            Log.e(TAG, "is null");

            player = new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .setLegacyStreamType(AudioManager.STREAM_ALARM)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());

            /*
             * THIS COMMENTING MEANS THE PACKAGE WILL NOT WORK WITH MULTIPUL SOUNDS
             * or user selected sounds, only default
             * 
             * 
             * if (resIds.size() > 0) {
             * Random rand = new Random();
             * int n = rand.nextInt(resIds.size());
             * 
             * resId = resIds.get(n);
             * 
             * // player = MediaPlayer.create(get(), resId);
             * player.setDataSource(get(), resId);
             * } else
             */
            // if (soundName != null && !soundName.equals("")) {
            // if (mContext.getResources().getIdentifier(soundName, "raw",
            // mContext.getPackageName()) != 0) {
            // resId = mContext.getResources().getIdentifier(soundName, "raw",
            // mContext.getPackageName());
            // } else {
            // soundName = soundName.substring(0, soundName.lastIndexOf('.'));
            // resId = mContext.getResources().getIdentifier(soundName, "raw",
            // mContext.getPackageName());
            // }

            // // player = MediaPlayer.create(get(), resId);
            // player.setDataSource(get(), resId);
            // } else {
            // player = MediaPlayer.create(get(), this.uri);
            try {
                player.setDataSource(get(), this.uri);
            } catch (IOException e) {
                Log.d("ReactNative", "setDataSource exception ");
            }
            // }
        }
        try {
            player.prepare();
        } catch (IOException e) {
            Log.d("ReactNative", "setDataSource exception ");
        }
        return player;
    }

    void stopPlayer() {
        try {
            player.stop();
            player.reset();
            player.release();

            player = null;
        } catch (Exception e) {
            Log.e(TAG, "player not found");
        }
    }
}
