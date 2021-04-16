package upariscommonmarkjava.buildsite.parallel;

import java.util.function.Consumer;

public class NotifierThread <Type> {
    private Type actualVar;
    NotifierThread(final ObserverThread<Type> observer,final Consumer<Type> lambda){
        new Thread(() -> {
            while(observer.notify(this)) {
                 lambda.accept(actualVar);
            }
        }).start();
    }

    void setVarible(final Type actualVar){
        this.actualVar = actualVar;
    }
}
