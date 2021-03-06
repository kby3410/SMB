package com.example.smb;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.mylibrary.ItempApi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import static android.provider.Settings.ACTION_SETTINGS;

public class MainActivity extends AppCompatActivity {


    private DBHandler handler;
    private EditText ip,id,password;
    private Button connect,usb,Setting,Eshare;
    private ArrayList<String> fileList = new ArrayList<>();
    private ArrayList<String> IpList = new ArrayList<>();
    private ArrayList<String> IdList = new ArrayList<>();
    private ArrayList<String> PassList = new ArrayList<>();
    private DBRecyclerAdapter DBadapter;
    private ArrayList<DBData> DBfilelist = new ArrayList<>();
    private ImageView LanCheck,P_Lancheck;
    private Intent AppListIntent;
    private VideoView mVideoview;
    private TextView TIME1, TIME2;
    private Handler mHandler;
    private volatile boolean running = true;
    private ProgressDialog pDialog;
    private File targetFile;
    private static final String IP_ADDRESS = "http://www.krizer.co.kr/krizer_edit/smb1.1.apk";
    String filename = "test.apk";
    String Ip_text = "IP or SMB ????????? ?????????????????????.";
    String Id_text = "????????? or ??????????????? ?????????????????????.";
    String Usb_text = "USB??? ?????????????????????.";
    String test = "com.example.smb";
    String REBOOT_ACTION = "ads.android.setreboot.action";


    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();    //????????? ?????????
        actionBar.hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        targetFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/TEST/" + filename);
        ip = (EditText)findViewById(R.id.ip);
        id = (EditText)findViewById(R.id.id);
        password = (EditText)findViewById(R.id.password);
        connect = (Button)findViewById(R.id.connect);
        usb = (Button)findViewById(R.id.usb);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Eshare = (Button) findViewById(R.id.eshare);
        Setting = (Button) findViewById(R.id.setting);
        LanCheck = (ImageView) findViewById(R.id.lancheck);
        mVideoview = (VideoView) findViewById(R.id.videoView);
        TIME1 = (TextView) findViewById(R.id.time1);
        TIME2 = (TextView) findViewById(R.id.time2);


