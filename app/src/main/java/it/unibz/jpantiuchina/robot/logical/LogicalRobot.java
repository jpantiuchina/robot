package it.unibz.jpantiuchina.robot.logical;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import it.unibz.jpantiuchina.robot.hardware.Robot;

/**
 * Our configured robot. Knows sensor locations and filters them.
 */
public final class LogicalRobot
{
    private final Robot robot;

    private static final int[] FORWARD_LEFT_LOOKING_PROXIMITY_SENSORS   = {3, 4};
    private static final int[] FORWARD_RIGHT_LOOKING_PROXIMITY_SENSORS  = {5, 6};
    private static final int[] BACKWARD_LEFT_LOOKING_PROXIMITY_SENSORS  = {1, 2};
    private static final int[] BACKWARD_RIGHT_LOOKING_PROXIMITY_SENSORS = {7, 8};
    private static final int FORWARD_LEFT_LOOKING_INFRARED_SENSOR  = 1;
    private static final int FORWARD_RIGHT_LOOKING_INFRARED_SENSOR = 2;

    private final FilteredValue frontRightObstacleDistance = new FilteredValue();
    private final FilteredValue frontLeftObstacleDistance  = new FilteredValue();
    private final FilteredValue backRightObstacleDistance  = new FilteredValue();
    private final FilteredValue backLeftObstacleDistance   = new FilteredValue();


    public LogicalRobot(InputStream inputStream, OutputStream outputStream)
    {
        robot = new Robot(inputStream, outputStream);
    }


    public void setMotorSpeedsAndUpdateSensors(float leftSpeedInCmps, float rightSpeedInCmps) throws IOException
    {
        robot.setMotorSpeedsAndUpdateSensors(leftSpeedInCmps, rightSpeedInCmps);

        frontLeftObstacleDistance. addNewValue(minFilteredValue(FORWARD_LEFT_LOOKING_PROXIMITY_SENSORS, FORWARD_LEFT_LOOKING_INFRARED_SENSOR));
        frontRightObstacleDistance.addNewValue(minFilteredValue(FORWARD_RIGHT_LOOKING_PROXIMITY_SENSORS, FORWARD_RIGHT_LOOKING_INFRARED_SENSOR));
        backLeftObstacleDistance.  addNewValue(minFilteredValue(BACKWARD_LEFT_LOOKING_PROXIMITY_SENSORS, 0));
        backRightObstacleDistance. addNewValue(minFilteredValue(BACKWARD_RIGHT_LOOKING_PROXIMITY_SENSORS, 0));
    }




    private int minFilteredValue(int[] proximitySensors, int infraredSensor)
    {
        int result = Integer.MAX_VALUE;
        if (infraredSensor != 0)
            result = robot.getInfraredSensorRangeInCm(infraredSensor);
        for (int sensor : proximitySensors)
            result = Math.min(result, robot.getProximitySensorRangeInCm(sensor));
        return result;
    }



    public int getFilteredFrontLeftObstacleDistanceInCm()
    {
        return frontLeftObstacleDistance.getFilteredValue();
    }

    public int getFilteredFrontRightObstacleDistanceInCm()
    {
        return frontRightObstacleDistance.getFilteredValue();
    }

    public int getFilteredBackLeftObstacleDistanceInCm()
    {
        return backLeftObstacleDistance.getFilteredValue();
    }

    public int getFilteredBackRightObstacleDistanceInCm()
    {
        return backRightObstacleDistance.getFilteredValue();
    }

    public float getBatteryVoltage()
    {
        return robot.getBatteryVoltage();
    }
}
