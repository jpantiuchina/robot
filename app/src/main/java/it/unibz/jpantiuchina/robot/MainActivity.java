package it.unibz.jpantiuchina.robot;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import it.unibz.jpantiuchina.robot.controller.ConnectingRobotController;
import it.unibz.jpantiuchina.robot.controller.DemoPlayer;
import it.unibz.jpantiuchina.robot.controller.RobotController.Direction;
import it.unibz.jpantiuchina.robot.hardware.Robot;


public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, ConnectingRobotController.ConnectingRobotControllerStatusListener
{
    private static final int ENABLE_BT_REQUEST_TAG = 1; // means nothing, just an unique integer
    private static final int SPEECH_REQUEST_TAG    = 2; // same here
    private static final String TAG = "MainActivity";
    private static final int TICK_DELAY = 100;
    private static final int SPEED_BAR_FLOAT_TO_INT_MULTIPLIER = 100;

    private final Handler handler = new Handler();

    private SeekBar sbSpeed;
    private TextView tvFrontRight;
    private TextView tvFrontLeft;
    private TextView tvBackLeft;
    private TextView tvBackRight;

    private ConnectingRobotController controller;


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

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null)
            throw new RuntimeException("Device does not support bluetooth");

        if (bluetoothAdapter.isEnabled())
        {
            start();
        }
        else
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_TAG);
        }
    }


    private void start()
    {
        controller = new ConnectingRobotController(this, this);
        sbSpeed.setProgress(SPEED_BAR_FLOAT_TO_INT_MULTIPLIER * 5); // set speed to 5 cm/s
        handler.postDelayed(tickTimerRunnable, TICK_DELAY);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == ENABLE_BT_REQUEST_TAG)
        {
            if (resultCode == RESULT_OK)
                start();
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
            controller.setDirection(Direction.BACKWARD);
        else
        if (text.contains("forward") || text.contains("вперед") || text.equals("go"))
            controller.setDirection(Direction.FORWARD);
        else
        if (text.contains("left") || text.contains("лево"))
            controller.setDirection(Direction.LEFT);
        else
        if (text.contains("right") || text.contains("право"))
            controller.setDirection(Direction.RIGHT);
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
        if (controller != null)
            controller.shutdown();
        super.onDestroy();
    }




    @Override
    public void onConnectingRobotControllerStatusChanged(ConnectingRobotController source)
    {
        TextView tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvStatus.setText(source.getStatus().name());
    }



    public void turnLeft(View view)
    {
        Log.i(TAG, "left");
        if (controller != null)
            controller.setDirection(controller.getDirection() == Direction.RIGHT ? Direction.STOP : Direction.LEFT);
    }

    public void turnRight(View view)
    {
        Log.i(TAG, "right");
        if (controller != null)
            controller.setDirection(controller.getDirection() == Direction.LEFT ? Direction.STOP : Direction.RIGHT);
    }

    public void goForward(View view)
    {
        Log.i(TAG, "forward");
        if (controller != null)
            controller.setDirection(controller.getDirection() == Direction.BACKWARD ? Direction.STOP : Direction.FORWARD);
    }

    public void goBackward(View view)
    {
        Log.i(TAG, "backward");
        if (controller != null)
            controller.setDirection(controller.getDirection() == Direction.FORWARD ? Direction.STOP : Direction.BACKWARD);
    }

    private void stop()
    {
        Log.i(TAG, "stop");
        if (controller != null)
            controller.setDirection(Direction.STOP);
    }


    public void listen(View view)
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Forward, back, left, right, stop?");
        startActivityForResult(intent, SPEECH_REQUEST_TAG);
    }




    private DemoPlayer demoPlayer;

    public void demo(View view)
    {
        if (demoPlayer == null)
        {
            demoPlayer = new DemoPlayer(controller);
        }

        if (demoPlayer.isPlaying())
            demoPlayer.stop();
        else
            demoPlayer.play();
    }



    private static void updateDistanceText(TextView textView, int distance)
    {
        if (distance >= 0)
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
        else
        {
            textView.setText("?");
            textView.setTextColor(Color.BLACK);
        }
    }

    private void onTick()
    {
        controller.tick();
        updateDistanceText(tvFrontLeft,  controller.getFilteredFrontLeftObstacleDistanceInCm ());
        updateDistanceText(tvFrontRight, controller.getFilteredFrontRightObstacleDistanceInCm());
        updateDistanceText(tvBackLeft,   controller.getFilteredBackLeftObstacleDistanceInCm  ());
        updateDistanceText(tvBackRight,  controller.getFilteredBackRightObstacleDistanceInCm ());

        handler.postDelayed(tickTimerRunnable, TICK_DELAY);
    }



    /////////////////////////////////////// SEEK BAR SUPPORT ///////////////////////////////////////

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        if (controller != null)
            controller.setSpeed((float) progress / SPEED_BAR_FLOAT_TO_INT_MULTIPLIER);
    }
    @Override public void onStartTrackingTouch(SeekBar seekBar) { }
    @Override public void onStopTrackingTouch(SeekBar seekBar) { }



    ////////////////////////////////////////// TICK TIMER //////////////////////////////////////////

    // http://stackoverflow.com/questions/4597690/android-timer-how
    private final Runnable tickTimerRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            onTick();
        }
    };

}





