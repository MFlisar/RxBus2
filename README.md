###RXBus [![Release](https://jitpack.io/v/MFlisar/RXBus.svg)](https://jitpack.io/#MFlisar/RXBus) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-RXBus-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/3520)

TODO: Anpassen RxJava => RxJava2!!!

### What does it do?

* it allows you to *post events* to a bus
* it allows you to *subscribe to special events* whereever you want
* it allows you to *queue events* until an activity is resumed (to make sure views are accessable for example)
* it allows you to queue events as soon as activity is paused and emit events as soon soon as it is resumed
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
    compile 'com.github.MFlisar:RXBus:1.0'
}
```

### Migration

If you update from version <0.5 or version <0.9, follow this short migration guide: [MIGRATION GUIDE](https://github.com/MFlisar/RXBus/blob/master/MIGRATION.md)

### Usage

*Content*

- [Demo](#demo)
- [Simple usage](#simple-usage)
- [Sending an event](#sending-an-event)
- [Advanced usage - QUEUING AND BINDING](#advanced-usage---queuing-and-binding)
- [Advanced usage - KEYS](#advanced-usage---keys)
- [Advanced usage](#advanced-usage)
- [Helper class - `RXSubscriptionManager`](#helper-class---rxsubscriptionmanager)

#####Demo

Just check out the [DemoActivity](https://github.com/MFlisar/RXBus/blob/master/demo/src/main/java/com/michaelflisar/rxbus/demo/DemoActivity.java), it will show the base usage and the difference between the default and the queued `RXBus`

#####Simple usage

Use the `RXBusBuilder` to create subscriptions or simple observables. Just like following:
```java
// Variant 1 - create a simple observable :
Observable<TestEvent> simpleObservable = RXBusBuilder.create(TestEvent.class).build();

// Variant 2 - subscribe with the BUILDER:
Subscription simpleSubscription = RXBusBuilder.create(TestEvent.class)
    .subscribe(new Action1<TestEvent>() {
        @Override
        public void call(TestEvent event) {
            // handle event...
            
            // event MUST have been send with either of following:
            // RXBus.get().sendEvent(new TestEvent()); => class bound bus usage
            // RXBus.get().sendEvent(new TestEvent(), R.id.observer_key_1, true); => key bound bus usage, with sendToDefaultBusAsWell = true, which will result in that all class bound observers (like this one) retrieve this event as well
        }
    });
```
#####Sending an event
```java
// Send an event to the bus - all observers that observe this class WITHOUT a key will receive this event
RXBus.get().sendEvent(new TestEvent());
// Send an event to the bus - only observers that observe the class AND key will receive this event
RXBus.get().sendEvent(new TestEvent(), R.id.observer_key_1);
// Send an event to the bus - all observers that either observe the class or the class AND key will receive this event
RXBus.get().sendEvent(new TestEvent(), R.id.observer_key_1, true);
```
#####Advanced usage - QUEUING AND BINDING

You can use this library to subscribe to events and only get them when your activity is resumed, so that you can be sure views are available, for example. Just like following:
```java
RXBusBuilder.create(TestEvent.class)
    // this enables the queuing mode! Passed object must implement IRXBusQueue interface, see the demo app for an example
    .withQueuing(rxBusQueue)
    .withOnNext(new Action1<TestEvent>() {
        @Override
        public void call(TestEvent s) {
            // activity IS resumed, you can safely update your UI for example
            
            // event MUST have been send with either of following:
            // RXBus.get().sendEvent(new TestEvent()); => class bound bus usage
            // RXBus.get().sendEvent(new TestEvent(), R.id.observer_key_1, true); => key bound bus usage, with sendToDefaultBusAsWell = true, which will result in that all class bound observers (like this one) retrieve this event as well
        }
    })
    .buildSubscription();
