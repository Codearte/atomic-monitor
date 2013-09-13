package pl.codearte;

import java.util.concurrent.atomic.AtomicLong;

public abstract class BackOffAtomicMonitor implements Monitor {

    private final AtomicLong monitor = new AtomicLong();

    @Override
    public void enter() throws InterruptedException {
        final Thread thread = Thread.currentThread();
        final long id = thread.getId();
        while (!monitor.compareAndSet(0, id)) {
            backOff();
            if (thread.isInterrupted()) throw new InterruptedException();
        }
    }

    public void exit() {
        monitor.set(0);
    }

    protected abstract void backOff();

}
