package chrislo27.rhre.util

import java.awt.Component
import javax.swing.JDialog
import javax.swing.JFileChooser
import javax.swing.UIManager

open class FileChooser : JFileChooser() {

	override fun createDialog(parent: Component?): JDialog {
		val dialog = super.createDialog(parent)
//		dialog.setLocationByPlatform(true)
		dialog.isAlwaysOnTop = true
		return dialog
	}

	companion object {

		init {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
			} catch (e: Exception) {
				e.printStackTrace()
				ionium.templates.Main.logger.warn("Failed to set UIManager look-and-feel")
			}

		}
	}

}
