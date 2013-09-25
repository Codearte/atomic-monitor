package pl.codearte;

import pl.codearte.asm.X86Asm;

public class PausingMonitor extends BackOffAtomicMonitor {

    @Override
    protected void backOff() {
        X86Asm.pause();
    }

}
