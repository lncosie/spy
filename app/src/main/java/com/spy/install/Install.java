package com.spy.install;


import java.io.File;
import java.lang.reflect.Method;

import android.content.pm.IPackageInstallObserver;
import android.content.pm.IPackageManager;
import android.app.Activity;

import android.net.Uri;

import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;


public class Install  {
    /**** 表示安装时以更新方式安装，即app不存在时安装，否则进行卸载再安装 ****/
    private final int INSTALL_REPLACE_EXISTING = 0x00000002;
    /**** Apk存储目录，这里我放置在了SDcard的Download目录下 ****/
    private final String sdPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    public void installPackage(String apkName) {
        PackageInstallObserver installObserver = new PackageInstallObserver();
        try {
            String apkPath = sdPath.concat("/").concat(apkName).concat(".apk");
            Class<?> ServiceManager = Class.forName("android.os.ServiceManager");
            Method getService = ServiceManager.getDeclaredMethod("getService", String.class);
            getService.setAccessible(true);
            IBinder packAgeBinder = (IBinder) getService.invoke(null, "package");
            IPackageManager iPm = IPackageManager.Stub.asInterface(packAgeBinder);
            iPm.installPackage(Uri.fromFile(new File(apkPath)), installObserver, INSTALL_REPLACE_EXISTING, new File(apkPath).getPath());

        } catch (Exception e) {
            e.printStackTrace();
            try {
                installObserver.packageInstalled(null, -1);
            } catch (RemoteException ignore) {

            }
        }
    }

    /**
     * 安装监听
     */
    public class PackageInstallObserver extends IPackageInstallObserver.Stub
    {

        //@Override
        public void packageInstalled(String packageName, int returnCode) throws RemoteException {
            if (returnCode == 1) //返回1表示安装成功，否则安装失败
            {
                //Toast.makeText(MainActivity.this, "安装成功！", Toast.LENGTH_SHORT).show();
                Log.e("Installed", "packageName=" + packageName + ",returnCode=" + returnCode);
            } else {
                //Toast.makeText(MainActivity.this, "安装失败！", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
