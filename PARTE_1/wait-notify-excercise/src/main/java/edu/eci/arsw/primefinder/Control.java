package edu.eci.arsw.primefinder;

import java.util.Scanner;

public class Control extends Thread {

    private final static int NTHREADS = 3;
    private final static int MAXVALUE = 30000000;
    private final static int TMILISECONDS = 1000;

    private final int NDATA = MAXVALUE / NTHREADS;

    private PrimeFinderThread pft[];
    private final Object lock = new Object();  // Objeto usado para sincronización entre hilos

    private Control() {
        super();
        this.pft = new PrimeFinderThread[NTHREADS];

        int i;
        // Creación de hilos PrimeFinderThread con el objeto lock para sincronización
        for (i = 0; i < NTHREADS - 1; i++) {
            PrimeFinderThread elem = new PrimeFinderThread(i * NDATA, (i + 1) * NDATA, lock);
            pft[i] = elem;
        }
        pft[i] = new PrimeFinderThread(i * NDATA, MAXVALUE + 1, lock);
    }

    public static Control newControl() {
        return new Control();
    }

    @Override
    public void run() {
        // Inicia todos los hilos
        for (int i = 0; i < NTHREADS; i++) {
            pft[i].start();
        }

        try {
            // Bucle infinito para pausar y reanudar hilos cada TMILISECONDS
            while (true) {
                Thread.sleep(TMILISECONDS);  // Pausa la ejecución por TMILISECONDS milisegundos

                // Pausa todos los hilos
                for (PrimeFinderThread thread : pft) {
                    thread.pauseThread();
                }

                // Imprime el número de primos encontrados hasta ahora
                int totalPrimes = 0;
                for (PrimeFinderThread thread : pft) {
                    totalPrimes += thread.getPrimes().size();
                }
                System.out.println("Primos encontrados hasta ahora: " + totalPrimes);

                // Espera a que el usuario presione ENTER para continuar
                System.out.println("Presione ENTER para continuar...");
                new Scanner(System.in).nextLine();

                // Reanuda todos los hilos
                synchronized (lock) {
                    for (PrimeFinderThread thread : pft) {
                        thread.resumeThread();  // Cambia el estado de pausa a false
                    }
                    lock.notifyAll();  // Notifica a todos los hilos que están esperando en el objeto lock
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Restablecer el estado de interrupción del hilo principal
        }
    }
}
