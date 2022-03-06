package org.toxsoft.uskat.s5.utils;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.core.tslib.bricks.strid.impl.StridUtils.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.DataDef;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Вспомогательные методы для работы с константами, опциями, параметрами.
 *
 * @author mvk
 */
public interface IS5HardConstants {

  // ------------------------------------------------------------------------------------
  // Всмпомогательные методы
  //
  /**
   * Создать описание опции используя другую опцию как кальку
   *
   * @param aOptionPath String путь (префикc) опции ИД-путь
   * @param aSourceOption {@link IDataDef} опция-источник
   * @return {@link IDataDef} опция с новым значением по умолчанию
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static IDataDef createOption( String aOptionPath, IDataDef aSourceOption ) {
    TsNullArgumentRtException.checkNulls( aOptionPath, aSourceOption );
    IOptionSetEdit params = new OptionSet( aSourceOption.params() );
    String id = makeIdPath( aOptionPath, getLast( aSourceOption.id() ) );
    return DataDef.createOverride1( id, aSourceOption, params );
  }

  /**
   * Создать описание опции используя другую опцию как кальку и заменяя значение по умолчанию
   *
   * @param aOptionPath String путь (префикc) опции ИД-путь
   * @param aSourceOption {@link IDataDef} опция-источник
   * @param aNewDefaultValue {@link IAtomicValue} новое значение по умолчанию
   * @return {@link IDataDef} опция с новым значением по умолчанию
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static IDataDef createOption( String aOptionPath, IDataDef aSourceOption, IAtomicValue aNewDefaultValue ) {
    TsNullArgumentRtException.checkNulls( aOptionPath, aSourceOption, aNewDefaultValue );
    IOptionSetEdit params = new OptionSet( aSourceOption.params() );
    params.setValue( TSID_DEFAULT_VALUE, aNewDefaultValue );
    params.setBool( TSID_IS_MANDATORY, false );
    String id = makeIdPath( aOptionPath, getLast( aSourceOption.id() ) );
    return DataDef.createOverride1( id, aSourceOption, params );
  }

  /**
   * Устанавливает значения опции в именованном наборе
   *
   * @param aOps {@link IOptionSetEdit} именнованный набор данных
   * @param aSourceOption {@link IDataDef} опция-источник
   * @param aDestOption {@link IDataDef} опция-приемник
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static void copyValue( IOptionSetEdit aOps, IDataDef aSourceOption, IDataDef aDestOption ) {
    TsNullArgumentRtException.checkNulls( aOps, aSourceOption, aDestOption );
    aDestOption.setValue( aOps, aSourceOption.getValue( aOps ) );
  }

  /**
   * Устанавливает значения опции в именованном наборе если оно в нем не представлено
   *
   * @param aOps {@link IOptionSetEdit} именнованный набор данных
   * @param aSourceOption {@link IDataDef} опция-источник
   * @param aDestOption {@link IDataDef} опция-приемник
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static void copyValueIfAbsent( IOptionSetEdit aOps, IDataDef aSourceOption, IDataDef aDestOption ) {
    TsNullArgumentRtException.checkNulls( aOps, aSourceOption, aDestOption );
    if( aOps.hasValue( aDestOption ) ) {
      // Значение опции уже представлено в целевом наборе
      return;
    }
    aDestOption.setValue( aOps, aSourceOption.getValue( aOps ) );
  }
}
