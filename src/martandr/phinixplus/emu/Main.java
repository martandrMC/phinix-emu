package martandr.phinixplus.emu;

import java.io.File;
import java.text.DecimalFormat;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import martandr.phinixplus.emu.cpu.Addressable;
import martandr.phinixplus.emu.cpu.CPU;
import martandr.phinixplus.emu.cpu.UniformMemory;
import martandr.phinixplus.emu.gui.GUI;

public class Main {
	private static GUI gui;
	public static GUI getGUI() { return gui; }
	
	private static CPU cpu;
	public static CPU getCPU() { return cpu; }
	
	public static void main(String[] args) throws Exception {
		UniformMemory memory = new UniformMemory();
		System.out.println("[INFO] Loaded memory with "+memory.load(new File("program.hex"))+" words");
		
		BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(256);
		
		Thread printer = new Thread(() -> {
			while(true) {
				try { System.out.println(queue.take()); }
				catch(InterruptedException e) { break; }
			}
			try { while(!queue.isEmpty()) System.out.println(queue.take()); }
			catch (InterruptedException e) { e.printStackTrace(); }
		});
		printer.start();
		
		Addressable io = new Addressable() {
			@Override
			public void write(int address, int value) {
				address &= 255;
				value &= 65535;
				try { if(address == 0) queue.put(value); }
				catch (InterruptedException e) { e.printStackTrace(); }
			}
			@Override
			public int read(int address) { return 0;}
			@Override
			public int save(File file) { return 0;}
			@Override
			public int load(File file) { return 0;}
		};
		
		cpu = new CPU(memory, io);
		long max_cycles = 100_000;
		long counter, time = System.nanoTime();
		for(counter=0; !isSet(cpu.getState().reg_st, 0) && counter < max_cycles; counter++)
			cpu.execute();
		long elapsed = System.nanoTime() - time;
		
		printer.interrupt();
		printer.join();
		System.out.print("[INFO] Took "+elapsed+" ns to execute "+counter+" instructions");
		System.out.println(" ("+new DecimalFormat(".00").format((counter*1000000.0)/elapsed)+" kHz)");
	}
	
	public static String asHex(int num) {
		num &= 65535;
		String str = Integer.toHexString(num);
		for(int i=str.length(); i<4; i++) str = "0" + str;
		return str;
	}
	
	public static boolean isSet(int num, int pos) {
		num &= 65535;
		pos &= 15;
		return (num & (1<<pos)) != 0;
	}
}
