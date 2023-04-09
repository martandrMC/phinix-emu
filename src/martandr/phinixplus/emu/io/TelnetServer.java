package martandr.phinixplus.emu.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TelnetServer extends Thread {
	
	private BlockingQueue<Integer> inbound, outbound;
	private Socket csoc;
	
	public TelnetServer(int port) {
		inbound = new ArrayBlockingQueue<>(512);
		outbound = new ArrayBlockingQueue<>(512);
		
		try {
			ServerSocket ssoc = new ServerSocket(port);
			csoc = ssoc.accept();
			ssoc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int get() {
		Integer val = inbound.poll();
		return val == null ? 0 : val&255;
	}
	
	public int getPacked() {
		int val = get();
		if(val == 0) return 0;
		val = val << 8 | get();
		return val;
	}
	
	public void put(int i) {
		try { outbound.put(i&255); }
		catch(InterruptedException e) { e.printStackTrace(); }
	}

	public void putPacked(int i) {
		put(i>>8);
		if((i&255) != 0) put(i);
	}

	@Override
	public void run() {
		InputStream istm = null;
		OutputStream ostm = null;
		try {
			istm = csoc.getInputStream();
			ostm = csoc.getOutputStream();
			
			// IAC WON'T SGA IAC WON'T ECHO
			ostm.write(new byte[] {-1, -5, 3, -1, -5, 1});
			ostm.flush();
			// Wait for response commands and toss them out
			while(istm.available() == 0) Thread.sleep(50);
			istm.skip(istm.available());
			
			while(true) {
				if(csoc.isInputShutdown() && csoc.isOutputShutdown()) break;
				if(!outbound.isEmpty()) ostm.write(outbound.take());
				if(inbound.remainingCapacity() > 0 && istm.available() > 0) inbound.put(istm.read());
				System.err.println(outbound.remainingCapacity());
				//Thread.sleep(0, 5000);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch(InterruptedException e) {
			System.err.println("[INFO] Telnet server closing");
		} finally {
			try {
				istm.close();
				ostm.close();
				csoc.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
	}
	
}
