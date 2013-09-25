package pl.codearte.asm;

import crazyivan.CrazyIvan;
import crazyivan.InlineAssembler;
import jnr.x86asm.Assembler;

import java.lang.invoke.MethodHandle;

public class X86Asm {

    public static final MethodHandle pause = CrazyIvan.asm(long.class, new InlineAssembler() {
        @Override
        public void assemble(final Assembler asm) {
            asm.pause();
            asm.ret();
        }
    });

    public static void pause() {
        try {
            pause.invoke();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
