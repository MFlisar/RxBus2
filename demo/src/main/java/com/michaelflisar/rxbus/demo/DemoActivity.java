package com.michaelflisar.rxbus.demo;

import android.os.Bundle;
import android.util.Log;

import com.michaelflisar.rxbus2.RxBus;
import com.michaelflisar.rxbus2.RxBusBuilder;
import com.michaelflisar.rxbus2.rx.RxBusMode;
import com.michaelflisar.rxbus2.rx.RxDisposableManager;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by flisar on 28.04.2016.
 */
public class DemoActivity extends PauseAwareActivity
{
    private static final String TAG = DemoActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testGeneral();
        testWithKeys();
        testAdvanced();

        // -----------------
        // Send some events
        // -----------------

        // lets send some sync events
        for (int i = 0; i < 5; i++)
            RxBus.get().send(getLogMessage("onCreate", "main thread i=" + i));

        // lets say another thread is currently emitting events => send some async events
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Log.d(TAG, "Thread startet...");
                for (int i = 0; i < 5; i++)
                    RxBus.get().send(getLogMessage("onCreate", "some thread i=" + i));
            }
        }).start();

        // lets send some events bound to a key (can be a string or an integer)
        // 1 loop: sends events to the given key ONLY
        // 2 loop: sends events to all observers of the key AND to all simple String event observer
        for (int i = 0; i < 5; i++)
            RxBus.get().key(R.id.custom_event_id_1).send(getLogMessage("onCreate", "KEY 1 main thread i=" + i));
        for (int i = 0; i < 5; i++)
            RxBus.get().key(R.id.custom_event_id_2).sendToDefaultBus().send(getLogMessage("onCreate", "KEY 2 (AND ALL String listeners) main thread i=" + i));
    }

    @Override
    public void onPause()
    {
        RxBus.get().send(getLogMessage("onPause", "BEFORE on pause"));
        Log.d(TAG, "ACTIVITY BEFORE PAUSED");
        super.onResume();
        Log.d(TAG, "ACTIVITY AFTER PAUSED");
        RxBus.get().send(getLogMessage("onPause", "AFTER on pause"));
    }

    @Override
    public void onResume()
    {
        RxBus.get().send(getLogMessage("onResume", "BEFORE on resume"));
        Log.d(TAG, "ACTIVITY BEFORE RESUMED");
        super.onResume();
        Log.d(TAG, "ACTIVITY AFTER RESUMED");
        RxBus.get().send(getLogMessage("onResume", "AFTER on resume"));
    }

    @Override
    public void onDestroy()
    {
        // unsubscribe - we used the RXSubscriptionManager for every subscription and bound all subscriptions to this class,
        // so following will safely unsubscribe every subscription
        RxDisposableManager.unsubscribe(this);
        super.onDestroy();
    }

    // -----------------------------
    // Logging
    // -----------------------------

    private String getLogMessage(String method, String msg)
    {
        return "[" + method + "] {" + Thread.currentThread().getName() + "} : " + msg;
    }

    private void logEvent(String event, boolean queuedBus, String key, String extra)
    {
        Log.d(TAG, String.format("Type: %s%s (key=%s), Event: %s", queuedBus ? "QUEUED BUS" : "SIMPLE BUS", extra != null ? extra : "", key == null ? "NONE" : key, event));
    }

    // -----------------------------
    // Tests
    // -----------------------------

    private void testGeneral()
    {
        // 1) Just subscribe to a bus event => use the builders subscribe overload for this!
        Disposable disposableManual = RxBusBuilder.create(String.class)
                .subscribe(new Consumer<String>(){
                    @Override
                    public void accept(String s) {
                        logEvent(s, false, null, null);
                    }
                });
        // ATTENTION: this subscription MUST be handled by you, unsubscribe whenever you want!
        // currently it will leak the activity!!!

        // 2) Subscribe to an event and let RXSubscriptionManager manage your subscription - you just need to call
        // RXSubscriptionManager.unsubscribe(boundObject); to unsubscribe ALL subscriptions for a bound object
        // additionally this here enable queuing + emits items on the main thread
        RxBusBuilder.create(String.class)
                .withQueuing(this)          // optional: if enabled, events will be queued while the IRxBusQueue is paused!
                .withBound(this)            // optional: this binds the subcritpion to this object and you can unsubscribe all bound subscriptions at once
                .withMode(RxBusMode.Main)   // optional: set the thread to main or background if wanted, events will be emitted on the corresponding thread
                .subscribe(new Consumer<String>(){
                    @Override
                    public void accept(String s) {
                        logEvent(s, true, null, null);
                    }
                });

        // 3) Get a simple observable and do whatever you want with it
        // all RxBus options like queuing and keys are available here as well!!!
        Flowable<String> observable = RxBusBuilder.create(String.class)
                // optional:
//                .withQueuing(this)
//                .withKey(...)
                .build();
        // do something with this observable...
    }

    private void testWithKeys()
    {
        // you can use everything that is shown in testGeneral here as well, example will not show all possible combinations!

        // 1) Subscribe to a string event and only listen to a special key (+ queuing is enabled as well)
        // Subscription is managed automatically as well by RXSubscriptionManager
        RxBusBuilder.create(String.class)
                // all optional!!!
                .withQueuing(this)
                .withBound(this)
                .withKey(R.id.custom_event_id_1) // you may add multiple keys as well!
                .withMode(RxBusMode.Main)
                .subscribe(new Consumer<String>(){
                    @Override
                    public void accept(String s) {
                        logEvent(s, true, "custom_event_id_1", null);
                    }
                });

        RxBusBuilder.create(String.class)
                // all optional!!!
                .withQueuing(this)
                .withBound(this)
                .withKey(R.id.custom_event_id_2) // you may add multiple keys as well!
                .withMode(RxBusMode.Main)
                .subscribe(new Consumer<String>(){
                    @Override
                    public void accept(String s) {
                        logEvent(s, true, "custom_event_id_2", null);
                    }
                });

        Flowable<String> flowable = RxBusBuilder.create(String.class)
                .withQueuing(this)
                .withKey(R.id.custom_event_id_1) // you may add multiple keys as well!
                .build();
    }

    private void testAdvanced()
    {
        // 1) subscribe to a string event but emit integers => just pass in a transformer to the subcribe function!
        RxBusBuilder.create(String.class)
                .withQueuing(this)
                .withBound(this)
                .withKey(R.id.custom_event_id_1) // you may add multiple keys as well!
                .withMode(RxBusMode.Main)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer s) {
                        logEvent(s.toString(), true, "custom_event_id_1", " [TRANSFORMED to HASH]");
                    }
                }, new FlowableTransformer<String, Integer>() {
                    @Override
                    public Flowable<Integer> apply(Flowable<String> observable) {
                        return observable
                                .map(new Function<String, Integer>() {
                                    @Override
                                    public Integer apply(String s) {
                                        return s.hashCode();
                                    }
                                });
                    }
                });

        // 2) You need more control or dont want to use the transformer to compose a new observable? Then create an observable only and do the rest yourself!
        Flowable<String> flowable = RxBusBuilder.create(String.class)
                .withQueuing(this)
                .withKey(R.id.custom_event_id_1) // you may add multiple keys as well!
                .build();

        // do whatever youn want with the observable
        Flowable result = flowable
//                ....
//                .toList(...)
//                .flatMap(...)
//                .map(...)
        ;
        Disposable disposable = result.subscribe(new Consumer() {
            @Override
            public void accept(Object o) {
                // ...
            }
        });
        // Don't forget to manage the subcription!! If you want you can use the RXSubscriptionManager manually here:
        RxDisposableManager.addDisposable(this, disposable);
    }
}