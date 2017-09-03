package com.example.administrator.myscanservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompleteReceiver extends BroadcastReceiver {
    public BootCompleteReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String action = intent.getAction();
        //开机启动
        if(action.equals("android.intent.action.BOOT_COMPLETED")){
//            Intent intent0 = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
//            intent0.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent0);
            Intent intent1 = new Intent(context ,ScanService.class);
            // 启动指定Server
            context.startService(intent1);
        }
    }
}
