/*
 * Copyright Ericsson AB 2011-2014. All Rights Reserved.
 * 
 * The contents of this file are subject to the Lesser GNU Public License,
 *  (the "License"), either version 2.1 of the License, or
 * (at your option) any later version.; you may not use this file except in
 * compliance with the License. You should have received a copy of the
 * License along with this software. If not, it can be
 * retrieved online at https://www.gnu.org/licenses/lgpl.html. Moreover
 * it could also be requested from Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * BECAUSE THE LIBRARY IS LICENSED FREE OF CHARGE, THERE IS NO
 * WARRANTY FOR THE LIBRARY, TO THE EXTENT PERMITTED BY APPLICABLE LAW.
 * EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR
 * OTHER PARTIES PROVIDE THE LIBRARY "AS IS" WITHOUT WARRANTY OF ANY KIND,
 
 * EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE
 * LIBRARY IS WITH YOU. SHOULD THE LIBRARY PROVE DEFECTIVE,
 * YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR OR CORRECTION.
 *
 * IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING
 * WILL ANY COPYRIGHT HOLDER, OR ANY OTHER PARTY WHO MAY MODIFY AND/OR
 * REDISTRIBUTE THE LIBRARY AS PERMITTED ABOVE, BE LIABLE TO YOU FOR
 * DAMAGES, INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL
 * DAMAGES ARISING OUT OF THE USE OR INABILITY TO USE THE LIBRARY
 * (INCLUDING BUT NOT LIMITED TO LOSS OF DATA OR DATA BEING RENDERED
 * INACCURATE OR LOSSES SUSTAINED BY YOU OR THIRD PARTIES OR A FAILURE
 * OF THE LIBRARY TO OPERATE WITH ANY OTHER SOFTWARE), EVEN IF SUCH
 * HOLDER OR OTHER PARTY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. 
 * 
 */
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
