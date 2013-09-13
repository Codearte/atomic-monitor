package pl.codearte.asm;

import crazyivan.CrazyIvan;
import crazyivan.InlineAssembler;
import jnr.x86asm.Assembler;

import java.lang.invoke.MethodHandle;

public class X86Pause {

    private final MethodHandle handle;

    public static X86Pause pauseFor(final int count) {
        return new X86Pause(CrazyIvan.asm(long.class, new InlineAssembler() {
            @Override
            public void assemble(final Assembler asm) {
                for (int i = 0; i < count; i++) asm.pause();
                asm.ret();
            }
        }));
    }

    private X86Pause(final MethodHandle handle) {
        this.handle = handle;
    }

    public void pause() {
        try {
            handle.invoke();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
