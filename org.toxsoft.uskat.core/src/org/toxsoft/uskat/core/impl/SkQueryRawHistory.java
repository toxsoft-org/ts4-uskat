package org.toxsoft.uskat.core.impl;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.core.api.hqserv.ESkQueryState.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.coll.IMapEdit;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.hqserv.*;
import org.toxsoft.uskat.core.impl.dto.DtoQueryParam;

/**
 * {@link ISkQueryProcessedData} implementation.
 *
 * @author mvk
 */
public final class SkQueryRawHistory
    extends SkAsynchronousQuery
    implements ISkQueryRawHistory {

  private final IMapEdit<Gwid, IDtoQueryParam>               args      = new ElemMap<>();
  private final IMapEdit<Gwid, ITimedListEdit<ITemporal<?>>> argsDatas = new ElemMap<>();

  /**
   * Constructor
   *
   * @param aService {@link SkCoreServHistQueryService} the query service
   * @param aOptions {@link IOptionSet} - optional query execution parameters.
   * @throws TsNullArgumentRtException any argurment = null
   */
  public SkQueryRawHistory( SkCoreServHistQueryService aService, IOptionSet aOptions ) {
    super( aService, aOptions );
  }

  // ------------------------------------------------------------------------------------
  // ISkQueryRawHistory
  //
  @Override
  public IGwidList listGwids() {
    return new GwidList( args.keys() );
  }

  @Override
  public IGwidList prepare( IGwidList aGwids ) {
    TsNullArgumentRtException.checkNull( aGwids );
    checkInvalidState( this, EXECUTING, CLOSED );
    args.clear();
    argsDatas.clear();
    IStringMapEdit<IDtoQueryParam> params = new StringMap<>();
    for( Gwid gwid : aGwids ) {
      if( gwid.kind() != EGwidKind.GW_RTDATA && //
          gwid.kind() != EGwidKind.GW_EVENT && //
          gwid.kind() != EGwidKind.GW_CMD ) {
        continue;
      }
      for( Gwid concreteGwid : expandGwid( gwid ) ) {
        String funcId = EMPTY_STRING;
        IOptionSet funcArgs = IOptionSet.NULL;
        IDtoQueryParam param = DtoQueryParam.create( concreteGwid, ITsCombiFilterParams.ALL, funcId, funcArgs );
        args.put( concreteGwid, param );
        params.put( concreteGwid.asString(), param );
      }
    }
    backend().prepareQuery( queryId(), params );
    changeState( ESkQueryState.PREPARED );
    return listGwids();
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public <V extends ITemporal<V>> ITimedList<V> get( Gwid aGwid ) {
    TsNullArgumentRtException.checkNull( aGwid );
    if( state() != READY ) {
      return (ITimedList<V>)EMPTY_TIMED_LIST;
    }
    Object retValue = argsDatas.findByKey( aGwid );
    return (retValue != null ? (ITimedList<V>)retValue : (ITimedList<V>)EMPTY_TIMED_LIST);
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public <T extends ITemporal<T>> IMap<Gwid, ITimedList<T>> getAll() {
    if( state() != READY ) {
      return IMap.EMPTY;
    }
    return (IMap<Gwid, ITimedList<T>>)(Object)argsDatas;
  }

  // ------------------------------------------------------------------------------------
  // SkAsynchronousQuery abstract implementation
  //
  @Override
  protected void doNextData( IStringMap<ITimedList<ITemporal<?>>> aValues, ESkQueryState aState ) {
    for( String k : aValues.keys() ) {
      Gwid gwid = Gwid.of( k );
      ITimedListEdit<ITemporal<?>> v = argsDatas.findByKey( gwid );
      if( v == null ) {
        v = new TimedList<>();
        argsDatas.put( gwid, v );
      }
      v.addAll( aValues.getByKey( k ) );
    }
  }

}