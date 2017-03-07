package com.michaelflisar.rxbus2;

import com.michaelflisar.rxbus2.exceptions.RxBusEventIsNullException;
import com.michaelflisar.rxbus2.rx.RxQueueKey;

import org.reactivestreams.Processor;

import java.util.HashMap;

import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;

/**
 * Created by Michael on 22.04.2016.
 */
public class RxBus
{
    // ---------------------------
    // Singleton
    // ---------------------------

    private static RxBus INSTANCE = null;

    static RxBus getInstance()
    {
        if (INSTANCE == null)
            INSTANCE = new RxBus();
        return INSTANCE;
    }

    private HashMap<RxQueueKey, Processor> mProcessorKeys = new HashMap<>();

    // ---------------------------
    // public bus functions - send events => returns a fresh build to send events to the bus
    // ---------------------------

    /**
     * Returns a fresh event get build to send events to the bus
     */
    public static synchronized RxBusSenderBuilder get()
    {
        return new RxBusSenderBuilder();
    }

    // ---------------------------
    // package internal bus functions - used by RxBusBuilder only
    // ---------------------------

    /**
     * Get an observable that observes all events of the the class the
     * <p>
     * @param eventClass  the class of event you want to observe
     * @return an Flowable, that will observe all events of the @param key class
     */
    synchronized <T> Flowable<T> observeEvent(Class<T> eventClass)
    {
        RxBusEventIsNullException.checkEvent(eventClass);

        Processor processor = getProcessor(new RxQueueKey(eventClass), true);
        return (Flowable)processor;
    }

    /**
     * Get an observable that observes all events that are send with the key and are of the type of the event class
     * <p>
     * @param eventClass  the class of event you want to observe
     * @param key  the event key you want to observe
     * @return an Flowable, that will observe all events of the @param key class
     */
    synchronized <T> Flowable<T> observeEvent(Class<T> eventClass, Integer key)
    {
        return observeEvent(new RxQueueKey(eventClass).withId(key));
    }

    /**
     * Get an observable that observes all events that are send with the key and are of the type of the event class
     * <p>
     * @param eventClass  the class of event you want to observe
     * @param key  the event key you want to observe
     * @return an Flowable, that will observe all events of the @param key class
     */
    synchronized <T> Flowable<T> observeEvent(Class<T> eventClass, String key)
    {
        return observeEvent(new RxQueueKey(eventClass).withId(key));
    }

    /**
     * Get an observable that observes all events that are send with the key and are of the type of the event class
     * <p>
     * @param key  the event key you want to observe
     * @return an Flowable, that will observe all events of the @param key class
     */
    synchronized <T> Flowable<T> observeEvent(RxQueueKey key)
    {
        if (key == null)
            throw new RuntimeException("You can't use a null key");

        Processor processor = getProcessor(key, true);
        return (Flowable)processor;
    }

    // ---------------------------
    // private helper functions
    // ---------------------------

    synchronized Processor getProcessor(RxQueueKey key, boolean createIfMissing)
    {
        // 1) look if key already has a publisher processor, if so, return it
        if (mProcessorKeys.containsKey(key))
            return mProcessorKeys.get(key);
        // 2) else, create a new one and put it into the map
        else if (createIfMissing)
        {
            Processor processor = PublishProcessor.create().toSerialized();
            mProcessorKeys.put(key, processor);
            return processor;
        }
        else
            return null;
    }
}