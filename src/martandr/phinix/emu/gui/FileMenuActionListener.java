package martandr.phinix.emu.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import martandr.phinix.emu.Main;

public class FileMenuActionListener implements ActionListener {
	private final int type;
	
	public FileMenuActionListener(int type) { this.type = type; }
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser(Paths.get("").toAbsolutePath().toString());
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("Logisim 16bit hex", "hex"));
		chooser.setAcceptAllFileFilterUsed(true);
		int ret;
		switch(type) {
			case 0:
				ret = chooser.showOpenDialog(Main.getGUI().getMainWindow());
				if(ret == JFileChooser.APPROVE_OPTION) System.out.println("Open Memory File: "+chooser.getSelectedFile().getAbsolutePath());
				break;
			case 1:
				ret = chooser.showSaveDialog(Main.getGUI().getMainWindow());
				if(ret == JFileChooser.APPROVE_OPTION) System.out.println("Save Memory File: "+chooser.getSelectedFile().getAbsolutePath());
				break;
			default:
				System.err.println("FileMenuActionListener created with invalid handle type "+type);
				break;
		}
	}
}
