package pl.codearte;

import pl.codearte.asm.X86Pause;

public class PausingMonitor extends BackOffAtomicMonitor {

    private final X86Pause x86Pause;

    public PausingMonitor(final int pauseCount) {
        x86Pause = X86Pause.pauseFor(pauseCount);
    }

    @Override
    protected void backOff() {
        x86Pause.pause();
    }
}
