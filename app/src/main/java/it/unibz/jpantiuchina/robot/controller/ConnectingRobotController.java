package it.unibz.jpantiuchina.robot.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import it.unibz.jpantiuchina.robot.controller.RobotController.Direction;



/**
 * The same as RobotController, but establishes and reestablished Bluetooth connection to the device
 */
public final class ConnectingRobotController
{
    public enum Status {DISCOVERING, CONNECTING, CONNECTED}

    public interface ConnectingRobotControllerStatusListener
    {
        void onConnectingRobotControllerStatusChanged(ConnectingRobotController source);
    }

    private static final String TAG = "ConnectingRC";


    private static final String ROBOT_BLUETOOTH_NAME = "HC-06";
    // http://stackoverflow.com/questions/23963815/sending-data-from-android-to-arduino-with-hc-06-bluetooth-module
    private static final UUID ARDUINO_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private final ContextWrapper contextWrapper;
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket socket;

    private final ConnectingRobotControllerStatusListener statusListener;
    private RobotController controller;

    private float speed;

    private Status status;

    public ConnectingRobotController(ContextWrapper contextWrapper, ConnectingRobotControllerStatusListener statusListener)
    {
        this.contextWrapper = contextWrapper;
        this.statusListener = statusListener;

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        contextWrapper.registerReceiver(bluetoothDeviceFoundReceiver, filter);

        startDiscovery();
    }

    private void setStatus(Status status)
    {
        this.status = status;
        statusListener.onConnectingRobotControllerStatusChanged(this);
    }

    public Status getStatus()
    {
        return status;
    }


    public void shutdown()
    {
        if (status == Status.DISCOVERING)
            bluetoothAdapter.cancelDiscovery();
        closeConnection();
        contextWrapper.unregisterReceiver(bluetoothDeviceFoundReceiver);
    }



    private void startDiscovery()
    {
        Log.i(TAG, "Searching for device " + ROBOT_BLUETOOTH_NAME);
        setStatus(Status.DISCOVERING);
        bluetoothAdapter.startDiscovery();
    }



    private void onBluetoothDiscoveryFinished()
    {
        Log.i(TAG, "Bluetooth discovery finished");
        if (status == Status.DISCOVERING)
            startDiscovery();
    }


    private void closeConnection()
    {
        controller = null;
        if (socket != null)
        {
            try
            {
                socket.close();
                socket = null;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }


    private void onBluetoothDeviceFound(BluetoothDevice device)
    {
        Log.i(TAG, "Found Bluetooth device " + device.getName() + ' ' + device.getAddress());

        if (!ROBOT_BLUETOOTH_NAME.equals(device.getName()))
            return;

        setStatus(Status.CONNECTING);
        bluetoothAdapter.cancelDiscovery();
        Log.i(TAG, "Connecting to this device");

        try
        {
            UUID uuid = ARDUINO_SPP;
            Log.i(TAG, "Connecting to UUID " + uuid);
            socket = device.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
            controller = new RobotController(socket.getInputStream(), socket.getOutputStream());
            controller.setSpeed(speed);
            setStatus(Status.CONNECTED);
            Log.i(TAG, "Successfully connected");
        }
        catch (IOException e)
        {
            onSocketFailure(e);
        }
    }



    private void onSocketFailure(IOException e)
    {
        Log.e(TAG, "Connection error", e);
        closeConnection();
        if (status != Status.DISCOVERING)
            startDiscovery();
    }


    /////////////////////////////// RobotController DELEGATES //////////////////////////////////////

    public void tick()
    {
        if (controller != null)
        {
            try
            {
                controller.tick();
            }
            catch (IOException e)
            {
                onSocketFailure(e);
            }
        }
    }


    public void setSpeed(float speed)
    {
        this.speed = speed;
        if (controller != null)
        {
            try
            {
                controller.setSpeed(speed);
            }
            catch (IOException e)
            {
                onSocketFailure(e);
            }
        }
    }


    public void setDirection(Direction direction)
    {
        if (controller != null)
        {
            try
            {
                controller.setDirection(direction);
            }
            catch (IOException e)
            {
                onSocketFailure(e);
            }
        }
    }

    public Direction getDirection()
    {
        return controller != null ? controller.getDirection() : Direction.STOP;
    }

    public int getFilteredFrontLeftObstacleDistanceInCm()
    {
        return controller != null ? controller.getFilteredFrontLeftObstacleDistanceInCm() : -1;
    }

    public int getFilteredFrontRightObstacleDistanceInCm()
    {
        return controller != null ? controller.getFilteredFrontRightObstacleDistanceInCm() : -1;
    }

    public int getFilteredBackLeftObstacleDistanceInCm()
    {
        return controller != null ? controller.getFilteredBackLeftObstacleDistanceInCm() : -1;
    }

    public int getFilteredBackRightObstacleDistanceInCm()
    {
        return controller != null ? controller.getFilteredBackRightObstacleDistanceInCm() : -1;
    }

    public float getBatteryVoltage()
    {
        return controller != null ? controller.getBatteryVoltage() : -1;
    }


    //////////////////////////// BLUETOOTH DISCOVERY LISTENER //////////////////////////////////////


    private final BroadcastReceiver bluetoothDeviceFoundReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
            {
                onBluetoothDiscoveryFinished();
            }

            if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND))
            {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                onBluetoothDeviceFound(bluetoothDevice);
            }
        }
    };
}
