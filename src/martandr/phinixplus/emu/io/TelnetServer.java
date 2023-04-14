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
		try {
			return inbound.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public int getPacked() {
		try {
			int val = inbound.take();
			return val << 8 | inbound.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return 0;
		}
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
			ostm.write(new byte[] {-1, -3, 3, -1, -2, 1});
			ostm.flush();
			// Wait for response commands and toss them out
			while(istm.available() == 0) Thread.sleep(50);
			System.err.print("[DBG] Telnet response: ");
			for(int i=istm.available(); i>0; i--)
				System.err.print(Integer.toHexString(istm.read()) + " ");
			System.err.println();
			
			while(true) {
				if(!outbound.isEmpty()) ostm.write(outbound.take());
				if(inbound.remainingCapacity() > 0 && istm.available() > 0) inbound.put(istm.read());
				Thread.sleep(0);
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
