package com.example.shana.iodemo;


import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import android.net.Uri;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.os.Handler;

import java.io.IOException;
import java.net.URI;
import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private SurfaceView sv;
    private MediaPlayer player;
    private TextView play, tv_playtime, tv_alltime,title;
    private SurfaceHolder holder;
    private Timer timer;
    private Handler handler;
    private SeekBar seekBar;
    private int vHeight,vWidth,cHeight,cWidth;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取设备的的屏幕大小
        DisplayMetrics dp=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dp);
        cHeight= dp.heightPixels;
        cWidth= dp.widthPixels;
        initView();
        setSurfaceView();
        getResourcesPath();//获取播放资源
        updateView();

    }

    private void getResourcesPath() {
        String mPath = "";
        Intent intend = getIntent();
        Uri uri = intend.getData();
        if (uri != null) {
        } else {
            Bundle data = getIntent().getExtras();
            if (data != null) {
                String path = data.getString("path");
                if (path != null && !"".equals(path)) {
                    mPath = path;
                }
            }
        }
        try {
            if (!"".equals(mPath)) {
                player.setDataSource(mPath);//这儿涉及一个生命周期问题,必须是先设置好资源才能去prepare播放
                player.prepareAsync();
            } else {
                AssetFileDescriptor afd = this.getResources().openRawResourceFd(R.raw.papi1);
                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
                player.prepareAsync();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*************************prepare完之后执行的方法******************************/
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                //根据视频与设备的屏幕大小比对，进行缩放操作
                vWidth=player.getVideoWidth();
                vHeight=player.getVideoHeight();
                if(vWidth>cWidth||vHeight>cHeight){
                    float mW=vWidth/cWidth;
                    float mH=vHeight/cWidth;
                    float ex=Math.max(mH,mW);//选择倍数大的进行缩放
                    vWidth= (int) Math.ceil(vWidth/ex);//向上取整
                    vHeight= (int) Math.ceil(vHeight/ex);
                    sv.setLayoutParams(new LinearLayout.LayoutParams(vWidth,vHeight));
                }
                player.start();
                play.setBackgroundResource(R.drawable.ic_media_pause);
                timer = new Timer();
                timer.schedule(new MySeekBarTask(), 50, 500);
            }
        });
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(timer!=null){
                    timer.cancel();//播放完成后取消进度条的更新计时器
                    timer=null;
                }
            }
        });
    }

    private void updateView() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {//对时间显示和进度条进行处理
                switch (message.what) {
                    case 1:
                        Time progress = new Time(player.getCurrentPosition());//当前播放时间
                        Time allTime = new Time(player.getDuration());//视频总长度
                        String CurrentTime = progress.toString();
                        String AllTime = allTime.toString();
                        tv_alltime.setText(AllTime); //显示总时间
                        tv_playtime.setText(CurrentTime); //显示当前播放时间
                        int seekbarprogress = 0;
                        if (player.getDuration() > 0) {
                            seekbarprogress = seekBar.getMax() * player.getCurrentPosition() / player.getDuration();
                        }
                        seekBar.setProgress(seekbarprogress);
                        break;
                }
                return false;
            }
        });
    }

    //对surfaceView进行初始化和控制
    private void setSurfaceView() {
        holder = sv.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                player.setDisplay(holder);

            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });

    }

    //初始化控件
    private void initView() {
        sv = (SurfaceView) findViewById(R.id.surface);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        title= (TextView) findViewById(R.id.tv_title);
        player = new MediaPlayer();
        play = (TextView) findViewById(R.id.btn_play);
        tv_playtime = (TextView) findViewById(R.id.play_time);
        tv_alltime = (TextView) findViewById(R.id.all_time);
        seekBar.setOnSeekBarChangeListener(this);

    }

    //    播放或者暂停
    public void play(View v) throws IOException {
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
                play.setBackgroundResource(R.drawable.ic_media_play);
            } else {
                player.start();
                play.setBackgroundResource(R.drawable.ic_media_pause);
            }
        }
    }

    //    回退
    public void rew(View v) {

        if (player.isPlaying()) {
            int CurrentPosition = player.getCurrentPosition();
            if (CurrentPosition > 10000) {
                player.seekTo(CurrentPosition - 10000);
            }
        }

    }

    //    快进
    public void ff(View v) {

        if (player.isPlaying()) {
            int Duration = player.getDuration();
            int CurrentPosition = player.getCurrentPosition();
            if (CurrentPosition + 10000 < Duration) {
                player.seekTo(CurrentPosition + 10000);
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.stop();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "文件选择");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                Intent intent = new Intent(MainActivity.this, FileChioce.class);
                startActivity(intent);
                player.pause();
                play.setBackgroundResource(R.drawable.ic_media_play);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    class MySeekBarTask extends TimerTask {
        @Override
        public void run() {
            Message message = Message.obtain(handler);
            message.what = 1;
            message.sendToTarget();
        }
    }

}