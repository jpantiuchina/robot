package it.unibz.jpantiuchina.robot.hardware;


import it.unibz.jpantiuchina.robot.util.RobotMath;

final class InfraredSensor
{
    private final int sensorIndex;
    private int range;


    InfraredSensor(int sensorIndex)
    {
        this.sensorIndex = sensorIndex;
    }


    void readFromScanResult(byte[] buffer)
    {
        float voltage = Util.convert2SignedBytesToUnsigned(buffer, 41 + sensorIndex * 2);
        voltage = RobotMath.constraint(voltage, 15, 120);
        range = (int) RobotMath.map(voltage, 15, 120, 80, 10);
    }


    int getRangeInCm()
    {
        return range;
    }


    @Override
    public String toString()
    {
        return "Infrared Sensor #" + sensorIndex + ": " + range + " cm";
    }
}
