###RxBus2 [![Release](https://jitpack.io/v/MFlisar/RxBus2.svg)](https://jitpack.io/#MFlisar/RxBus2) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-RxBus2-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/5358)

![RxBus2 - Reactive Event Bus](https://github.com/MFlisar/RxBus2/blob/master/logo/logo.png "RxBus2")

This is an reactive implementation of an event bus, with a few convenient functions especially useful for handling events with activities, fragments and similar.

#####RxJava V1: If you are looking for a version for RxJava V1, check out my [RXBus](https://github.com/MFlisar/RXBus)

### What does it do?

* it allows you to *send events* to a bus
* it allows you to *subscribe to special events* wherever you want
* it allows you to *queue events* until an activity is resumed (to make sure views are accessable for example) and even to *pause and resume events* => for example, queue events while an activity is paused and emit them as soon as it get's resumed
* it's very **lightweight**
 
### Gradle (via [JitPack.io](https://jitpack.io/))

1. add jitpack to your project's `build.gradle`:
```groovy
repositories {
    maven { url "https://jitpack.io" }
}
```
2. add the compile statement to your module's `build.gradle`:
```groovy
dependencies {
    compile 'com.github.MFlisar:RxBus2:0.1'
}
```

### Usage

*Content*

- [Demo](#demo)
- [Simple usage](#simple-usage)
- [Sending an event](#sending-an-event)
- [Advanced usage - QUEUING AND BINDING](#advanced-usage---queuing-and-binding)
- [Advanced usage - KEYS](#advanced-usage---keys)
- [Advanced usage - TRANSFORM](#advanced-usage---transform)
- [Advanced usage - SUB CLASSES](#advanced-usage---sub-classes)
- [Helper class - `RxDisposableManager`](#helper-class---rxdisposablemanager)

#####Demo

Just check out the [DemoActivity](https://github.com/MFlisar/RxBus2/blob/master/demo/src/main/java/com/michaelflisar/rxbus2/demo/DemoActivity.java), it will show the base usage and the difference between the default and the queued `RxBus`

#####Simple usage

Use the `RxBusBuilder` to create `Subscriptions` or simple `Flowables`. Just like following:
```java
// Variant 1 - create a simple Flowable:
Flowable<TestEvent> simpleFlowable = RxBusBuilder.create(TestEvent.class).build();

// Variant 2 - subscribe with the BUILDER:
Disposable simpleDisposable = RxBusBuilder.create(TestEvent.class)
    .subscribe(new Consumer<TestEvent>() {
        @Override
        public void accept(TestEvent event) {
            // Event received, handle it...
           }
    });
```

#####Sending an event

```java
// Send an event to the bus - all observers that observe this class WITHOUT a key will receive this event
RxBus.get().send(new TestEvent());
// Send an event to the bus - only observers that observe the class AND key will receive this event
RxBus.get()
    .withKey(R.id.observer_key_1)
    .send(new TestEvent());
// Send an event to the bus - all observers that either observe the class or the class AND key will receive this event
RxBus.get()
    .withKey(R.id.observer_key_1)
    .withSendToDefaultBus()
    .send(new TestEvent());
// Send an event to the bus and cast it to a specific class (a base class of multiple classes)
// This allows you to send casted objects to the bus
// so that all observers of the casted class will receive this event 
// (of course only if the cast of the send event is possible, otherwise an exception is thrown!)
RxBus.get()
    .withCast(TestEvent.class)
    .send(new SubTestEvent());
```

Of course you combine those functionalities as you want!

#####Advanced usage - QUEUING AND BINDING

You can use this library to subscribe to events and only get them when your activity is resumed, so that you can be sure views are available. Or you can just pause and resume the bus based on any logic you want. Just like following:
```java
RxBusBuilder.create(TestEvent.class)
    // this enables the queuing mode! Passed object must implement IRxBusQueue interface, see the demo app for an example
    .withQueuing(rxBusQueue)
    .withOnNext(new Consumer<TestEvent>() {
        @Override
        public void accept(TestEvent s) {
            // activity IS resumed, you can safely update your UI for example
        }
    })
    .buildSubscription();
```

Additionally, you can bind the subscription to an object and afterwards call `RxDisposableManager.unsubscribe(boundObject);` to unsubcribe ANY subscription that was bound to `boundObject` just like following:

```java
// boundObject... can be for example your activity
RxBusBuilder.create(TestEvent.class)
    // this enables the queuing mode! Passed object must implement IRXBusQueue interface, see the demo app for an example
    .withQueuing(rxBusQueue)
    .withBound(boundObject)
    .subscribe(new Consumer<TestEvent>() {
        @Override
        public void accept(TestEvent s) {
            // activity IS resumed, you can safely update your UI for example
         }
    });
// call this for example in your activities onDestroy or wherever appropriate to unsubscribe ALL subscriptions at once that are bound to the boundOBject
RxDisposableManager.unsubscribe(boundObject);
```

#####Advanced usage - KEYS

You can use this library to subscribe to events of a typ and ONLY get them when it was send to the bus with a special key (and and additionally only when your activity is resumed, as this example shows via `.withQueuing()`), so that you can distinct event subscriptions of the same class based on a key (the key can be an `Integer` or a `String`). Just like following:

```java
RxBusBuilder.create(TestEvent.class)
    // this enables the binding to the key
    .withKey(R.id.observer_key_1) // you can provide multiple keys as well
    .withQueuing(rxBusQueue)
    .withBound(boundObject)
    .subscribe(new Consumer<TestEvent>() {
        @Override
        public void accept(TestEvent event) {
            // activity IS resumed, you can safely update your UI for example
        }
    });
```

#####Advanced usage - Transfrom

You can pass in a `FlowableTransformer` to transform the observed event to whatever you want!

```java
FlowableTransformer<TestEvent, TestEventTransformed> transformer = new FlowableTransformer<TestEvent, TestEventTransformed>() {
    @Override
    public Observable<TestEventTransformed> apply(Flowable<TestEvent> observable) {
        return observable
                .map(new Function<TestEvent, TestEventTransformed>() {
                    @Override
                    public TestEventTransformed apply(TestEvent event) {
                        return event.transform();
                    }
                });
    }
};
RxBusBuilder.create(TestEvent.class)
    .withQueuing(rxBusQueue)
    .withBound(boundObject)
    .subscribe(new Consumer<TestEventTransformed>() {
        @Override
        public void accept(TestEventTransformed transformedEvent) {
        }
    }, transformer);
```

#####Advanced usage - Sub Classes

#####Helper class - `RxDisposableManager`

This class helps to bind `Disposables` to objects and offers an easy way to unsubscribe all `Disposables` that are bound to an object at once. You can simply use this directly via `RxDisposableManager.addDisposable(boundObject, flowable)` or use it directly with the `RxBusBuilder` via the `RxBusBuilder.withBound(boundObject)` which will automatically add the `Disposable` to the `RxDisposableManager` as soon as you call `RxBusBuilder.subscribe(...)`. This will automatically add the `Disposable` to the `RxDisposableManager` when you call `RxBusBuilder.subscribe(...)`. Afterwards you unsubscribe via `RxDisposableManager.unsubscribe(boundObject);`. The bound object can be an `Activity` or `Fragment` for example, but any other object as well.

*Example - Direct usage*

```java
Subscription subscription = RxBusBuilder.create(...).subscribe(...);
RxDisposableManager.addDisposable(activity, subscription);
```

*Example - RxBusBuilder*

```java
RxBusBuilder.create(...).withBound(activity)...;
```

Now you only have to make sure to unsubscribe again like following:

```java
RxDisposableManager.unsubscribe(activity);
```

This will remove ANY subscription that is bound to `activity` and therefore this can be used in your `activity's` `onDestroy` method to make sure ALL subscriptions are unsubscribed at once so that you don't leak the activity.
