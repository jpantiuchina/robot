package it.unibz.jpantiuchina.robot.util;


public final class RobotMath
{
    public static float constraint(float value, float min, float max)
    {
        return Math.max(min, Math.min(value, max));
    }

    public static float map(float value, float inMin, float inMax, float outMin, float outMax)
    {
        return (value - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }
}
