package org.toxsoft.uskat.base.gui.km5;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
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
    this( aAttrInfo.id(), ainf2dt( aAttrInfo ) );
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
  }

  static IDataType ainf2dt( IDtoAttrInfo aAttrInfo ) {
    // если у булевого типа не указана форматна строка по умолчанию, зададим отображение галочкой
    IDataType dd = aAttrInfo.dataType();
    IOptionSetEdit p = new OptionSet();
    p.setAll( dd.params() );
    if( dd.atomicType() == EAtomicType.BOOLEAN ) {
      if( !p.hasValue( TSID_FORMAT_STRING ) ) {
        p.setStr( TSID_FORMAT_STRING, FMT_BOOL_CHECK );
      }
    }
    return new DataType( dd.atomicType(), p );
  }

}
