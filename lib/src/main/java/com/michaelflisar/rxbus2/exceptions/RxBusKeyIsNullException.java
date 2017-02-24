package com.michaelflisar.rxbus2.exceptions;

/**
 * Created by flisar on 03.05.2016.
 */
public class RxBusKeyIsNullException extends RuntimeException
{
    public static void checkKey(Object key)
    {
        if (key == null)
            throw new RxBusKeyIsNullException();
    }

    public RxBusKeyIsNullException()
    {
        super("You can't use a null withKey!");
    }
}
