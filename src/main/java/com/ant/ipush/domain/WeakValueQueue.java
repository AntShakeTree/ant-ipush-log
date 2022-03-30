package com.ant.ipush.domain;

import lombok.SneakyThrows;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.LinkedBlockingQueue;

public class WeakValueQueue<T> {
    private final ReferenceQueue<T> queue;
    private final LinkedBlockingQueue<WeakValue> linkedBlockingQueue;

    private T getReferenceValue(WeakValue valueRef) {
        return valueRef == null ? null : (T) valueRef.get();
    }


    public WeakValueQueue() {
        queue = new ReferenceQueue<>();
        linkedBlockingQueue = new LinkedBlockingQueue<>(1 << 15);
    }

    @SneakyThrows
    public T take(){
        T t=getReferenceValue(linkedBlockingQueue.take());
        return t;
    }

    @SneakyThrows
    public void push(T value) {
        if (value == null) {
            return;
        }
        processQueue();
        WeakValue valueRef = new WeakValue(value, queue);
        linkedBlockingQueue.put(valueRef);
    }

    private void processQueue() {
        WeakValue valueRef;
        while ((valueRef = (WeakValue) queue.poll()) != null) {
            linkedBlockingQueue.remove(valueRef.get());
        }
    }


    private class WeakValue extends WeakReference<T> {


        private WeakValue(T value, ReferenceQueue<T> queue) {
            super(value, queue);
        }
    }

    public static void main(String[] args) {
        WeakValueQueue<Integer> loggingEventWeakValueQueue=new WeakValueQueue<>();
        loggingEventWeakValueQueue.push(1);
        loggingEventWeakValueQueue.push(2);
        loggingEventWeakValueQueue.push(3);
        Integer i;
        while ((i =loggingEventWeakValueQueue.take())!=null){
            System.out.println(i);
        }



//        loggingEventWeakValueQueue.

    }
}
