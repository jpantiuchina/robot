package it.unibz.jpantiuchina.robot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import it.unibz.jpantiuchina.robot.hardware.Robot;


public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener
{
    private static final int ENABLE_BT_REQUEST_TAG = 1; // means nothing, just an unique integer
    private static final int SPEECH_REQUEST_TAG = 2; // same here


    private static final String TAG = "MainActivity";
    private static final String ROBOT_BLUETOOTH_NAME = "HC-06";
    // http://stackoverflow.com/questions/23963815/sending-data-from-android-to-arduino-with-hc-06-bluetooth-module
    private static final UUID ARDUINO_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int TICK_DELAY = 100;
    private static final int SPEED_BAR_FLOAT_TO_INT_MULTIPLIER = 100;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;

    private RobotController robotController;
    private SeekBar sbSpeed;

    private TextView tvFrontRight;
    private TextView tvFrontLeft;
    private TextView tvBackLeft;
    private TextView tvBackRight;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sbSpeed = (SeekBar) findViewById(R.id.sbSpeed);
        sbSpeed.setOnSeekBarChangeListener(this);
        sbSpeed.setMax((int) (Robot.MAX_SPEED_IN_CMPS * SPEED_BAR_FLOAT_TO_INT_MULTIPLIER));

        tvFrontLeft  = (TextView) findViewById(R.id.tvFrontLeft );
        tvFrontRight = (TextView) findViewById(R.id.tvFrontRight);
        tvBackLeft   = (TextView) findViewById(R.id.tvBackLeft  );
        tvBackRight  = (TextView) findViewById(R.id.tvBackRight );

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

        if (requestCode == SPEECH_REQUEST_TAG && resultCode == RESULT_OK)
        {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String text = results.get(0).toLowerCase(Locale.US);
            recognizeSpeech(text);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void recognizeSpeech(String text)
    {
        if (text.contains("stop") || text.contains("стоп") || text.contains("стой"))
            stop();
        else
        if (text.contains("back") || text.contains("назад"))
            goBackward(null);
        else
        if (text.contains("forward") || text.contains("вперед") || text.equals("go"))
            goForward(null);
        else
        if (text.contains("left") || text.contains("лево"))
            turnLeft(null);
        else
        if (text.contains("right") || text.contains("право"))
            turnRight(null);
        else
        if (text.contains("fast") || text.contains("быстр"))
            sbSpeed.setProgress(sbSpeed.getProgress() * 2);
        else
        if (text.contains("slow") || text.contains("медлен"))
            sbSpeed.setProgress(sbSpeed.getProgress() / 2);
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
        sbSpeed.setProgress(SPEED_BAR_FLOAT_TO_INT_MULTIPLIER * 5); // set speed to 5 cm/s
        tickTimerHandler.postDelayed(tickTimerRunnable, TICK_DELAY);
    }





    public void turnLeft(View view)
    {
        Log.i(TAG, "left");
        if (robotController != null)
            robotController.turnLeft();
    }

    public void turnRight(View view)
    {
        Log.i(TAG, "right");
        if (robotController != null)
            robotController.turnRight();
    }

    public void goForward(View view)
    {
        Log.i(TAG, "forward");
        if (robotController != null)
            robotController.goForward();
    }

    public void goBackward(View view)
    {
        Log.i(TAG, "backward");
        if (robotController != null)
            robotController.goBackward();
    }

    private void stop()
    {
        Log.i(TAG, "stop");
        if (robotController != null)
            robotController.stop();
    }


    public void listen(View view)
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Forward, back, left, right, stop?");
        startActivityForResult(intent, SPEECH_REQUEST_TAG);
    }



    private static void updateDistanceText(TextView textView, int distance)
    {
        textView.setText(distance + " cm");

        int color = Color.BLACK;
        if (distance < 10)
            color = Color.RED;
        else
        {
            if (distance < 50)
                color = 0xFFFF7F00; // should be orange
        }

        textView.setTextColor(color);
    }

    private void onTick()
    {
        if (robotController != null)
        {
            robotController.tick();
            Robot robot = robotController.getRobot();
            updateDistanceText(tvFrontLeft,  robot.getFilteredFrontLeftObstacleDistanceInCm ());
            updateDistanceText(tvFrontRight, robot.getFilteredFrontRightObstacleDistanceInCm());
            updateDistanceText(tvBackLeft,   robot.getFilteredBackLeftObstacleDistanceInCm  ());
            updateDistanceText(tvBackRight,  robot.getFilteredBackRightObstacleDistanceInCm ());
        }
        tickTimerHandler.postDelayed(tickTimerRunnable, TICK_DELAY);
    }



    /////////////////////////////////////// SEEK BAR SUPPORT ///////////////////////////////////////

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        if (robotController != null)
            robotController.setSpeed((double) progress / SPEED_BAR_FLOAT_TO_INT_MULTIPLIER);
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
            onTick();
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





