package com.lakeel.altla.ghost.alpha.mock.view.filter;

public interface Filterable<E, Condition> {

    Filter<E, Condition> getFilter();
}
