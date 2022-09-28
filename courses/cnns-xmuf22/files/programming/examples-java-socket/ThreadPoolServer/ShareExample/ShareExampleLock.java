/**
 ** XMU CNNS Class Demo Lock
 **/

import java.util.concurrent.locks.*;

public class ShareExampleLock extends Thread {
    private static final ReentrantLock lock = new ReentrantLock();

    private static int cnt = 0; // shared state
    public void run() {
	   lock.lock();
        int y = cnt;
        System.out.println("Calculating...");
        cnt = y + 1;
	   lock.unlock();
    }

    public static void main(String args[]) {
        Thread t1 = new ShareExampleLock();
        Thread t2 = new ShareExampleLock();
        t1.start();
        t2.start();     

	   try {
	       Thread.sleep(1000);     
	       System.out.println("cnt = " + cnt);
	   } catch (InterruptedException e) {
	   }
    }
}
