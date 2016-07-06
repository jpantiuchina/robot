package it.unibz.jpantiuchina.robot.hardware;

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
    public static final double MAX_SPEED_IN_CMPS = 100;

    private final InputStream in;
    private final OutputStream out;

    private final ProximitySensor[] proximitySensors = new ProximitySensor[8];
    private final ProximitySensor[] forwardLeftLookingProximitySensors;
    private final ProximitySensor[] forwardRightLookingProximitySensors;
    private final ProximitySensor[] backwardLeftLookingProximitySensors;
    private final ProximitySensor[] backwardRightLookingProximitySensors;
    private final InfraredSensor[] infraredSensors = new InfraredSensor[2];
    private final InfraredSensor forwardLeftLookingInfraredSensor;
    private final InfraredSensor forwardRightLookingInfraredSensor;
    private final ThermalSensor thermalSensor = new ThermalSensor();

    private final byte[] scanResponseBuffer = new byte[60];
    private final byte[] scanCommandBuffer = {0x5A, 0x04, 0, 0};


    public Robot(InputStream inputStream, OutputStream outputStream)
    {
        in = inputStream;
        out = outputStream;
        for (int i = 0; i < proximitySensors.length; i++)
            proximitySensors[i] = new ProximitySensor(i);
        forwardLeftLookingProximitySensors   = new ProximitySensor[] {proximitySensors[2], proximitySensors[3]};
        forwardRightLookingProximitySensors  = new ProximitySensor[] {proximitySensors[4], proximitySensors[5]};
        backwardLeftLookingProximitySensors  = new ProximitySensor[] {proximitySensors[0], proximitySensors[1]};
        backwardRightLookingProximitySensors = new ProximitySensor[] {proximitySensors[6], proximitySensors[7]};
        for (int i = 0; i < infraredSensors.length; i++)
            infraredSensors[i] = new InfraredSensor(i);
        forwardLeftLookingInfraredSensor = infraredSensors[0];
        forwardRightLookingInfraredSensor = infraredSensors[1];
    }

    /**
     * @param leftSpeedInCmps speed in cm/s from range [-MAX_SPEED_IN_CMPS, MAX_SPEED_IN_CMPS]
     * @param rightSpeedInCmps like leftMotorSpeedInCmps
     */
    private void setMotorSpeedsAndUpdateSensors(double leftSpeedInCmps, double rightSpeedInCmps) throws IOException
    {
//        Log.i("Robot", "Scan " + leftSpeedInCmps + " cm/s, " + rightSpeedInCmps + " cm/s");

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
    }


    private static byte convertCmpsToMotorSpeedByte(double speed)
    {
        // 0 -- full reverse, 128 -- stop, 255 -- full forward
        speed = Math.max(-MAX_SPEED_IN_CMPS, Math.min(MAX_SPEED_IN_CMPS, speed));
        speed /= MAX_SPEED_IN_CMPS;
        speed *= 127;
        return (byte) (Math.round(speed) + 128);
    }



    public void setMotorSpeedsAndUpdateSensorsWithoutExceptions(double leftMotorSpeed, double rightMotorSpeed)
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



//    public ProximitySensor getProximitySensorDataBySensorNumber(int i)
//    {
//        return proximitySensors[i - 1];
//    }

//    public ThermalSensor getThermalSensor()
//    {
//        return thermalSensor;
//    }


    private static int minFilteredValue(ProximitySensor[] proximitySensors, InfraredSensor infraredSensor)
    {
        int result = Integer.MAX_VALUE;
        if (infraredSensor != null)
            result = infraredSensor.getFilteredRangeInCm();
        for (ProximitySensor sensor : proximitySensors)
            result = Math.min(result, sensor.getFilteredRangeInCm());
        return result;
    }


    public int getFilteredFrontLeftObstacleDistanceInCm()
    {
        return minFilteredValue(forwardLeftLookingProximitySensors, forwardLeftLookingInfraredSensor);
    }

    public int getFilteredFrontRightObstacleDistanceInCm()
    {
        return minFilteredValue(forwardRightLookingProximitySensors, forwardRightLookingInfraredSensor);
    }

    public int getFilteredBackLeftObstacleDistanceInCm()
    {
        return minFilteredValue(backwardLeftLookingProximitySensors, null);
    }

    public int getFilteredBackRightObstacleDistanceInCm()
    {
        return minFilteredValue(backwardRightLookingProximitySensors, null);
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