```

Additionally, you can bind the subscription to an object and afterwards call `RXSubscriptionManager.unsubscribe(boundObject);` to unsubcribe ANY subscription that was bound to `boundObject` just like following:

```java
// boundObject... can be for example your activity
RXBusBuilder.create(TestEvent.class)
    // this enables the queuing mode! Passed object must implement IRXBusQueue interface, see the demo app for an example
    .withQueuing(rxBusQueue)
    .withBound(boundObject)
    .subscribe(new Action1<Object>() {
        @Override
        public void call(TestEvent s) {
            // activity IS resumed, you can safely update your UI for example

            // event MUST have been send with either of following:
            // RXBus.get().sendEvent(new TestEvent()); => class bound bus usage
            // RXBus.get().sendEvent(new TestEvent(), R.id.observer_key_1, true); => key bound bus usage, with sendToDefaultBusAsWell = true, which will result in that all class bound observers (like this one) retrieve this event as well
        }
    });
// call this for example in your activities onDestroy or whereever appropriate to unsubscribe ALL subscriptions at once that are bound to the boundOBject
RXSubscriptionManager.unsubscribe(boundObject);
```

#####Advanced usage - KEYS

You can use this library to subscribe to events of a typ and ONLY get them when it was send to the bus with a special key (and only when your activity is resumed, as this example shows via `.withQueuing()`), so that you can distinct event subscriptions of the same class based on a key (the key can be an `Integer` or a `String`). Just like following:
```java
RXBusBuilder.create(TestEvent.class)
    // this enables the binding to the key
    .withKey(R.id.observer_key_1) // you can provide multiple keys as well
    .withQueuing(rxBusQueue)
    .withBound(boundObject)
    .subscribe(new Action1<String>() {
        @Override
        public void call(TestEvent event) {
            // activity IS resumed, you can safely update your UI for example

            // event MUST have been with either of those:
            // RXBus.get().sendEvent(new TestEvent(), R.id.observer_key_1); => key bound bus usage, class bound observers WON't retrieve this event as well!
            // RXBus.get().sendEvent(new TestEvent(), R.id.observer_key_1, true); => key bound bus usage, with sendToDefaultBusAsWell = true, resulting in class bound observers WILL retrieve this event as well!
        }
    });
```

#####Advanced usage

You can pass in a `Observable.Transformer` to transform the observed event to whatever you want!

```java
Observable.Transformer<TestEvent, TestEventTransformed> transformer = new Observable.Transformer<TestEvent, TestEventTransformed>() {
    @Override
    public Observable<TestEventTransformed> call(Observable<TestEvent> observable) {
        return observable
                .map(new Func1<TestEvent, TestEventTransformed>() {
                    @Override
                    public TestEventTransformed call(TestEvent event) {
                        return event.transform();
                    }
                });
    }
};
RXBusBuilder.create(TestEvent.class)
    .withQueuing(rxBusQueue)
    .withBound(boundObject)
    .subscribe(new Action1<TestEventTransformed>() {
        @Override
        public void call(TestEventTransformed transformedEvent) {
        }
    }, transformer);
```

#####Helper class - `RXSubscriptionManager`

This class helps to bind `subscriptions` to objects and offers an easy way to unsubscribe all `subscriptions` that are bound to an object at once. You can simply use is directly with the `RXBusBuilder` via the `RXBusBuilder.withBound(boundObject)`. This will automatically add the subscription to the `RXSubscriptionManager` when you call `RXBusBuilder.subscribe(...).

*Directly*
```java
Subscription subscription = RXBusBuilder.create(...).build();
RXSubscriptionManager.addSubscription(activity, subscription);
```

*RXBusBuilder*
```java
RXBusBuilder.create(...).withBound(activity);
```

Now you only have to make sure to unsubscribe again like following:
```java
RXSubscriptionManager.unsubscribe(activity);
```

This will remove ANY subscription that is bound to `activity` and therefore this can be used in your `activity's` `onDestroy` method to make sure ALL subscriptions are unsubscribed at once and that you don't leak the activity.


### Credits

The `RxValve` class is from this gist: https://gist.github.com/akarnokd/1c54e5a4f64f9b1e46bdcf62b4222f08
