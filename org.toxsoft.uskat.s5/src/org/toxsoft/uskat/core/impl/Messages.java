package org.toxsoft.uskat.core.impl;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

@SuppressWarnings( "javadoc" )
public class Messages {

  private static final String BUNDLE_NAME = "org.toxsoft.uskat.core.impl.messages"; //$NON-NLS-1$

  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );

  private Messages() {
  }

  public static String getString( String key ) {
    try {
      return RESOURCE_BUNDLE.getString( key );
    }
    catch( @SuppressWarnings( "unused" ) MissingResourceException e ) {
      return '!' + key + '!';
    }
  }
}
