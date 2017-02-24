package com.michaelflisar.rxbus2.interfaces;

import org.reactivestreams.Publisher;

/**
 * Created by Michael on 22.04.2016.
 */
public interface IRxBusQueue
{
    boolean isBusResumed();
    Publisher<Boolean> getResumeObservable();
}
