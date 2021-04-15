package upariscommonmarkjava.parallel;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import upariscommonmarkjava.buildsite.parallel.ObserverThread;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

public class ObserverThreadTest {
    @Test
    void testObserverThreadTest() {
        final int size = 100000;
        final Queue<Integer> queue = new PriorityQueue<>();
        for(int x = 0; x < size; x++)
            queue.add(x);

        final ArrayList<Integer> mod2 = new ArrayList<>();
        final ArrayList<Integer> mod3 = new ArrayList<>();
        final ArrayList<Integer> mod5 = new ArrayList<>();
        final ArrayList<Integer> modOther = new ArrayList<>();

        new ObserverThread<>(5,queue, x ->
        {
            if (x % 2 == 0)
                synchronized(mod2){mod2.add(x);}
            else if (x % 3 == 0)
                synchronized(mod3){mod3.add(x);}
            else if (x % 5 == 0)
                synchronized(mod5){mod5.add(x);}
            else
                synchronized(modOther){modOther.add(x);}
        });

        Assertions.assertEquals(size, mod2.size() + mod3.size() + mod5.size() + modOther.size());
    }

    @Test
    void testObserverThreadTest2() {
        final int size = 4;
        final Queue<Integer> queue = new PriorityQueue<>();
        for(int x = 0; x < size; x++)
            queue.add(x);

        final ArrayList<Integer> elem = new ArrayList<>();
        new ObserverThread<>(5,queue, elem::add);
        Assertions.assertEquals(size, elem.size());
    }
}
