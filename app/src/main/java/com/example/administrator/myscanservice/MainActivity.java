package com.example.administrator.myscanservice;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import msg.MsgValue;
import scan.ScanManager;

public class MainActivity extends AppCompatActivity {
    //用于管理红外扫描器
    //private ScanManager scanManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //扫码管理器
        //scanManager = new ScanManager(this,handler);
        Intent intent1 = new Intent(this ,ScanService.class);
        // 启动指定Server
        startService(intent1);
    }
//    ICallBack.Stub mCallback = new ICallBack.Stub() {
//        @Override
//        public void onReturnValue(byte[] buffer, int size)
//                throws RemoteException {
//            String result = new String(buffer);
//            Intent intent=new Intent("com.example.broadcasttest.MY_BROADCAST");
//            intent.putExtra("extra_data",result);
//            //sendBroadcast(intent);
//            sendOrderedBroadcast(intent,null);
//            SendMessage(MsgValue.TELL_ME_SOME_INFOR,0,0,result);
//        }
//    };
    /**
     * 处理各个类发来的UI请求消息
     */
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //弹出信息
                case MsgValue.TELL_ME_SOME_INFOR:
                    String message = msg.obj.toString();
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    break;
                default:
            }
            super.handleMessage(msg);
        }
    };
    @Override
    protected void onDestroy() {
       // scanManager.unregisterCall();

        super.onDestroy();
    }
    void SendMessage(int what, int arg1, int arg2, Object obj) {
        if (handler != null) {
            Message.obtain(handler, what, arg1, arg2, obj).sendToTarget();
        }
    }
}
