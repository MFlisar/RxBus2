package com.michaelflisar.rxbus2.exceptions;

/**
 * Created by flisar on 03.05.2016.
 */
public class RxBusEventIsNullException extends RuntimeException
{
    public static void checkEvent(Object event)
    {
        if (event == null)
            throw new RxBusEventIsNullException();
    }

    public RxBusEventIsNullException()
    {
        super("You can't send a null event!");
    }
}