        checkPermission();
        init();
        DBhandler();
        ethernet();
        Thread();
        getSaveFolder("SMB");
        getSaveFolder("TEST");

        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // ??????????????? ?????? ??????
                CheckVer();
            }
        }, 6000);


        String folderName = "SMB";
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + folderName);
        getFolderSize(dir);
        double bytes = getFolderSize(dir);
        double kilobytes = (bytes / 1024);
        double megabytes = (kilobytes / 1024);
        double gigabytes = (megabytes / 1024);

        if(gigabytes > 5){
            removeDir(dir.getPath());
            getSaveFolder("SMB");
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);   //????????? ?????????

        if (isInstallApp("com.eshare.clientv2")){

        }else {
            Eshare.setBackgroundResource(0);
        }



        Setting.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent setting_intent = new Intent(ACTION_SETTINGS);
                ip.getText().clear();
                id.getText().clear();
                password.getText().clear();
                startActivity(setting_intent);
            }
        });

        Eshare.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (isInstallApp("com.eshare.clientv2")){
                    Intent youtube_Intent = getPackageManager().getLaunchIntentForPackage("com.eshare.clientv2");
                    ip.getText().clear();
                    id.getText().clear();
                    password.getText().clear();
                    startActivity(youtube_Intent);
                }else {

                }
            }
        });

        usb.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View view) {
                fileList.clear();
                String k = String.valueOf(Environment.getStorageDirectory());
                File j  = new File(k);
                File Usb_list[] = j.listFiles();
                if(j.listFiles().length == 2){
                    Handler cHandler = new Handler(Looper.getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            Dialog(Usb_text);
                        }
                    };
                    cHandler.sendEmptyMessage(0);
                }else {
                    for (int i = 0; i < j.listFiles().length; i++) {
                        assert Usb_list != null;
                        if(Usb_list[i].getName().equals("emulated") || Usb_list[i].getName().equals("self")){

                        }else {
                            System.out.println(Usb_list[i].getName());
                            fileList.add(Usb_list[i].getName());
                        }
                    }
                    Intent intent = new Intent(getApplicationContext(),Usblist.class);
                    intent.putExtra("title",k );
                    intent.putExtra("current",k + "/");
                    intent.putStringArrayListExtra("list",fileList);
                    ip.getText().clear();
                    id.getText().clear();
                    password.getText().clear();
                    startActivity(intent);
                }
            }
        });
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SMB_Thread();
            }
        });
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
                            //checkwifi();
                        }
                    });
                    try {
                        Thread.sleep(1000); // 1000 ms = 1???
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } // while
            } // run()
        }; // new Thread() { };
        thread.start();
    }



    void SMB_Thread(){
        new Thread() {

            public void run() {
                String url = "smb://";
                    NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, id.getText().toString(), password.getText().toString());
                SmbFile dir = null;
                try {
                    dir = new SmbFile(url + ip.getText(), auth);
                    fileList.clear();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    for (SmbFile f : dir.listFiles()) {
                        fileList.add(f.getName());
                    }
                    Intent intent = new Intent(getApplicationContext(), list.class);
                    intent.putExtra("ip", ip.getText().toString() + "/");
                    intent.putExtra("id", id.getText().toString());
                    intent.putExtra("pass", password.getText().toString());
                    intent.putStringArrayListExtra("list", fileList);

                    handler.insert(ip.getText().toString(), id.getText().toString(), password.getText().toString());
                    mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ip.getText().clear();
                            id.getText().clear();
                            password.getText().clear();
                        }
                    }, 0);

                    startActivity(intent);
                    terminate();

                } catch (SmbAuthException e) {
                    e.printStackTrace();
                    Handler cHandler = new Handler(Looper.getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            Dialog(Id_text);
                        }
                    };
                    cHandler.sendEmptyMessage(0);
                } catch (SmbException e) {
                    e.printStackTrace();
                    Handler cHandler = new Handler(Looper.getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            Dialog(Ip_text);
                        }
                    };
                    cHandler.sendEmptyMessage(0);
                }
            }
        }.start();
    }


    public static long getFolderSize(File folder) {
        long length = 0;
        File[] files = folder.listFiles();
        if (files != null) {
            int count = files.length;
            for (int i = 0; i < count; i++) {
                if (files[i].isFile()) {
                    length += files[i].length();
                } else {
                    length += getFolderSize(files[i]);
                }
            }
        }

        return length;
    }

    //?????? & ?????? ??????
    public static void removeDir(String mRootPath) {
        File file = new File(mRootPath);
        File[] childFileList = file.listFiles();
        for(File childFile : childFileList)
        {
            if(childFile.isDirectory()) {
                removeDir(childFile.getAbsolutePath());    //?????? ????????????
            }
            else {
                childFile.delete();    //?????? ??????
            }
        }
        file.delete();    //root ??????
    }

    public void doRootStuff(){
        try {
            String line;
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write(("busybox mount -o remount,rw -t ext4 /dev/block/platform/ff0f0000.dwmmc/by-name/system /system\n").getBytes()); // "Permissive"
            stdin.write(("cp /storage/emulated/0/TEST/test.apk /system/app/SmbLauncher/SmbLauncher.apk\n").getBytes()); // E/[Error]: cp: /system/media/bootanimation_test.zip: Read-only file system
            stdin.write(("chmod 644 /system/app/SmbLauncher/SmbLauncher.apk\n").getBytes());
            stdin.write("exit\n".getBytes());
            stdin.flush();
            stdin.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                Log.d("[Output]", line);
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                Log.e("[Error]", line);
            }
            br.close();
            process.waitFor();
            process.destroy();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void CheckVer() {
        new Thread() {
            public void run() {
                try {
                    URL url;
                    HttpURLConnection conn = null;
                    url = new URL(IP_ADDRESS); // ????????? ????????????.
                    conn = (HttpURLConnection) url.openConnection();
                    AppListIntent = new Intent(Intent.ACTION_MAIN, null);
                    AppListIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> pack = getPackageManager().queryIntentActivities(AppListIntent, 0);
                    Runtime.getRuntime().exec("su");

                    if (200 != conn.getResponseCode()) {
                        System.out.println("con test" + conn.getResponseCode());
                    } else {
                        System.out.println("con test22 = " + conn.getResponseCode());
                        //System.out.println("contest" + conn.getResponseCode());
                        for (int i = 0; i < pack.size(); i++) {
                            PackageInfo packageInfo = null;
                            packageInfo = getPackageManager().getPackageInfo(pack.get(i).activityInfo.applicationInfo.packageName, 0);

                            if (test.equals(pack.get(i).activityInfo.applicationInfo.packageName)) {
                                System.out.println("app??????" + packageInfo.versionName);
                                System.out.println("????????????" + conn.getURL().getFile().substring(16, 19));
                                if (conn.getURL().getFile().substring(16, 19).equals(packageInfo.versionName.substring(0, 3))) {
                                    System.out.println("??????" + packageInfo.versionName);
                                    System.out.println("??????" + conn.getURL().getFile());
                                } else {
                                    ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                                    NetworkInfo ninfo = cm.getActiveNetworkInfo();
                                    if (ninfo == null) {
                                        System.out.println("??????????????? x");
                                    } else {
                                        Handler mHandler = new Handler(Looper.getMainLooper());
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                // ??????????????? ?????? ??????
                                                DownloadFileAsync downloadFileAsync = new DownloadFileAsync();
                                                downloadFileAsync.execute();
                                            }
                                        }, 0);
                                    }
                                }

                            }
                        }
                    }

                } catch (PackageManager.NameNotFoundException nameNotFoundException) {
                    nameNotFoundException.printStackTrace();
                } catch (MalformedURLException malformedURLException) {
                    malformedURLException.printStackTrace();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

        }.start();


    }



    class DownloadFileAsync extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog= new ProgressDialog(MainActivity.this); //ProgressDialog ?????? ??????
            pDialog.setTitle("???????????? ??? ?????????.");                   //ProgressDialog ??????
            pDialog.setMessage("Loading.....");             //ProgressDialog ?????????
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); //??????????????? ProgressDialog ????????? ??????
            pDialog.setCanceledOnTouchOutside(false); //ProgressDialog??? ???????????? ?????? dialog??? ???????????? ?????? ???????????? ?????? ??????
            pDialog.show(); //ProgressDialog ????????????
        }

        @Override
        protected String doInBackground(String... strings) {
            int contentLength;
            URL url;
            HttpURLConnection conn = null;
            InputStream inStream = null;
            OutputStream outStream = null;
            BufferedInputStream bin = null;
            BufferedReader reader = null;
            BufferedOutputStream bout = null;
            try {
                //Process p = Runtime.getRuntime().exec("su");
                url = new URL(IP_ADDRESS); // ????????? ????????????.
                conn = (HttpURLConnection)url.openConnection();
               // DataOutputStream os = new DataOutputStream(p.getOutputStream());

                System.out.println("conn" + conn);

                    contentLength = conn.getContentLength();
                    // BufferedInputStream??? ?????? ????????? ????????? ????????? ?????? ?????????.
                    inStream = conn.getInputStream();
                    outStream = new FileOutputStream(targetFile.getPath());
                    bin = new BufferedInputStream(inStream);
                    bout = new BufferedOutputStream(outStream);
                    int bytesRead = 0;
                    byte[] buffer = new byte[83886080];
                    long total = 0;
                    while ((bytesRead = bin.read(buffer, 0, 1024)) != -1) {
                        total += bytesRead;
                        bout.write(buffer, 0, bytesRead);
                        publishProgress((int)((total*100)/contentLength));
                    }
                System.out.println("start = ");

            }catch (Exception e){
                try {
                    throw e;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }finally {
                try{
                    bin.close();
                    bout.close();
                    inStream.close();
                    outStream.close();
                    conn.disconnect();
                }catch (Exception e){

                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
            pDialog.setProgress(values[0]); //???????????? pos_dialog????????? ProgressDialog??? ????????? ?????? ??????
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            pDialog.dismiss(); //ProgressDialog ????????? ?????? ??????
            pDialog=null;      //???????????? ?????????
            doRootStuff();
            ItempApi.rebootDevice(MainActivity.this);

            //doInBackground() ?????????????????? ????????? ?????? "Complete Load" string Toast??? ????????? ??????
        }
    }

    private File getSaveFolder(String folderName) {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + folderName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    void Dialog(String text){
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);    //???????????????
        alert.setTitle("??????");
        alert.setMessage(text);

        alert.setCancelable(false);
        alert.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {      //????????? ???????????? ????????????
        return false;
    }

    private void hideKeyboard() {              //?????? ????????? ????????? ?????????
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        hideKeyboard();
        return super.dispatchTouchEvent(ev);
    }

    public void terminate() {
        running = false;
    }

    public void checkPermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void init() {
        RecyclerView recyclerView = findViewById(R.id.DBRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        DBadapter = new DBRecyclerAdapter(DBfilelist,this);
        recyclerView.setAdapter(DBadapter);
        RecyclerDecoration spaceDecoration = new RecyclerDecoration(8);
        recyclerView.addItemDecoration(spaceDecoration);

    }

    private void DBhandler(){
        DBfilelist.clear();
        IpList.clear();
        IdList.clear();
        PassList.clear();
        handler = DBHandler.open(this);
        Cursor cursor;
        cursor = handler.select();
        while (cursor.moveToNext()){
            IpList.add(cursor.getString(0));
            IdList.add(cursor.getString(1));
            PassList.add(cursor.getString(2));
        }
        for(int i = 0; i < IpList.size(); i++){
            DBData save_data = new DBData(IpList.get(i), IdList.get(i), PassList.get(i));
            System.out.println(IpList.get(i));
            DBfilelist.add(i, save_data);
        }
        DBadapter.notifyDataSetChanged();
    }


    @Override
    protected void onStop() {
        super.onStop();
        terminate();
    }

    @Override
    protected void onRestart(){               //?????? ??????,???????????? ?????? ?????????????????? ????????????
        super.onRestart();
        DBhandler();
        running = true;
        Thread();
    }

    void ethernet(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ninfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET); //????????? ??????
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

    private boolean isInstallApp(String pakageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(pakageName);
        if (intent == null) {
            //?????????
            return false;
        } else {
            //??????
            return true;
        }
    }
}