package it.unibz.jpantiuchina.robot.hardware;


final class ProximitySensor
{
    private final int sensorIndex;
    private int range = -1;

    ProximitySensor(int sensorIndex)
    {
        this.sensorIndex = sensorIndex;
    }


    void readFromScanResult(byte[] buffer)
    {
        range = Util.convert2SignedBytesToUnsigned(buffer, sensorIndex * 2);
    }


    int getRangeInCm()
    {
        return range;
    }


    @Override
    public String toString()
    {
        return "My Proximity Sensor #" + sensorIndex + ": " + range + " cm";
    }
}
