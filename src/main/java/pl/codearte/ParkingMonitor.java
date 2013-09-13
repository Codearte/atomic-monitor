package pl.codearte;

import java.util.concurrent.locks.LockSupport;

public class ParkingMonitor extends BackOffAtomicMonitor {

    @Override
    protected void backOff() {
        LockSupport.parkNanos(1);
    }
}
