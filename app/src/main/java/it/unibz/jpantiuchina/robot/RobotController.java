package it.unibz.jpantiuchina.robot;

import java.io.InputStream;
import java.io.OutputStream;

import it.unibz.jpantiuchina.robot.hardware.Robot;

public final class RobotController
{
    public Robot getRobot()
    {
        return robot;
    }

    public void stop()
    {
        setDirection(Direction.STOP);
    }

    private enum Direction { FORWARD, BACKWARD, LEFT, RIGHT, STOP }

    private final Robot robot;

    private Direction currentDirection = Direction.STOP;
    private double speed;

    public RobotController(InputStream inputStream, OutputStream outputStream)
    {
        robot = new Robot(inputStream, outputStream);
    }

    public void setSpeed(double speed)
    {
        this.speed = Math.max(0, Math.min(Robot.MAX_SPEED_IN_CMPS, speed));
        tick();
    }


    private void setDirection(Direction direction)
    {
        currentDirection = direction;
        tick();
    }


    public void goBackward()
    {
        setDirection(currentDirection == Direction.FORWARD ? Direction.STOP : Direction.BACKWARD);
    }

    public void turnLeft()
    {
        setDirection(currentDirection == Direction.RIGHT ? Direction.STOP : Direction.LEFT);
    }

    public void turnRight()
    {
        setDirection(currentDirection == Direction.LEFT ? Direction.STOP : Direction.RIGHT);
    }


    public void goForward()
    {
        setDirection(currentDirection == Direction.BACKWARD ? Direction.STOP : Direction.FORWARD);
    }


    private static double getMaxAllowedSpeedForObstacleDistance(int obstacleDistanceInCm)
    {
        int minDistance = 10;
        int maxDistance = 30;
        obstacleDistanceInCm = Math.max(minDistance, Math.min(maxDistance, obstacleDistanceInCm));
        double fraction = (double) (obstacleDistanceInCm - minDistance) / (maxDistance - minDistance);
        return fraction * Robot.MAX_SPEED_IN_CMPS;
    }


    /**
     * Must be called constantly to support status and speed updates
     */
    public void tick()
    {
        double adjustedSpeed = speed;

        double leftMotorSpeed = 0;
        double rightMotorSpeed = 0;

        switch (currentDirection)
        {
            case FORWARD:
                leftMotorSpeed  =  adjustedSpeed;
                rightMotorSpeed =  adjustedSpeed;
                int frontLeft   = robot.getFilteredFrontLeftObstacleDistanceInCm();
                int frontRight  = robot.getFilteredFrontLeftObstacleDistanceInCm();
                leftMotorSpeed  = Math.min(leftMotorSpeed,  getMaxAllowedSpeedForObstacleDistance(frontRight));
                rightMotorSpeed = Math.min(rightMotorSpeed, getMaxAllowedSpeedForObstacleDistance(frontLeft));
                break;
            case BACKWARD:
                leftMotorSpeed  = -adjustedSpeed;
                rightMotorSpeed = -adjustedSpeed;
                int backLeft    = robot.getFilteredBackLeftObstacleDistanceInCm();
                int backRight   = robot.getFilteredBackRightObstacleDistanceInCm();
                leftMotorSpeed  = -Math.min(-leftMotorSpeed,  getMaxAllowedSpeedForObstacleDistance(backRight));
                rightMotorSpeed = -Math.min(-rightMotorSpeed, getMaxAllowedSpeedForObstacleDistance(backLeft));
                break;
            case LEFT:
                leftMotorSpeed  = -adjustedSpeed;
                rightMotorSpeed =  adjustedSpeed;
                break;
            case RIGHT:
                leftMotorSpeed  =  adjustedSpeed;
                rightMotorSpeed = -adjustedSpeed;
                break;
            case STOP:
                break;
        }

        robot.setMotorSpeedsAndUpdateSensorsWithoutExceptions(leftMotorSpeed, rightMotorSpeed);
    }


    public String getStatus()
    {
        return robot.toString();
    }


//    private void setMotorSpeeds(int left, int right)
//    {
//        leftMotorSpeed = left;
//        rightMotorSpeed = right;
//        tick();
//    }
}
