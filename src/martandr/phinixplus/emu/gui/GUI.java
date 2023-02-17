package martandr.phinixplus.emu.gui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

public class GUI {
	private static final String[][] regfile_names = {
			{"x0: $zr", "x1: $t0/at", "x2: $t1",    "x3: $t2", "x4: $t3", "x5: $t4", "x6: $a0/rp", "x7: $a1",
			 "x8: $a2", "x9: $a3",    "xA: $s0/fp", "xB: $s1", "xC: $s2", "xD: $s3", "xE: $s4",    "xF: $s5"},
			{"y0: $a4", "y1: $a5",    "y2: $a6",    "y3: $a7", "y4: $t5", "y5: $t6", "y6: $t7",    "y7: $t8",
			 "y8: $s6", "y9: $s7",    "yA: $s8/gp", "yB: $sp", "yC: $k0" ,"yD: $k1", "yE: $k2",    "yF: $kp"}
	};
	
	private JFrame main_window;
	public JFrame getMainWindow() { return main_window; }
	
	private JTextField[][] regfile_vals;
	public JTextField[][] getRegisterFileValues() { return regfile_vals; }
	
	private JTextField ip_val, jp_val, rf_val, stf_val, stg_val;
	public JTextField getSpecialRegisterValues(int select) {
		switch(select) {
			case 0: return ip_val;
			case 1: return jp_val;
			case 2: return rf_val;
			case 3: return stf_val;
			case 4: return stg_val;
			default: return null;
		}
	}
	
