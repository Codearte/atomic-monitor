package pl.codearte;

public class YieldingMonitor extends BackOffAtomicMonitor {

    @Override
    protected void backOff() {
        Thread.yield();
    }
}
