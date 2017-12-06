package com.lafaya.test06;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lafaya.test06.utils.StatusBarCompat;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    protected boolean statusBarCompat = true;
    private static long DOUBLE_CLICK_TIME = 0L;

    //声明相关变量
    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView lvLeftMenu;
    private String[] lvs = {"设置", "帮助", "反馈", "退出"};
    private ArrayAdapter arrayAdapter;

    private TextView view_exit_app,door_status;
    private LinearLayout door_status_01;





    //bluetooth
    static final private int GET_DEVICE = 0;
    public static  BluetoothComm bluetoothComm = new BluetoothComm();
    public static DoorStatus doorStatus = new DoorStatus();


    public static PageHomelayout pageHomelayout = new PageHomelayout();





    //Handle case
    public final static  int BT_CONNECTED = 2;
    public final static int BT_CONNECTFLASE = 3;
    public final static int BT_RECEIVE = 4;
    public final static int BT_ERROR = 5;
    public final static int BT_ERROR2 = 6;
    public final static int BT_DISCONNECT = 7;
    public final static int SEND_WAITE = 88;
    public final static int POPUP_HIDE = 99;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //初始化蓝牙
        bluetoothComm.bluetoothInitialize(this, handler);

        //layout
        intLayout();






        if (statusBarCompat) {
            StatusBarCompat.compat(this, ContextCompat.getColor(this, R.color.colorPrimary));
            transparent19and20();
        }

        findViews(); //获取控件

        toolbar.setTitle("LAFAYA");//设置Toolbar标题
        toolbar.setTitleTextColor(Color.parseColor("#ffffff")); //设置标题颜色
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //创建返回键，并实现打开关/闭监听
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        //设置菜单列表
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, lvs);
        lvLeftMenu.setAdapter(arrayAdapter);

        lvLeftMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "点击" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void intLayout() {
        pageHomelayout.activityHomelayout(this,handler);
    }


    //返回键监听
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU && mDrawerLayout != null) {
            if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            } else {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
            return true;
        }  else if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            } else {
                if ((System.currentTimeMillis() - DOUBLE_CLICK_TIME) > 2000) {
                    Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
                    DOUBLE_CLICK_TIME = System.currentTimeMillis();
                } else {
                    finish();
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void transparent19and20() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    private void findViews() {
        toolbar = (Toolbar) findViewById(R.id.tl_custom);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.dl_left);
        lvLeftMenu = (ListView) findViewById(R.id.lv_left_menu);

        view_exit_app = (TextView)findViewById(R.id.view_exit_app);
        view_exit_app.setVisibility(View.GONE);
        door_status = (TextView)findViewById(R.id.door_status);
        door_status_01 = (LinearLayout)findViewById(R.id.door_status_01);



    }








    //弹出窗口显示
    public  void popupView(String textstring,int time) {
        view_exit_app.setText(textstring);
        view_exit_app.setVisibility(View.VISIBLE);
        Timer tExit = new Timer();
        tExit.schedule(new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = POPUP_HIDE;
                handler.sendMessage(msg);
            }
        }, time); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
    }



    @Override
    //收到其它窗口结束时的返回值
    protected  void onActivityResult(int requestCode, int resultCode,Intent data){
        if(requestCode == GET_DEVICE){
            //蓝牙连接
            if(resultCode == RESULT_OK){
                //蓝牙连接
                popupView("正在连接自动门......",1000);
                bluetoothComm.bluetoothconnect(data,handler,MainActivity.this);
            }
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        //应用的最后一个Activity关闭时应释放DB
        //dbManager.closeDB();
        //应用的最后一个Activity关闭时应释放bluetooth
        bluetoothComm.bluetooth.removeCommunicationCallback();
        bluetoothComm.bluetooth.disconnect();

    }









    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case BT_CONNECTED:    //蓝牙连接成功
                    doorStatus.door_connect = true;
                    popupView("自动门已连接，正在加载自动门数据。", 2000);
                    door_status.setText("已连接");
                    door_status_01.setVisibility(View.GONE);
                    break;
                case BT_CONNECTFLASE:
                    doorStatus.door_connect = false;
                    popupView("自动门连接失败，请重新连接。",2000);
                    door_status.setText("未连接");
                    door_status_01.setVisibility(View.VISIBLE);
                    break;
                case BT_ERROR:
                    popupView("自动门连接错误",2000);
                    break;
                case BT_ERROR2:
                    popupView("自动门连接错误",2000);
                    break;
                case BT_DISCONNECT://bluetooth断开成功
                    doorStatus.door_connect = false;
                    popupView("自动门已断开",2000);
                    break;
                case SEND_WAITE:
                    bluetoothComm.resend_count++;
                    if(bluetoothComm.resend_count > 5){
                        bluetoothComm.resend_count = 0;
                        bluetoothComm.sendwaittime.cancel();
                        bluetoothComm.sendwaittime_flag = false;
                    }
                    else{
                        bluetoothComm.reSendMessage();
                    }
                    break;
                case POPUP_HIDE:
                    view_exit_app.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    };



}
