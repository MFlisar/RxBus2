package com.michaelflisar.rxbus2.exceptions;

/**
 * Created by flisar on 03.05.2016.
 */
public class RxBusEventCastException extends RuntimeException
{
    public static void checkEvent(Object event, Class<?> classToCast)
    {
        if (!classToCast.isInstance(event))
            throw new RxBusEventCastException(event.getClass(), classToCast);
    }

    public RxBusEventCastException(Class eventClass, Class castClass)
    {
        super(String.format("You can't cast an event of type %s to %s, because the event is not an instance of %s or a sub class of it!", eventClass, castClass, castClass));
    }
}