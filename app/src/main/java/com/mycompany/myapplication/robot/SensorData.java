package com.mycompany.myapplication.robot;


public final class SensorData
{
    private final ProximitySensorData[] proximitySensorDataArray;
    private final InfraredSensorData[] infraredSensorDataArray;

    public SensorData()
    {
        proximitySensorDataArray = new ProximitySensorData[8];
        for (int i = 0; i < proximitySensorDataArray.length; i++)
            proximitySensorDataArray[i] = new ProximitySensorData(i);


        infraredSensorDataArray = new InfraredSensorData[2];
            infraredSensorDataArray[0] = new InfraredSensorData(41);
            infraredSensorDataArray[1] = new InfraredSensorData(42);
    }



    void readFromScanResult(byte[] buffer)
    {
        for (ProximitySensorData proximitySensorData : proximitySensorDataArray)
            proximitySensorData.readFromScanResult(buffer);

        for (InfraredSensorData infraredSensorData : infraredSensorDataArray)
            infraredSensorData.readFromScanResult(buffer);
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

        for (InfraredSensorData infraredSensorData: infraredSensorDataArray)
            result+= infraredSensorData + "\n";

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


class InfraredSensorData
{
    private final int sensorIndex;
    private int range;

    public InfraredSensorData(int sensorIndex)
    {
        this.sensorIndex = sensorIndex;
    }


    void readFromScanResult(byte[] buffer)
    {

        byte signedHighByte = buffer[sensorIndex]; // 2 bytes for each sensor
        byte signedLowByte  = buffer[sensorIndex+ 1];
        // http://stackoverflow.com/questions/7401550/how-to-convert-int-to-unsigned-byte-and-back
        int highByte = ((int) signedHighByte) & 0xFF;
        int lowByte  = ((int) signedLowByte ) & 0xFF;
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
        return "Infrared Sensor #" + sensorIndex + ": " + getRangeInCm() + " cm";
    }
}



