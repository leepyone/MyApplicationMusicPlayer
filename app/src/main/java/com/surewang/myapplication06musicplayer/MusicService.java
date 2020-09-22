package com.surewang.myapplication06musicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.io.IOException;

public class MusicService extends Service {

    private NotificationManager notificationManager;
    private static final int ONGOING_NOTIFICATION_ID =1001;
    private static final String CHANNEL_ID = "Music Channel";
    private MediaPlayer mediaPlayer;
    private final IBinder binder = new MusicServiceBinder();

    public MusicService() {
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        String data = intent.getStringExtra(MainActivity.DATA_URI);
        String title = intent.getStringExtra(MainActivity.TITLE);
        String artist = intent.getStringExtra(MainActivity.ARTIST);
        Uri dataUri = Uri.parse(data);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,"Music Channel",NotificationManager.IMPORTANCE_HIGH);
            if(notificationManager !=null){
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        Intent notificationIntent = new Intent(getApplicationContext(),MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,notificationIntent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID);

        Notification notification =
                builder.setContentTitle(title).setContentText(artist).setSmallIcon(R.drawable.ic_launcher_foreground).setContentIntent(pendingIntent).build();

        startForeground(ONGOING_NOTIFICATION_ID,notification);

        if(mediaPlayer!=null){
            try{
                mediaPlayer.reset();
                mediaPlayer.setDataSource(getApplicationContext(),dataUri);
                mediaPlayer.prepare();
                mediaPlayer.start();

                Intent musicStartIntent = new Intent(MainActivity.ACTION_MUSIC_START);
                sendBroadcast(musicStartIntent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public void onDestroy(){
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        super.onDestroy();
    }

    public class MusicServiceBinder extends Binder{


        MusicService getService(){
            return  MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;

    }
    public void pause(){
        if(mediaPlayer!=null)
            mediaPlayer.pause();
    }

    public void play(){
        if(mediaPlayer!=null)
            mediaPlayer.start();
    }

    public int getDuration(){
        int duration = 0;
        if(mediaPlayer!=null)
            duration = mediaPlayer.getDuration();
        return  duration;
    }

    public int getCurrentPosition(){
        int position = 0;
        if(mediaPlayer !=null)
            position = mediaPlayer.getCurrentPosition();
        return position;

    }
    public boolean isPlaying(){
        if(mediaPlayer!=null)
            return mediaPlayer.isPlaying();
        return false;
    }

}
