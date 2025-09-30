package org.kmymoney.jkmymoney;

import java.io.File;

import org.java.plugin.boot.Application;
import org.java.plugin.boot.ApplicationPlugin;
import org.java.plugin.util.ExtendedProperties;

/**
 * Entry-Point for the JPF-Library we are using to support
 * plugins..
 */
public class JPFEditorMain extends ApplicationPlugin {

	/* (non-Javadoc)
	 * @see org.java.plugin.boot.ApplicationPlugin#initApplication(org.java.plugin.util.ExtendedProperties, java.lang.String[])
	 */
	@Override
	protected Application initApplication(final ExtendedProperties arg0, final String[] args) {
		JKMyMoney ste = new JKMyMoney(getManager(), getDescriptor());
		ste.setVisible(true);
		if (args.length > 0) {
			ste.loadFile(new File(args[0]));
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.java.plugin.Plugin#doStart()
	 */
	@Override
	protected void doStart() throws Exception {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see org.java.plugin.Plugin#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		// do nothing
	}

}
