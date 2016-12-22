package com.example.shana.iodemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.media.audiofx.BassBoost;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FileChioce extends Activity implements View.OnClickListener {
    private ListView lv;
    private List<Map<String,Object>> list;
    private File RootPath;
    private String CurrentDir;//设置当前目录
    private EditText et_currentDir;
    private Button btn_jump,btn_back;
    private String[] MediaType={".mp4",".mp3",".flac",".rmvb",".mov",".avi",".3gp"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_chioce);
        intView();
        RootPath=Environment.getExternalStorageDirectory();
        initFolder(RootPath);
    }

    private void initFolder(File file) {
        CurrentDir=file.getPath();
        et_currentDir.setText(CurrentDir);
        list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map;
        File []files=file.listFiles();
        for(File ofile:files){
            map=new HashMap<String,Object>();
            String name=ofile.getName();

//            Toast.makeText(this,ofile.getName(),Toast.LENGTH_SHORT).show();
            if(ofile.isFile()){
                int start=name.lastIndexOf(".");
                String endName=name.substring(start);//获取文件后缀名比对是否为媒体资源
                for(String end:MediaType){
                    if(endName.equals(end))
                        map.put("name",name);
                        map.put("isFile","0");
                }
            }else{
                map.put("name",name);
                map.put("isFile","1");
            }
            list.add(map);
        }
        lv.setAdapter(new MyAdapter(list,this));

    }
    private void intView() {
        lv= (ListView) findViewById(R.id.lv);
        et_currentDir= (EditText) findViewById(R.id.et_currentDir);
        btn_jump= (Button) findViewById(R.id.btn_jump);
        btn_back= (Button) findViewById(R.id.btn_back);
        btn_jump.setOnClickListener(this);
        btn_back.setOnClickListener(this);
        et_currentDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_currentDir.setFocusable(true);
               et_currentDir.setFocusableInTouchMode(true);
                et_currentDir.requestFocus();
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String name=((TextView)view.findViewById(R.id.tv_file)).getText().toString();//获取点击的文件名字
                File file=new File(CurrentDir+File.separator+name);//将点击对象转换成一个File对象
                if(file.isFile()){
                    //是文件的话直接打开
                    Intent intent=new Intent(FileChioce.this,MainActivity.class);//跳转到播放器界面并执行播放
                    /*
                    *跳转的时候发送一个文件路径数据
                     */
                    Bundle bundle=new Bundle();
                    bundle.putString("path",file.getPath());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }else{
                    initFolder(file);
                    //是文件夹的话打开下一级
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_jump:

                String name=et_currentDir.getText().toString().trim();
                if(name.length()>=Environment.getExternalStorageDirectory().toString().length()){
                    File file=new File(name);//
                    if(file.exists()){
                        if(file.isFile()){
                            //是媒体资源文件的话直接打开
                            Intent intent=new Intent(FileChioce.this,MainActivity.class);//跳转到播放器界面并执行播放
                    /*
                    *跳转的时候发送一个文件路径数据
                     */
                            Bundle bundle=new Bundle();
                            bundle.putString("path",file.getPath());
                            intent.putExtras(bundle);
                        }else{
                            initFolder(file);
                            //是文件夹的话打开下一级
                        }
                    }else{
                        Toast.makeText(this,"文件不存在或者输入路径有误",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.btn_back:
//                比对的是字符串
                if(!CurrentDir.toString().equals(Environment.getExternalStorageDirectory().toString())){//当前目录不为根目录的时候返回是有效的
                    String backname=CurrentDir.substring(0,CurrentDir.lastIndexOf("/"));//通过匹配最后一个/去获取上一级的路径
                    initFolder(new File(backname));
                }
                break;
        }




    }

    class MyAdapter extends BaseAdapter{
        List<Map<String,Object>> list;
        Context mContext;
        public MyAdapter( List<Map<String,Object>> list, Context mContext){
            this.list=list;
            this.mContext=mContext;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            LayoutInflater mInflater=LayoutInflater.from(mContext);
            if(view==null){
                holder=new ViewHolder();
                view= mInflater.inflate(R.layout.listview_temp,null);
                holder.img_floder= (ImageView) view.findViewById(R.id.img_folder);
                holder.img_music= (ImageView) view.findViewById(R.id.img_music);
                holder.file= (TextView) view.findViewById(R.id.tv_file);
                view.setTag(holder);
            }else {
                holder= (ViewHolder) view.getTag();
            }
                holder.file.setText(list.get(i).get("name").toString());
               ;
                if(list.get(i).get("isFile").toString().equals("0")){
                    holder.img_floder.setVisibility(View.GONE);
                }else{
                    holder.img_music.setVisibility(View.GONE);
                }
            return view;
        }
        class ViewHolder{
            ImageView img_floder;
            ImageView img_music;
            TextView file;
        }
    }
}
