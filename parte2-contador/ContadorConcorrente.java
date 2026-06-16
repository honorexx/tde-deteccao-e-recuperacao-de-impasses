import java.util.concurrent.Semaphore;

public class ContadorConcorrente {
    private static final int T = 8;
    private static final int M = 200_000;
    private static int contador = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PARTE 2 - CONTADOR SEM SINCRONIZACAO ===");
        executarSemSincronizacao();

        System.out.println("\n=== PARTE 2 - CONTADOR COM SEMAFORO BINARIO ===");
        executarComSemaforo();
    }

    private static void executarSemSincronizacao() throws Exception {
        contador = 0;
        Thread[] threads = new Thread[T];
        long inicio = System.nanoTime();

        for (int i = 0; i < T; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < M; j++) {
                    int temp = contador;
                    if (j % 100 == 0) {
                        Thread.yield();
                    }
                    contador = temp + 1;
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        long fim = System.nanoTime();
        imprimirResultado("sem sincronizacao", fim - inicio);
    }

    private static void executarComSemaforo() throws Exception {
        contador = 0;
        Semaphore semaforo = new Semaphore(1, true);
        Thread[] threads = new Thread[T];
        long inicio = System.nanoTime();

        for (int i = 0; i < T; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < M; j++) {
                    try {
                        semaforo.acquire();
                        contador++;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    } finally {
                        semaforo.release();
                    }
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        long fim = System.nanoTime();
        imprimirResultado("com semaforo", fim - inicio);
    }

    private static void imprimirResultado(String versao, long nanos) {
        int esperado = T * M;
        double ms = nanos / 1_000_000.0;
        System.out.printf("Versao: %s%n", versao);
        System.out.printf("Esperado: %d%n", esperado);
        System.out.printf("Obtido:   %d%n", contador);
        System.out.printf("Tempo:    %.2f ms%n", ms);
    }
}
