package com.lakeel.altla.ghost.alpha.virtualobject.helper;

import java.util.Objects;

public final class IntRange {

    public final int min;

    public final int max;

    public IntRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public boolean contains(int value) {
        return min <= value && value <= max;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntRange intRange = (IntRange) o;
        return min == intRange.min && max == intRange.max;
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }

    @Override
    public String toString() {
        return "[" + min + ", " + max + "]";
    }
}
