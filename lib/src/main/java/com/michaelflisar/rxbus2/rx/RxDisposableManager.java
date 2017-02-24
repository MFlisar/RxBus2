package com.michaelflisar.rxbus2.rx;

import java.util.HashMap;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by flisar on 28.04.2016.
 */
public class RxDisposableManager
{
    // ---------------------------
    // Singleton
    // ---------------------------

    private static RxDisposableManager INSTANCE = null;

    private static RxDisposableManager get()
    {
        if (INSTANCE == null)
            INSTANCE = new RxDisposableManager();
        return INSTANCE;
    }

    private static HashMap<Class<?>, CompositeDisposable> mDisposables = new HashMap<>();

    // ---------------------------
    // public static bus functions
    // ---------------------------

    public static void addDisposable(Object boundObject, Disposable disposable)
    {
        get().doAddDisposable(boundObject, disposable);
    }

    public static void unsubscribe(Object boundObject)
    {
        get().doUnsubscribe(boundObject);
    }

    // ---------------------------
    // private bus functions
    // ---------------------------

    private void doAddDisposable(Object boundObject, Disposable disposable)
    {
        CompositeDisposable disposables = mDisposables.get(boundObject.getClass());
        if (disposables == null)
        {
            disposables = new CompositeDisposable();
            disposables.add(disposable);
            mDisposables.put(boundObject.getClass(), disposables);
        }
        else
            disposables.add(disposable);
    }

    private void doUnsubscribe(Object boundObject)
    {
        CompositeDisposable disposables = mDisposables.get(boundObject.getClass());
        if (disposables != null)
        {
            disposables.dispose();
            mDisposables.remove(boundObject.getClass());
        }
    }
}
