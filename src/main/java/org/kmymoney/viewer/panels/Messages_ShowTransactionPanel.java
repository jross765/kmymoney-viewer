package org.kmymoney.viewer.panels;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages_ShowTransactionPanel
{
	private static final String BUNDLE_NAME = Messages_ShowTransactionPanel.class.getName().toLowerCase(); // + ".properties"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );

	private Messages_ShowTransactionPanel()
	{
	}

	public static String getString(String key)
	{
		try
		{
			return RESOURCE_BUNDLE.getString( key );
		}
		catch ( MissingResourceException e )
		{
			return '!' + key + '!';
		}
	}
}
