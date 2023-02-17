package martandr.phinixplus.emu.gui;

import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;

public abstract class ContentFilteringFocusListener implements FocusListener {
	private String previous_content;
	private boolean key_listener_added = false;
	
	protected abstract boolean acceptEdit(String str);
	protected abstract String finalizeEdit(String str);
	
	@Override
	public void focusGained(FocusEvent e) {
		if(e.getComponent() instanceof JTextField) {
			JTextField parent = (JTextField) e.getComponent();
			previous_content = parent.getText();
			if(!key_listener_added) {
				addListener(parent);
				key_listener_added = true;
			}
		} else System.err.println("ContentFilteringFocusListener supports only JTextField");
	}
	
	@Override
	public void focusLost(FocusEvent e) {
		if(e.getComponent() instanceof JTextField) {
			JTextField parent = (JTextField) e.getComponent();
			if(!acceptEdit(parent.getText())) parent.setText(previous_content);
			else parent.setText(finalizeEdit(parent.getText()));
		}
	}
	
	private void addListener(JTextField field) {
		field.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					if(!acceptEdit(field.getText())) Toolkit.getDefaultToolkit().beep();
					else {
						previous_content = finalizeEdit(field.getText());
						field.setCaretPosition(0);
					}
				}
			}
		});
	}
}
