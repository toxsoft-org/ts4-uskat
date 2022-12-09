package org.toxsoft.uskat.ggprefs.lib.impl;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetUtils;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strid.impl.AbstractStridableParameterizedKeeper;
import org.toxsoft.core.tslib.bricks.strid.impl.StridableParameterizedSer;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.ggprefs.lib.IDpuGuiGwPrefsSectionDef;

/**
 * Реализация {@link IDpuGuiGwPrefsSectionDef}.
 *
 * @author goga
 */
public class DpuGuiGwPrefsSectionDef
    extends StridableParameterizedSer
    implements IDpuGuiGwPrefsSectionDef {

  private static final long serialVersionUID = -3226307957164490084L;

  /**
   * Keeper singleton.
   */
  @SuppressWarnings( "hiding" )
  public static final IEntityKeeper<IDpuGuiGwPrefsSectionDef> KEEPER =
      new AbstractStridableParameterizedKeeper<>( IDpuGuiGwPrefsSectionDef.class, null ) {

        @Override
        protected IDpuGuiGwPrefsSectionDef doCreate( String aId, IOptionSet aParams ) {
          return new DpuGuiGwPrefsSectionDef( aId, aParams );
        }
      };

  /**
   * Конструктор.
   *
   * @param aId String - идентификатор (ИД-путь) типа
   * @param aParams {@link IOptionSet} - начальные значения {@link #params()}
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsIllegalArgumentRtException идентификатор не ИД-путь
   */
  public DpuGuiGwPrefsSectionDef( String aId, IOptionSet aParams ) {
    super( aId, aParams );
  }

  /**
   * Конструктор.
   *
   * @param aId String - идентификатор (ИД-путь) типа
   * @param aName String - название
   * @param aDescription String - описание
   * @param aParams {@link IOptionSet} - начальные значения {@link #params()}
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsIllegalArgumentRtException идентификатор не ИД-путь
   */
  public DpuGuiGwPrefsSectionDef( String aId, String aName, String aDescription, IOptionSet aParams ) {
    super( aId, aParams );
    params().setStr( TSID_NAME, aName );
    params().setStr( TSID_DESCRIPTION, aDescription );
  }

  /**
   * Статический конструктор.
   *
   * @param aId String - идентификатор
   * @param aName String - название
   * @param aDescription String - описание
   * @param aIdsAndValues Object[] - identifier / value pairs as for {@link OptionSetUtils#createOpSet(Object...)}
   * @return {@link DpuGuiGwPrefsSectionDef} - созданный экземпляр
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsIllegalArgumentRtException идентификатор не ИД-путь
   * @throws TsIllegalArgumentRtException number of elements in array is uneven
   * @throws ClassCastException argument types convention is violated
   */
  public static DpuGuiGwPrefsSectionDef create1( String aId, String aName, String aDescription,
      Object... aIdsAndValues ) {
    TsNullArgumentRtException.checkNulls( aId, aName, aDescription );
    IOptionSetEdit params = OptionSetUtils.createOpSet( aIdsAndValues );
    params.setStr( TSID_NAME, aName );
    params.setStr( TSID_DEFAULT_VALUE, aDescription );
    return new DpuGuiGwPrefsSectionDef( aId, params );
  }

  /**
   * Статический конструктор.
   *
   * @param aId String - идентификатор
   * @param aIdsAndValues Object[] - identifier / value pairs as for {@link OptionSetUtils#createOpSet(Object...)}
   * @return {@link DpuGuiGwPrefsSectionDef} - созданный экземпляр
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsIllegalArgumentRtException идентификатор не ИД-путь
   * @throws TsIllegalArgumentRtException number of elements in array is uneven
   * @throws ClassCastException argument types convention is violated
   */
  public static DpuGuiGwPrefsSectionDef create2( String aId, Object... aIdsAndValues ) {
    TsNullArgumentRtException.checkNull( aId );
    IOptionSet params = OptionSetUtils.createOpSet( aIdsAndValues );
    return new DpuGuiGwPrefsSectionDef( aId, params );
  }

}
