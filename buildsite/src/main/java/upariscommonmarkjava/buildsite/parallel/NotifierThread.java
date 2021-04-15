package upariscommonmarkjava.buildsite.parallel;

import java.util.function.Consumer;

public class NotifierThread <Type> {
    private final Thread thread;
    private Type actualVar;

    NotifierThread(final ObserverThread<Type> observer,final Consumer<Type> lambda){
        this.thread = new Thread(() -> {
            lambda.accept(actualVar);
            observer.notify(this);
        });
    }

    public void start(final Type actualVar){
        this.actualVar = actualVar;
        this.thread.start();
    }
}
