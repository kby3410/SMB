package com.example.smb;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import static android.provider.Settings.ACTION_SETTINGS;

public class Usblist extends AppCompatActivity implements View.OnClickListener {
    String current,title;
    private UsbRecyclerAdapter Usbadapter;
    private ArrayList<String> list = new ArrayList<>();
    private ArrayList<String> select = new ArrayList<>();
    private ArrayList<UsbData> filelist = new ArrayList<>();
    int File_image;
    int size = 0;
    private ImageView LanCheck;
    private Button set_button;
    private TextView TIME1, TIME2;
    private VideoView Usb_Videoview;
    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    private volatile boolean connect = false;
    private volatile boolean check = false;
    private volatile boolean running = true;
    public static ArrayList<Activity> actList = new ArrayList<Activity>();

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        actList.add(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        ActionBar actionBar = getSupportActionBar();    //액션바 숨기기
        actionBar.hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        init();
        LanCheck = (ImageView) findViewById(R.id.lancheck);
        set_button = (Button) findViewById(R.id.setting);
        TIME1 = (TextView) findViewById(R.id.time1);
        TIME2 = (TextView) findViewById(R.id.time2);
        Usb_Videoview = (VideoView) findViewById(R.id.videoView);
        ethernet();
        Thread();
        String k = String.valueOf(Environment.getStorageDirectory());
        File j  = new File(k);
        File Usb_list[] = j.listFiles();
        size = Usb_list.length;
        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(homeKeyReceiver,filter);

        set_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent setting_intent = new Intent(ACTION_SETTINGS);
                startActivity(setting_intent);
            }
        });
    }

    void Thread(){
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (running) {
                    runOnUiThread(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.R)
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
                            //checkwifi();
                            getfilelist();

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
                        startActivity(new Intent(Usblist.this,MainActivity.class));
                        terminate();
                    }
                }
            }
        }
    };

    public void terminate() {
        running = false;
    }



    @RequiresApi(api = Build.VERSION_CODES.R)
    public void getfilelist(){

        String k = String.valueOf(Environment.getStorageDirectory());
        File j  = new File(k);
        File Usb_list[] = j.listFiles();
        String[] array = current.split("/");
        check = false;
        connect = false;

        if (array.length == 2) {
            if (size == Usb_list.length) {
                System.out.println("size = " + size);

                for (int jk = 0; jk < Usb_list.length; jk++) {
                    String[] usb_array = String.valueOf(Usb_list[jk]).split("/");

                    for (int kk = 0; kk < list.size(); kk++) {
                        if (usb_array[2].equals(list.get(kk))) {
                            System.out.println("usb list = " + list.get(kk));
                            check = true;
                        }
                    }
                }
                if (!check) {
                    terminate();
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);    //다이얼로그
                    alert.setTitle("알림");
                    alert.setMessage("USB가 존재하지 않습니다.");
                    alert.setCancelable(false);
                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            for(int i = 0; i < actList.size(); i++) {
                                actList.get(i).finish();
                            }
                            dialog.dismiss();
                            startActivity(new Intent(Usblist.this, MainActivity.class));
                        }
                    });
                    alert.show();
                }
            } else {
                terminate();
                AlertDialog.Builder alert = new AlertDialog.Builder(this);    //다이얼로그
                alert.setTitle("알림");
                alert.setMessage("USB가 연결 or 해제 되었습니다.");
                alert.setCancelable(false);
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        for(int i = 0; i < actList.size(); i++) {
                            actList.get(i).finish();
                        }
                        dialog.dismiss();
                        startActivity(new Intent(Usblist.this, MainActivity.class));

                    }
                });
                alert.show();
            }
        }


        if(array.length > 2) {
            System.out.println("for문 시작");
            for (int i = 0; i < Usb_list.length; i++) {
                String[] usb_array = String.valueOf(Usb_list[i]).split("/");
                if (usb_array[2].equals(array[2])) {
                    connect = true;
                    break;
                }
            }
            if (connect == false){
                terminate();
                AlertDialog.Builder alert = new AlertDialog.Builder(this);    //다이얼로그
                alert.setTitle("알림");
                alert.setMessage("USB 연결이 끊겼습니다.");
                alert.setCancelable(false);
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        for(int i = 0; i < actList.size(); i++) {
                            actList.get(i).finish();
                        }
                        dialog.dismiss();
                        startActivity(new Intent(Usblist.this,MainActivity.class));
                    }
                });
                alert.show();
            }
        }
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        terminate();
    }

    @Override
    protected  void onPause() {
        super.onPause();
       // videostart();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onRestart() {
        super.onRestart();
        RecyclerView recyclerView = findViewById(R.id.Recycle);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        Usbadapter = new UsbRecyclerAdapter(filelist,this);
        recyclerView.setAdapter(Usbadapter);
    }

        @Override
    public void onClick(View view) {

    }

    private void init() {
        RecyclerView recyclerView = findViewById(R.id.Recycle);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        Usbadapter = new UsbRecyclerAdapter(filelist,this);
        recyclerView.setAdapter(Usbadapter);
        RecyclerDecoration spaceDecoration = new RecyclerDecoration(1);
        recyclerView.addItemDecoration(spaceDecoration);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), new LinearLayoutManager(this).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        Intent intent = getIntent();
        list.clear();
        select.clear();
        title = intent.getStringExtra("title");
        current = intent.getStringExtra("current");
        list = intent.getStringArrayListExtra("list");
        for(int i = 0; i < list.size(); i++){
            if (list.get(i).endsWith("mp4") || list.get(i).endsWith("avi") )
            {
                File_image = R.drawable.video_1;
            }
            else if (list.get(i).endsWith("jpg") || list.get(i).endsWith("jpeg") ||
                    list.get(i).endsWith("JPG") || list.get(i).endsWith("gif") ||
                    list.get(i).endsWith("png") || list.get(i).endsWith("bmp"))
            {
                File_image = R.drawable.image_1;
            }
            else if (list.get(i).endsWith("txt"))
            {
                File_image = R.drawable.txt_1;
            }
            else if (list.get(i).endsWith("doc") || list.get(i).endsWith("docx"))
            {
                File_image = R.drawable.doc_1;
            }
            else if (list.get(i).endsWith("xls") || list.get(i).endsWith("xlsx"))
            {
                File_image = R.drawable.xls_1;
            }
            else if (list.get(i).endsWith("ppt") || list.get(i).endsWith("pptx"))
            {
                File_image = R.drawable.ppt_1;
            }
            else if (list.get(i).endsWith("pdf")) {
                File_image = R.drawable.pdf_1;
            }
            else if (list.get(i).endsWith("hwp")) {
                File_image = R.drawable.hwp_1;
            }else {
                File_image = R.drawable.file_1;
            }
            UsbData save_data = new UsbData(list.get(i), current, File_image);
            filelist.add(i, save_data);
        }
        Usbadapter.notifyDataSetChanged();
    }

   /* void checkwifi(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ninfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        //ni = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET); //유선랜 상태
        if(ninfo.isConnected()){
            List_Wifi.setBackgroundResource(R.drawable.wifi_on);
        }else{
            List_Wifi.setBackgroundResource(R.drawable.wifi_off);
        }
    }*/

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
