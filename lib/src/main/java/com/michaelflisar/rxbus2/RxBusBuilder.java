package com.michaelflisar.rxbus2;

import com.michaelflisar.rxbus2.interfaces.IRxBusQueue;
import com.michaelflisar.rxbus2.rx.RxBusMode;
import com.michaelflisar.rxbus2.rx.RxQueueKey;
import com.michaelflisar.rxbus2.rx.RxDisposableManager;
import com.michaelflisar.rxbus2.rx.RxUtil;

import org.reactivestreams.Subscriber;

import java.util.ArrayList;
import java.util.List;

import hu.akarnokd.rxjava2.operators.FlowableTransformers;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.subscribers.DisposableSubscriber;

/**
 * Created by Michael on 01.05.2016.
 */

public class RxBusBuilder<T>
{
    private Class<T> mEventClass;
    private List<RxQueueKey<T>> mKeys = null;

    private RxBusMode mBusMode = null;

    private IRxBusQueue mQueuer = null;
    private int mValvePrefetch = 1000;
    private boolean mBackpressureBeforeValve = true;
    private boolean mQueueSubscriptionSafetyCheckEnabled = true;

    private Object mBoundObject = null;

    public static <T> RxBusBuilder<T> create(Class<T> eventClass)
    {
        return new RxBusBuilder<T>(eventClass);
    }

    private RxBusBuilder(Class<T> eventClass)
    {
        mEventClass = eventClass;
    }

    public RxBusBuilder<T> withMode(RxBusMode mode)
    {
        mBusMode = mode;
        return this;
    }

    public RxBusBuilder<T> withQueuing(IRxBusQueue queuer)
    {
        mQueuer = queuer;
        return this;
    }

    public RxBusBuilder<T> withQueuing(IRxBusQueue queuer, int valvePrefetch)
    {
        mQueuer = queuer;
        mValvePrefetch = valvePrefetch;
        return this;
    }

    public RxBusBuilder<T> withBackpressure(boolean enabled)
    {
        mBackpressureBeforeValve = enabled;
        return this;
    }

    public RxBusBuilder<T> withSafetyCheck(boolean enabled)
    {
        mQueueSubscriptionSafetyCheckEnabled = enabled;
        return this;
    }

    public RxBusBuilder<T> withKeys(RxQueueKey<T>... key)
    {
        if (key.length > 0)
        {
            mKeys = new ArrayList<>();
            for (int i = 0; i < key.length; i++)
                mKeys.add(key[i]);
        }
        else
            mKeys = null;
        return this;
    }
    public RxBusBuilder<T> withKey(int... key)
    {
        if (key.length > 0)
        {
            mKeys = new ArrayList<>();
            for (int i = 0; i < key.length; i++)
                mKeys.add( new RxQueueKey(mEventClass, key[i]));
        }
        else
            mKeys = null;
        return this;
    }

    public RxBusBuilder<T> withKey(String... key)
    {
        if (key.length > 0)
        {
            mKeys = new ArrayList<>();
            for (int i = 0; i < key.length; i++)
                mKeys.add( new RxQueueKey(mEventClass, key[i]));
        }
        else
            mKeys = null;
        return this;
    }

    public RxBusBuilder<T> withBound(Object boundObject)
    {
        mBoundObject = boundObject;
        return this;
    }

    // ---------------------------
    // flowable - build
    // ---------------------------

    public Flowable<T> build()
    {
        return build(true);
    }

    public Flowable<T> build(boolean applySchedular)
    {
        Flowable<T> flowable = null;
        if (mKeys != null)
        {
            for (int i = 0; i < mKeys.size(); i++)
            {
                if (i == 0)
                    flowable = RxBus.getInstance().observeEvent(mKeys.get(i));
                else
                    flowable = flowable.mergeWith(RxBus.getInstance().observeEvent(mKeys.get(i)));
            }
        }
        else
            flowable = RxBus.getInstance().observeEvent(mEventClass);

        if (mBackpressureBeforeValve)
            flowable = flowable.onBackpressureBuffer();

        if (mQueuer != null)
            flowable = flowable.compose(FlowableTransformers.<T>valve(mQueuer.getResumeObservable(), mQueuer.isBusResumed(), mValvePrefetch));
        if (applySchedular)
            flowable = applySchedular(flowable);
        return flowable;
    }

    // ---------------------------
    // subscribe - variants
    // ---------------------------

    public Disposable subscribe(Consumer<T> onNext)
    {
        return subscribe(onNext, null, null, null);
    }

    public Disposable subscribe(Consumer<T> onNext, Consumer<Throwable> onError)
    {
        return subscribe(onNext, onError, null, null);
    }


    public Disposable subscribe(Consumer<T> onNext, Consumer<Throwable> onError, Action onComplete)
    {
        return subscribe(onNext, onError, onComplete, null);
    }


    public <R> Disposable subscribe(Consumer<R> onNext, FlowableTransformer<T, R> transformer)
    {
        return subscribe(onNext, null, null, transformer);
    }


    public <R> Disposable subscribe(Consumer<R> onNext, Consumer<Throwable> onError, FlowableTransformer<T, R> transformer)
    {
        return subscribe(onNext, onError, null, transformer);
    }

    // ---------------------------
    // subscribe implementations
    // ---------------------------

    public <R> Disposable subscribe(Consumer<R> onNext, Consumer<Throwable> onError, Action onCompleted, FlowableTransformer<T, R> transformer)
    {
        Flowable flowable = build(false);
        if (transformer != null)
            flowable = flowable.compose(transformer);

        if (onNext == null)
            onNext = data -> {};
        if (onError == null)
            onError = error -> { throw new OnErrorNotImplementedException(error); };
        if (onCompleted == null)
            onCompleted = () -> {};

        Consumer<R> actualOnNext = onNext;
        if (mQueuer != null && mQueueSubscriptionSafetyCheckEnabled)
            actualOnNext = RxBusUtil.wrapQueueConsumer(onNext, mQueuer);

        flowable = applySchedular(flowable);
        Disposable disposable = flowable.subscribe(actualOnNext, onError, onCompleted);
        if (mBoundObject != null)
            RxDisposableManager.addDisposable(mBoundObject, disposable);
        return disposable;
    }

    public <R> Disposable subscribe(DisposableSubscriber<R> subscriber, FlowableTransformer<T, R> transformer)
    {
        Flowable flowable = build(false);
        if (transformer != null)
            flowable = flowable.compose(transformer);

        Subscriber<R> actualSubscriber = subscriber;
        if (mQueuer != null && mQueueSubscriptionSafetyCheckEnabled)
            actualSubscriber = RxBusUtil.wrapSubscriber(subscriber, mQueuer);

        flowable = applySchedular(flowable);
        Disposable disposable = (DisposableSubscriber)flowable.subscribeWith(actualSubscriber);
        if (mBoundObject != null)
            RxDisposableManager.addDisposable(mBoundObject, disposable);
        return disposable;
    }

    private Flowable<T> applySchedular(Flowable<T> flowable)
    {
        if (mBusMode == RxBusMode.Background)
            return flowable.compose(RxUtil.<T>applyBackgroundSchedulers());
        else if (mBusMode == RxBusMode.Main)
            return flowable.compose(RxUtil.<T>applySchedulars());
        return flowable;
    }
}
