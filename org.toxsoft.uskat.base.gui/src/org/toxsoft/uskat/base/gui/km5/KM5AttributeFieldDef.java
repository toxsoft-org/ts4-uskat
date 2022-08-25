package org.toxsoft.uskat.base.gui.km5;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * Класс описания атрибута
 *
 * @author hazard157
 * @param <T> - modelled entity type
 */
public class KM5AttributeFieldDef<T extends ISkObject>
    extends M5AttributeFieldDef<T> {

  /**
   * Конструктор из описания атрибута объекта {@link IDtoAttrInfo}.
   *
   * @param aAttrInfo {@link IDtoAttrInfo} - описание атрибута
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public KM5AttributeFieldDef( IDtoAttrInfo aAttrInfo ) {
    this( aAttrInfo.id(), aAttrInfo.dataType() );
    params().addAll( aAttrInfo.params() );
    setNameAndDescription( aAttrInfo.nmName(), aAttrInfo.description() );
  }

  /**
   * Конструктор для создания полей, не существующих в {@link ISkObject}.
   *
   * @param aId String - идентификатор поля (ИД-путь)
   * @param aAttrType {@link IDataType} - тип данных атрибута
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aId не ИД-путь
   */
  public KM5AttributeFieldDef( String aId, IDataType aAttrType ) {
    super( aId, aAttrType );
    // if no default format string for boolean AV, set it as chek mark
    if( atomicType() == EAtomicType.BOOLEAN ) {
      if( !params().hasValue( TSID_FORMAT_STRING ) ) {
        params().setStr( TSID_FORMAT_STRING, FMT_BOOL_CHECK );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // M5FieldDef
  //

  @Override
  protected IAtomicValue doGetFieldValue( T aEntity ) {
    return aEntity.attrs().getValue( id() );
  }

}
