package com.example.smb;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
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
import java.net.NoRouteToHostException;
import java.util.ArrayList;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class DBRecyclerAdapter extends RecyclerView.Adapter<DBRecyclerAdapter.ItemViewHolder> {
    private ArrayList<DBData> listData;
    private ArrayList<String> DBfileList = new ArrayList<>();
    Context mContext;
    private ProgressDialog progressBar;
    private DBHandler handler;



    public DBRecyclerAdapter(ArrayList<DBData> listData, Context context) {
        this.listData = listData;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dbitem, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DBRecyclerAdapter.ItemViewHolder holder, int position) {
        holder.onBind(listData.get(position));
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView FileList;

        ItemViewHolder(View itemView) {
            super(itemView);
            FileList = itemView.findViewById(R.id.DBTextView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {       //해당 아이템 클릭 리스너
                    final int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        final Context context = v.getContext();
                        PopupMenu popup= new PopupMenu(context.getApplicationContext(), v);
                        popup.getMenuInflater().inflate(R.menu.option_menu, popup.getMenu());
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()){
                                    case R.id.m1:
                                        new Thread(){
                                            public void run(){
                                                String url = "smb://";

                                                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, listData.get(pos).getId(), listData.get(pos).getPass());

                                                SmbFile DBdir = null;
                                                try {
                                                    DBdir = new SmbFile(url+listData.get(pos).getIp(), auth);
                                                    DBfileList.clear();

                                                } catch (MalformedURLException e) {
                                                    e.printStackTrace();

                                                }
                                                try {
                                                    for (SmbFile DBf : DBdir.listFiles())
                                                    {
                                                        DBfileList.add(DBf.getName());
                                                    }
                                                    Intent intent = new Intent(context.getApplicationContext(),list.class);
                                                    intent.putExtra("ip",listData.get(pos).getIp()+"/");
                                                    intent.putExtra("id",listData.get(pos).getId());
                                                    intent.putExtra("pass",listData.get(pos).getPass());
                                                    intent.putStringArrayListExtra("list",DBfileList);
                                                    context.startActivity(intent);
                                                } catch (SmbException e) {
                                                    e.printStackTrace();
                                                    System.out.println("실패");
                                                    Handler handler2 = new Handler(Looper.getMainLooper()) {
                                                        @Override public void handleMessage(Message msg) {
                                                            Toast.makeText(context, "컴퓨터 전원 확인 or 인터넷 연결 확인", Toast.LENGTH_LONG).show();
                                                        }
                                                    };
                                                    handler2.sendEmptyMessage(0);
                                                }
                                            }
                                        }.start();
                                        break;
                                    case R.id.m2:
                                        handler = DBHandler.open(context);
                                        AlertDialog.Builder alert = new AlertDialog.Builder(context);    //다이얼로그
                                        alert.setTitle("알림");
                                        alert.setMessage("해당IP를 삭제하시겠습니까?");
                                        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                handler.delete(listData.get(pos).getIp());
                                                remove(pos);                            //클릭된 뷰 삭제 및 데이터 삭제
                                                dialog.dismiss();
                                            }
                                        });
                                        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                Toast.makeText(context, "취소하였습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        alert.show();
                                        break;
                                    default:
                                        break;
                                }
                                return false;
                            }
                        });
                        popup.show();
                    }
                }
            });
        }

        void onBind(DBData data) {
            FileList.setText(data.getIp()+"    ("+data.getId()+")");
        }

        public void remove(int position) {                   //해당포지션 삭제
            try {
                listData.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, listData.size());
            } catch (IndexOutOfBoundsException ex) {
                ex.printStackTrace();
            }
        }
    }
}
