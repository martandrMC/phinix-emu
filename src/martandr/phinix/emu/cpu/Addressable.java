package martandr.phinix.emu.cpu;

import java.io.File;

public interface Addressable {
	public int read(int address);
	public void write(int address, int value);
	
	public int load(File file);
	public int save(File file);
}
