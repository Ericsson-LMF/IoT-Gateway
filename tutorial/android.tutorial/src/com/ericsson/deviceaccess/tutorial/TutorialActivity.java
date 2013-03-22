package com.ericsson.deviceaccess.tutorial;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.ericsson.deviceaccess.api.GenericDevice;
import com.ericsson.deviceaccess.api.GenericDeviceFramework;
import com.ericsson.deviceaccess.api.GenericDeviceFrameworkListener;
import com.ericsson.deviceaccess.spi.schema.SchemaBasedGenericDevice;

public class TutorialActivity extends Activity {
    private GenericDeviceFramework gdaFramework;
    private SchemaBasedGenericDevice dummyDev;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Here we ask Android framework to create binding between this activity and
        // a service named GenericDeviceFramework service
        Intent intent = new Intent(GenericDeviceFramework.class.getName());
        bindService(intent, gdaFrameworkConn, BIND_AUTO_CREATE);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        if (gdaFramework != null) {
            if (dummyDev != null) {
                try {
                    gdaFramework.unregister(dummyDev);
                } catch (RemoteException e) {
                    Log.e(getClass().getName(), "Failed to unregister device", e);
                }
                dummyDev = null;
            }
            gdaFramework = null;
        }
        unbindService(gdaFrameworkConn);
        super.onDestroy();
    }

    // This is a listener of service connection events. This is just a usual way of
    // getting Android service connected to your application. So please look at
    // generic information on how to use Android service to know more.
    private ServiceConnection gdaFrameworkConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder binder) {
            Log.d(getClass().getName(), "GDA framework service is connected");
            gdaFramework = GenericDeviceFramework.Stub.asInterface(binder);

            try {
                gdaFramework.addListener(new GDAFrameworkListenerImpl());
            } catch (RemoteException e) {
                Log.e(getClass().getName(), "Faild to add myself as listener of GDA framework events");
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(getClass().getName(), "GDA framework service is disconnected");
            gdaFramework = null;
        }

    };

    // A listener implementation to track of events occurred on GenericDeviceFramework service
    private class GDAFrameworkListenerImpl extends GenericDeviceFrameworkListener.Stub {

        @Override
        public void onFrameworkReady() throws RemoteException {
            Log.d(getClass().getName(), "GDA framework got ready!");
            dummyDev = new DummyDevice();
            dummyDev.setId("androidTutorial");
            dummyDev.setName("Device created by tutorial Android app");
            dummyDev.setOnline(true);
            dummyDev.setIcon("http://ergo.ericssonresearch.jp/res/icons/android.jpg");

            gdaFramework.register(dummyDev);
        }

        @Override
        public void onFrameworkDestroy() throws RemoteException {
            Log.d(getClass().getName(), "GDA framework destroyed");
        }


        @Override
        public boolean addingDevice(GenericDevice dev) throws RemoteException {
            Log.d(getClass().getName(), "A new device discovered! " + dev.getName());
            return true;
        }

        @Override
        public void modifiedDevice(GenericDevice dev, String updatedPaths)
                throws RemoteException {
            Log.d(getClass().getName(), "Device got parameter update! " + dev.getName());
        }

        @Override
        public void removedDevice(GenericDevice dev) throws RemoteException {
            Log.d(getClass().getName(), "Device is removed ! " + dev.getName());
        }

    }


}
