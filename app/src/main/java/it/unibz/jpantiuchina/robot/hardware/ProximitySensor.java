package it.unibz.jpantiuchina.robot.hardware;


public final class ProximitySensor
{
    private final int sensorIndex;
    private int range;
    private final FilteredValue filteredValue = new FilteredValue();

    public ProximitySensor(int sensorIndex)
    {
        this.sensorIndex = sensorIndex;
    }


    void readFromScanResult(byte[] buffer)
    {
        range = Util.convert2SignedBytesToUnsigned(buffer, sensorIndex * 2);
        filteredValue.addNewValue(range);
    }


    private int getRangeInCm()
    {
        return range;
    }


    public int getFilteredRangeInCm()
    {
        return filteredValue.getFilteredValue();
    }


    @Override
    public String toString()
    {
        return "My Proximity Sensor #" + sensorIndex + ": " + range + " cm";
    }
}
