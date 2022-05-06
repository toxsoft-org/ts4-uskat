package org.toxsoft.uskat.sysext.realtime.supports.histdata.sequences.sync;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

@SuppressWarnings( { "unused" } )
class Messages {

  private static final String BUNDLE_NAME =
      "org.toxsoft.uskat.sysext.realtime.supports.histdata.sequences.sync.messages"; //$NON-NLS-1$

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
