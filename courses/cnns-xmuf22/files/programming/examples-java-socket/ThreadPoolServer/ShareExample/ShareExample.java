/**
 ** XMU CNNS Class Demo Race Condition
 **/

public class ShareExample extends Thread {

    private static int cnt = 0; // shared state

    public void run() {
        int y = cnt;
        System.out.println("Calcuating...");
        cnt = y + 1;
    }

    public static void main(String args[]) {
        Thread t1 = new ShareExample();
        Thread t2 = new ShareExample();
        t1.start();
        t2.start();     

	try {
	    Thread.sleep(1000);     
	    System.out.println("cnt = " + cnt);
	} catch (InterruptedException e) {
	}
    }
}
