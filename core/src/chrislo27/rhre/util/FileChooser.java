package chrislo27.rhre.util;

import chrislo27.rhre.Main;

import javax.swing.*;
import java.awt.*;

public class FileChooser extends JFileChooser {

	static {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
				UnsupportedLookAndFeelException e) {
			e.printStackTrace();
			Main.logger.warn("Failed to set UIManager look-and-feel");
		}
	}

	@Override
	protected JDialog createDialog(Component parent) throws HeadlessException {
		JDialog dialog = super.createDialog(parent);
//		dialog.setLocationByPlatform(true);
		dialog.setAlwaysOnTop(true);
		return dialog;
	}

}
