package it.unibz.jpantiuchina.robot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.UUID;



public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener
{
    private static final int ENABLE_BT_REQUEST_TAG = 546; // means nothing, just an unique integer
    private static final String TAG = "MainActivity";
    private static final String ROBOT_BLUETOOTH_NAME = "HC-06";
    // http://stackoverflow.com/questions/23963815/sending-data-from-android-to-arduino-with-hc-06-bluetooth-module
    private static final UUID ARDUINO_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int TICK_DELAY = 100;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;

    private RobotController robotController;
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SeekBar sbSpeed = (SeekBar) findViewById(R.id.sbSpeed);
        sbSpeed.setOnSeekBarChangeListener(this);

        textView = (TextView) findViewById(R.id.textView);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null)
            throw new RuntimeException("Device does not support bluetooth");

        if (!bluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_TAG);
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothDeviceFoundReceiver, filter);

        if (bluetoothAdapter.isEnabled())
            startDiscovery();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == ENABLE_BT_REQUEST_TAG)
        {
            if (resultCode == RESULT_OK)
                startDiscovery();
            else
                throw new RuntimeException("Cannot work with disabled Bluetooth.");
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy()
    {
        if (socket != null)
        {
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        unregisterReceiver(bluetoothDeviceFoundReceiver);
        super.onDestroy();
    }


    private void startDiscovery()
    {
        Log.i(TAG, "Searching for device " + ROBOT_BLUETOOTH_NAME);
        bluetoothAdapter.startDiscovery();
    }




    private void onBluetoothDeviceFound(BluetoothDevice device) throws Exception
    {
        Log.i(TAG, "Found Bluetooth device " + device.getName() + ' ' + device.getAddress());

        if (!ROBOT_BLUETOOTH_NAME.equals(device.getName()))
            return;

        Log.i(TAG, "Connecting to this device");
        UUID uuid = ARDUINO_SPP;

        Log.i(TAG, "Connecting to UUID " + uuid);
        socket = device.createRfcommSocketToServiceRecord(uuid);

        bluetoothAdapter.cancelDiscovery(); // Save resources and prevent double connects

        try
        {
            socket.connect();
        }
        catch (IOException e)
        {
            socket.close();
            Log.e(TAG, "Cannot connect", e);
            throw new RuntimeException(e);
        }

        Log.i(TAG, "Successfully connected");

        robotController = new RobotController(socket.getInputStream(), socket.getOutputStream());
        tickTimerHandler.postDelayed(tickTimerRunnable, TICK_DELAY);
    }





    public void turnLeft(View view)
    {
        Log.i(TAG, "left");
        robotController.turnLeft();
    }

    public void turnRight(View view)
    {
        Log.i(TAG, "right");
        robotController.turnRight();
    }

    public void goForward(View view)
    {
        Log.i(TAG, "forward");
        robotController.goForward();
    }

    public void goBackward(View view)
    {
        Log.i(TAG, "backward");
        robotController.goBackward();
    }



    /////////////////////////////////////// SEEK BAR SUPPORT ///////////////////////////////////////

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        robotController.setSpeed(127 - progress);
    }
    @Override public void onStartTrackingTouch(SeekBar seekBar) { }
    @Override public void onStopTrackingTouch(SeekBar seekBar) { }



    ////////////////////////////////////////// TICK TIMER //////////////////////////////////////////

    // http://stackoverflow.com/questions/4597690/android-timer-how
    private final Handler tickTimerHandler = new Handler();
    private final Runnable tickTimerRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            if (robotController != null)
            {
                robotController.tick();
                textView.setText(robotController.getStatus());
            }
            tickTimerHandler.postDelayed(this, TICK_DELAY);
        }
    };



    //////////////////////////////// BLUETOOTH DISCOVERY SUPPORT ///////////////////////////////////

    private final BroadcastReceiver bluetoothDeviceFoundReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            try
            {
                onBluetoothDeviceFound(bluetoothDevice);
            }
            catch (Exception e)
            {
                Log.e(TAG, "Error in device discovery", e);
            }
        }
    };
}





