package org.kmymoney.viewer.widgets;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalToolTipUI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Usage:
 * <pre>
 * public JToolTip createToolTip() {
 *					MultiLineToolTip tip = new MultiLineToolTip();
 *					tip.setComponent(this);
 *					return tip;
 *				}
 *	</pre>
 */
public class MultiLineToolTip extends JToolTip {
	public MultiLineToolTip() {
		setUI(new MultiLineToolTipUI());
	}


	private static class MultiLineToolTipUI extends MetalToolTipUI {
		/**
		 * Automatically created logger for debug and error-output.
		 */
		private final Logger LOGGER = LoggerFactory.getLogger(MultiLineToolTipUI.class);

		private String[] myStrings;

		private int maxWidth = 0;

		public void paint(final Graphics g, final JComponent c) {
		 // Font.getStringBounds() cannot be used here because we have
			FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(g.getFont());
			Dimension size = c.getSize();
			g.setColor(c.getBackground());
			g.fillRect(0, 0, size.width, size.height);
			g.setColor(c.getForeground());

			if (myStrings != null) {
				for (int i = 0; i < myStrings.length; i++) {
					String str = myStrings[i];
					if (str == null) {
						str = "";
					}
					g.drawString(str, 3, (metrics.getHeight()) * (i + 1));
				}
			}
		}

		public Dimension getPreferredSize(final JComponent c) {
		 // Font.getStringBounds() cannot be used here because we have no Graphics2D-object
			FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(c.getFont());
			String tipText = ((JToolTip) c).getTipText();
			if (tipText == null) {
				tipText = "";
			}

			// read Tooltip-Text
			BufferedReader br = new BufferedReader(new StringReader(tipText));
			String line;
			int maxWidth = 0;
			List<String> lines = new LinkedList<String>();
			try {
				while ((line = br.readLine()) != null) {
					int width = SwingUtilities.computeStringWidth(metrics, line);
					maxWidth = (maxWidth < width) ? width : maxWidth;
					if (line != null) {
						lines.add(line);
					}
				}
			} catch (IOException ex) {
				LOGGER.error("Cannot read Tooltip-Text", ex);
			}

			myStrings = lines.toArray(new String[lines.size()]);

			int height = metrics.getHeight() * lines.size();
			this.maxWidth = maxWidth;
			return new Dimension(maxWidth + 6, height + 4);
		}
	}
}