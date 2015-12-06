package com.mycompany.myapplication.robot;


public final class SensorData
{
    private final ProximitySensorData[] proximitySensorDataArray = new ProximitySensorData[8];


    public SensorData()
    {
        for (int i = 0; i < proximitySensorDataArray.length; i++)
            proximitySensorDataArray[i] = new ProximitySensorData(i);
    }


    void readFromScanResult(byte[] buffer)
    {
        for (ProximitySensorData proximitySensorData : proximitySensorDataArray)
            proximitySensorData.readFromScanResult(buffer);
    }


    public ProximitySensorData getProximitySensorDataBySensorNumber(int i)
    {
        return proximitySensorDataArray[i - 1];
    }

    @Override
    public String toString()
    {
        String result = "Sensor Data\n";

        for (ProximitySensorData proximitySensorData : proximitySensorDataArray)
            result += proximitySensorData + "\n";

        return result;
    }
}


class ProximitySensorData
{
    private final int sensorIndex;
    private int range;

    public ProximitySensorData(int sensorIndex)
    {
        this.sensorIndex = sensorIndex;
    }


    void readFromScanResult(byte[] buffer)
    {
        // http://stackoverflow.com/questions/7401550/how-to-convert-int-to-unsigned-byte-and-back
        int highByte = buffer[sensorIndex * 2    ] & 0xFF;
        int lowByte  = buffer[sensorIndex * 2 + 1] & 0xFF;
        // merging high and low bytes to 16-bit integer
        range = (highByte << 8) + lowByte;
    }


    public int getRangeInCm()
    {
        return range;
    }


    @Override
    public String toString()
    {
        return "Proximity Sensor #" + sensorIndex + ": " + getRangeInCm() + " cm";
    }
}
