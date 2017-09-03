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
        //setContentView(R.layout.activity_main);
        //扫码管理器
        //scanManager = new ScanManager(this,handler);
        //开启服务
        Intent intent1 = new Intent(this ,ScanService.class);
        // 启动指定Server
        startService(intent1);
    }
    @Override
    protected void onDestroy() {
       // scanManager.unregisterCall();
        super.onDestroy();
    }

}
