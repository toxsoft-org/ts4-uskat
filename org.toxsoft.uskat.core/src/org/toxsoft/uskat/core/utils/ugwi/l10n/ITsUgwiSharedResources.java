package org.toxsoft.uskat.core.utils.ugwi.l10n;

import org.toxsoft.uskat.core.utils.ugwi.*;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
public interface ITsUgwiSharedResources {

  // common
  String MSG_ERR_UGWI_NAMESPACE_NOT_IDPATH = Messages.getString( "MSG_ERR_UGWI_NAMESPACE_NOT_IDPATH" ); //$NON-NLS-1$

  /**
   * {@link Ugwi}
   */
  String MSG_ERR_UGWI_KIND_NOT_IDPATH     = Messages.getString( "MSG_ERR_UGWI_KIND_NOT_IDPATH" );     //$NON-NLS-1$
  String MSG_ERR_INV_UGWI_NO_COLON        = Messages.getString( "MSG_ERR_INV_UGWI_NO_COLON" );        //$NON-NLS-1$
  String MSG_ERR_INV_UGWI_NO_FIRST_SLASH  = Messages.getString( "MSG_ERR_INV_UGWI_NO_FIRST_SLASH" );  //$NON-NLS-1$
  String MSG_ERR_INV_UGWI_NO_SECOND_SLASH = Messages.getString( "MSG_ERR_INV_UGWI_NO_SECOND_SLASH" ); //$NON-NLS-1$
  String MSG_ERR_INV_UGWI_UNEXPECTED_EOL  = Messages.getString( "MSG_ERR_INV_UGWI_UNEXPECTED_EOL" );  //$NON-NLS-1$

  /**
   * {@link UgwiKind}
   */
  String FMT_ERR_UWGI_NOT_OF_THIS_KIND = Messages.getString( "FMT_ERR_UWGI_NOT_OF_THIS_KIND" ); //$NON-NLS-1$

  /**
   * {@link UgwiKindNone}
   */
  String STR_UK_NONE   = Messages.getString( "STR_UK_NONE" );   //$NON-NLS-1$
  String STR_UK_NONE_D = Messages.getString( "STR_UK_NONE_D" ); //$NON-NLS-1$

}
