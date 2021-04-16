package upariscommonmarkjava.buildsite.parallel;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ObserverThread <Type>{
    private final Queue<Type> elements;
    private final AtomicInteger running;
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

    synchronized boolean notify(final NotifierThread<Type> notifierThread){
        if (elements.isEmpty()) {
            if (running.decrementAndGet() == 0)
                this.notify();
            return false;
        } else {
            notifierThread.setVarible(elements.poll());
            return true;
        }
    }

    private synchronized void lock(){
        try {
            this.wait();
        } catch (InterruptedException ignore) {}
    }
}