	public GUI(int width, int height, Runnable exit_operation) {
		regfile_vals = new JTextField[2][16];
		EventQueue.invokeLater(() -> {
			// Setup window with basic parameters
			main_window = new JFrame("PHINIX+ System Emulator");
			main_window.getContentPane().setLayout(null);
			main_window.setBounds(0, 0, width, height);
			main_window.setResizable(false);
			
			// Setup exit handler
			if(exit_operation == null) main_window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			else {
				main_window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				main_window.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						exit_operation.run();
					}
				});
			}
		});
	}
	
	public void initMenuBar() {
		EventQueue.invokeLater(() -> {
			JMenuBar menu_bar = new JMenuBar();
			
			JMenu file_menu = new JMenu("File");
			menu_bar.add(file_menu);
			
			JMenuItem file_loadmem = new JMenuItem("Load Memory...");
			file_loadmem.addActionListener(new FileMenuActionListener(0));
			file_menu.add(file_loadmem);
			
			JMenuItem file_savemem = new JMenuItem("Save Memory...");
			file_savemem.addActionListener(new FileMenuActionListener(1));
			file_menu.add(file_savemem);
			
			main_window.setJMenuBar(menu_bar);
		});
	}
	
	public void initContentPane() {
		EventQueue.invokeLater(() -> {
			// CPU State Panel
			JPanel cpu_state = new JPanel(null);
			cpu_state.setBounds(10, 10, 550, 420);
			cpu_state.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
			
			JLabel cpu_state_title = new JLabel("State Control");
			cpu_state_title.setHorizontalAlignment(SwingConstants.CENTER);
			cpu_state_title.setFont(new Font("Arial Black", Font.BOLD, 20));
			cpu_state_title.setBounds(0, 0, 550, 40);
			
			cpu_state.add(cpu_state_title);
			populateStatePanel(cpu_state);
			main_window.getContentPane().add(cpu_state);
			
			// Clock Control Panel
			JPanel clock_control = new JPanel(null);
			clock_control.setBounds(570, 10, 320, 120);
			clock_control.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
			
			JLabel clock_control_title = new JLabel("Clock Control");
			clock_control_title.setHorizontalAlignment(SwingConstants.CENTER);
			clock_control_title.setFont(new Font("Arial Black", Font.BOLD, 20));
			clock_control_title.setBounds(0, 0, 320, 40);
			
			clock_control.add(clock_control_title);
			populateClockPanel(clock_control);
			main_window.getContentPane().add(clock_control);
			
			// Memory Inspector Panel
			JPanel memory_inspector = new JPanel(null);
			memory_inspector.setBounds(570, 140, 320, 290);
			memory_inspector.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
			
			JLabel memory_title = new JLabel("Memory Inspector");
			memory_title.setHorizontalAlignment(SwingConstants.CENTER);
			memory_title.setFont(new Font("Arial Black", Font.BOLD, 20));
			memory_title.setBounds(0, 0, 320, 40);
			
			memory_inspector.add(memory_title);
			populateMemoryPanel(memory_inspector);
			main_window.getContentPane().add(memory_inspector);
		});
	}
	
	private void populateStatePanel(JPanel cpu_state) {
		// Primary Register File Panel
		JPanel primary_regfile = new JPanel(null);
		primary_regfile.setBounds(10, 40, 260, 270);
		primary_regfile.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		
		JLabel primary_regfile_title = new JLabel("Primary Register File");
		primary_regfile_title.setHorizontalAlignment(SwingConstants.CENTER);
		primary_regfile_title.setFont(new Font("Arial Black", Font.BOLD, 16));
		primary_regfile_title.setBounds(0, 0, 260, 30);
		
		primary_regfile.add(primary_regfile_title);
		for(int i=0; i<16; i++) {
			JLabel reg_nam = new JLabel(regfile_names[0][i]);
			reg_nam.setBounds(i>7?190:60, 30*(i&7)+30, 60, 20);
			primary_regfile.add(reg_nam);
			
			JTextField reg_val = new JTextField("0000");
			reg_val.setHorizontalAlignment(SwingConstants.CENTER);
			reg_val.setFont(new Font("Monospaced", Font.PLAIN, 11));
			reg_val.setBounds(i>7?10:140, 30*(i&7)+30, 40, 20);
			reg_val.addFocusListener(new HexadecimalFilter());
			primary_regfile.add(reg_val);
			regfile_vals[0][i] = reg_val;
		}
		cpu_state.add(primary_regfile);
		
		// Secondary Register File Panel
		JPanel secondary_regfile = new JPanel(null);
		secondary_regfile.setBounds(280, 40, 260, 270);
		secondary_regfile.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		
		JLabel secondary_regfile_title = new JLabel("Secondary Register File");
		secondary_regfile_title.setHorizontalAlignment(SwingConstants.CENTER);
		secondary_regfile_title.setFont(new Font("Arial Black", Font.BOLD, 16));
		secondary_regfile_title.setBounds(0, 0, 260, 30);
		
		secondary_regfile.add(secondary_regfile_title);
		for(int i=0; i<16; i++) {
			JLabel reg_nam = new JLabel(regfile_names[1][i]);
			reg_nam.setBounds(i>7?190:60, 30*(i&7)+30, 60, 20);
			secondary_regfile.add(reg_nam);
			
			JTextField reg_val = new JTextField("0000");
			reg_val.setHorizontalAlignment(SwingConstants.CENTER);
			reg_val.setFont(new Font("Monospaced", Font.PLAIN, 11));
			reg_val.setBounds(i>7?10:140, 30*(i&7)+30, 40, 20);
			reg_val.addFocusListener(new HexadecimalFilter());
			secondary_regfile.add(reg_val);
			regfile_vals[1][i] = reg_val;
		}
		cpu_state.add(secondary_regfile);
		
		// Special Purpose Registers Panel
		JPanel special_purpose = new JPanel(null);
		special_purpose.setBounds(10, 320, 280, 90);
		special_purpose.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		
		JLabel special_purpose_title = new JLabel("Special Purpose Registers");
		special_purpose_title.setFont(new Font("Arial Black", Font.BOLD, 16));
		special_purpose_title.setHorizontalAlignment(SwingConstants.CENTER);
		special_purpose_title.setBounds(0, 0, 280, 30);
		
		special_purpose.add(special_purpose_title);
		{
			ip_val = new JTextField("0000");
			ip_val.setHorizontalAlignment(SwingConstants.CENTER);
			ip_val.setFont(new Font("Monospaced", Font.PLAIN, 11));
			ip_val.setBounds(10, 30, 40, 20);
			ip_val.addFocusListener(new HexadecimalFilter());
			special_purpose.add(ip_val);
			
			JLabel ip_nam = new JLabel("$ip");
			ip_nam.setBounds(60, 30, 30, 20);
			special_purpose.add(ip_nam);
			
			jp_val = new JTextField("0000");
			jp_val.setHorizontalAlignment(SwingConstants.CENTER);
			jp_val.setFont(new Font("Monospaced", Font.PLAIN, 11));
			jp_val.setBounds(10, 60, 40, 20);
			jp_val.addFocusListener(new HexadecimalFilter());
			special_purpose.add(jp_val);
			
			JLabel jp_nam = new JLabel("$jp");
			jp_nam.setBounds(60, 60, 30, 20);
			special_purpose.add(jp_nam);
			
			rf_val = new JTextField();
			rf_val.setBounds(110, 30, 120, 20);
			special_purpose.add(rf_val);
			rf_val.setFont(new Font("Monospaced", Font.PLAIN, 11));
			rf_val.setHorizontalAlignment(SwingConstants.CENTER);
			rf_val.setText("1010010001010101");
			rf_val.setColumns(10);
			
			JLabel rf_nam = new JLabel("$rf");
			rf_nam.setBounds(240, 30, 30, 20);
			special_purpose.add(rf_nam);
			
			stf_val = new JTextField("F: ----");
			stf_val.setHorizontalAlignment(SwingConstants.CENTER);
			stf_val.setFont(new Font("Monospaced", Font.PLAIN, 11));
			stf_val.setBounds(110, 60, 60, 20);
			special_purpose.add(stf_val);
			
			stg_val = new JTextField("G: 0000");
			stg_val.setHorizontalAlignment(SwingConstants.CENTER);
			stg_val.setFont(new Font("Monospaced", Font.PLAIN, 11));
			stg_val.setBounds(170, 60, 60, 20);
			special_purpose.add(stg_val);
			
			JLabel st_nam = new JLabel("$st");
			st_nam.setBounds(240, 60, 30, 20);
			special_purpose.add(st_nam);
		}
		cpu_state.add(special_purpose);
		
		// Extras Panel
		JPanel extras = new JPanel(null);
		extras.setBounds(300, 320, 240, 90);
		extras.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		cpu_state.add(extras);
	}
	
	private void populateClockPanel(JPanel clock_control) {
		JToggleButton start_button = new JToggleButton("Start");
		start_button.setBounds(10, 40, 80, 30);
		clock_control.add(start_button);
		
		JRadioButton step_select = new JRadioButton("Step");
		step_select.setBounds(100, 40, 60, 30);
		clock_control.add(step_select);
		
		JRadioButton run_select = new JRadioButton("Auto");
		run_select.setBounds(170, 40, 60, 30);
		clock_control.add(run_select);
		
		JLabel speedometer = new JLabel("999 kHz");
		speedometer.setFont(new Font("Dialog", Font.BOLD, 16));
		speedometer.setBounds(240, 40, 70, 30);
		clock_control.add(speedometer);
		
		JButton step_btn = new JButton("Pulse");
		step_btn.setBounds(10, 80, 80, 30);
		clock_control.add(step_btn);
		
		JSlider speed_dial = new JSlider();
		speed_dial.setValue(0);
		speed_dial.setMaximum(100000);
		speed_dial.setBounds(100, 80, 210, 30);
		clock_control.add(speed_dial);
	}
	
	private void populateMemoryPanel(JPanel memory_inspector) {
		JRadioButton follow_manual = new JRadioButton("Manual");
		follow_manual.setBounds(10, 40, 80, 20);
		memory_inspector.add(follow_manual);
		
		JRadioButton follow_fetches = new JRadioButton("Follow Fetch");
		follow_fetches.setBounds(100, 40, 100, 20);
		memory_inspector.add(follow_fetches);
		
		JRadioButton follow_data = new JRadioButton("Follow Data");
		follow_data.setBounds(210, 40, 100, 20);
		memory_inspector.add(follow_data);
		
		JSpinner memregion_val = new JSpinner();
		memregion_val.setFont(new Font("Monospaced", Font.PLAIN, 12));
		memregion_val.setModel(new SpinnerNumberModel(0, null, 255, 1));
		memregion_val.setBounds(10, 70, 40, 20);
		memory_inspector.add(memregion_val);
		
		JLabel memregion_nam = new JLabel("Selected Memory Region");
		memregion_nam.setBounds(60, 70, 150, 20);
		memory_inspector.add(memregion_nam);
		
		JScrollPane mempage = new JScrollPane();
		mempage.setBounds(10, 100, 300, 180);
		memory_inspector.add(mempage);
		
		JTable table_1 = new JTable(
				new String[][] {
					{"0000", "A455", "9999", "1234", "FEED", "BEEF", "BAAD", "F00D"},
					{"6942", "0491", "6843", "AF18", "FF38", "01FE", "55E9", "0910"},
					{"0000", "A455", "9999", "1234", "FEED", "BEEF", "BAAD", "F00D"},
					{"6942", "0491", "6843", "AF18", "FF38", "01FE", "55E9", "0910"},
					{"0000", "A455", "9999", "1234", "FEED", "BEEF", "BAAD", "F00D"},
					{"6942", "0491", "6843", "AF18", "FF38", "01FE", "55E9", "0910"},
					{"0000", "A455", "9999", "1234", "FEED", "BEEF", "BAAD", "F00D"},
					{"6942", "0491", "6843", "AF18", "FF38", "01FE", "55E9", "0910"},
					{"0000", "A455", "9999", "1234", "FEED", "BEEF", "BAAD", "F00D"},
					{"6942", "0491", "6843", "AF18", "FF38", "01FE", "55E9", "0910"}
				},
				new String[] {"A", "B", "C", "D", "E", "F", "G", "H"});
		table_1.setFont(new Font("Monospaced", Font.PLAIN, 11));
		table_1.setRowSelectionAllowed(false);
		table_1.setFillsViewportHeight(true);
		
		mempage.setViewportView(table_1);
	}
	
	public void finalizeWindow() throws InvocationTargetException, InterruptedException {
		EventQueue.invokeAndWait(() -> {
			main_window.setState(JFrame.ICONIFIED);
			main_window.setVisible(true);
			Dimension pane_size = main_window.getContentPane().getSize();
			Dimension old_size = main_window.getSize();
			Dimension new_size = new Dimension(old_size.width*2-pane_size.width, old_size.height*2-pane_size.height);
			Dimension mon_size = Toolkit.getDefaultToolkit().getScreenSize();
			main_window.setBounds((mon_size.width - new_size.width)/2, (mon_size.height - new_size.height)/2, new_size.width, new_size.height);
			main_window.setState(JFrame.NORMAL);
		});
	}
}

class HexadecimalFilter extends ContentFilteringFocusListener {
	@Override
	protected boolean acceptEdit(String str) {
		return str.matches("^[0-9A-Fa-f]{0,4}$");
	}

	@Override
	protected String finalizeEdit(String str) {
		str = str.toUpperCase();
		for(int i=str.length(); i<4; i++) str = "0" + str;
		return str;
	}
}
