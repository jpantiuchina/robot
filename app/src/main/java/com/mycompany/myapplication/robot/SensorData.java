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
        //read professor method
        for (ProximitySensorData professorsproximitySensorData : proximitySensorDataArray)
            professorsproximitySensorData.infraRedData(buffer);

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

        byte signedHighByte = buffer[sensorIndex * 2]; // 2 bytes for each sensor
        byte signedLowByte  = buffer[sensorIndex * 2 + 1];
        // http://stackoverflow.com/questions/7401550/how-to-convert-int-to-unsigned-byte-and-back
        int highByte = ((int) signedHighByte) & 0xFF;
        int lowByte  = ((int) signedLowByte ) & 0xFF;
        // merging high and low bytes to 16-bit integer
        range = (highByte << 8) + lowByte;
    }

    //-------- professor’s solution
    private short value;
    void infraRedData(byte[] buffer) {
        byte highbyte = buffer[sensorIndex * 2];
        byte lowbyte  = buffer[sensorIndex * 2 + 1];
        value = (short)(highbyte << 8);
        value += lowbyte;
    }

    public short getValue() {
        return value;
    }
    //--------end of professor’s solution

    public int getRangeInCm()
    {
        return range;
    }


    @Override
    public String toString()
    {
        return "My Proximity Sensor #" + sensorIndex + ": " + getRangeInCm() + " cm" + "\n" +
               "Prof. Proximity sensor #" + sensorIndex + ":" + getValue() + " cm";
    }
}
