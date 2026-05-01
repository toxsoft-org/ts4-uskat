package org.toxsoft.uskat.core.gui.km5.sded2.skobj;

import java.util.*;

/**
 * Constants from <code>IXxxResources</code> to resources in <code>messages_xx_YY.properties</code> dispatcher.
 * <p>
 * This version of <code>Messages</code> class is to be used when localized texts are needed.
 * <p>
 * Note: while {@link Messages_Sk} may always be used instead of this file as a template, using this file is kind of
 * optimization because this class does not creates one more {@link ResourceBundle} instance.
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
