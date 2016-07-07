package it.unibz.jpantiuchina.robot.hardware;


import it.unibz.jpantiuchina.robot.util.RobotMath;

final class InfraredSensor
{
    private final int sensorIndex;
    private int range = -1;


    InfraredSensor(int sensorIndex)
    {
        this.sensorIndex = sensorIndex;
    }


    void readFromScanResult(byte[] buffer)
    {
        float voltage = Util.convert2SignedBytesToUnsigned(buffer, 41 + sensorIndex * 2);
//        Log.i("IR", "Voltage: " + voltage);
        range = (int) RobotMath.mapAndConstrain(voltage, 15, 200, 80, 10);
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
