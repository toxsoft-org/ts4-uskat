package org.toxsoft.uskat.ws.conn.mws.l10n;

import java.util.*;

/**
 * Constants from <code>IXxxResources</code> to resources in <code>messages_xx_YY.properties</code> dispatcher.
 * <p>
 * This version of <code>Messages</code> class is to be used when English versions if the localizable string constants
 * has to be used. The method {@link #getString(String)} returns localized text, while {@link #getEngStr(String)}
 * returns English text of the constant.
 * <p>
 * Notes:
 * <ul>
 * <li>as for all toxsoft.org sources codes this class assumes that English translations are placed in
 * <code>messages.properties</code> as a fallback texts;</li>
 * <li>it is <b>important</b> to rename this file to <code>Messages</code> after copy-pasting from the templates
 * project.</li>
 * </ul>
 *
 * @author hazard157
 */
class Messages {

  private static final String BUNDLE_NAME = Messages.class.getName().toLowerCase();

  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );

  private static final ResourceBundle RESOURCE_BUNDLE_ENGLISH = ResourceBundle.getBundle( BUNDLE_NAME, Locale.ENGLISH );

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

  /**
   * Returns the English text for the constant from <code>IXxxResources</code>.
   * <p>
   * Returned text is the same as for locale <code>en_EN</code>.
   *
   * @param aKey String - the constant from the <code>IXxxResources</code>
   * @return String - text in English language
   */
  public static String getEngStr( String aKey ) {
    try {
      return RESOURCE_BUNDLE_ENGLISH.getString( aKey );
    }
    catch( @SuppressWarnings( "unused" ) MissingResourceException e ) {
      return '!' + aKey + '!';
    }
  }

}
