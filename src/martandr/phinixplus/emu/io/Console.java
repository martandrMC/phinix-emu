package martandr.phinixplus.emu.io;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Console extends Thread {
	
	private BlockingQueue<Integer> queue;
	public Console() { queue = new ArrayBlockingQueue<>(512); }
	
	public void put(int i) {
		try { queue.put(i); }
		catch (InterruptedException e){ e.printStackTrace(); }
	}
	
	@Override
	public void run() {
		while(true) {
			try { System.out.println(queue.take()); }
			catch(InterruptedException e) { break; }
		}
		try { while(!queue.isEmpty()) System.out.println(queue.take()); }
		catch (InterruptedException e) { e.printStackTrace(); }
	}
}
