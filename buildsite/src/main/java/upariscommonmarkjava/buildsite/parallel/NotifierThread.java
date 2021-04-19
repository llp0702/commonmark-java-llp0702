package upariscommonmarkjava.buildsite.parallel;

import java.util.function.Consumer;
/** Class subject [design pattern observer-notifier] */
public final class NotifierThread <Type> {
    private Type actualVar;

    /**
     * @param observer la class à notifier quand on a fini de travailler
     * @param lambda action à effectuer
     */
    NotifierThread(final ObserverThread<Type> observer,final Consumer<Type> lambda){
        new Thread(() -> {
            while(observer.notify(this)) {
                 lambda.accept(actualVar);
            }
        }).start();
    }
    /** met à jour la variable sur laquel travailler */
    final void setVarible(final Type actualVar){
        this.actualVar = actualVar;
    }
}
