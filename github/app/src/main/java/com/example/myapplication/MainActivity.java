package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/*https://www.cnblogs.com/zhujiabin/p/6252903.html*/
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.send_bu)
    Button sendBu;
    @BindView(R.id.sockte_et)
    EditText sockteEt;
    @BindView(R.id.connect_bu)
    Button connectBu;
    @BindView(R.id.connect_tv)
    TextView connectTv;
    @BindView(R.id.img_bu)
    Button imgBu;
    @BindView(R.id.img_img)
    ImageView imgImg;
    @BindView(R.id.obtain_bu)
    Button obtainBu;
    @BindView(R.id.obtain_tv)
    TextView obtainTv;
    @BindView(R.id.so_img)
    Button soImg;
    @BindView(R.id.so_img_iv)
    ImageView soImgIv;
    private Bitmap bmp = null;
    private boolean bool;

    Socket socket;
    Handler handler = null;
    InputStream is;
    InputStreamReader isr;
    BufferedReader br;
    String response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //创建新的handler不能这样创建，会内存泄漏，我是测试使用的，这是警告
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        obtainTv.setText(response);
                        break;
                    case 1:
                        connectTv.setText(bool + "");//显示true就是连接成功
                        break;
                    case 2:
                        imgImg.setImageBitmap(bmp);
                        break;
                }
            }
        };


    }


    @OnClick({R.id.send_bu, R.id.connect_bu, R.id.img_bu, R.id.obtain_bu, R.id.so_img})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            //发数据给服务器
            case R.id.send_bu:
                try {
                    String msg = sockteEt.getText().toString();
                    if (msg.equals("")) {
                        Toast.makeText(MainActivity.this, "能能，程序员请输入", Toast.LENGTH_LONG).show();
                    }
                    if (bool == false) {
                        Toast.makeText(MainActivity.this, "能能，程序员请连接", Toast.LENGTH_LONG).show();

                    }
                    if (bool) {
                        if (msg.equals("")) {
                            return;
                        } else {
                            //发送过服务器的数据
                            DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
                            writer.writeUTF(msg);
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            //判断是否和服务器连接成功
            case R.id.connect_bu:
                Log.i("connect_bu", "connect_bu 111111");
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            socket = new Socket("192.168.1.54", 8756);
                            bool = socket.isConnected();
                            Message message = new Message();
                            message.what = 1;
                            message.obj = bool;
                            handler.sendMessage(message);
                           // socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                break;
            //接受服务器的数据
            case R.id.obtain_bu:
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            is = socket.getInputStream();//输入流
                            isr = new InputStreamReader(is);
                            br = new BufferedReader(isr);
                            response = br.readLine();
                            Message message = new Message();
                            message.what = 0;
                            handler.sendMessage(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                break;
            case R.id.img_bu:
               new Thread(){
                   @Override
                   public void run() {
                       try {
                           DataInputStream dataInput = new DataInputStream(socket.getInputStream());
                           int size = dataInput.readInt();
                           byte[] data = new byte[size];
                           int len = 0;
                           while (len<size){
                               len += dataInput.read(data,len,size-len);
                           }
                           ByteArrayOutputStream output = new ByteArrayOutputStream();
                           bmp = BitmapFactory.decodeByteArray(data,0,data.length);
                           bmp.compress(Bitmap.CompressFormat.PNG,100,output);
                           Message message = new Message();
                           message.what = 2;
                           message.obj = bmp;
                           handler.sendMessage(message);
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                   }
               }.start();
                break;
            case R.id.so_img:
                try {
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lu);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
                    long len  = baos.size();
                    Log.i("MainActivity_","图片长度="+len);

                    out.writeLong(len);
                    out.write(baos.toByteArray());
                    //out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }


}
