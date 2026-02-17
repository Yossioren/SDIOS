package com.SDIOS.ServiceControl;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.SDIOS.ServiceControl.AnomalyDetection.uilts.ConcurrentLinkedList;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ConcurrentListTest {
    private static void test_add(List<Integer> list) {
        assert list.isEmpty();
        list.add(1);
        assert !list.isEmpty();
        assert list.get(0) == 1;
        list.add(2);
        assert !list.isEmpty();
        assert list.get(0) == 1 && list.get(1) == 2;
        list.add(4);
        assert list.get(0) == 1 && list.get(1) == 2 && list.get(2) == 4;
        list.add(1, 3);
        assert list.get(0) == 1 && list.get(1) == 2 && list.get(2) == 3 && list.get(3) == 4;
    }

    @Test
    public void testListFunctions() {
        List<Integer> list = new ConcurrentLinkedList<>();
        test_add(list);
        assert list.remove(3) == 4;
        assert !list.isEmpty();
        assert list.remove(0) == 1;
        assert !list.isEmpty();
        list.clear();
        test_add(list);
    }

    @Test
    public void testRemoveSubset() {
        ConcurrentLinkedList<Integer> list = new ConcurrentLinkedList<>();
        assert list.size() == 0;
        assert list.count_recycled() == 0;
        test_add(list);
        assert list.size() == 4;
        assert list.count_recycled() == 0;
        list.remove_subset(1, 2);
        assert list.size() == 2;
        assert list.count_recycled() == 2;
        assert list.remove(0) == 1;
        assert list.size() == 1;
        assert list.count_recycled() == 3;
        assert list.remove(0) == 4;
        assert list.size() == 0;
        assert list.count_recycled() == 4;
        test_add(list);
        assert list.count_recycled() == 0;
        list.remove_subset(0, 2);
        assert list.remove(0) == 4;
        test_add(list);
        list.remove_subset(1, 3);
        assert list.remove(0) == 1;
        test_add(list);
        list.remove_subset(0, 3);
        assert list.isEmpty();
        assert list.size() == 0;
        assert list.count_recycled() == 4;
        test_add(list);
        assert list.size() == 4;
        assert list.count_recycled() == 0;
    }

    @Test
    public void testSize() {
        List<Integer> list = new ConcurrentLinkedList<>();
        assert list.size() == 0;
        list.add(1);
        assert list.size() == 1;
        list.add(2);
        assert list.size() == 2;
        list.remove(1);
        assert list.size() == 1;
        list.clear();
        assert list.size() == 0;
        list.clear();
        assert list.size() == 0;
    }

    @Test
    public void testLock() {
        AtomicInteger lock = new AtomicInteger(0);
        assert lock.get() == 0;
        assert lock.compareAndSet(0, 1);
        assert !lock.compareAndSet(0, 1);
        assert !lock.compareAndSet(0, 1);
        assert lock.get() == 1;
        assert lock.decrementAndGet() == 0;
        assert lock.get() == 0;
        assert lock.compareAndSet(0, 1);
        assert !lock.compareAndSet(0, 1);
    }
}

