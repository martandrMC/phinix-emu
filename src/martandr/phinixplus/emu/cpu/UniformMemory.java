package martandr.phinixplus.emu.cpu;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class UniformMemory implements Addressable {
	private int[] dedotated_wam;
	
	public UniformMemory() {
		dedotated_wam = new int[65536];
	}

	@Override
	public int read(int address) {
		return dedotated_wam[address&65535];
	}

	@Override
	public void write(int address, int value) {
		dedotated_wam[address&65535] = value&65535;
	}

	@Override
	public int load(File file) {
		Scanner sc;
		try { sc = new Scanner(file); }
		catch(FileNotFoundException e) {
			System.err.println("[ERR] File \""+file.getName()+"\" doesn't exist");
			return 0;
		}
		if(!(sc.hasNextLine() && sc.nextLine().equals("v2.0 raw"))) {
			System.err.println("[ERR] File \""+file.getName()+"\" is of invalid format");
			sc.close();
			return 0;
		}
		int counter = 0;
		while(sc.hasNextLine()) {
			String line = sc.nextLine().trim();
			if(line.isEmpty() || line.startsWith(";")) continue;
			String[] words = line.split("\\s+");
			for(String word : words) {
				if(counter >= 65536) {
					System.err.println("[WARN] File \""+file.getName()+"\" contains more than 65536 words; skipping extras");
					break;
				}
				int value = 0;
				try { value = Integer.parseInt(word, 16); }
				catch(NumberFormatException e) {
					System.err.println("[WARN] File \""+file.getName()+"\" contains invalid value \""
							+word+"\" at location "+counter+"; replacing with zero");
				}
				if(value != (value&65535)) {
					value &= 65535;
					System.err.println("[WARN] File \""+file.getName()+"\" contains value out of range \""
							+word+"\" at location "+counter+"; trimming to 16 bits");
				}
				dedotated_wam[counter++] = value;
			}
		}
		sc.close();
		return counter;
	}

	@Override
	public int save(File file) {
		System.err.println("Unimplemented method!");
		return 0;
	}
}
