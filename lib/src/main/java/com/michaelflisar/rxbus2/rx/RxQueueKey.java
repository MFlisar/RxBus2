package com.michaelflisar.rxbus2.rx;

/**
 * Created by flisar on 03.05.2016.
 */
public class RxQueueKey<T>
{
    private Class<T> mClassId = null;
    private Integer mIntegerId = null;
    private String mStringId = null;

    public RxQueueKey(Class<T> classOfKey, int id)
    {
        if (classOfKey == null)
            throw new RuntimeException("You can't create a withKey for a null class!");

        mClassId = classOfKey;
        mIntegerId = id;
    }

    public RxQueueKey(Class<T> classOfKey, String id)
    {
        if (classOfKey == null)
            throw new RuntimeException("You can't create a withKey for a null class!");
        if (id == null)
            throw new RuntimeException("You can't create a null based withKey!");

        mClassId = classOfKey;
        mStringId = id;
    }

    // ---------------------
    // Equals/Hash
    // ---------------------

    public boolean equals(Object obj)
    {
        if(this == obj)
            return true;
        if((obj == null) || (obj.getClass() != this.getClass()))
            return false;

        RxQueueKey other = (RxQueueKey)obj;

        // Equality is defined by values for keys!
        boolean isEqual = true;
        if (mIntegerId != null)
            isEqual &= mIntegerId.equals(other.mIntegerId);
        if (mStringId != null)
            isEqual &= mStringId.equals(other.mStringId);
        if (mClassId != null)
            isEqual &= mClassId.equals(other.mClassId);
        return isEqual;
    }

    public int hashCode()
    {
        int hash = 7;
        if (mIntegerId != null)
            hash = 31 * hash + mIntegerId.hashCode();
        if (mStringId != null)
            hash = 31 * hash + mStringId.hashCode();
        if (mClassId != null)
            hash = 31 * hash + mClassId.hashCode();
        return hash;
    }

}
