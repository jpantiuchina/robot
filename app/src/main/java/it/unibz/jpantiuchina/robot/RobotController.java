package it.unibz.jpantiuchina.robot;

import java.io.InputStream;
import java.io.OutputStream;

import it.unibz.jpantiuchina.robot.hardware.Robot;

public final class RobotController
{
    private final Robot robot;

    private int leftMotorSpeed;
    private int rightMotorSpeed;

    private int speed;


    public void setSpeed(int speed)
    {
        this.speed = speed;
    }


    public void goBackward()
    {
        if (currentDirection == Direction.FORWARD)
        {
            stop();
        }
        else
        {
            setMotorSpeeds(-speed, -speed);
            currentDirection = Direction.BACKWARD;
        }

    }

    private enum Direction { FORWARD, BACKWARD, LEFT, RIGHT, STOP }

    private Direction currentDirection = Direction.STOP;



    public void turnLeft()
    {
        if (currentDirection == Direction.RIGHT)
        {
            stop();
        }
        else
        {
            setMotorSpeeds(-speed, speed);
            currentDirection = Direction.LEFT;
        }
    }

    public void turnRight()
    {
        if (currentDirection == Direction.LEFT)
        {
            stop();
        }
        else
        {
            setMotorSpeeds(speed, -speed);
            currentDirection = Direction.RIGHT;
        }
    }

    public void goForward()
    {
        if (currentDirection == Direction.BACKWARD)
        {
            stop();
        }
        else
        {
            setMotorSpeeds(speed, speed);
            currentDirection = Direction.FORWARD;
        }
    }


    private void stop()
    {
        setMotorSpeeds(0, 0);
        currentDirection = Direction.STOP;
    }


    public RobotController(InputStream inputStream, OutputStream outputStream)
    {
        robot = new Robot(inputStream, outputStream);
    }


    public void tick()
    {
        robot.setMotorSpeedsAndUpdateSensorsWithoutExceptions(leftMotorSpeed, rightMotorSpeed);
    }


    public String getStatus()
    {
        return robot.toString();
    }


    private void setMotorSpeeds(int left, int right)
    {
        leftMotorSpeed = left;
        rightMotorSpeed = right;
        tick();
    }
}
