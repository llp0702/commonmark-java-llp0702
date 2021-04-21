package upariscommonmarkjava.parallel;

import com.ibm.icu.impl.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import upariscommonmarkjava.buildsite.parallel.IndependantQueue;
import upariscommonmarkjava.buildsite.parallel.Needs;
import upariscommonmarkjava.buildsite.parallel.NeedsException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class IndependantQueueTest {

    private static <Type> List<Needs<Type>> arrayListOf(final Needs<Type>... elements){
        final List<Needs<Type>> list = new ArrayList<>();
        Collections.addAll(list, elements);
        return list;
    }


    @Test
    void testIndependantQueue0()
    {
        final ArrayList<Needs<String>> constraints = new ArrayList<>();
        constraints.add(new Needs<>("A","B", "E"));
        constraints.add(new Needs<>("B", "E"));

        Assertions.assertThrows(NeedsException.class,() -> IndependantQueue.generate(constraints));
    }

    private void testPattern(final List<Needs<String>> in, final String[][] out) {
        try {
            final List<List<String>> result = IndependantQueue.generate(in);
            Assertions.assertEquals(out.length, result.size());

            for(int i = 0; i < out.length; i++)
                Assertions.assertArrayEquals(out[i],result.get(i).toArray());

        }catch(NeedsException ne) {
            Assert.fail(ne);
        }
    }

    @Test
    void testIndependantQueue()
    {
        testPattern(arrayListOf(new Needs<>("A","B", "E"),
                            new Needs<>("C","E", "D"),
                            new Needs<>("B"), new Needs<>("E"),
                            new Needs<>("D")),
                        new String[][]{
                            new String[]{"B","E","D"},
                            new String[]{"A","C"}
                    });
    }

    @Test
    void testIndependantQueue2()
    {
        testPattern(arrayListOf(new Needs<>("A","B"),
                    new Needs<>("B","C"),
                    new Needs<>("C", "D"),
                    new Needs<>("D", "E"),
                    new Needs<>("E")),
                new String[][]{
                        new String[]{"E"},
                        new String[]{"D"},
                        new String[]{"C"},
                        new String[]{"B"},
                        new String[]{"A"}
                });
    }

    @Test
    void testIndependantQueue3()
    {
        Needs<String> needs1 = new Needs<String>("A");
        needs1.addNeededValue("B");
        needs1.addNeededValue("C");
        Needs<String> needs2 = new Needs<String>("C");
        needs2.addNeededValue("D");
        Needs<String> needs3 = new Needs<String>("B");
        Needs<String> needs4 = new Needs<String>("D");

        testPattern(arrayListOf(needs1,needs2,needs3,needs4),
                new String[][]{
                        new String[]{"B","D"},
                        new String[]{"C"},
                        new String[]{"A"}
                });
    }
}
