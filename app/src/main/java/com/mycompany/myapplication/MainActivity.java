package com.mycompany.myapplication;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.IOException;
import java.util.UUID;


public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        System.out.println("------------------------------------------");
        BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (myBluetoothAdapter == null)
        {
            System.out.println("Device does not support bluetooth");
        } else if (myBluetoothAdapter.isEnabled())
        {
            System.out.println("Bluetooth is activated");

            // Register the BroadcastReceiver
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestro
            myBluetoothAdapter.startDiscovery();
        }

    }


//    public static int REQUEST_BLUETOOTH = 1;
    public static String MY_DEVICE_NAME = "new-lenovo";
//    public static String MY_DEVICE_NAME = "HC-06";
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;


    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction()))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                System.err.println("Found Bluetooth device " + device.getName() + " " + device.getAddress());

                if (MY_DEVICE_NAME.equals(device.getName()))
                {
                    System.err.println("Connecting to this device");
                    Thread myThread = new ConnectThread(device);
                    myThread.start();
                }
            }
        }
    };

    private class ConnectThread extends Thread
    {

        @SuppressLint("NewApi")
        public ConnectThread(BluetoothDevice device)
        {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try
            {
                // MY_UUID is the app's UUID string, also used by the server code

                UUID uuid = device.getUuids()[0].getUuid();
                System.err.println("Connecting to UUID " + uuid);
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e)
            {
                return;
            }
            mmSocket = tmp;
        }

        public void run()
        {

            try
            {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                System.out.println("Connected");

            } catch (IOException connectException)
            {
                // Unable to connect; close the socket and get out
                try
                {
                    mmSocket.close();
                } catch (IOException closeException)
                {
                }
                return;
            }

        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel()
        {
            try
            {
                mmSocket.close();
            } catch (IOException e)
            {
            }
        }

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void turnLeft(View view)
    {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
    }


    private void log(String s)
    {
        System.err.println(s);
    }



}





