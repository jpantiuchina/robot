package it.unibz.jpantiuchina.robot.hardware;

import android.util.Log;

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

    private final ProximitySensor[] proximitySensors = new ProximitySensor[8];
    {
        for (int i = 0; i < proximitySensors.length; i++)
            proximitySensors[i] = new ProximitySensor(i);
    }

    private final InfraredSensor[] infraredSensors = new InfraredSensor[2];
    {
        for (int i = 0; i < infraredSensors.length; i++)
            infraredSensors[i] = new InfraredSensor(i);
    }

    private final ThermalSensor thermalSensor = new ThermalSensor();

    private final byte[] scanResponseBuffer = new byte[60];
    private final byte[] scanCommandBuffer = new byte[] { 0x5A, 0x04, 0, 0 };


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
     */
    private void setMotorSpeedsAndUpdateSensors(int leftMotorSpeed, int rightMotorSpeed) throws IOException
    {
        Log.i("Robot", "Scan " + leftMotorSpeed + " " + rightMotorSpeed);

        // Sending scan command to robot
        scanCommandBuffer[2] = (byte) leftMotorSpeed;
        scanCommandBuffer[3] = (byte) rightMotorSpeed;
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


    public void setMotorSpeedsAndUpdateSensorsWithoutExceptions(int leftMotorSpeed, int rightMotorSpeed)
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




    public boolean danger ()
    {
        return proximitySensors[2].getRangeInCm() <= 20 || proximitySensors[3].getRangeInCm() <= 20 || proximitySensors[4].getRangeInCm() <= 20 || proximitySensors[5].getRangeInCm() <= 20;
    }

    public boolean isDangerOnRight ()
    {
        return proximitySensors[5].getRangeInCm() <= 20 || proximitySensors[3].getRangeInCm() <= 20;
    }

    public boolean isDangerOnLeft ()
    {
        return proximitySensors[4].getRangeInCm() <= 20 || proximitySensors[2].getRangeInCm() <= 20;
    }

    public ProximitySensor getProximitySensorDataBySensorNumber(int i)
    {
        return proximitySensors[i - 1];
    }

    public ThermalSensor getThermalSensor()
    {
        return thermalSensor;
    }


    @Override
    public String toString()
    {
        String result = "Sensor Data\n";


        for (ProximitySensor proximitySensor : proximitySensors)
            result += proximitySensor + "\n";



        for (InfraredSensor infraredSensor : infraredSensors)
            result += infraredSensor + "\n";

        result += thermalSensor + "\n";


        return result;
    }
}

