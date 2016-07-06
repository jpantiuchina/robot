package it.unibz.jpantiuchina.robot.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import it.unibz.jpantiuchina.robot.hardware.Robot;
import it.unibz.jpantiuchina.robot.logical.LogicalRobot;
import it.unibz.jpantiuchina.robot.util.RobotMath;

public final class RobotController
{
    public enum Direction {FORWARD, BACKWARD, LEFT, RIGHT, STOP}

    private float speed;
    private Direction direction = Direction.STOP;

    private final LogicalRobot robot;


    public RobotController(InputStream inputStream, OutputStream outputStream)
    {
        robot = new LogicalRobot(inputStream, outputStream);
    }

    public void setSpeed(float speed) throws IOException
    {
        this.speed = RobotMath.constraint(speed, 0, Robot.MAX_SPEED_IN_CMPS);
        tick();
    }


    public void setDirection(Direction direction) throws IOException
    {
        this.direction = direction;
        tick();
    }


    public Direction getDirection()
    {
        return direction;
    }


    private static float getMaxAllowedSpeedForObstacleDistance(int obstacleDistanceInCm)
    {
        return RobotMath.map(obstacleDistanceInCm, 10, 30, 0, Robot.MAX_SPEED_IN_CMPS);
    }


    /**
     * Must be called constantly to support status and speed updates
     */
    public void tick() throws IOException
    {
        float adjustedSpeed = speed;

        float leftMotorSpeed = 0;
        float rightMotorSpeed = 0;

        switch (direction)
        {
            case FORWARD:
                leftMotorSpeed  =  adjustedSpeed;
                rightMotorSpeed =  adjustedSpeed;
                int frontLeft   = robot.getFilteredFrontLeftObstacleDistanceInCm();
                int frontRight  = robot.getFilteredFrontRightObstacleDistanceInCm();
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

        robot.setMotorSpeedsAndUpdateSensors(leftMotorSpeed, rightMotorSpeed);
    }


    public int getFilteredFrontLeftObstacleDistanceInCm()
    {
        return robot.getFilteredFrontLeftObstacleDistanceInCm();
    }

    public int getFilteredFrontRightObstacleDistanceInCm()
    {
        return robot.getFilteredFrontRightObstacleDistanceInCm();
    }

    public int getFilteredBackLeftObstacleDistanceInCm()
    {
        return robot.getFilteredBackLeftObstacleDistanceInCm();
    }

    public int getFilteredBackRightObstacleDistanceInCm()
    {
        return robot.getFilteredBackRightObstacleDistanceInCm();
    }
}
