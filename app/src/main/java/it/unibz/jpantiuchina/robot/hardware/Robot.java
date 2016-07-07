package it.unibz.jpantiuchina.robot.hardware;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import it.unibz.jpantiuchina.robot.util.RobotMath;


/**
 * Robot interface abstraction. Does not know how to connect to robot physically,
 * just gets an InputStream and an OutputStream for communication. Knows how to
 * control robot motors and get sensor data (ROBOSPINE command SCAN subcommand).
 */
public final class Robot
{
    public static final float MAX_SPEED_IN_CMPS = 100;

    private final InputStream in;
    private final OutputStream out;

    private final ProximitySensor[] proximitySensors = new ProximitySensor[8];
    private final InfraredSensor[] infraredSensors = new InfraredSensor[2];
    private final ThermalSensor thermalSensor = new ThermalSensor();
    private final BatteryVoltage batteryVoltage = new BatteryVoltage();

    private final byte[] scanResponseBuffer = new byte[60];
    private final byte[] scanCommandBuffer = {0x5A, 0x04, 0, 0};


    public Robot(InputStream inputStream, OutputStream outputStream)
    {
        in = inputStream;
        out = outputStream;
        for (int i = 0; i < proximitySensors.length; i++)
            proximitySensors[i] = new ProximitySensor(i);
        for (int i = 0; i < infraredSensors.length; i++)
            infraredSensors[i] = new InfraredSensor(i);
    }

    /**
     * @param leftSpeedInCmps speed in cm/s from range [-MAX_SPEED_IN_CMPS, MAX_SPEED_IN_CMPS]
     * @param rightSpeedInCmps like leftMotorSpeedInCmps
     */
    public void setMotorSpeedsAndUpdateSensors(float leftSpeedInCmps, float rightSpeedInCmps) throws IOException
    {
        // Sending scan command to robot
        scanCommandBuffer[2] = convertCmpsToMotorSpeedByte(leftSpeedInCmps);
        scanCommandBuffer[3] = convertCmpsToMotorSpeedByte(rightSpeedInCmps);
        out.write(scanCommandBuffer);

        // Reading the response
        int read = 0;
        while (read < scanResponseBuffer.length)
            read += in.read(scanResponseBuffer, read, scanResponseBuffer.length - read);

        // Updating sensor data from response
        for (ProximitySensor proximitySensor : proximitySensors)
            proximitySensor.readFromScanResult(scanResponseBuffer);
        for (InfraredSensor infraredSensor : infraredSensors)
            infraredSensor.readFromScanResult(scanResponseBuffer);
        thermalSensor.readFromScanResult(scanResponseBuffer);
        batteryVoltage.readFromScanResult(scanResponseBuffer);
    }


    private static byte convertCmpsToMotorSpeedByte(float speed)
    {
        // 0 is full reverse, 128 is stop, 255 is full forward
        speed = RobotMath.constraint(speed, -MAX_SPEED_IN_CMPS, MAX_SPEED_IN_CMPS);
        speed = RobotMath.map(speed, -MAX_SPEED_IN_CMPS, MAX_SPEED_IN_CMPS, -127, 127);
        return (byte) (Math.round(speed) + 128);
    }



    public void setMotorSpeedsAndUpdateSensorsWithoutExceptions(float leftMotorSpeed, float rightMotorSpeed)
    {
        try
        {
            setMotorSpeedsAndUpdateSensors(leftMotorSpeed, rightMotorSpeed);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }



    public int getProximitySensorRangeInCm(int sensorNumber)
    {
        return proximitySensors[sensorNumber - 1].getRangeInCm();
    }

    public int getInfraredSensorRangeInCm(int sensorNumber)
    {
        return infraredSensors[sensorNumber - 1].getRangeInCm();
    }


    public float getBatteryVoltage()
    {
        return batteryVoltage.getVoltage();
    }





    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder("Sensor Data\n");

        for (ProximitySensor proximitySensor : proximitySensors)
        {
            result.append(proximitySensor);
            result.append('\n');
        }

        for (InfraredSensor infraredSensor : infraredSensors)
        {
            result.append(infraredSensor);
            result.append('\n');
        }

        result.append(thermalSensor);
        result.append('\n');

        return result.toString();
    }
}

