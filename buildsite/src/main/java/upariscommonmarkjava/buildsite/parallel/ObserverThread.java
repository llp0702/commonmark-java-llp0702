package upariscommonmarkjava.buildsite.parallel;

import java.util.Queue;
import java.util.function.Consumer;

public class ObserverThread <Type>{
    private final Queue<Type> elements;
    private int running;
    public ObserverThread(int nb_thread, final Queue<Type> elements, final Consumer<Type> lambda){
        this.running = Math.min(nb_thread,elements.size());
        this.elements = elements;
        for(; nb_thread > 0 && !elements.isEmpty(); nb_thread--)
            new NotifierThread<>(this, lambda);
        lock();
    }

    synchronized boolean notify(final NotifierThread<Type> notifierThread){
        if(elements.isEmpty()) {
            running--;
            if(running == 0)
                this.notify();
            return false;
        }
        else {
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
