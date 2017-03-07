package com.michaelflisar.rxbus2.demo.classes;

/**
 * Created by Michael on 25.02.2017.
 */

public class TestEvent
{
    private String mText = null;

    public TestEvent withText(String text)
    {
        mText = text;
        return this;
    }

    public String toString()
    {
        if (mText != null)
            return this.getClass().getSimpleName() + " - " + mText;
        return this.getClass().getSimpleName();
    }

    public static class TestSubEvent1 extends TestEvent
    {

    }

    public static class TestSubEvent2 extends TestEvent
    {

    }

    public static class TestSubEvent3 extends TestEvent
    {

    }

    public static class TestSubEvent4 extends TestEvent
    {

    }
}
