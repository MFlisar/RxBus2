package com.michaelflisar.rxbus2.demo;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.michaelflisar.rxbus2.interfaces.IRxBusQueue;
import com.michaelflisar.rxbus2.rx.RxDisposableManager;

import org.reactivestreams.Publisher;

import io.reactivex.processors.BehaviorProcessor;

/**
 * Created by flisar on 28.04.2016.
 */
public class PauseAwareActivity extends AppCompatActivity implements IRxBusQueue
{
    private static final String TAG = PauseAwareActivity.class.getSimpleName();

    private final BehaviorProcessor<Boolean> mResumedProcessor = BehaviorProcessor.createDefault(false);

    public PauseAwareActivity()
    {
        super();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(TAG, "BASE BEFORE BUS onResume");
        mResumedProcessor.onNext(true);
        Log.d(TAG, "BASE AFTER BUS onResume");
    }

    @Override
    protected void onPause()
    {
        Log.d(TAG, "BASE BEFORE BUS onPause");
        mResumedProcessor.onNext(false);
        Log.d(TAG, "BASE AFTER BUS onPause");
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        RxDisposableManager.unsubscribe(this);
        super.onDestroy();
    }

    // --------------
    // Interface RxBus
    // --------------

    @Override
    public boolean isBusResumed()
    {
        return mResumedProcessor.getValue();
    }

    @Override
    public Publisher<Boolean> getResumeObservable()
    {
        return mResumedProcessor;
    }
}
