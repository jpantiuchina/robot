package it.unibz.jpantiuchina.robot.hardware;


public final class ProximitySensor
{
    private final int sensorIndex;
    private int range;

    public ProximitySensor(int sensorIndex)
    {
        this.sensorIndex = sensorIndex;
    }


    void readFromScanResult(byte[] buffer)
    {
        range = Util.convert2SignedBytesToUnsigned(buffer, sensorIndex * 2);
    }


    public int getRangeInCm()
    {
        return range;
    }


    @Override
    public String toString()
    {
        return "My Proximity Sensor #" + sensorIndex + ": " + getRangeInCm() + " cm";
    }
}
