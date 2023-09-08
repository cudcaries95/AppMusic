package com.example.lab1;

import androidx.annotation.IdRes;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    public static ArrayList<Music> list;
    public static ToggleButton toggleButton, toggleButtonReplay;
    public static int media_active;
    public static ImageView image;
    public static TextView name,casi;
    private ImageView prev, next;
    public static boolean pause;
    private boolean replay;
    private Intent intent;
    private ProgressBar progressBar;
    public static byte [] art;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        toggleButton = findViewById(R.id.toggle);
        toggleButtonReplay = findViewById(R.id.toggle_replay);
        image = findViewById(R.id.image);
        name = findViewById(R.id.name);
        casi = findViewById(R.id.casi);
        prev = findViewById(R.id.prev);
        next = findViewById(R.id.next);
        progressBar = findViewById(R.id.progessBar);
        intent = new Intent(MainActivity.this, MyService.class);
        list = new ArrayList<>();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getList();
            }
            else {
                Toast.makeText(MainActivity.this, "can kiem tra, hien dialog xin quyen",Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            }
        }

        media_active = list.get(0).getId();
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animation);
        Music music = new Music();
        restore(music);
        toggleButtonReplay.setChecked(replay);
        boolean contain = false;
        for (int i = 0; i < list.size(); i++) {
            if (music.getId() == list.get(i).getId()) {
                contain = true;
                break;
            }
        }
        if (contain == false) {
            media_active = list.get(0).getId();
            art=MainActivity.getAlbumArt(list.get(0).getImage());
            if(art!=null)
                Glide.with(this).asBitmap().load(art).into(image);
            else
                Glide.with(this).asBitmap().load(R.drawable.music).into(image);
            name.setText(list.get(0).getName());
            casi.setText(list.get(0).getName_casi());
            intent.putExtra("media", media_active);
            toggleButton.setChecked(true);
            stopService(intent);
            startService(intent);
            pause = true;
            toggleButton.setChecked(false);
            image.clearAnimation();
            save();
            Log.d("not contain", "not contain");
        } else {
            media_active = music.getId();
            art=MainActivity.getAlbumArt(music.getImage());
            if(art!=null)
                Glide.with(this).asBitmap().load(art).into(image);
            else
                Glide.with(this).asBitmap().load(R.drawable.music).into(image);
            name.setText(music.getName());
            casi.setText(music.getName_casi());
            intent.putExtra("media", media_active);
            toggleButton.setChecked(true);
            save();
            Log.d("contain", "contain");
        }
        image.startAnimation(animation);
        if (MyService.mp != null) {
            progressBar.setProgress(MyService.mp.getCurrentPosition() / 1000);
            progressBar.setMax(MyService.mp.getDuration() / 1000);
            new Thread() {
                @Override
                public void run() {
                    for (int i = MyService.mp.getCurrentPosition(); i < MyService.mp.getDuration(); i = i + 1000) {
                        try {
                            progressBar.setProgress(MyService.mp.getCurrentPosition() / 1000);
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            break;
                        }
                    }
                }
            }.start();
            Log.d("mp not null", "mp not null");
        }
        if (pause == true) {
            stopService(intent);
            startService(intent);
            pause = true;
            toggleButton.setChecked(false);
            image.clearAnimation();
        } else {
            if (MyService.mp == null) {
                stopService(intent);
                startService(intent);
                Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, media_active);
                try {
                    MyService.mp=new MediaPlayer();
                    MyService.mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    MyService.mp.setDataSource(getApplicationContext(),trackUri);
                    MyService.mp.prepare();
                } catch (Exception e) {
                    Log.e("MUSIC SERVICE", "Error starting data source", e);
                }
                pause = true;
                toggleButton.setChecked(false);
                image.clearAnimation();

                Log.d("mp null", "mp null");
            }
        }
        save();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        RecycleMusicAdapter recycleMusicAdapter = new RecycleMusicAdapter(this, list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(recycleMusicAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                pause = true;
                toggleButton.setChecked(false);
                MyService.mp.stop();
                Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, list.get(position).getId());
                try {
                    MyService.mp=new MediaPlayer();
                    MyService.mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    MyService.mp.setDataSource(getApplicationContext(),trackUri);
                    MyService.mp.prepare();
                } catch (Exception e) {
                    Log.e("MUSIC SERVICE", "Error starting data source", e);
                }
                media_active = list.get(position).getId();
                art=MainActivity.getAlbumArt(list.get(position).getImage());
                if(art!=null)
                    Glide.with(MainActivity.this).asBitmap().load(art).into(image);
                else
                    Glide.with(MainActivity.this).asBitmap().load(R.drawable.music).into(image);
                name.setText(list.get(position).getName());
                casi.setText(list.get(position).getName_casi());
                toggleButton.setChecked(true);
                pause = false;
                save();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        toggleButtonReplay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    replay = true;
                    save();
                } else {
                    replay = false;
                    save();
                }
            }
        });
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (MyService.mp != null) {
                        MyService.mp.start();
                        pause = false;
                        progressBar.setMax(MyService.mp.getDuration() / 1000);
                        new Thread() {
                            @Override
                            public void run() {
                                for (int i = MyService.mp.getCurrentPosition(); i < MyService.mp.getDuration(); i = i + 1000) {
                                    try {
                                        progressBar.setProgress(MyService.mp.getCurrentPosition() / 1000);
                                        Thread.sleep(1000);
                                    } catch (InterruptedException ex) {
                                        break;
                                    }
                                }
                            }
                        }.start();
                        image.startAnimation(animation);
                        MyService.mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                if (replay == true) {
                                    replay();
                                } else
                                    autoPlay();
                            }
                        });
                        save();
                    }
                } else {
                    if (MyService.mp != null) {
                        MyService.mp.pause();
                        pause = true;
                        image.clearAnimation();
                        save();
                    }
                }
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause = true;
                toggleButton.setChecked(false);
                for (int i = 0; i < list.size(); i++) {
                    if (media_active == list.get(i).getId()) {
                        if (i == 0)
                            media_active = list.get(list.size() - 1).getId();
                        else
                            media_active = list.get(i - 1).getId();
                        break;
                    }
                }
                for (int i = 0; i < list.size(); i++) {
                    if (media_active == list.get(i).getId()) {
                        MyService.mp.stop();
                        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, list.get(i).getId());
                        try {
                            MyService.mp=new MediaPlayer();
                            MyService.mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            MyService.mp.setDataSource(getApplicationContext(),trackUri);
                            MyService.mp.prepare();
                        } catch (Exception e) {
                            Log.e("MUSIC SERVICE", "Error starting data source", e);
                        }
                        media_active = list.get(i).getId();
                        art=MainActivity.getAlbumArt(list.get(i).getImage());
                        if(art!=null)
                            Glide.with(MainActivity.this).asBitmap().load(art).into(image);
                        else
                            Glide.with(MainActivity.this).asBitmap().load(R.drawable.music).into(image);
                        name.setText(list.get(i).getName());
                        casi.setText(list.get(i).getName_casi());
                        toggleButton.setChecked(true);
                        pause = false;
                        save();
                        break;
                    }
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause = true;
                toggleButton.setChecked(false);
                for (int i = 0; i < list.size(); i++) {
                    if (media_active == list.get(i).getId()) {
                        if (i == list.size() - 1)
                            media_active = list.get(0).getId();
                        else
                            media_active = list.get(i + 1).getId();
                        break;
                    }
                }
                for (int i = 0; i < list.size(); i++) {
                    if (media_active == list.get(i).getId()) {
                        MyService.mp.stop();
                        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, list.get(i).getId());
                        try {
                            MyService.mp=new MediaPlayer();
                            MyService.mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            MyService.mp.setDataSource(getApplicationContext(),trackUri);
                            MyService.mp.prepare();
                        } catch (Exception e) {
                            Log.e("MUSIC SERVICE", "Error starting data source", e);
                        }
                        media_active = list.get(i).getId();
                        art=MainActivity.getAlbumArt(list.get(i).getImage());
                        if(art!=null)
                            Glide.with(MainActivity.this).asBitmap().load(art).into(image);
                        else
                            Glide.with(MainActivity.this).asBitmap().load(R.drawable.music).into(image);
                        name.setText(list.get(i).getName());
                        casi.setText(list.get(i).getName_casi());
                        toggleButton.setChecked(true);
                        pause = false;
                        save();
                        break;
                    }
                }

            }
        });
    }
    public static byte[] getAlbumArt(String uri){
        MediaMetadataRetriever mediaMetadataRetriever=new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(uri);
        byte [] art=mediaMetadataRetriever.getEmbeddedPicture();
        mediaMetadataRetriever.release();
        return art;
    }
    private void getList(){
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            // Get columns
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int idAlbum = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do {
                int thisId = musicCursor.getInt(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist=musicCursor.getString(artistColumn);
                String thisAlbum=musicCursor.getString(idAlbum);
                list.add(new Music(thisId,thisAlbum,thisTitle,thisArtist));
                Log.d("list",thisAlbum);
            }
            while (musicCursor.moveToNext());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onDestroy() {
        if (pause == true) {
            stopService(intent);
        } else {
            showNotification(getNotification());
        }
        save();
        super.onDestroy();
    }

    public void replay() {
        if (MyService.mp != null) {
            toggleButton.setChecked(false);
            toggleButton.setChecked(true);
            MyService.mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (replay == true) {
                        replay();
                    } else
                        autoPlay();
                }
            });
        }
    }

    public void autoPlay() {
        pause = true;
        toggleButton.setChecked(false);
        for (int i = 0; i < list.size(); i++) {
            if (media_active == list.get(i).getId()) {
                if (i == list.size() - 1)
                    media_active = list.get(0).getId();
                else
                    media_active = list.get(i + 1).getId();
                break;
            }
        }
        for (int i = 0; i < list.size(); i++) {
            if (media_active == list.get(i).getId()) {
                MyService.mp.stop();
                Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, list.get(i).getId());
                try {
                    MyService.mp=new MediaPlayer();
                    MyService.mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    MyService.mp.setDataSource(getApplicationContext(),trackUri);
                    MyService.mp.prepare();
                } catch (Exception e) {
                    Log.e("MUSIC SERVICE", "Error starting data source", e);
                }

                media_active = list.get(i).getId();
                art=MainActivity.getAlbumArt(list.get(i).getImage());
                if(art!=null)
                    Glide.with(this).asBitmap().load(art).into(image);
                else
                    Glide.with(this).asBitmap().load(R.drawable.music).into(image);
                name.setText(list.get(i).getName());
                casi.setText(list.get(i).getName_casi());
                toggleButton.setChecked(true);
                pause = false;
                save();
                break;
            }
        }
        MyService.mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                autoPlay();
            }
        });
    }
    public void save() {
        SharedPreferences sharedPreferences = getSharedPreferences("music.txt", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String img = "";
        String name = "";
        String casi = "";
        int media = -1;
        for (int i = 0; i < list.size(); i++) {
            if (media_active == list.get(i).getId()) {
                img = list.get(i).getImage();
                name = list.get(i).getName();
                casi = list.get(i).getName_casi();
                media = list.get(i).getId();
                break;
            }
        }
        editor.putString("img", img);
        editor.putString("name", name);
        editor.putString("casi", casi);
        editor.putInt("media", media);
        editor.putBoolean("pause", pause);
        editor.putBoolean("replay", replay);
        editor.commit();
    }

    public void restore(Music music) {
        SharedPreferences sharedPreferences = getSharedPreferences("music.txt", MODE_PRIVATE);
        music.setId(sharedPreferences.getInt("id", -1));
        music.setImage(sharedPreferences.getString("img", ""));
        music.setName(sharedPreferences.getString("name", ""));
        music.setName_casi(sharedPreferences.getString("casi", ""));
        music.setId(sharedPreferences.getInt("media", -1));
        pause = sharedPreferences.getBoolean("pause", false);
        replay = sharedPreferences.getBoolean("replay", false);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showNotification(Notification notification) {
        Intent notificationIntent = new Intent(this, MyReciever.class);
        notificationIntent.putExtra("noti", notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, pendingIntent);
    }

    private Notification getNotification() {
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification);
        notificationLayout.setOnClickPendingIntent(R.id.play_noti,onButtonNotificationClick(R.id.play_noti));
        notificationLayout.setOnClickPendingIntent(R.id.pause_noti,onButtonNotificationClick(R.id.pause_noti));
        notificationLayout.setOnClickPendingIntent(R.id.prev_noti,onButtonNotificationClick(R.id.prev_noti));
        notificationLayout.setOnClickPendingIntent(R.id.next_noti,onButtonNotificationClick(R.id.next_noti));

        Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(),1,resultIntent,PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notification-id");
        builder.setSmallIcon(R.drawable.ic_music);
        builder.setContentTitle("Music is playing");
        builder.setCustomBigContentView(notificationLayout);
        builder.setAutoCancel(true);
        builder.setContentIntent(resultPendingIntent);
        builder.setChannelId("Channel");
        return builder.build();
    }
    private PendingIntent onButtonNotificationClick(@IdRes int id) {
        Intent intent = new Intent(this,MyReciever2.class);
        intent.putExtra("btn", id);
        return PendingIntent.getBroadcast(this, id, intent, 0);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "xin duoc quyen roi", Toast.LENGTH_SHORT).show();
        }
    }

}


