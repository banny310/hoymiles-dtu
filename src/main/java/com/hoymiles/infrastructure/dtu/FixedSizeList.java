package com.hoymiles.infrastructure.dtu;

import com.google.common.collect.ForwardingList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class FixedSizeList<T> extends ForwardingList<T> {
    private final List<T> delegate;
    private final int maxSize;

    public FixedSizeList(List<T> delegate, int maxSize) {
        this.delegate = delegate;
        this.maxSize = maxSize;
    }

    @Override protected List<T> delegate() {
        return delegate;
    }

    @Override public boolean add(T element) {
        assertMaxSizeNotReached(1);
        return super.add(element);
    }

    @Override public void add(int index, T element) {
        assertMaxSizeNotReached(1);
        super.add(index, element);
    }

    @Override public boolean addAll(@NotNull Collection<? extends T> collection) {
        assertMaxSizeNotReached(collection.size());
        return super.addAll(collection);
    }

    @Override public boolean addAll(int index, @NotNull Collection<? extends T> elements) {
        assertMaxSizeNotReached(elements.size());
        return super.addAll(index, elements);
    }

    private void assertMaxSizeNotReached(int size) {
        while (delegate.size() + size > maxSize) {
            // remove last one
            delegate.remove(0);
        }
    }
}
