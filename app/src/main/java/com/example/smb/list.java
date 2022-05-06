package com.example.smb;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.icu.text.SymbolTable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.provider.Settings.ACTION_SETTINGS;

public class list extends AppCompatActivity implements View.OnClickListener {

    String current;
    String id;
    String pass;
    String s_current;
    String s_id;
    String s_pass;
    ArrayList<String> s_list;
    private RecyclerAdapter adapter;
    private ArrayList<String> list = new ArrayList<>();
    private ArrayList<String> select = new ArrayList<>();
    private ArrayList<Data> filelist = new ArrayList<>();
    int File_image;
    private ImageView LanCheck;
    private Button set_button;
    private TextView TIME1, TIME2;
    private VideoView list_Videoview;
    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    private volatile boolean running = true;
    public static ArrayList<Activity> actList = new ArrayList<Activity>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        actList.add(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        ActionBar actionBar = getSupportActionBar();    //액션바 숨기기
        actionBar.hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        RecyclerView Recycle = (RecyclerView)findViewById(R.id.Recycle);
        RecyclerDecoration spaceDecoration = new RecyclerDecoration(1);
        Recycle.addItemDecoration(spaceDecoration);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(Recycle.getContext(), new LinearLayoutManager(this).getOrientation());
        Recycle.addItemDecoration(dividerItemDecoration);

        init();
        getfilelist();
        LanCheck = (ImageView) findViewById(R.id.lancheck);
        set_button = (Button) findViewById(R.id.setting);
        TIME1 = (TextView) findViewById(R.id.time1);
        TIME2 = (TextView) findViewById(R.id.time2);
        list_Videoview = (VideoView) findViewById(R.id.videoView);
        ethernet();
        Thread();

        if(savedInstanceState==null) {

        } else {
            current = savedInstanceState.getString("current");
            id = savedInstanceState.getString("id");
            pass = savedInstanceState.getString("pass");
            list = savedInstanceState.getStringArrayList("list");
            System.out.println("s_current = " + current);
        }

        set_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent setting_intent = new Intent(ACTION_SETTINGS);
                startActivity(setting_intent);
            }
        });

        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(homeKeyReceiver,filter);
    }

    public void terminate() {
        running = false;
    }

    void Thread(){
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (running) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            long now = System.currentTimeMillis();
                            Date date = new Date(now);
                            SimpleDateFormat time1 = new SimpleDateFormat("HH:mm:ss     a", Locale.ENGLISH);
                            SimpleDateFormat time2 = new SimpleDateFormat("dd/MM/yyyy    EEEE", Locale.ENGLISH);
                            String formatDate1 = time1.format(date);
                            String formatDate2 = time2.format(date);
                            TIME1.setText(formatDate1);
                            TIME2.setText(formatDate2);
                            ethernet();
                            netWork();
                        }
                    });
                    try {
                        Thread.sleep(1000); // 1000 ms = 1초
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } // while
            } // run()
        }; // new Thread() { };
        thread.start();
    }

    void netWork(){
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ninfo = cm.getActiveNetworkInfo();
        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if(ninfo == null){
            terminate();
            AlertDialog.Builder alert = new AlertDialog.Builder(this);    //다이얼로그
            alert.setTitle("알림");
            alert.setMessage("인터넷 연결이 끊겼습니다.");
            alert.setCancelable(false);
            alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    for(int i = 0; i < actList.size(); i++) {
                        actList.get(i).finish();
                    }
                    dialog.dismiss();
                    startActivity(new Intent(list.this,MainActivity.class));
                }
            });
            alert.show();
        }else{

        }
    }

    public void getfilelist(){

        Intent intent = getIntent();
        list.clear();
        select.clear();
        current = intent.getStringExtra("ip");
        id = intent.getStringExtra("id");
        pass = intent.getStringExtra("pass");
        list = intent.getStringArrayListExtra("list");

        for(int i = 0; i < list.size(); i++) {
            if(list.get(i).endsWith("mp4") || list.get(i).endsWith("avi") || list.get(i).charAt(list.get(i).length()-1) == '/' || list.get(i).endsWith("jpg") || list.get(i).endsWith("jpeg") ||
                    list.get(i).endsWith("JPG") || list.get(i).endsWith("png") || list.get(i).endsWith("bmp") || list.get(i).endsWith("txt") ||
                    list.get(i).endsWith("doc") || list.get(i).endsWith("docx") || list.get(i).endsWith("xls") ||  list.get(i).endsWith("xlsx") ||
                    list.get(i).endsWith("hwp") ||
                    list.get(i).endsWith("ppt") || list.get(i).endsWith("pptx") || list.get(i).endsWith("pdf")){
                select.add(list.get(i));
            }
        }

        for(int i = 0; i < select.size(); i++){
            if(select.get(i).charAt(select.get(i).length()-2) == '$')  {
                select.remove(i);
                i--;
            }
        }
        for(int i = 0; i < select.size(); i++){
            if (select.get(i).endsWith("mp4") || select.get(i).endsWith("avi") )
            {
                File_image = R.drawable.video_1;
            }
            else if (select.get(i).charAt(select.get(i).length()-1) == '/')
            {
                File_image = R.drawable.file_1;
            }
            else if (select.get(i).endsWith("jpg") || select.get(i).endsWith("jpeg") ||
                    select.get(i).endsWith("JPG") || select.get(i).endsWith("gif") ||
                    select.get(i).endsWith("png") || select.get(i).endsWith("bmp"))
            {
                File_image = R.drawable.image_1;
            }
            else if (select.get(i).endsWith("txt"))
            {
                File_image = R.drawable.txt_1;
            }
            else if (select.get(i).endsWith("doc") || select.get(i).endsWith("docx"))
            {
                File_image = R.drawable.doc_1;
            }
            else if (select.get(i).endsWith("xls") || select.get(i).endsWith("xlsx"))
            {
                File_image = R.drawable.xls_1;
            }
            else if (select.get(i).endsWith("ppt") || select.get(i).endsWith("pptx"))
            {
                File_image = R.drawable.ppt_1;
            }
            else if (select.get(i).endsWith("pdf")) {
                File_image = R.drawable.pdf_1;
            }
            else if (select.get(i).endsWith("hwp")) {
                File_image = R.drawable.hwp_1;
            }
            Data save_data = new Data(select.get(i), current, id, pass, File_image);
            filelist.add(i, save_data);
        }
        adapter.notifyDataSetChanged();
    }

    private BroadcastReceiver homeKeyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                        for(int i = 0; i < actList.size(); i++) {
                            actList.get(i).finish();
                        }
                        startActivity(new Intent(list.this,MainActivity.class));
                        terminate();
                    }
                }
            }
        }
    };

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        outState.putString("current", current);
        outState.putString("id", id);
        outState.putString("pass", pass);
        outState.putStringArrayList("list", list);
        super.onSaveInstanceState(outState);
    }

   /* @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        test2 = savedInstanceState.getInt("current");
        System.out.println("test2 = " + test2);
    }*/

    @Override
    protected  void onPause() {
        super.onPause();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        terminate();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        init();
        //getfilelist();
    }

    @Override
    public void onClick(View view) {

    }

    private void init() {
        RecyclerView recyclerView = findViewById(R.id.Recycle);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new RecyclerAdapter(filelist,this);
        recyclerView.setAdapter(adapter);
    }

    void ethernet(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ninfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET); //유선랜 상태
        if(ni.isConnected()){
            if (getResources().getConfiguration().orientation ==
                    Configuration.ORIENTATION_PORTRAIT) {
                LanCheck.setBackgroundResource(R.drawable.p_ethernet_check);
            } else {
                LanCheck.setBackgroundResource(R.drawable.ethernet_check);
            }

        }else if(ninfo.isConnected()){
            if (getResources().getConfiguration().orientation ==
                    Configuration.ORIENTATION_PORTRAIT) {
                LanCheck.setBackgroundResource(R.drawable.wifi_check_1);
            } else {
                LanCheck.setBackgroundResource(R.drawable.wifi_check);
            }

        }else {
            LanCheck.setBackgroundResource(0);
        }
    }
}
