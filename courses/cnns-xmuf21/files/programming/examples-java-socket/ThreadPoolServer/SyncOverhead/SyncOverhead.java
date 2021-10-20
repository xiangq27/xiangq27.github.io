/*
 * 
 * CS433/533 Demo
 */
import java.util.concurrent.locks.*;

public class SyncOverhead {

    private static int cnt = 0;
    private static final ReentrantLock lock = new ReentrantLock();
	
    public void run1() {
        int y = cnt;
        cnt = y + 1;
    }


    public synchronized void run2() {
        int y = cnt;
        cnt = y + 1;
    }

    public void run3() {
		synchronized(this) {
	    	int y = cnt;
	    	cnt = y + 1;
		}
    }

    public void run4() {
		lock.lock();
		int y = cnt;
		cnt = y + 1;
		lock.unlock();
    }

    public void run5() {
		lock.lock();
		try {
	    	int y = cnt;
	    	cnt = y + 1;
		}
		finally {
	    	lock.unlock();
		}
    }

    public static void main(String args[]) {

	int method = 1;
	int times = 1000000;
	try {
	    method = Integer.parseInt(args[0]);
	    times = Integer.parseInt(args[1]);
	} catch (Exception e) {
	    System.out.println("Usage: java SyncOverhead <method> <times>");
	    System.exit(1);
	}

	if (times < 0 || method < 1 || method > 5) {
	    System.out.println("Invalid input");
	    System.exit(1);
	}
			
	SyncOverhead s = new SyncOverhead();

	long start = System.currentTimeMillis();
	for (int i = 0; i < times; i++) {
	    switch (method) {
	    case 1: s.run1(); break;
	    case 2: s.run2(); break;
	    case 3: s.run3(); break;
	    case 4: s.run4(); break;
	    case 5: s.run5(); break;
	    }
	}
	long stop = System.currentTimeMillis();

	System.out.println("Run time = " + (stop - start) + " msec.");
    }
}
