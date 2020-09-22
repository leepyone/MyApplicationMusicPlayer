package com.surewang.myapplication06musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    private final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private ContentResolver  contentResolver;
    private ListView playList;
    private MediaCursorAdapter mediaCursorAdapter ;
    private final String SELECTION = MediaStore.Audio.Media.IS_MUSIC + "= ? "+" AND " +MediaStore.Audio.Media.MIME_TYPE+" LIKE ?";
    private final String[] SELECTION_ARGS={ Integer.toString(1),"audio/mpeg"};
    //底部导航栏的相关控件
    private BottomNavigationView navigation;
    private TextView tvBottomTitle;
    private TextView tvBottomAuthor;
    private ImageView ivAlbumThumbnail;
    private ImageView ivPlay;
    //play
    private MediaPlayer mediaPlayer = null;
    private static final String TAG = "MyActivity";
    public static final String DATA_URI = "com.wangshuo.data_uri";


    //item click
    private ListView.OnItemClickListener itemClickListener = new ListView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cursor = mediaCursorAdapter.getCursor();
            if(cursor!=null && cursor.moveToPosition(position)){

                int dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int albumIdIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                long albumId = cursor.getLong(albumIdIndex);
                navigation.setVisibility(View.VISIBLE);

                //设置底部导航栏的信息
                int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

                String title = cursor.getString(titleIndex);
                String artist = cursor.getString(artistIndex);
                Uri albumUri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,albumId);

//                Cursor albumCursor = contentResolver.query(albumUri,null,null,null,null);
                Bitmap thumbnail = null;
                try {
                     thumbnail = contentResolver.loadThumbnail(albumUri, new Size(100,100), null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ivAlbumThumbnail.setImageBitmap(thumbnail);
//                if(albumCursor!=null && albumCursor.getCount()>0){
//
//                    albumCursor.moveToFirst();
//                    int albumArtIndex = albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
//                    String albumArt = albumCursor.getString(albumArtIndex);
//                    //图像的显示
//
//                    Glide.with(MainActivity.this).load(albumArt).into(ivAlbumThumbnail);
//
//                    albumCursor.close();
//                }


                if(tvBottomTitle!=null){
                    tvBottomTitle.setText(title);

                }
                if(tvBottomAuthor!=null){
                    tvBottomAuthor.setText(artist);
                }

                String data = cursor.getString(dataIndex);
                Uri dataUri = Uri.parse(data);

                if(mediaPlayer!=null){
                    try{
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(MainActivity.this,dataUri);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    };

    private boolean pause ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contentResolver = getContentResolver();
        mediaCursorAdapter = new MediaCursorAdapter(MainActivity.this);

        bind();

        //权限的检测和申请
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){

            }else {
                requestPermissions(PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        }else{
              initPlayList();
        }
        playList =findViewById(R.id.lv_songList);
        playList.setAdapter(mediaCursorAdapter);
        playList.setOnItemClickListener(itemClickListener);
    }

    @Override
    protected void onStart(){
        super.onStart();
        if(mediaPlayer==null)
            mediaPlayer = new MediaPlayer();


    }

    @Override
    protected void  onStop(){

        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer =null;
            Log.d(TAG,"onStop invoked!");

        }
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){

        switch (requestCode){
            case REQUEST_EXTERNAL_STORAGE:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    initPlayList();
                }
                break;
            default:
                break;
        }

    }
    private void bind(){

        navigation = findViewById(R.id.navigation);
        LayoutInflater.from(MainActivity.this).inflate(R.layout.bottom_media_toolbar,navigation,true);

        ivPlay = findViewById(R.id.iv_play);
        tvBottomTitle =navigation.findViewById(R.id.tv_bottom_title);
        tvBottomAuthor = navigation.findViewById(R.id.tv_bottom_author);
        ivAlbumThumbnail = navigation.findViewById(R.id.iv_thumbnail);

        if(ivPlay!=null)
            ivPlay.setOnClickListener(MainActivity.this);
        navigation.setVisibility(View.GONE);
    }
    public void initPlayList() {

        Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, SELECTION, SELECTION_ARGS, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        mediaCursorAdapter.swapCursor(cursor);
        mediaCursorAdapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(View v) {
        ListView listView =v.findViewById(R.id.lv_songList);
//        ImageView play

    }
}