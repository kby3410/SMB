package com.example.smb;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;


public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder> {
    private ArrayList<Data> listData;
    private ArrayList<String> fileList = new ArrayList<>();
    Context mContext;
    private ArrayList<String> Dir_Sort = new ArrayList<>();
    private ArrayList<String> File_Sort = new ArrayList<>();
    private ProgressDialog pDialog;
    private Handler mHandler,mHandler1;

    public RecyclerAdapter(ArrayList<Data> listData, Context context) {
        this.listData = listData;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.ItemViewHolder holder, int position) {
        holder.onBind(listData.get(position));
    }


    @Override
    public int getItemCount() {
        return listData.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView FileList;
        private ImageView imageView;

        ItemViewHolder(View itemView) {
            super(itemView);
            FileList = itemView.findViewById(R.id.textView1);
            imageView = itemView.findViewById(R.id.imageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {       //해당 아이템 클릭 리스너
                    final int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        final Context context = v.getContext();
                        new Thread() {
                            public void run() {
                                String url = "smb://";

                                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, listData.get(pos).getId(), listData.get(pos).getPass());
                                SmbFile dir = null;
                                try {
                                    dir = new SmbFile(url + listData.get(pos).getIp() + listData.get(pos).getTitle(), auth);
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }

                                if (listData.get(pos).getTitle().charAt(listData.get(pos).getTitle().length() - 1) == '/') {
                                    try{
                                        for (SmbFile f : dir.listFiles()) {
                                            if(f.isDirectory()){
                                                Dir_Sort.add(f.getName());
                                            }else {
                                                File_Sort.add(f.getName());
                                            }
                                        }
                                        Collections.sort(Dir_Sort);
                                        Collections.sort(File_Sort);
                                        for (int i = 0; i < Dir_Sort.size(); i++) {
                                            fileList.add(Dir_Sort.get(i));
                                        }

                                        for (int i = 0; i < File_Sort.size(); i++) {
                                            if ( File_Sort.get(i).endsWith("avi") || File_Sort.get(i).endsWith("mp4") || File_Sort.get(i).charAt(File_Sort.get(i).length() - 1) == '/' || File_Sort.get(i).endsWith("jpg") || File_Sort.get(i).endsWith("jpeg") ||
                                                    File_Sort.get(i).endsWith("JPG") || File_Sort.get(i).endsWith("png") || File_Sort.get(i).endsWith("bmp") || File_Sort.get(i).endsWith("txt") ||
                                                    File_Sort.get(i).endsWith("doc") || File_Sort.get(i).endsWith("docx") || File_Sort.get(i).endsWith("xls") ||  File_Sort.get(i).endsWith("xlsx") ||
                                                    File_Sort.get(i).endsWith("ppt") || File_Sort.get(i).endsWith("pptx") || File_Sort.get(i).endsWith("pdf") || File_Sort.get(i).endsWith("hwp")) {
                                                fileList.add(File_Sort.get(i));
                                            }
                                        }
                                        for (int i = 0; i < fileList.size(); i++) {
                                            if (fileList.get(i).charAt(fileList.get(i).length() - 2) == '$') {
                                                fileList.remove(i);
                                                i--;
                                            }
                                        }

                                        if (fileList.size() == 0) {
                                            Handler cHandler = new Handler(Looper.getMainLooper()) {
                                                @Override
                                                public void handleMessage(Message msg) {
                                                    AlertDialog.Builder alert = new AlertDialog.Builder(context);    //다이얼로그
                                                    alert.setTitle("알림");
                                                    alert.setMessage("폴더에 파일이 존재하지 않습니다.");

                                                    alert.setCancelable(false);
                                                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                            dialog.dismiss();
                                                        }
                                                    });
                                                    alert.show();
                                                }
                                            };
                                            cHandler.sendEmptyMessage(0);

                                        } else {
                                            Intent intent = new Intent(context.getApplicationContext(), list.class);
                                            intent.putExtra("ip", listData.get(pos).getIp() + listData.get(pos).getTitle());
                                            intent.putExtra("id", listData.get(pos).getId());
                                            intent.putExtra("pass", listData.get(pos).getPass());
                                            intent.putStringArrayListExtra("list", fileList);
                                            context.startActivity(intent);
                                        }
                                    } catch (SmbException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    try {
                                        showDocumentFile(context, pos);
                                    } catch (SmbException e) {
                                        e.printStackTrace();
                                        Handler cHandler = new Handler(Looper.getMainLooper()) {
                                            @Override
                                            public void handleMessage(Message msg) {
                                                AlertDialog.Builder alert = new AlertDialog.Builder(context);    //다이얼로그
                                                alert.setTitle("알림");
                                                alert.setMessage("컴퓨터 연결을 확인해주세요.");

                                                alert.setCancelable(false);
                                                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        dialog.dismiss();
                                                        context.startActivity(new Intent(context.getApplicationContext(), MainActivity.class));
                                                    }
                                                });
                                                alert.show();
                                            }
                                        };
                                        cHandler.sendEmptyMessage(0);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }.start();
                    }
                }
            });
        }

        void onBind(Data data) {
            FileList.setText(data.getTitle());
            imageView.setImageResource(data.getIcon());
        }

        SmbFile dir1 = null;
        public void showDocumentFile(final Context c, int pos) throws IOException {

            String url = "smb://";
            NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(null, listData.get(pos).getId(), listData.get(pos).getPass());

            String strSDPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            getSaveFolder();

            try {
                dir1 = new SmbFile(url + listData.get(pos).getIp() + listData.get(pos).getTitle(), auth1);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            final File targetFile = new File(strSDPath + "/SMB/" + listData.get(pos).getTitle());

            class DownloadFileAsync extends AsyncTask<String, Integer, String> {
                long start = System.currentTimeMillis();
                double trRate = 0;
                long total = 0;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    pDialog= new ProgressDialog(c); //ProgressDialog 객체 생성
                    pDialog.setTitle("파일 다운로드 중");                   //ProgressDialog 제목
                    pDialog.setMessage("Loading.....");             //ProgressDialog 메세지
                    pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); //막대형태의 ProgressDialog 스타일 설정
                    pDialog.setCanceledOnTouchOutside(false); //ProgressDialog가 진행되는 동안 dialog의 바깥쪽을 눌러 종료하는 것을 금지
                    pDialog.show(); //ProgressDialog 보여주기
                }

                @Override
                protected String doInBackground(String... strings) {
                    //SmbFileInputStream in = null;
                    //SmbFileOutputStream out = null;
                    //FileInputStream fileInputStream = null;
                    BufferedInputStream inBuf = null;
                   // InputStream inStream = null;
                    //OutputStream outStream = null;
                    //BufferedInputStream bin = null;
                    BufferedOutputStream bout = null;

                    try {
                        inBuf = new BufferedInputStream(new SmbFileInputStream(dir1));
                        bout = new BufferedOutputStream(new FileOutputStream(targetFile));
                        //inStream = dir1.getInputStream();
                        //outStream = new FileOutputStream(targetFile);
                        //bin = new BufferedInputStream(inStream);
                        //bout = new BufferedOutputStream(outStream);
                        int bytesRead = 0;
                        byte[] buffer = new byte[1024*1024];
                        /*while(true){
                            bytesRead = inBuf.read(buffer); // 버퍼단위로읽는다.
                            if(bytesRead == -1) break;
                            total += bytesRead;
                            bout.write(buffer,0,bytesRead);
                            publishProgress((int)((total*100)/dir1.length()));
                        }*/

                        while ((bytesRead = inBuf.read(buffer,0,262144)) > 0){
                            total += bytesRead;
                            bout.write(buffer, 0, bytesRead);
                            publishProgress((int)((total*100)/dir1.length()));
                        }

                        /*while ((bytesRead = bin.read(buffer, 0, 1024)) != -1) {
                            total += bytesRead;
                            bout.write(buffer, 0, bytesRead);
                            publishProgress((int)((total*100)/dir1.length()));
                        }*/

                    }catch (Exception e){
                        try {
                            throw e;
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }finally {
                        try{

                            inBuf.close();
                            bout.close();

                        }catch (Exception e){

                        }
                    }
                    return null;
                }

                @Override
                protected void onProgressUpdate(Integer... values) {
                    // TODO Auto-generated method stub
                    super.onProgressUpdate(values);
                    pDialog.setProgress(values[0]); //전달받은 pos_dialog값으로 ProgressDialog에 변경된 위치 적용
                }

                @Override
                protected void onPostExecute(String result) {
                    // TODO Auto-generated method stub
                    super.onPostExecute(result);
                    pDialog.dismiss(); //ProgressDialog 보이지 않게 하기
                    pDialog=null;      //참조변수 초기화
                    long end = System.currentTimeMillis();
                    System.out.println("Elapsed " + (end - start)/1000.0 + "초");
                    long mb = total/1024/1024;
                    trRate =  (end - start)/1000.0;
                    System.out.println("MB " + mb);
                    System.out.println(mb/trRate + "MB/s");
                    File_Intent(dir1,targetFile,c);
                    //doInBackground() 메소드로부터 리턴된 결과 "Complete Load" string Toast로 화면에 표시
                }
            }

            if (targetFile.exists()) {
                System.out.println("파일이 존재함");
                if (targetFile.length() == dir1.length()) {
                    File_Intent(dir1,targetFile,c);
                } else {
                    try {
                        mHandler = new Handler(Looper.getMainLooper());
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                DownloadFileAsync downloadFileAsync = new DownloadFileAsync();
                                downloadFileAsync.execute();
                            }
                        }, 0);

                    } catch (Exception e) {
                        throw e;
                    }
                }
            } else {
                try {
                    mHandler1 = new Handler(Looper.getMainLooper());
                    mHandler1.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DownloadFileAsync downloadFileAsync = new DownloadFileAsync();
                            downloadFileAsync.execute();
                        }
                    }, 0);
                } catch (Exception e) {
                    throw e;
                }
            }
        }
    }

    public void File_Intent(SmbFile dir1, File targetfile, Context c){
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        File file = new File(dir1.getPath(), dir1.getName());
        Uri furi = Uri.fromFile(targetfile);
        System.out.println(targetfile);


        // 파일 확장자별 Mime Type을 지정한다.
        if (file.getName().endsWith("mp3")) {
            intent.setDataAndType(furi, "audio/*");
        } else if (file.getName().endsWith("mp4") || file.getName().endsWith("avi")) {
            intent.setDataAndType(furi, "video/*");
        } else if (file.getName().endsWith("jpg") || file.getName().endsWith("jpeg") ||
                file.getName().endsWith("JPG") || file.getName().endsWith("gif") ||
                file.getName().endsWith("png") || file.getName().endsWith("bmp")) {
            intent.setDataAndType(furi, "image/*");
        } else if (file.getName().endsWith("txt")) {
            intent.setDataAndType(furi, "text/*");
        } else if (file.getName().endsWith("doc") || file.getName().endsWith("docx")) {
            intent.setDataAndType(furi, "application/msword");
        } else if (file.getName().endsWith("xls") || file.getName().endsWith("xlsx")) {
            intent.setDataAndType(furi,
                    "application/vnd.ms-excel");
        } else if (file.getName().endsWith("ppt") || file.getName().endsWith("pptx")) {
            intent.setDataAndType(furi,
                    "application/vnd.ms-powerpoint");
        } else if (file.getName().endsWith("hwp")) {
            intent.setDataAndType(furi,
                    "application/hwp");
        } else if (file.getName().endsWith("pdf")) {
            intent.setDataAndType(furi, "application/pdf");
        }
        try {
            c.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(c, "지원하지 않는 파일입니다.", Toast.LENGTH_LONG).show();
        }
    }

    private File getSaveFolder() {
        String folderName = "SMB";
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + folderName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

}



