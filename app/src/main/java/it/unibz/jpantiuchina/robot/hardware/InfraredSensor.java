package it.unibz.jpantiuchina.robot.hardware;


public final class InfraredSensor
{
    private final int sensorIndex;
    private int range;
    private final FilteredValue filteredValue = new FilteredValue();


    public InfraredSensor(int sensorIndex)
    {
        this.sensorIndex = sensorIndex;
    }


    void readFromScanResult(byte[] buffer)
    {
        range = Util.convert2SignedBytesToUnsigned(buffer, 41 + sensorIndex * 2);
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
        return "Infrared Sensor #" + sensorIndex + ": " + range + " cm";
    }
}
