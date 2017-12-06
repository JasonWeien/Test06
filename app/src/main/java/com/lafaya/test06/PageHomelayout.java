package com.lafaya.test06;

import android.app.Activity;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * Created by Administrator on 2017/11/10.
 */
public class PageHomelayout {
    private ImageButton button_home_door,open_door;
    private Activity activity;
    private Handler handler;

    //layout 配置
    public void activityHomelayout( Activity inactivity, Handler inhandler){
        activity = inactivity;
        handler = inhandler;


        button_home_door = (ImageButton)activity.findViewById(R.id.button_home_door);
        button_home_door.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.doorStatus.door_connect) {
                    MainActivity.bluetoothComm.connectDevice(activity, false, handler);
                } else {
                    MainActivity.bluetoothComm.connectDevice(activity, true, handler);
                }
            }
        });
        button_home_door.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (MainActivity.doorStatus.door_connect) {
                        ((ImageButton) view).setImageDrawable(activity.getDrawable(R.drawable.door_off));
                    } else {
                        ((ImageButton) view).setImageDrawable(activity.getDrawable(R.drawable.door_on));
                    }
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (MainActivity.doorStatus.door_connect) {
                        ((ImageButton) view).setImageDrawable(activity.getDrawable(R.drawable.door_on));
                    } else {
                        ((ImageButton) view).setImageDrawable(activity.getDrawable(R.drawable.door_off));
                    }
                }
                return false;
            }
        });


        open_door = (ImageButton)activity.findViewById(R.id.open_door);
        open_door.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    ((ImageView) view).setImageDrawable(activity.getDrawable(R.drawable.shape_corners_big_black));
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    ((ImageView) view).setImageDrawable(activity.getDrawable(R.drawable.icon_big_mode_manual));

                }
                return false;
            }
        });
        open_door.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.bluetoothComm.bluetooth.send("0" + "A");
            }
        });

    }
}
