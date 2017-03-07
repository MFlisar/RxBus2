package com.michaelflisar.rxbus2;

/**
 * Created by flisar on 07.03.2017.
 */

public class RxBusDefaults
{
    // ---------------------------
    // Singleton
    // ---------------------------

    private static RxBusDefaults INSTANCE = null;

    static RxBusDefaults get()
    {
        if (INSTANCE == null)
            INSTANCE = new RxBusDefaults();
        return INSTANCE;
    }

    // ---------------------------
    // Variables
    // ---------------------------

    private boolean mSendToSuperClassesAsWell = false;

    boolean isSendToSuperClassesAsWell()
    {
        return mSendToSuperClassesAsWell;
    }

    public void setSendToSuperClassesAsWell(boolean enabled)
    {
        mSendToSuperClassesAsWell = enabled;
    }
}
