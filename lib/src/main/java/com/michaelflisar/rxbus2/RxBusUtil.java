package com.michaelflisar.rxbus2;

import com.michaelflisar.rxbus2.interfaces.IRxBusQueue;
import com.michaelflisar.rxbus2.rx.RxUtil;

import org.reactivestreams.Subscriber;

import io.reactivex.Observer;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subscribers.DisposableSubscriber;

/**
 * Created by flisar on 02.05.2016.
 */
public class RxBusUtil
{
    protected static <T> Consumer<T> wrapQueueConsumer(Consumer<T> action, IRxBusQueue isResumedProvider)
    {
        return new Consumer<T>()
        {
            @Override
            public void accept(T t) throws Exception
            {
                if (RxUtil.safetyQueueCheck(t, isResumedProvider))
                    action.accept(t);
            }
        };
    }

    protected static <T> DisposableSubscriber<T> wrapSubscriber(DisposableSubscriber<T> subscriber, IRxBusQueue isResumedProvider)
    {
        return new DisposableSubscriber<T>()
        {
            @Override
            public void onComplete()
            {
                subscriber.onComplete();
            }

            @Override
            public void onError(Throwable e)
            {
                subscriber.onError(e);
            }

            @Override
            public void onNext(T t)
            {
                if (RxUtil.safetyQueueCheck(t, isResumedProvider))
                    subscriber.onNext(t);
            }
        };
    }}
