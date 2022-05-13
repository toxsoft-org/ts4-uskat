package org.toxsoft.uskat.legacy.plexy.impl;

/**
 * Локализуемые ресурсы.
 *
 * @author goga
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  /**
   * {@link PlexyValueKeeper}.
   */
  String MSG_ERR_CANT_WRITE_NON_SERIALIZABLE_OBJ_REF =
      Messages.getString( "ISkResources.MSG_ERR_CANT_WRITE_NON_SERIALIZABLE_OBJ_REF" );
  String MSG_ERR_CANT_READ_NON_VALOBJ_REF            =
      Messages.getString( "ISkResources.MSG_ERR_CANT_READ_NON_VALOBJ_REF" );
  String MSG_ERR_CANT_FIND_OBJ_REF_CLASS             =
      Messages.getString( "ISkResources.MSG_ERR_CANT_FIND_OBJ_REF_CLASS" );

}
