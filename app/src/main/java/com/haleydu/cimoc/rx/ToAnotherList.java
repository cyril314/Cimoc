package com.haleydu.cimoc.rx;

import rx.Observable;
import rx.functions.Func1;

import java.util.List;


public class ToAnotherList<T, R> implements Observable.Transformer<List<T>, List<R>> {

    private Func1<T, R> func;

    public ToAnotherList(Func1<T, R> func) {
        this.func = func;
    }

    @Override
    public Observable<List<R>> call(Observable<List<T>> observable) {
        return observable.flatMap(new Func1<List<T>, Observable<T>>() {
            @Override
            public Observable<T> call(List<T> list) {
                return Observable.from(list);
            }
        }).map(func).toList();
    }

}
