package org.kmymoney.viewer.actions;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages_CopySplitIDToClipboard
{
	private static final String BUNDLE_NAME = Messages_CopySplitIDToClipboard.class.getName().toLowerCase(); // + ".properties"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );

	private Messages_CopySplitIDToClipboard()
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
