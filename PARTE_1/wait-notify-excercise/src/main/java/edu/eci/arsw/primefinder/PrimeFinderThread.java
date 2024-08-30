package edu.eci.arsw.primefinder;

import java.util.LinkedList;
import java.util.List;

public class PrimeFinderThread extends Thread {

    private final Object lock;  // Objeto usado para sincronización entre hilos
    int a, b;
    private List<Integer> primes;
    private volatile boolean paused;  // Volatile para garantizar la visibilidad de cambios entre hilos

    // Constructor modificado para aceptar el objeto de bloqueo (lock)
    public PrimeFinderThread(int a, int b, Object lock) {
        super();
        this.primes = new LinkedList<>();
        this.a = a;
        this.b = b;
        this.lock = lock;  // Asignar el objeto de bloqueo pasado como argumento
        this.paused = false;  // Inicialmente el hilo no está pausado
    }

    @Override
    public void run() {
        for (int i = a; i < b; i++) {
            // Bloque synchronized para gestionar la pausa y reanudación del hilo
            synchronized (lock) {
                while (paused) {  // Si paused es true, el hilo entra en modo espera
                    try {
                        lock.wait();  // Espera hasta ser notificado para continuar
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();  // Restablecer el estado de interrupción del hilo
                        return;  // Termina el hilo si es interrumpido
                    }
                }
            }
            if (isPrime(i)) {
                primes.add(i);
                System.out.println(i);
            }
        }
    }

    // Método para pausar el hilo
    public void pauseThread() {
        paused = true;
    }

    // Método para reanudar el hilo
    public void resumeThread() {
        synchronized (lock) {
            paused = false;  // Cambiar el estado de pausa a false
            lock.notify();  // Notificar a un hilo esperando en el objeto lock
        }
    }

    // Método para verificar si un número es primo
    boolean isPrime(int n) {
        boolean ans;
        if (n > 2) {
            ans = n % 2 != 0;
            for (int i = 3; ans && i * i <= n; i += 2) {
                ans = n % i != 0;
            }
        } else {
            ans = n == 2;
        }
        return ans;
    }

    public List<Integer> getPrimes() {
        return primes;
    }

}
