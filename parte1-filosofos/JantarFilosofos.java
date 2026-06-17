import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class JantarFilosofos {
    private static final int N = 5;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PARTE 1 - VERSAO INGENUA COM POSSIVEL DEADLOCK ===");
        executarVersaoIngenua();

        System.out.println("\n=== PARTE 1 - VERSAO CORRIGIDA COM GARCOM N-1 ===");
        executarVersaoCorrigida();
    }

    private static void executarVersaoIngenua() throws Exception {
        ReentrantLock[] garfos = criarGarfos();
        CountDownLatch todosComGarfosEsquerda = new CountDownLatch(N);

        for (int i = 0; i < N; i++) {
            final int id = i;
            Thread t = new Thread(() -> {
                int esquerda = id;
                int direita = (id + 1) % N;

                pensar(id);
                log(id, "com fome");
                garfos[esquerda].lock();
                log(id, "pegou o garfo esquerdo " + esquerda);
                todosComGarfosEsquerda.countDown();

                try {
                    todosComGarfosEsquerda.await();
                    log(id, "tentando pegar o garfo direito " + direita);
                    garfos[direita].lock();
                    try {
                        comer(id);
                    } finally {
                        garfos[direita].unlock();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    garfos[esquerda].unlock();
                }
            }, "Filosofo-Ingenuo-" + id);
            t.setDaemon(true);
            t.start();
        }

        Thread.sleep(1500);
        System.out.println("Diagnostico: todos seguram um garfo e aguardam outro. A versao ingenua entrou em deadlock.");
    }

    private static void executarVersaoCorrigida() throws Exception {
        ReentrantLock[] garfos = criarGarfos();
        Semaphore garcom = new Semaphore(N - 1, true);
        Thread[] filosofos = new Thread[N];

        for (int i = 0; i < N; i++) {
            final int id = i;
            filosofos[i] = new Thread(() -> {
                int esquerda = id;
                int direita = (id + 1) % N;

                for (int rodada = 1; rodada <= 3; rodada++) {
                    pensar(id);
                    log(id, "com fome - rodada " + rodada);

                    try {
                        garcom.acquire();
                        garfos[esquerda].lock();
                        garfos[direita].lock();
                        try {
                            comer(id);
                        } finally {
                            garfos[direita].unlock();
                            garfos[esquerda].unlock();
                            garcom.release();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }, "Filosofo-Corrigido-" + id);
            filosofos[i].start();
        }

        for (Thread t : filosofos) {
            t.join();
        }
<<<<<<< HEAD
        System.out.println("Conclusao: todos os filosofos conseguiram comer 3 vezes. Nao houve deadlock nem iniciação observavel.");
=======
        System.out.println("Conclusao: todos os filosofos conseguiram comer 3 vezes. Nao houve deadlock nem inanicao observavel.");
>>>>>>> 92dd32e61acb4ad7973a009623dd1e1177c775f1
    }

    private static ReentrantLock[] criarGarfos() {
        ReentrantLock[] garfos = new ReentrantLock[N];
        for (int i = 0; i < N; i++) {
            garfos[i] = new ReentrantLock(true);
        }
        return garfos;
    }

    private static void pensar(int id) {
        log(id, "pensando");
        dormir(100);
    }

    private static void comer(int id) {
        log(id, "comendo");
        dormir(120);
    }

    private static void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void log(int id, String msg) {
        System.out.printf("[%s] Filosofo %d: %s%n", Thread.currentThread().getName(), id, msg);
    }
}
