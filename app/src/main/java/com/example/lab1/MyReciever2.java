package com.example.lab1;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.bumptech.glide.Glide;

public class MyReciever2 extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        int id=intent.getIntExtra("btn",-1);
        if(id==R.id.play_noti){
            MyService.mp.start();
            MainActivity.toggleButton.setChecked(true);
        }
        if(id==R.id.pause_noti){
            MyService.mp.pause();
            MainActivity.toggleButton.setChecked(false);
        }
        if(id==R.id.prev_noti){
            MainActivity.pause = true;
            MainActivity.toggleButton.setChecked(false);
            for (int i = 0; i < MainActivity.list.size(); i++) {
                if (MainActivity.media_active == MainActivity.list.get(i).getId()) {
                    if (i == 0)
                        MainActivity.media_active = MainActivity.list.get(MainActivity.list.size() - 1).getId();
                    else
                        MainActivity.media_active = MainActivity.list.get(i - 1).getId();
                    break;
                }
            }
            for (int i = 0; i < MainActivity.list.size(); i++) {
                if (MainActivity.media_active == MainActivity.list.get(i).getId()) {
                    MyService.mp.stop();
                    Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MainActivity.list.get(i).getId());
                    try {
                        MyService.mp=new MediaPlayer();
                        MyService.mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        MyService.mp.setDataSource(context,trackUri);
                        MyService.mp.prepare();
                    } catch (Exception e) {
                        Log.e("MUSIC SERVICE", "Error starting data source", e);
                    }
                    MainActivity.media_active = MainActivity.list.get(i).getId();
                    MainActivity.art=MainActivity.getAlbumArt(MainActivity.list.get(i).getImage());
                    if(MainActivity.art!=null)
                        Glide.with(context).asBitmap().load(MainActivity.art).into(MainActivity.image);
                    else
                        Glide.with(context).asBitmap().load(R.drawable.music).into(MainActivity.image);
                    MainActivity.name.setText(MainActivity.list.get(i).getName());
                    MainActivity.casi.setText(MainActivity.list.get(i).getName_casi());
                    MainActivity.toggleButton.setChecked(true);
                    MainActivity.pause = false;
                    break;
                }
            }

        }
        if(id==R.id.next_noti){
            MainActivity.pause = true;
            MainActivity.toggleButton.setChecked(false);
            for (int i = 0; i <  MainActivity.list.size(); i++) {
                if ( MainActivity.media_active ==  MainActivity.list.get(i).getId()) {
                    if (i ==  MainActivity.list.size() - 1)
                        MainActivity.media_active =  MainActivity.list.get(0).getId();
                    else
                        MainActivity.media_active =  MainActivity.list.get(i + 1).getId();
                    break;
                }
            }
            for (int i = 0; i <  MainActivity.list.size(); i++) {
                if ( MainActivity.media_active ==  MainActivity.list.get(i).getId()) {
                    MyService.mp.stop();
                    Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MainActivity.list.get(i).getId());
                    try {
                        MyService.mp=new MediaPlayer();
                        MyService.mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        MyService.mp.setDataSource(context,trackUri);
                        MyService.mp.prepare();
                    } catch (Exception e) {
                        Log.e("MUSIC SERVICE", "Error starting data source", e);
                    }
                    MainActivity.media_active =  MainActivity.list.get(i).getId();
                    MainActivity.art=MainActivity.getAlbumArt(MainActivity.list.get(i).getImage());
                    if(MainActivity.art!=null)
                        Glide.with(context).asBitmap().load(MainActivity.art).into(MainActivity.image);
                    else
                        Glide.with(context).asBitmap().load(R.drawable.music).into(MainActivity.image);
                    MainActivity.name.setText( MainActivity.list.get(i).getName());
                    MainActivity.casi.setText(MainActivity.list.get(i).getName_casi());
                    MainActivity.toggleButton.setChecked(true);
                    MainActivity.pause = false;
                    break;
                }
            }
        }
    }


}
