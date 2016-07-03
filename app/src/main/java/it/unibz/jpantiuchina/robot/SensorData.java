package it.unibz.jpantiuchina.robot;


import java.util.Arrays;

public final class SensorData
{
    private final ProximitySensorData[] proximitySensorDataArray;
    private final InfraredSensorData[] infraredSensorDataArray;
    private final ThermalSensorData thermalSensorData;

    public SensorData()
    {
        proximitySensorDataArray = new ProximitySensorData[8];
        for (int i = 0; i < proximitySensorDataArray.length; i++)
            proximitySensorDataArray[i] = new ProximitySensorData(i);


        infraredSensorDataArray = new InfraredSensorData[2];
        infraredSensorDataArray[0] = new InfraredSensorData(0);
        infraredSensorDataArray[1] = new InfraredSensorData(1);

        thermalSensorData = new ThermalSensorData();

    }

    public boolean danger ()
    {
        return proximitySensorDataArray[2].getRangeInCm() <= 20 || proximitySensorDataArray[3].getRangeInCm() <= 20 || proximitySensorDataArray[4].getRangeInCm() <= 20 || proximitySensorDataArray[5].getRangeInCm() <= 20;
    }

    public boolean isDangerOnRight ()
    {
        return proximitySensorDataArray[5].getRangeInCm() <= 20 || proximitySensorDataArray[3].getRangeInCm() <= 20;
    }

    public boolean isDangerOnLeft ()
    {
        return proximitySensorDataArray[4].getRangeInCm() <= 20 || proximitySensorDataArray[2].getRangeInCm() <= 20;
    }



    void readFromScanResult(byte[] buffer)
    {
        for (ProximitySensorData proximitySensorData : proximitySensorDataArray)
            proximitySensorData.readFromScanResult(buffer);

        for (InfraredSensorData infraredSensorData : infraredSensorDataArray)
            infraredSensorData.readFromScanResult(buffer);

        thermalSensorData.readFromScanResult(buffer);

    }


    public ProximitySensorData getProximitySensorDataBySensorNumber(int i)
    {
        return proximitySensorDataArray[i - 1];
    }

    public ThermalSensorData getThermalSensorData()
    {
        return thermalSensorData;
    }

    @Override
    public String toString()
    {
        String result = "Sensor Data\n";


        for (ProximitySensorData proximitySensorData : proximitySensorDataArray)
            result += proximitySensorData + "\n";



        for (InfraredSensorData infraredSensorData: infraredSensorDataArray)
            result += infraredSensorData + "\n";

        result += thermalSensorData + "\n";


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

        byte signedHighByte = buffer[41 + sensorIndex * 2]; // 2 bytes for each sensor
        byte signedLowByte  = buffer[41 + sensorIndex * 2 + 1];
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


class ThermalSensorData
{
    private byte ambientTemperature;
    private final byte[] pixels = new byte[8];


    void readFromScanResult(byte[] buffer)
    {

        ambientTemperature = buffer[17];
        System.arraycopy(buffer, 18, pixels, 0, pixels.length);
    }

    public byte getAmbientTemperatureInDegreesC()
    {
        return ambientTemperature;
    }

    public byte[] getPixelsInDegreesC()
    {
        return pixels;
    }

    @Override
    public String toString()
    {
        return "Temperature Sensor: " + getAmbientTemperatureInDegreesC() + " C " +
                Arrays.toString(getPixelsInDegreesC());
    }
}



