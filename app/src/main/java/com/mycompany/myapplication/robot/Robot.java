package com.mycompany.myapplication.robot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Robot interface abstraction. Does not know how to connect to robot physically,
 * just gets an InputStream and an OutputStream for communication. Knows how to
 * control robot motors and get sensor data (ROBOSPINE command SCAN subcommand).
 */
public final class Robot
{
    private final InputStream in;
    private final OutputStream out;
    private final SensorData sensorData = new SensorData();
    private final byte[] buffer = new byte[60];
    private final byte[] command = new byte[] { 0x5A, 0x04, 0, 0 };


    public Robot(InputStream inputStream, OutputStream outputStream)
    {
        this.in = inputStream;
        this.out = outputStream;
    }

    /**
     * Controls robot motor speed (will be stopped automatically in 2 sec)
     *
     * @param leftMotorSpeed 0 stop, 1 full speed forward, -1 full speed backward, 2 slower
     * @param rightMotorSpeed like leftMotorSpeed
     * @return always the same object of type SensorData, that is modified on each call
     * @throws IOException
     */
    public SensorData scan(int leftMotorSpeed, int rightMotorSpeed) throws IOException
    {
        command[2] = (byte) leftMotorSpeed;
        command[3] = (byte) rightMotorSpeed;
        out.write(command);

        readFullBuffer(buffer);

        sensorData.readFromScanResult(buffer);

        return sensorData;
    }


    private void readFullBuffer(byte[] buffer) throws IOException
    {
        int read = 0;
        while (read < buffer.length)
            read += in.read(buffer, read, buffer.length - read);
    }
}
