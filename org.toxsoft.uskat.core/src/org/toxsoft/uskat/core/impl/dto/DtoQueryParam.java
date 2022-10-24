package org.toxsoft.uskat.core.impl.dto;

import java.io.Serializable;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.filter.impl.TsCombiFilterParamsKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.hqserv.IDtoQueryParam;
import org.toxsoft.uskat.core.api.hqserv.ISkQueryProcessedData;

/**
 * {@link IDtoQueryParam} implementation.
 *
 * @author mvk
 */
public final class DtoQueryParam
    implements IDtoQueryParam, Serializable {

  private static final long serialVersionUID = -2951742499712676666L;

  /**
   * Keeper identifier.
   */
  public static final String KEEPER_ID = "QueryParam"; //$NON-NLS-1$

  /**
   * Keeper singleton.
   */
  public static final IEntityKeeper<IDtoQueryParam> KEEPER =
      new AbstractEntityKeeper<>( IDtoQueryParam.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, IDtoQueryParam aEntity ) {
          Gwid.KEEPER.write( aSw, aEntity.dataGwid() );
          TsCombiFilterParamsKeeper.KEEPER.write( aSw, aEntity.filterParams() );
          aSw.writeQuotedString( aEntity.funcId() );
          OptionSetKeeper.KEEPER.write( aSw, aEntity.funcArgs() );
        }

        @Override
        protected IDtoQueryParam doRead( IStrioReader aSr ) {
          Gwid dataGwid = Gwid.KEEPER.read( aSr );
          ITsCombiFilterParams filter = TsCombiFilterParamsKeeper.KEEPER.read( aSr );
          String funcId = aSr.readQuotedString();
          IOptionSet funcArgs = OptionSetKeeper.KEEPER.read( aSr );
          return create( dataGwid, filter, funcId, funcArgs );
        }
      };

  private final Gwid                 dataGwid;
  private final ITsCombiFilterParams filter;
  private final String               funcId;
  private final IOptionSet           funcArgs;

  /**
   * Constructor.
   *
   * @param aDataGwid {@link Gwid} GWID of data to be queried
   * @param aFilter {@link ITsCombiFilterParams} parameters of the filter to by used for values filtering
   * @param aFuncId String the ID of function to be used for data processing.
   * @param aFuncArgs {@link IOptionSet} arguments of function {@link #funcId()}.
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  private DtoQueryParam( Gwid aDataGwid, ITsCombiFilterParams aFilter, String aFuncId, IOptionSet aFuncArgs ) {
    TsNullArgumentRtException.checkNulls( aDataGwid, aFilter, aFuncId, aFuncArgs );
    dataGwid = aDataGwid;
    filter = aFilter;
    funcId = aFuncId;
    funcArgs = aFuncArgs;
  }

  /**
   * Static constructor.
   *
   * @param aDataGwid {@link Gwid} GWID of data to be queried
   * @param aFilter {@link ITsCombiFilterParams} parameters of the filter to by used for values filtering
   * @param aFuncId String the ID of function to be used for data processing.
   * @param aFuncArgs {@link IOptionSet} arguments of function {@link #funcId()}.
   * @return {@link DtoQueryParam} single data argument to be queried by {@link ISkQueryProcessedData}
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static DtoQueryParam create( Gwid aDataGwid, ITsCombiFilterParams aFilter, String aFuncId,
      IOptionSet aFuncArgs ) {
    TsNullArgumentRtException.checkNulls( aDataGwid, aFilter, aFuncId, aFuncArgs );
    return new DtoQueryParam( aDataGwid, aFilter, aFuncId, aFuncArgs );
  }

  // ------------------------------------------------------------------------------------
  // IDtoQueryParam
  //
  @Override
  public Gwid dataGwid() {
    return dataGwid;
  }

  @Override
  public ITsCombiFilterParams filterParams() {
    return filter;
  }

  @Override
  public String funcId() {
    return funcId;
  }

  @Override
  public IOptionSet funcArgs() {
    return funcArgs;
  }
}
