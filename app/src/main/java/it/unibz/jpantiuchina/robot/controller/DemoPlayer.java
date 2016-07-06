package it.unibz.jpantiuchina.robot.controller;

import android.os.Handler;

import it.unibz.jpantiuchina.robot.controller.RobotController.Direction;


public final class DemoPlayer implements Runnable
{
    private static final class Action
    {
//        private final float leftMotorSpeed;
//        private final float rightMotorSpeed;
        private final int   durationInMs;
        private final Direction direction;

//        private Action(float leftMotorSpeed, float rightMotorSpeed, int durationInMs)
        private Action(Direction direction, int durationInMs)
        {
//            this.leftMotorSpeed = leftMotorSpeed;
//            this.rightMotorSpeed = rightMotorSpeed;
            this.direction = direction;
            this.durationInMs = durationInMs;
        }

        private Direction getDirection()
        {
            return direction;
        }

        private int getDurationInMs()
        {
            return durationInMs;
        }
    }

    private static final Action[] PROGRAM =
    {
        new Action(Direction.FORWARD, 2000),
        new Action(Direction.LEFT,    1000),
        new Action(Direction.FORWARD, 2000),
        new Action(Direction.LEFT,    1000),
        new Action(Direction.FORWARD, 2000),
        new Action(Direction.LEFT,    1000),
        new Action(Direction.FORWARD, 2000),
        new Action(Direction.LEFT,    1000),
    };

    private final Handler handler = new Handler();
    private final ConnectingRobotController controller;
    private int nextAction;

    public DemoPlayer(ConnectingRobotController controller)
    {
        this.controller = controller;
    }


    public boolean isPlaying()
    {
        return nextAction > 0;
    }

    public void play()
    {
        run();
    }

    public void stop()
    {
        controller.setDirection(Direction.STOP);
        nextAction = 0;
        handler.removeCallbacks(this);
    }



    @Override
    public void run()
    {
        if (nextAction > PROGRAM.length)
        {
            stop();
        }
        else
        {
            Action action = PROGRAM[nextAction];
            nextAction++;
            controller.setDirection(action.getDirection());
            handler.postDelayed(this, action.getDurationInMs());
        }

    }
}
