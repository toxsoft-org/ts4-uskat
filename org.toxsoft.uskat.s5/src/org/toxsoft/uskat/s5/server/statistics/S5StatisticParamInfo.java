package org.toxsoft.uskat.s5.server.statistics;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.server.statistics.IS5StatisticHardConstants.*;

import java.io.Serializable;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.errors.AvTypeCastRtException;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetUtils;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.impl.StridableParameterized;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;

/**
 * Описание параметра статистики
 *
 * @author mvk
 */
public final class S5StatisticParamInfo
    extends StridableParameterized
    implements Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "S5StatisticParamInfo"; //$NON-NLS-1$

  /**
   * Экземпляр-синглтон хранителя.
   */
  @SuppressWarnings( "hiding" )
  public static final IEntityKeeper<S5StatisticParamInfo> KEEPER =
      new AbstractEntityKeeper<>( S5StatisticParamInfo.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, S5StatisticParamInfo aEntity ) {
          StridableParameterized.KEEPER.write( aSw, aEntity );
        }

        @Override
        protected S5StatisticParamInfo doRead( IStrioReader aSr ) {
          StridableParameterized sp = StridableParameterized.KEEPER.read( aSr );
          return new S5StatisticParamInfo( sp.id(), sp.params() );
        }
      };

  /**
   * Constructor for atomic option info.
   *
   * @param aId String идентификатор параметра
   * @param aParams {@link IOptionSet} опции параметра
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   */
  protected S5StatisticParamInfo( String aId, IOptionSet aParams ) {
    super( aId, aParams );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Атомарный тип значений параметра
   *
   * @return {@link EAtomicType} атомарный тип значений
   */
  public EAtomicType atomicType() {
    return params().getValobj( S5ID_STATISTIC_TYPE );
  }

  /**
   * Статистическая функция
   *
   * @return {@link EStatisticFunc} статистическая функция
   */
  public EStatisticFunc func() {
    return params().getValobj( S5ID_STATISTIC_FUNC );
  }

  /**
   * Интервалы по которым требуется формирование значений параметра
   *
   * @return {@link IStridablesList}&lt; {@link IS5StatisticInterval}&gt; интервалы обработки статистики
   */
  public IStridablesList<IS5StatisticInterval> intervals() {
    return params().getValobj( S5ID_STATISTIC_INTERVALS );
  }

  /**
   * Constructs the plain atomic option info.
   *
   * @param aId String an identifier (IDpath)
   * @param aStatisticFunc {@link EStatisticFunc} statistic function
   * @param aAtomicType {@link EAtomicType} - atomic type
   * @param aIntervals {@link IStridablesList}&lt;{@link IS5StatisticInterval}&gt; список интервалов агрегации
   * @param aDefaultValue {@link IAtomicValue} - default value
   * @param aIdsAndValues {@link IOptionSet} - values
   * @return {@link S5StatisticParamInfo} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   * @throws AvTypeCastRtException default value is ioncomaptible to the specified atomic type
   */
  public static S5StatisticParamInfo create( String aId, //
      EStatisticFunc aStatisticFunc, IStridablesList<IS5StatisticInterval> aIntervals, //
      EAtomicType aAtomicType, IAtomicValue aDefaultValue, Object... aIdsAndValues ) {
    TsNullArgumentRtException.checkNulls( aId, aStatisticFunc, aAtomicType, aDefaultValue );
    AvTypeCastRtException.canAssign( aAtomicType, aDefaultValue.atomicType() );
    S5StatisticParamInfo opin = new S5StatisticParamInfo( aId, IOptionSet.NULL );
    opin.params().setBool( TSID_IS_MANDATORY, false );
    opin.params().addAll( OptionSetUtils.createOpSet( aIdsAndValues ) );
    opin.params().setValue( TSID_DEFAULT_VALUE, aDefaultValue );
    opin.params().setValobj( S5ID_STATISTIC_TYPE, aAtomicType );
    opin.params().setValobj( S5ID_STATISTIC_FUNC, aStatisticFunc );
    opin.params().setValobj( S5ID_STATISTIC_INTERVALS, new S5StatisticIntervalList( aIntervals ) );
    return opin;
  }
}
