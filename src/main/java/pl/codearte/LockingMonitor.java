package pl.codearte;

import java.util.concurrent.locks.ReentrantLock;

public class LockingMonitor implements Monitor {

    private final ReentrantLock lock;

    public LockingMonitor(final boolean fair) {
        lock = new ReentrantLock(fair);
    }

    @Override
    public void enter() throws InterruptedException {
        lock.lock();
    }

    @Override
    public void exit() {
        lock.unlock();
    }
}
