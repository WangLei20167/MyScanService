package scan;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.smartdevice.aidl.ICallBack;
import com.smartdevice.aidl.IZKCService;

import msg.MsgValue;

/**
 * Created by Administrator on 2017/8/24 0024.
 */

public class ScanManager {

    public static int module_flag = 0;
    public static int DEVICE_MODEL = 0;
    public boolean bindSuccessFlag = false;
    public static IZKCService mIzkcService;
    private boolean runFlag = true;
    public ServiceConnection mServiceConn;
    //
    RemoteControlReceiver screenStatusReceiver = null;
    private Context context;
    private Handler handler;

    public ScanManager(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        //
        initEnvironment();
    }
    //获取扫码数据，广播扫码内容
    ICallBack.Stub mCallback = new ICallBack.Stub() {
        @Override
        public void onReturnValue(byte[] buffer, int size)
                throws RemoteException {
            String result = new String(buffer);
            Intent intent=new Intent("com.example.broadcasttest.MY_BROADCAST");
            intent.putExtra("extra_data",result);
            //sendBroadcast(intent);
            //可截断的有序广播
            context.sendOrderedBroadcast(intent,null);
            SendMessage(MsgValue.TELL_ME_SOME_INFOR,0,0,result);
        }
    };

    //初始化扫码环境（控制红外扫描器）
    public void initEnvironment() {
        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e("client", "onServiceDisconnected");
                mIzkcService = null;
                bindSuccessFlag = false;
                //   Toast.makeText(MainActivity.this, "绑定失败", Toast.LENGTH_SHORT).show();
                SendMessage(MsgValue.TELL_ME_SOME_INFOR, 0, 0, "绑定服务失败");
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.e("client", "onServiceConnected");
                mIzkcService = IZKCService.Stub.asInterface(service);

                if (mIzkcService != null) {
                    try {
                        //Toast.makeText(MainActivity.this, "绑定成功", Toast.LENGTH_SHORT).show();
                        //SendMessage(0, 0, 0, "绑定服务成功");
                        DEVICE_MODEL = mIzkcService.getDeviceModel();
                        mIzkcService.setModuleFlag(module_flag);
                        mIzkcService.appendRingTone(true);
                        //mIzkcService.scanRepeatHint(false);
                        mIzkcService.dataAppendEnter(false);

                        if (module_flag == 3) {
                            mIzkcService.openBackLight(1);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    bindSuccessFlag = true;
                }
            }
        };
        //绑定服务
        bindService();
        //管理屏幕
        screenStatusReceiver = new RemoteControlReceiver();
        IntentFilter screenStatusIF = new IntentFilter();
        //注册广播
        screenStatusIF.addAction(Intent.ACTION_SCREEN_ON);
        screenStatusIF.addAction(Intent.ACTION_SCREEN_OFF);
        screenStatusIF.addAction(Intent.ACTION_SHUTDOWN);
        screenStatusIF.addAction("com.zkc.keycode");
        context.registerReceiver(screenStatusReceiver, screenStatusIF);
        //注册回调接口
        ExecutorFactory.executeThread(new Runnable() {
            @Override
            public void run() {
                while (runFlag) {
                    if (bindSuccessFlag) {
                        // 注册回调接口
                        try {
                            mIzkcService.registerCallBack("Scanner", mCallback);
                            // 关闭线程
                            runFlag = false;
                            mIzkcService.openScan(true);
                            //mHandler.sendEmptyMessage(0);
                        } catch (RemoteException e) {
                            // TODO Auto-generated catch block
                            runFlag = false;
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    //扫描
    public void onScan() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mIzkcService.scan();
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void bindService() {
        //com.zkc.aidl.all为远程服务的名称，不可更改
        //com.smartdevice.aidl为远程服务声明所在的包名，不可更改，
        // 对应的项目所导入的AIDL文件也应该在该包名下
        Intent intent = new Intent("com.zkc.aidl.all");
        intent.setPackage("com.smartdevice.aidl");
        context.bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    //解绑服务
    public void unbindService() {
        context.unbindService(mServiceConn);
    }

    //注销监听
    public void unregisterCall() {
        try {
            mIzkcService.openScan(false);
            mIzkcService.unregisterCallBack("Scanner", mCallback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        unbindService();
        context.unregisterReceiver(screenStatusReceiver);
    }

    //停止
    public void onStop() {
        if (module_flag == 3) {
            try {
                mIzkcService.openBackLight(0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    void SendMessage(int what, int arg1, int arg2, Object obj) {
        if (handler != null) {
            Message.obtain(handler, what, arg1, arg2, obj).sendToTarget();
        }
    }

    //该BroadcastReceiver的意图在于接收扫描按键（受系统控制的产品不起作用），屏幕打开, 屏幕关闭的广播；
    //屏幕打开需要打开扫描模块，唤醒扫描功能；
    //屏幕关闭须要关闭扫描模块，开启省电模式；
    int count = 0;

    public class RemoteControlReceiver extends BroadcastReceiver {

        private static final String TAG = "RemoteControlReceiver";

        @Override
        public void onReceive(Context context, final Intent intent) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String action = intent.getAction();
                    //beginToReceiverData = false;
                    Log.i(TAG, "System message " + action);
                    if (action.equals("com.zkc.keycode")) {
                        if (count++ > 0) {
                            count = 0;
                            int keyValue = intent.getIntExtra("keyvalue", 0);
                            Log.i(TAG, "KEY VALUE:" + keyValue);
                            if (keyValue == 136 || keyValue == 135 || keyValue == 131) {
                                Log.i(TAG, "Scan key down.........");
                                try {
                                    mIzkcService.scan();
                                    // mHandler.sendEmptyMessage(2);
                                } catch (RemoteException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else if (action.equals("android.intent.action.SCREEN_ON")) {
                        Log.i(TAG, "Power off,Close scan modules power.........");

                        if (mIzkcService != null) {
                            // beginToReceiverData = true;
                            try {
                                if (mIzkcService != null)
                                    mIzkcService.openScan(true);
                            } catch (RemoteException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    } else if (action.equals("android.intent.action.SCREEN_OFF")) {

                        Log.i(TAG, "ACTION_SCREEN_OFF,Close scan modules power.........");
                        try {
                            if (mIzkcService != null)
                                mIzkcService.openScan(false);
                        } catch (RemoteException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else if (action.equals("android.intent.action.ACTION_SHUTDOWN")) {

                        Log.i(TAG, "ACTION_SCREEN_ON,Open scan modules power.........");
                        try {
                            if (mIzkcService != null)
                                mIzkcService.openScan(false);
                        } catch (RemoteException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        }
    }

}
