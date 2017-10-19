package com.lakeel.altla.ghost.alpha.mock.view.filter;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public abstract class Filter<E, Condition> {

    @NonNull
    private final List<E> originalItems = new ArrayList<>();

    private Condition condition;

    public Filter(@NonNull List<E> originalItems) {
        this.originalItems.clear();
        this.originalItems.addAll(originalItems);
    }

    @CallSuper
    public void setCondition(@NonNull Condition condition) {
        this.condition = condition;
    }

    public void execute() {
        if (condition == null) throw new NullPointerException("The variable (condition) is null.");

        List<E> list = new ArrayList<>();
        list.addAll(originalItems);

        List<E> results = execute(list, condition);
        publishResults(results);
    }

    public void clear() {
        List<E> list = new ArrayList<>();
        list.addAll(originalItems);
        publishResults(list);
    }

    protected abstract List<E> execute(@NonNull List<E> items, @NonNull Condition c);

    protected abstract void publishResults(@NonNull List<E> results);
}
