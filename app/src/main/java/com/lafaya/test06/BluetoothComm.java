package com.lafaya.test06;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;

import android.os.Handler;
import android.preference.DialogPreference;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import me.aflak.bluetooth.Bluetooth;

/**
 * Created by Administrator on 2017/10/31.
 */
public class BluetoothComm {

    DoorStatus doorStatus = new DoorStatus();

    Bluetooth bluetooth;
    private int devicepos = 0;
    private Activity activity;
    private boolean registered = false;

    private Handler handler;
    private String msgwaitsend = null;

    Timer sendwaittime;//更新定时器
    boolean sendwaittime_flag = false;
    int resend_count = 0;

    private static boolean bluetooth_connect_flag = false;


    //Bluetooth初始化
    void bluetoothInitialize(Activity inactivity, Handler inhandler) {
        activity = inactivity;
        handler = inhandler;
        bluetooth = new Bluetooth(activity);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        activity.registerReceiver(mReceiver, filter);
        if(bluetooth.isConnected()){
            bluetooth.removeCommunicationCallback();
            bluetooth.disconnect();
        }
        //弹出连接设备窗口，要求连接设备
        activity.startActivityForResult(new Intent(activity,PageDevicelayout.class),0);
    }


    //bluetooth 连接函数
    void bluetoothconnect(Intent data, final Handler handler, final Activity activity){
        devicepos = data.getExtras().getInt("pos");
        registered = true;
        bluetooth = new Bluetooth(activity);

        //判断是否有已连接设备
        if(bluetooth.isConnected()){
            if(bluetooth.getPairedDevices().get(devicepos).getName().equals(bluetooth.getDevice().getName())){
                bluetooth_connect_flag = true;
                /*发送信息*/
                Message msg = new Message();
                msg.what = MainActivity.BT_CONNECTED;
                handler.sendMessage(msg);

            }
            else{
                /*关闭连接*/
                bluetooth.disconnect();
                bluetooth_connect_flag = false;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Handler handler = new Handler();        //使用库android.os.Handler
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                bluetooth.connectToDevice(bluetooth.getPairedDevices().get(devicepos));
                            }
                        },2000);

                    }
                });
            }
        }
        //连接新的设备
        else {
            bluetooth.connectToDevice(bluetooth.getPairedDevices().get(devicepos));
        }


        bluetooth.setCommunicationCallback(new Bluetooth.CommunicationCallback() {
            //设备连接成功
            @Override
            public void onConnect(BluetoothDevice device) {
                Message msg = new Message();
                /*发送内容为。。。。。*/
                msg.what = MainActivity.BT_CONNECTED;
                handler.sendMessage(msg);
            }

            //设备连接失败
            @Override
            public void onDisconnect(BluetoothDevice device, String message) {
                Message msg = new Message();
                /*发送内容为。。。。。*/
                msg.what = MainActivity.BT_CONNECTFLASE;
                handler.sendMessage(msg);
            }

            //收到消息
            @Override
            public void onMessage(String message) {
                Message msg = new Message();
                /*发送内容为。。。。。*/
                msg.what = MainActivity.BT_RECEIVE;
                Bundle bundle = new Bundle();
                bundle.putString("msg", message);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }

            //错误
            @Override
            public void onError(String message) {
                Message msg = new Message();
                /*发送内容为。。。。。*/
                msg.what = MainActivity.BT_ERROR;
                handler.sendMessage(msg);
            }

            //连接错误
            @Override
            public void onConnectError(BluetoothDevice device, String message) {
                Message msg = new Message();
                /*发送内容为。。。。。*/
                msg.what = MainActivity.BT_ERROR2;
                handler.sendMessage(msg);
            }
        });
    }



    //设备连接断开
    void connectDevice(Activity activity, boolean flag, final Handler handler){
        if(flag) {
            activity.startActivityForResult(new Intent(activity,PageDevicelayout.class),0);
        }
        else {
            Dialog dialog = new AlertDialog.Builder(activity).setTitle("断开自动门连接").setMessage("确定断开自动门连接？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    bluetooth.removeCommunicationCallback();
                    bluetooth.disconnect();
                    Message msg = new Message();
                    /*发送内容*/
                    msg.what = MainActivity.BT_CONNECTED;
                    handler.sendMessage(msg);
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i){

                }
            }).create();
            dialog.show();
        }
    }

    //信息发送
    void SendMessage(String msg, final Activity activity){

        /*判断蓝牙是否连接自动门*/
        if(!MainActivity.doorStatus.door_connect){     //蓝牙断开

            Dialog dialog = new AlertDialog.Builder(activity).setTitle("自动门未连接").setMessage("请先连接自动门！").
                    setPositiveButton("确认", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).create();
            dialog.show();
        }

        /*蓝牙连接则可以发送数据*/
        else {
                msgwaitsend = msg;      //装载数据
                resend_count = 0;       //重新发送次数
                sendwaittime_flag = true;   //发送等待时间标志位
                sendwaittime = new Timer();     //等待时间，计数器
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        msg.what = MainActivity.SEND_WAITE;
                        handler.sendMessage(msg);   //发送msg = 88数据到Handler,执行Handler机制
                    }
            };
            //查询间隔，每隔1000ms查询一次
            sendwaittime.schedule(task,1000,1000);
            MainActivity.bluetoothComm.bluetooth.send(msg);   //发送装载的数据
            //Toast.makeText(activity, msg,Toast.LENGTH_LONG).show();
        }
    }



    //信息重新发送
    void reSendMessage(){
        MainActivity.bluetoothComm.bluetooth.send(msgwaitsend);
    }


    //信息接收
    void ReceiveMassage(char[] msg){

    }
    //蓝牙状态
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state){
                    case BluetoothAdapter.STATE_OFF:
                        if(registered){
                            activity.unregisterReceiver(mReceiver);
                            registered = false;
                        }
                        Toast.makeText(activity, "蓝牙设备已关闭", Toast.LENGTH_LONG).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        if(registered) {
                            activity.unregisterReceiver(mReceiver);
                            registered=false;
                        }
                        Toast.makeText(activity, "蓝牙设备已打开", Toast.LENGTH_LONG).show();
                        break;
                }
            }

        }
    };



    void cleanWaitreceive(){
        //通讯。。等待
        if(sendwaittime_flag){
            resend_count = 0;
            sendwaittime.cancel();
            sendwaittime_flag = false;
        }
    }




}

