package com.example.administrator.myscanservice;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import msg.MsgValue;
import scan.ScanManager;

public class ScanService extends Service {
    //用于管理红外扫描器
    private ScanManager scanManager;
    public ScanService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //扫码管理器
        scanManager = new ScanManager(getApplicationContext(),handler);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
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
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    break;
                default:
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onDestroy() {
        scanManager.unregisterCall();
        super.onDestroy();
    }
}
