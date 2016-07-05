package it.unibz.jpantiuchina.robot.hardware;


public final class InfraredSensor
{
    private final int sensorIndex;
    private int range;

    public InfraredSensor(int sensorIndex)
    {
        this.sensorIndex = sensorIndex;
    }


    void readFromScanResult(byte[] buffer)
    {
        range = Util.convert2SignedBytesToUnsigned(buffer, 41 + sensorIndex * 2);
    }


    public int getRangeInCm()
    {
        return range;
    }


    @Override
    public String toString()
    {
        return "Infrared Sensor #" + sensorIndex + ": " + getRangeInCm() + " cm";
    }
}
