package com.SDIOS.ServiceControl.AnomalyDetection.uilts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;

import kotlin.NotImplementedError;

class Node<T> {
    public T value;
    public Node<T> next;
    public Node<T> previous;

    public Node(T value, Node<T> previous, Node<T> next) {
        this.value = value;
        this.previous = previous;
        this.next = next;
    }
}

public class ConcurrentLinkedList<T> implements List<T> {
    private final AtomicInteger lock = new AtomicInteger(0);
    private final AtomicInteger size = new AtomicInteger(0);
    private Node<T> head_recycle_bin = null;
    private Node<T> head = null;
    private Node<T> tail = null;

    private void acquire() {
        while (!lock.compareAndSet(0, 1)) {
            Thread.yield();
        }
    }

    private void release() {
        assert lock.decrementAndGet() == 0;
    }

    @Override
    public int size() {
        return size.get();
    }

    @Override
    public boolean isEmpty() {
        return head == null;
    }

    @Override
    public boolean contains(@Nullable Object o) {
        throw new NotImplementedError();
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        throw new NotImplementedError();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        throw new NotImplementedError();
    }

    @NonNull
    @Override
    public <T1> T1[] toArray(@NonNull T1[] t1s) {
        throw new NotImplementedError();
    }

    @Override
    public boolean add(T value) {
        acquire();
        if (tail == null) { // first value
            head = generate_node(value, null, null);
            tail = head;
        } else
            add_node_logic(value, tail);
        release();
        return true;
    }

    @Override
    public boolean remove(@Nullable Object o) {
        return false;
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> collection) {
        throw new NotImplementedError();
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends T> collection) {
        throw new NotImplementedError();
    }

    @Override
    public boolean addAll(int i, @NonNull Collection<? extends T> collection) {
        throw new NotImplementedError();
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> collection) {
        throw new NotImplementedError();
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> collection) {
        throw new NotImplementedError();
    }

    @Override
    public void clear() {
        Node<T> current = head;
        if (head != null) {
            while (current != null) {
                current.previous = null;
                current.value = null;
                current = current.next;
            }
            tail.next = head_recycle_bin;
            head_recycle_bin = head;
        }
        tail = null;
        head = null;
        size.set(0);
    }

    @Override
    public T get(int i) {
        Node<T> current = find_node_acquire(i);
        if (current == null) return null;
        return current.value;
    }

    @Nullable
    private Node<T> find_node(int i) {
        assert i < size() && i >= 0;
        int count = 0;
        Node<T> current = head;
        while (current != null && count++ < i)
            current = current.next;
        return current;
    }

    @Override
    public T set(int i, @Nullable T t) {
        Node<T> node = find_node_acquire(i);
        assert node != null;
        T previous_value = node.value;
        node.value = t;
        return previous_value;
    }

    @Override
    public void add(int i, @Nullable T value) {
        acquire();
        Node<T> requested_node = find_node(i);
        if (requested_node == null) {
            head = generate_node(value, null, null);
            tail = head;
            return;
        }
        add_node_logic(value, requested_node);
        release();
    }

    private void add_node_logic(@Nullable T value, @NonNull Node<T> previous_node) {
        Node<T> next = previous_node.next;
        Node<T> new_node = generate_node(value, previous_node, next);
        previous_node.next = new_node;
        if (next == null)
            tail = new_node;
        else
            next.previous = new_node;
    }

    private Node<T> generate_node(@Nullable T value, @Nullable Node<T> previous, @Nullable Node<T> next) {
        size.incrementAndGet();
        if (head_recycle_bin == null)
            return new Node<>(value, previous, next);
        Node<T> node = head_recycle_bin;
        head_recycle_bin = node.next;
        node.value = value;
        node.previous = previous;
        node.next = next;
        return node;
    }

    private Node<T> find_node_acquire(int i) {
        acquire();
        Node<T> node = find_node(i);
        release();
        return node;
    }

    @Override
    public T remove(int i) {
        acquire();
        try {
            Node<T> node = find_node(i);
            assert node != null;
            Node<T> previous = node.previous;
            Node<T> next = node.next;
            if (next == null)
                tail = previous;
            else
                next.previous = previous;
            if (previous == null)
                head = next;
            else
                previous.next = next;
            T value = node.value;
            recycle_node(node);
            return value;
        } finally {
            release();
        }
    }

    public void remove_subset(int start, int end) {
        assert end > start;
        assert start >= 0;
        acquire();
        try {
            Node<T> node = find_node(start);
            assert node != null;
            Node<T> previous = node.previous;
            for (int i = start; i <= end && node != null; i++) {
                node.previous = null;
                node.value = null;
                node = node.next;
            }
            Node<T> next = node;
            if (next == null) {
                tail.next = head_recycle_bin;
                tail = previous;
            } else {
                next.previous.next = head_recycle_bin;
                next.previous = previous;
            }
            if (previous == null) {
                head_recycle_bin = head;
                head = next;
            } else {
                head_recycle_bin = previous.next;
                previous.next = next;
            }
            size.set(size.get() - end + start - 1);
        } finally {
            release();
        }
    }

    private void recycle_node(@NonNull Node<T> node) {
        node.next = head_recycle_bin;
        node.previous = null;
        node.value = null;
        head_recycle_bin = node;
        size.decrementAndGet();
    }

    @Override
    public int indexOf(@Nullable Object o) {
        throw new NotImplementedError();
    }

    @Override
    public int lastIndexOf(@Nullable Object o) {
        throw new NotImplementedError();
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator() {
        throw new NotImplementedError();
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator(int i) {
        throw new NotImplementedError();
    }

    @NonNull
    @Override
    public List<T> subList(int start, int end) {
        List<T> output = new LinkedList<>();
        Node<T> node = find_node(start);
        assert end >= start;
        assert start >= 0;
        while (node != null && end-- > start) {
            output.add(node.value);
            node = node.next;
        }
        return output;
    }

    public int count_recycled() {
        int count = 0;
        Node<T> node = head_recycle_bin;
        while (node != null) {
            assert node.value == null;
            assert node.previous == null;
            node = node.next;
            count++;
        }
        return count;
    }
}
