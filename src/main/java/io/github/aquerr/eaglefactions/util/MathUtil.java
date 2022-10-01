package io.github.aquerr.eaglefactions.util;

public class MathUtil
{
    public static float round(final float number, final int decimalPlace) {
        int pow = 10;
        for (int i = 1; i < decimalPlace; i++)
            pow *= 10;
        float tmp = number * pow;
        return ( (float) ( (int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) ) ) / pow;
    }
}
