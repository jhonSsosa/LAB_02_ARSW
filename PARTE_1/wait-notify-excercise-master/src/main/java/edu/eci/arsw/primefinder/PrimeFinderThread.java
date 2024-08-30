package edu.eci.arsw.primefinder;

import java.util.LinkedList;
import java.util.List;

public class PrimeFinderThread extends Thread {

    private final int a, b;
    private final List<Integer> primes;
    private final Object lock;

    public PrimeFinderThread(int a, int b, Object lock) {
        super();
        this.primes = new LinkedList<>();
        this.a = a;
        this.b = b;
        this.lock = lock;
    }

    @Override
    public void run() {
        for (int i = a; i <= b; i++) {
            synchronized (lock) {
                while (Control.isPaused()) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (isPrime(i)) {
                synchronized (primes) {
                    primes.add(i);
                }
                System.out.println(i);
            }
        }
        interrupt();
        System.out.println("Hilo terminado.");
    }

    boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }

    public List<Integer> getPrimes() {
        return primes;
    }
}
