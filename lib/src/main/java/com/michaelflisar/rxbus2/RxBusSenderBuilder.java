package com.michaelflisar.rxbus2;

import com.michaelflisar.rxbus2.exceptions.RxBusEventCastException;
import com.michaelflisar.rxbus2.exceptions.RxBusEventIsNullException;
import com.michaelflisar.rxbus2.exceptions.RxBusKeyIsNullException;
import com.michaelflisar.rxbus2.rx.RxQueueKey;

import org.reactivestreams.Processor;

/**
 * Created by Michael on 24.02.2017.
 */

public class RxBusSenderBuilder
{
    private Class<?> mCast = null;
    private Object mKey = null;
    private boolean mSendToDefaultBus = false;
    private boolean mSendToSuperClasses;

    RxBusSenderBuilder()
    {
        mSendToSuperClasses = RxBusDefaults.get().isSendToSuperClassesAsWell();
    }

    /**
     * Force casting an event to the provided class so that any observer of the provided class will retrieve this event
     * Useful for sending many sub classes to one base class observer
     * Sending an event that is not assignable by this class will throw an exception.
     * <p>
     * @param  cast  the class to withCast the event to
     * @return an {@link RxBusSenderBuilder} for chaining additional calls before calling {@link RxBusSenderBuilder#send(Object)}
     */
    public RxBusSenderBuilder withCast(Class<?> cast)
    {
        mCast = cast;
        return this;
    }

    /**
     * Force sending an event to observers of the provided key only
     * <p>
     * @param  key the key this event should be broadcasted to
     * @return an {@link RxBusSenderBuilder} for chaining additional calls before calling {@link RxBusSenderBuilder#send(Object)}
     */
    public RxBusSenderBuilder withKey(Integer key)
    {
        RxBusKeyIsNullException.checkKey(key);
        mKey = key;
        return this;
    }

    /**
     * Force sending an event to observers of the provided key only
     * <p>
     * @param  key the key this event should be broadcasted to
     * @return an {@link RxBusSenderBuilder} for chaining additional calls before calling {@link RxBusSenderBuilder#send(Object)}
     */
    public RxBusSenderBuilder withKey(String key)
    {
        RxBusKeyIsNullException.checkKey(key);
        mKey = key;
        return this;
    }

    /**
     * Force sending an event the default class as well, even if a key is provider
     * <p>
     * @return an {@link RxBusSenderBuilder} for chaining additional calls before calling {@link RxBusSenderBuilder#send(Object)}
     */
    public RxBusSenderBuilder withSendToDefaultBus()
    {
        mSendToDefaultBus = true;
        return this;
    }

    /**
     * Force sending an event the all super class observers as well
     * <p>
     * @param enabled true, if this feature should be enabled, false otherwise
     * @return an {@link RxBusSenderBuilder} for chaining additional calls before calling {@link RxBusSenderBuilder#send(Object)}
     */
    public RxBusSenderBuilder withSendToSuperClasses(boolean enabled)
    {
        mSendToSuperClasses = enabled;
        return this;
    }

    /**
     * Send an event to the bus, applying all already chained settings
     * <p>
     * @param  event  the event that should be broadcasted to the bus
     * @return true, if the item is send to the bus, false otherwise (false will be returned if noone is listening to this event)
     */
    public synchronized boolean send(Object event)
    {
        RxBusEventIsNullException.checkEvent(event);
        if (mCast != null)
            RxBusEventCastException.checkEvent(event, mCast);

        boolean send = false;

        RxQueueKey key = new RxQueueKey(mCast == null ? event.getClass() : mCast);

        // 1) send to simple unbound bus
        if (mKey == null || mSendToDefaultBus)
            send |= sendToUnboundBus(key, event);

        // 2) send to key bound bus
        if (mKey != null)
            send |= sendToKeyBoundBus(key, event);

        // ---------
        // TEST
        // ---------

        // 3) send to unbound base class buses
        // 4) send to key bound bus of super classes
        if (mSendToSuperClasses)
        {
            key = key.getParentKey();
            while (key != null)
            {
                send |= sendToUnboundBus(key, event);
                if (mKey != null)
                    send |= sendToKeyBoundBus(key, event);
                key = key.getParentKey();
            }
        }

        return send;
    }

    private boolean sendToUnboundBus(RxQueueKey key, Object event)
    {
        boolean send = false;
        Processor processor = RxBus.getInstance().getProcessor(key, false);
        // only send event, if processor exists => this means someone has at least once subscribed to it
        if (processor != null)
        {
            if (mCast == null)
                processor.onNext(event);
            else
                processor.onNext(mCast.cast(event));
            send = true;
        }
        return send;
    }

    private boolean sendToKeyBoundBus(RxQueueKey key, Object event)
    {
        RxQueueKey keyToUse = key.clone();
        boolean send = false;
        Processor processor;
        if (mKey instanceof String)
            keyToUse.withId((String)mKey);
        else if (mKey instanceof Integer)
            keyToUse.withId((Integer)mKey);
        processor = RxBus.getInstance().getProcessor(keyToUse, false);

        // only send event, if processor exists => this means someone has at least once subscribed to it
        if (processor != null)
        {
            if (mCast == null)
                processor.onNext(event);
            else
                processor.onNext(mCast.cast(event));
            send = true;
        }
        return send;
    }


}
