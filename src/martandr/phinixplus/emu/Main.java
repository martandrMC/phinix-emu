package martandr.phinixplus.emu;

import java.io.File;
import java.text.DecimalFormat;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import martandr.phinixplus.emu.cpu.Addressable;
import martandr.phinixplus.emu.cpu.CPU;
import martandr.phinixplus.emu.cpu.UniformMemory;
import martandr.phinixplus.emu.gui.GUI;
import martandr.phinixplus.emu.io.Console;
import martandr.phinixplus.emu.io.TelnetServer;

public class Main {
	private static GUI gui;
	public static GUI getGUI() { return gui; }
	
	private static CPU cpu;
	public static CPU getCPU() { return cpu; }
	
	private static final boolean max_enable = false;
	private static final long max_cycles = 100_000_000L;
	
	public static void main(String[] args) throws Exception {
		UniformMemory memory = new UniformMemory();
		System.err.println("[INFO] Loaded memory with " + memory.load(new File("program.hex")) + " words");
		
		Console console = new Console();
		console.start();
		
		TelnetServer terminal = new TelnetServer(23);
		terminal.start();
		
		Addressable io = new Addressable() {
			@Override
			public void write(int address, int value) {
				address &= 255;
				value &= 65535;
				switch(address) {
					case 0x00: console.put(value); break;
					case 0xFE: terminal.put(value); break;
					case 0xFF: terminal.putPacked(value); break;
				}
			}
			@Override
			public int read(int address) {
				address &= 255;
				switch(address) {
					case 0xFE: return terminal.get();
					case 0xFF: return terminal.getPacked();
					default: return 0;
				}
			}
			@Override
			public int save(File file) { return 0; }
			@Override
			public int load(File file) { return 0; }
		};
		
		cpu = new CPU(memory, io);
		long counter = 0, time = System.nanoTime();
		while(true) {
			boolean halt = isSet(cpu.getState().reg_st, 0);
			boolean exit = halt || (counter >= max_cycles && max_enable);
			if(exit) break;
			cpu.execute();
			counter++;
		}
		long elapsed = System.nanoTime() - time;
		
		Thread.sleep(200);
		console.interrupt();
		console.join();
		terminal.interrupt();
		terminal.join();
		System.err.print("[INFO] Took "+elapsed+" ns to execute "+counter+" instructions");
		System.err.println(" ("+new DecimalFormat(".00").format((counter*1000000.0)/elapsed)+" kHz)");
	}
	
	// Helper Functions
	
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
