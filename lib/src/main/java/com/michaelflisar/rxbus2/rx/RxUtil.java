package com.michaelflisar.rxbus2.rx;

import com.michaelflisar.rxbus2.RxBus;
import com.michaelflisar.rxbus2.interfaces.IRxBusQueue;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Michael on 28.04.2016.
 */
public class RxUtil
{
    private static final FlowableTransformer schedulersTransformer = new FlowableTransformer() {
            @Override
            public Flowable apply(Flowable flowable) {
                return flowable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };

    private static final FlowableTransformer schedulersTransformerBackground = new FlowableTransformer() {
        @Override
        public Flowable apply(Flowable flowable) {
            return flowable
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io());
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> FlowableTransformer<T, T> applyBackgroundSchedulers() {
        return (FlowableTransformer<T, T>) schedulersTransformerBackground;
    }

    // default background subscription, foreground observation schedulars
    @SuppressWarnings("unchecked")
    public static <T> FlowableTransformer<T, T> applySchedulars() {
        return (FlowableTransformer<T, T>) schedulersTransformer;
    }

    public static <T> boolean safetyQueueCheck(T event, IRxBusQueue isResumedProvider)
    {
        if (isResumedProvider.isBusResumed())
            return true;
        else
        {
            RxBus.get().send(event);
            return false;
        }
    }

}
