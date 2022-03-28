package com.test;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class AtomicTest {
    static AtomicBoolean a = new AtomicBoolean(false);

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        Runnable r1 = () -> {
            log.info("i am r1");
            while (!test("r1", 10000)) {

            }
            latch.countDown();
        };
        Runnable r2 = () -> {
            log.info("i am r2");

            while (!test("r2", 6000)) {

            }

            latch.countDown();
        };
        Runnable r3 = () -> {
            log.info("i am r3");
            while (!test("r3", 2000)) {

            }

            latch.countDown();
        };

        Thread t1 = new Thread(r1);
        Thread t2 = new Thread(r2);
        Thread t3 = new Thread(r3);
        t1.start();
        t2.start();
        t3.start();

        latch.await(); // Wait for countdown
    }

    public static boolean test(String name, int sleep) {
        if (a.compareAndSet(false, true)) {
            log.info("{} come in!", name);
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("i am {},exit", name);
            a.compareAndSet(true, false);
            return true;
        }
        return false;
    }
}
