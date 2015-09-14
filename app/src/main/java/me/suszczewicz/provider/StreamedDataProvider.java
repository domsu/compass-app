package me.suszczewicz.provider;

import android.content.Context;

import rx.Observable;
import rx.Subscriber;

public abstract class StreamedDataProvider<T> {

    private Observable<T> observable;
    private Subscriber<? super T> subscriber;
    protected Context context;

    public StreamedDataProvider(Context context) {
        this.context = context;

        observable = Observable.create(subscriber -> this.subscriber = subscriber);
    }

    public Observable<T> getObservable() {
        return observable;
    }

    public void emitException(Exception exception) {
        if (canEmit())
            subscriber.onError(exception);
    }

    public void emitData(T item) {
        if (canEmit())
            subscriber.onNext(item);
    }

    public abstract void start();

    public abstract void stop();

    private boolean canEmit() {
        return subscriber != null && !subscriber.isUnsubscribed();
    }
}
