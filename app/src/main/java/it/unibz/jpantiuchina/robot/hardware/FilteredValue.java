package it.unibz.jpantiuchina.robot.hardware;

import java.util.Arrays;

final class FilteredValue
{
    private final int[] buffer = new int[5];

    private int nextElementIndex;
    private int filteredValue;

    FilteredValue()
    {
        Arrays.fill(buffer, Integer.MAX_VALUE);
    }

    public void addNewValue(int value)
    {
        buffer[nextElementIndex] = value;
        nextElementIndex++;
        nextElementIndex %= buffer.length;

        int result = Integer.MAX_VALUE;
        for (int i : buffer)
            result = Math.min(result, i);
        filteredValue = result;
    }

    public int getFilteredValue()
    {
        return filteredValue;
    }
}
