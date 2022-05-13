package org.toxsoft.uskat.legacy.file.dirwalk;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

@SuppressWarnings( { "javadoc", "unused", "nls" } )
public class Messages {

  private static final String BUNDLE_NAME = "org.toxsoft.uskat.legacy.file.dirwalk";

  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );

  private Messages() {
  }

  public static String getString( String key ) {
    try {
      return RESOURCE_BUNDLE.getString( key );
    }
    catch( MissingResourceException e ) {
      return '!' + key + '!';
    }
  }
}
