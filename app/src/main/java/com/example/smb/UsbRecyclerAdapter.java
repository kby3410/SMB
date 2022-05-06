package com.example.smb;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;


public class UsbRecyclerAdapter extends RecyclerView.Adapter<UsbRecyclerAdapter.ItemViewHolder> {
    private ArrayList<UsbData> UsblistData;
    private ArrayList<String> fileList = new ArrayList<>();
    Context mContext;
    private ArrayList<File> Usb_Dir_Sort = new ArrayList<>();
    private ArrayList<File>  Usb_File_Sort = new ArrayList<>();

    public UsbRecyclerAdapter(ArrayList<UsbData> UsblistData, Context context) {
        this.UsblistData = UsblistData;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsbRecyclerAdapter.ItemViewHolder holder, int position) {
        holder.onBind(UsblistData.get(position));
    }

    @Override
    public int getItemCount() {
        return UsblistData.size();
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
                                String path = UsblistData.get(pos).getIp()+UsblistData.get(pos).getTitle();
                                File Usbfile = new File(path);


                                if (Usbfile.isDirectory()) {
                                        File Usb_list[] = Usbfile.listFiles();

                                        for (int i = 0; i < Usb_list.length; i++) {
                                            if(Usb_list[i].isDirectory()){
                                                Usb_Dir_Sort.add(Usb_list[i]);
                                            }else {
                                                Usb_File_Sort.add(Usb_list[i]);
                                            }
                                            Collections.sort(Usb_Dir_Sort);
                                            Collections.sort(Usb_File_Sort);

                                        }
                                    for (int i = 0; i < Usb_Dir_Sort.size(); i++) {
                                        fileList.add(Usb_Dir_Sort.get(i).getName());
                                    }
                                    for(int i = 0; i < Usb_File_Sort.size(); i++){
                                        if ( Usb_File_Sort.get(i).getName().endsWith("avi") || Usb_File_Sort.get(i).getName().endsWith("mp4") || Usb_File_Sort.get(i).isDirectory() || Usb_File_Sort.get(i).getName().endsWith("jpg") || Usb_File_Sort.get(i).getName().endsWith("jpeg") ||
                                                Usb_File_Sort.get(i).getName().endsWith("JPG") || Usb_File_Sort.get(i).getName().endsWith("png") || Usb_File_Sort.get(i).getName().endsWith("bmp") || Usb_File_Sort.get(i).getName().endsWith("txt") ||
                                                Usb_File_Sort.get(i).getName().endsWith("doc") || Usb_File_Sort.get(i).getName().endsWith("docx") || Usb_File_Sort.get(i).getName().endsWith("xls") ||  Usb_File_Sort.get(i).getName().endsWith("xlsx") ||
                                                Usb_File_Sort.get(i).getName().endsWith("ppt") || Usb_File_Sort.get(i).getName().endsWith("pptx") || Usb_File_Sort.get(i).getName().endsWith("pdf") || Usb_File_Sort.get(i).getName().endsWith("hwp")) {
                                            fileList.add(Usb_File_Sort.get(i).getName());
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
                                            Intent intent = new Intent(context.getApplicationContext(), Usblist.class);
                                            intent.putExtra("title", UsblistData.get(pos).getTitle());
                                            intent.putExtra("current", UsblistData.get(pos).getIp()+UsblistData.get(pos).getTitle()+"/");
                                            intent.putStringArrayListExtra("list", fileList);
                                            context.startActivity(intent);
                                        }
                                } else {
                                    File_Intent(Usbfile,context, pos);
                                }
                            }
                        }.start();
                    }
                }
            });
        }

        void onBind(UsbData usbData) {
            FileList.setText(usbData.getTitle());
            imageView.setImageResource(usbData.getIcon());
        }
    }
    public void File_Intent(File file, Context c, int pos){
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.setAction(Intent.ACTION_VIEW);
        Uri furi = Uri.fromFile(file);


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
            intent.setDataAndTypeAndNormalize(furi,
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

}



