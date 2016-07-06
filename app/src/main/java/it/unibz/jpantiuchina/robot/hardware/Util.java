package it.unibz.jpantiuchina.robot.hardware;


final class Util
{
    static int convert2SignedBytesToUnsigned(byte[] buffer, int startByte)
    {
        int highByte = convertSignedByteToUnsignedByte(buffer, startByte);
        int lowByte  = convertSignedByteToUnsignedByte(buffer, startByte + 1);
        // merging high and low bytes to 16-bit integer
        return (highByte << 8) + lowByte;
    }


    private static int convertSignedByteToUnsignedByte(byte[] buffer, int startByte)
    {
        // http://stackoverflow.com/questions/7401550/how-to-convert-int-to-unsigned-byte-and-back
        // noinspection UnnecessaryExplicitNumericCast
        return ((int) buffer[startByte]) & 0xFF;
    }

}
