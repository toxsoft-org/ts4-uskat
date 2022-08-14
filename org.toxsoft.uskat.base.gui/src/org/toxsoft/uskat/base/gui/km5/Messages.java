package org.toxsoft.uskat.base.gui.km5;

import org.eclipse.osgi.util.*;

@SuppressWarnings( "javadoc" )
public class Messages
    extends NLS {

  private static final String BUNDLE_NAME = "ru.uskat.mws.lib.core.km5.messages"; //$NON-NLS-1$

  public static String FMT_ERR_CANT_EDIT_CLASS_ID;
  public static String FMT_ERR_CANT_EDIT_STRID;
  public static String FMT_ERR_DUP_CLASS_STRID;
  public static String FMT_ERR_INV_OBJ_STRID;
  public static String FMT_ERR_NO_CLASS_ID;
  public static String FMT_ERR_NO_OBJ_SKID;
  public static String STR_D_FDEF_CLASS_ID;
  public static String STR_D_FDEF_DESCRIPTION;
  public static String STR_D_FDEF_NAME;
  public static String STR_D_FDEF_SKID;
  public static String STR_D_FDEF_STRID;
  public static String STR_D_KM5M_OBJECT;
  public static String STR_N_FDEF_CLASS_ID;
  public static String STR_N_FDEF_DESCRIPTION;
  public static String STR_N_FDEF_NAME;
  public static String STR_N_FDEF_SKID;
  public static String STR_N_FDEF_STRID;
  public static String STR_N_KM5M_OBJECT;

  static {
    // initialize resource bundle
    NLS.initializeMessages( BUNDLE_NAME, Messages.class );
  }

  private Messages() {
    // nop
  }

}
