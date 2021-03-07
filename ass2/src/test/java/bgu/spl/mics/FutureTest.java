package bgu.spl.mics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FutureTest {
    private Future<Integer> f;
    @Before
    public void setUp() throws Exception {
        try {
            f = new Future<>();
        }
        catch(Exception e){
            throw new IllegalArgumentException("Future default constructor isnt defined");
        }

    }

    @After
    public void tearDown() throws Exception {
        f=null;
        assertNull(f);
    }
    //Checking get()
    @Test
    public void get() {
        f.resolve(2);
        assertEquals(2,f.get().intValue());
    }

    @Test
    public void resolve() {
        f.resolve(2);
        assertEquals(2,f.get().intValue());
    }

    @Test
    public void isDone() {
        assertEquals(false,f.isDone());
        f.resolve(2);
        assertEquals(true,f.isDone());

    }
    //Checking get(T,long timeout, TimeUnit unit)
    @Test
    public void get1() {
        Integer i = f.get(3, TimeUnit.SECONDS);
        assertNull(i);
        f.resolve(5);
        Integer i2 = f.get(5,TimeUnit.SECONDS);
        assertEquals(5,i2.intValue());
    }
}