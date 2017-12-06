package com.lafaya.test06;

import android.app.Activity;

/**
 * Created by Administrator on 2017/11/1.
 */
public class DoorStatus {
    public boolean door_connect = false;
    private boolean lafaya_send_flag = false;

    private char cmd_opendoor = 0xC7;
    private char LafayaSTX = 0x7E;

    public static char  sendslidingID = 0x21;








    //生成校验码
    //生成带校验码数据
    //校验码生成：两两参数进行异或（去除握手字和结束字）
    private String lafayacreatecheckcode(char[] msg) {
        lafaya_send_flag = true;
        char sum = 0x00;
        String strmsg = "";
        for(int i = 0; i < msg.length; i++){
            sum ^= msg[i];
            if(msg[i] < 0x0F){
                strmsg += '0';
                strmsg += Integer.toHexString(msg[i] & 0x00FF).toUpperCase();
            }else{
                strmsg += Integer.toHexString(msg[i] & 0x00FF).toUpperCase();
            }
        }
        return strmsg + Integer.toHexString(sum & 0x00FF).toUpperCase() + "\r";
    }



    //开门命令
    public String sendLafayaOpendoor(char addrs){
        char[] msg = new char[]{addrs,cmd_opendoor};
        return LafayaSTX +lafayacreatecheckcode(msg);
    }

    //开门方法
  //  public void lafaya_opendoor_comand(){}

}
