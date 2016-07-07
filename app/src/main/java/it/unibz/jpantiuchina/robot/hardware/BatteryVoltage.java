package it.unibz.jpantiuchina.robot.hardware;


final class BatteryVoltage
{
    private float voltage = -1;


    void readFromScanResult(byte[] buffer)
    {
        voltage = Util.convertSignedByteToUnsignedByte(buffer, 38) * 0.1f;
    }


    float getVoltage()
    {
        return voltage;
    }


    @Override
    public String toString()
    {
        return "Battery voltage: " + voltage + " V";
    }
}
