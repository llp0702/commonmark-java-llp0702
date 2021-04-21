package upariscommonmarkjava.buildsite.parallel;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
/** La class obeserver du design pattern [observer-notifier] */
public final class ObserverThread <Type>{
    private final Queue<Type> elements;
    private final AtomicInteger running;

    /**
     * @param nb_thread nombre de thread utilisable (un par Notifier)
     * @param elements nombre d'éléments indépendants à traiter
     * @param lambda fonction de traitement de l'élément
     */
    public ObserverThread(int nb_thread, final Queue<Type> elements, final Consumer<Type> lambda){
        this.running = new AtomicInteger(Math.min(nb_thread,elements.size()));
        this.elements = elements;
        for(; nb_thread > 0; nb_thread--) {
            if(elements.isEmpty()) {
                break;
            }
            new NotifierThread<>(this, lambda);
        }

        if(!(this.running.get() == 0 || elements.isEmpty()))
            lock();
    }

    /** fonction de réception de la notification */
    final synchronized boolean notify(final NotifierThread<Type> notifierThread){
        if (elements.isEmpty()) {
            if (running.decrementAndGet() == 0)
                this.notify();
            return false;
        } else {
            notifierThread.setVarible(elements.poll());
            return true;
        }
    }

    /** fonction de mise en attente de l'observer dans le thread principale */
    private synchronized void lock(){
        try {
            this.wait();
        } catch (InterruptedException ignore) {}
    }
}
