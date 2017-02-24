package com.michaelflisar.rxbus2.rx;

import com.michaelflisar.rxbus2.interfaces.IRxBusQueue;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by flisar on 02.05.2016.
 */
public class RxSubscriptionUtil
{
    public static <T> Consumer<T> wrapQueueAction(Consumer<T> action, IRxBusQueue isResumedProvider)
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

    public static <T> Observer<T> wrapObserver(Observer<T> observer, IRxBusQueue isResumedProvider)
    {
        return new Observer<T>()
        {
            @Override
            public void onSubscribe(Disposable d) {
                observer.onSubscribe(d);
            }

            @Override
            public void onComplete()
            {
                observer.onComplete();
            }

            @Override
            public void onError(Throwable e)
            {
                observer.onError(e);
            }

            @Override
            public void onNext(T t)
            {
                if (RxUtil.safetyQueueCheck(t, isResumedProvider))
                    observer.onNext(t);
            }
        };
    }

    public static <T> Subscriber<T> wrapSubscriber(Subscriber<T> subscriber, IRxBusQueue isResumedProvider)
    {
        return new Subscriber<T>()
        {
            @Override
            public void onSubscribe(Subscription s) {
                subscriber.onSubscribe(s);
            }

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
