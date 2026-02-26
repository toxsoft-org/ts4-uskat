package org.toxsoft.uskat.backend.memtext;

import java.util.*;

/**
 * Constants from <code>IXxxResources</code> to resources in <code>messages_xx_YY.properties</code> dispatcher.
 *
 * @author hazard157
 */
class Messages {

  private static final String BUNDLE_NAME = Messages.class.getName().toLowerCase();

  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );

  private Messages() {
  }

  /**
   * Returns the localized text for the constant from <code>IXxxResources</code>.
   *
   * @param aKey String - the constant from the <code>IXxxResources</code>
   * @return String - localized text
   */
  public static String getString( String aKey ) {
    try {
      return RESOURCE_BUNDLE.getString( aKey );
    }
    catch( @SuppressWarnings( "unused" ) MissingResourceException e ) {
      return '!' + aKey + '!';
    }
  }

}
