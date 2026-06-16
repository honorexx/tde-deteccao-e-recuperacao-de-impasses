import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.locks.ReentrantLock;

public class DeadlockDemo {
    private static final ReentrantLock LOCK_A_DEADLOCK = new ReentrantLock();
    private static final ReentrantLock LOCK_B_DEADLOCK = new ReentrantLock();

    private static final ReentrantLock LOCK_A_CORRIGIDO = new ReentrantLock();
    private static final ReentrantLock LOCK_B_CORRIGIDO = new ReentrantLock();

    public static void main(String[] args) throws Exception {
        System.out.println("=== PARTE 3 - VERSAO QUE TRAVA ===");
        reproduzirDeadlock();

        System.out.println("\n=== PARTE 3 - VERSAO CORRIGIDA COM HIERARQUIA ===");
        corrigirDeadlock();
    }

    private static void reproduzirDeadlock() throws Exception {
        Thread t1 = new Thread(() -> {
            LOCK_A_DEADLOCK.lock();
            System.out.println("T1 adquiriu LOCK_A");
            dormir(100);
            System.out.println("T1 tentando adquirir LOCK_B");
            LOCK_B_DEADLOCK.lock();
        }, "Thread-1-deadlock");

        Thread t2 = new Thread(() -> {
            LOCK_B_DEADLOCK.lock();
            System.out.println("T2 adquiriu LOCK_B");
            dormir(100);
            System.out.println("T2 tentando adquirir LOCK_A");
            LOCK_A_DEADLOCK.lock();
        }, "Thread-2-deadlock");

        t1.setDaemon(true);
        t2.setDaemon(true);

        t1.start();
        t2.start();

        Thread.sleep(800);
        diagnosticarDeadlock();
    }

    private static void corrigirDeadlock() throws Exception {
        Thread t1 = new Thread(DeadlockDemo::usarLocksNaOrdemCorreta, "Thread-1-corrigida");
        Thread t2 = new Thread(DeadlockDemo::usarLocksNaOrdemCorreta, "Thread-2-corrigida");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("Conclusao: as duas threads terminaram porque ambas respeitam a mesma ordem: LOCK_A antes de LOCK_B.");
    }

    private static void usarLocksNaOrdemCorreta() {
        LOCK_A_CORRIGIDO.lock();
        System.out.println(Thread.currentThread().getName() + " adquiriu LOCK_A");

        try {
            dormir(80);

            LOCK_B_CORRIGIDO.lock();
            System.out.println(Thread.currentThread().getName() + " adquiriu LOCK_B");

            try {
                System.out.println(Thread.currentThread().getName() + " concluiu");
            } finally {
                LOCK_B_CORRIGIDO.unlock();
            }
        } finally {
            LOCK_A_CORRIGIDO.unlock();
        }
    }

    private static void diagnosticarDeadlock() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        long[] ids = bean.findDeadlockedThreads();

        if (ids == null) {
            System.out.println("Nenhum deadlock detectado automaticamente.");
            return;
        }

        System.out.println("Deadlock detectado pelo ThreadMXBean:");

        ThreadInfo[] infos = bean.getThreadInfo(ids, true, true);
        for (ThreadInfo info : infos) {
            System.out.println("- " + info.getThreadName() + " esta aguardando " + info.getLockName());
        }
    }

    private static void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}