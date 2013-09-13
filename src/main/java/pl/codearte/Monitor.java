package pl.codearte;

public interface Monitor {

    void enter() throws InterruptedException;
    void exit();

}
