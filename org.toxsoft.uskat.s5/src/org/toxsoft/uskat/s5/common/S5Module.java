package org.toxsoft.uskat.s5.common;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.common.IS5Resources.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.impl.DataDef;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetUtils;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strid.impl.AbstractStridableParameterizedKeeper;
import org.toxsoft.core.tslib.bricks.strid.impl.StridableParameterizedSer;
import org.toxsoft.core.tslib.utils.TsVersion;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;

/**
 * Информация о модуле системы
 *
 * @author mvk
 */
public final class S5Module
    extends StridableParameterizedSer {

  private static final long serialVersionUID = 157157L;

  /**
   * Версия модуля
   * <p>
   * Тип: {@link EAtomicType#VALOBJ} содержит {@link TsVersion}
   */
  public static final IDataDef DDEF_VERSION = DataDef.create( "Version", EAtomicType.VALOBJ, //$NON-NLS-1$
      TSID_NAME, STR_N_VERSION, //
      TSID_DESCRIPTION, STR_D_VERSION, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( new TsVersion( 0, 0 ) ) );

  /**
   * Список модулей от который зависит целевой модуль
   * <p>
   * Тип: {@link EAtomicType#VALOBJ} содержит {@link TsVersion}
   */
  public static final IDataDef DDEF_DEPENDS = DataDef.create( "Depends", EAtomicType.VALOBJ, //$NON-NLS-1$
      TSID_NAME, STR_N_DEPENDS, //
      TSID_DESCRIPTION, STR_D_DEPENDS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( new S5ModuleList() ) );

  /**
   * Value-object registration identifier for {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "Module"; //$NON-NLS-1$

  /**
   * Keeper singleton.
   */
  @SuppressWarnings( "hiding" )
  public static final IEntityKeeper<S5Module> KEEPER =
      new AbstractStridableParameterizedKeeper<>( S5Module.class, null ) {

        @Override
        protected S5Module doCreate( String aId, IOptionSet aParams ) {
          return new S5Module( aId, aParams );
        }
      };

  /**
   * Text format {@link S5Module}
   */
  private static final String TO_STRING_FORMAT = "%s:%s"; //$NON-NLS-1$

  /**
   * Конструктор
   *
   * @param aId идентификатор модуля.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5Module( String aId ) {
    super( aId );
  }

  /**
   * Констрктор
   *
   * @param aId String идентификатор модуля.
   * @param aName String имя модуля
   * @param aDescription String описание модуля
   * @param aVersion {@link TsVersion} версия модуля
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5Module( String aId, String aName, String aDescription, TsVersion aVersion ) {
    this( aId, OptionSetUtils.createOpSet( //
        TSID_NAME, aName, //
        TSID_DESCRIPTION, aDescription, //
        S5Module.DDEF_VERSION, aVersion //
    ) );
  }

  /**
   * Конструктор
   *
   * @param aId идентификатор модуля.
   * @param aParams {@link IOptionSet} параметры для инициализации
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5Module( String aId, IOptionSet aParams ) {
    super( aId, aParams );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //

  /**
   * Возвращает версию целевого модуля
   *
   * @return {@link TsVersion} версия модуля
   */
  public TsVersion version() {
    return DDEF_VERSION.getValue( params() ).asValobj();
  }

  /**
   * Возвращает cписок модулей от который зависит целевой модуль
   *
   * @return {@link S5ModuleList} список модулей
   */
  public S5ModuleList depends() {
    return DDEF_DEPENDS.getValue( params() ).asValobj();
  }

  // ------------------------------------------------------------------------------------
  // Реализация Object
  //
  @Override
  public String toString() {
    return String.format( TO_STRING_FORMAT, DDEF_NAME.getValue( this.params() ),
        DDEF_VERSION.getValue( this.params() ) );
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals( Object aObject ) {
    return super.equals( aObject );
  }

  // ------------------------------------------------------------------------------------
  // Вспомогательные методы
  //
}
