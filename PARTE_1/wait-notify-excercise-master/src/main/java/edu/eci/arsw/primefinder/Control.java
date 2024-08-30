package edu.eci.arsw.primefinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Control extends Thread {

    private final static int NTHREADS = 10;
    private final static int MAXVALUE = 100;
    private final static int TMILISECONDS = 5000; // Cambiado a 5000 para pruebas
    private final PrimeFinderThread[] pft;
    private final Object lock = new Object();
    private static boolean paused = false;
    private static int cont = 0;

    private Control() {
        super();
        this.pft = new PrimeFinderThread[NTHREADS];
    
        int i;
        for (i = 0; i < NTHREADS; i++) {
            int start = i * (MAXVALUE / NTHREADS);
            int end = (i + 1) * (MAXVALUE / NTHREADS) + (MAXVALUE / NTHREADS);
            PrimeFinderThread elem = new PrimeFinderThread(start, end, lock);
            pft[i] = elem;
        }
    }

    public static Control newControl() {
        return new Control();
    }

    public static boolean isPaused() {
        return paused;
    }

    @Override
    public void run() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (lock) {
                    paused = true;
                    lock.notifyAll();
                }
            }
        }, TMILISECONDS, TMILISECONDS);

        for (int i = 0; i < NTHREADS; i++) {
            pft[i].start();
        }
        while (true) {
            synchronized (lock) {
                while (!paused) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                showPrimes();
                System.out.println(maxPrime(pft) + " " + MAXVALUE);
                if (maxPrime(pft) >= MAXVALUE) {
                    System.out.println("Se encontró el máximo primo antes del valor máximo. Deteniendo ejecución.");
                    break;
                }
                waitForUserInput();
                paused = false;
                lock.notifyAll();
            }
        }
    }

    private void showPrimes() {
        for (int i = 0; i < NTHREADS; i++) {
            cont += pft[i].getPrimes().size();
        }
        System.out.println("Se han generado " + cont + " primos en total.");
    }

    private void waitForUserInput() {
        System.out.println("Press ENTER to continue...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }

    private int maxPrime(PrimeFinderThread[] list) {
        List<Integer> allPrimes = new ArrayList<>();
        int currentMax = Integer.MIN_VALUE;
        boolean updated = false;
    
        for (PrimeFinderThread thread : list) {
            List<Integer> primes = thread.getPrimes();
            if (!primes.isEmpty()) {
                int localMax = Collections.max(primes);
                if (localMax > currentMax) {
                    currentMax = localMax;
                    updated = true;
                }
                allPrimes.addAll(primes);
            }
        }
    
        if (!updated) {
            return MAXVALUE;
        }
    
        return currentMax;
    }
}
