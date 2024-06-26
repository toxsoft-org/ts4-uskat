package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.api.hqserv.ESkQueryState.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.hqserv.*;

/**
 * {@link ISkQueryProcessedData} implementation.
 *
 * @author mvk
 */
public final class SkQueryProcessedData
    extends SkAsynchronousQuery
    implements ISkQueryProcessedData {

  private final IStringMapEdit<IDtoQueryParam>               args      = new StringMap<>();
  private final IStringMapEdit<ITimedListEdit<ITemporal<?>>> argsDatas = new StringMap<>();

  /**
   * Constructor
   *
   * @param aService {@link SkCoreServHistQuery} the query service
   * @param aOptions {@link IOptionSet} - optional query execution parameters.
   * @throws TsNullArgumentRtException any argurment = null
   */
  public SkQueryProcessedData( SkCoreServHistQuery aService, IOptionSet aOptions ) {
    super( aService, aOptions );
  }

  // ------------------------------------------------------------------------------------
  // ISkQueryProcessedData
  //
  @Override
  public IStringMap<IDtoQueryParam> listArgs() {
    return args;
  }

  @Override
  public void prepare( IStringMap<IDtoQueryParam> aArgs ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aArgs );
    checkInvalidState( this, EXECUTING, CLOSED );
    args.setAll( aArgs );
    argsDatas.clear();
    backend().prepareQuery( queryId(), args );
    changeState( ESkQueryState.PREPARED );
  }

  @Override
  public boolean isArgDataReady( String aArgDataId ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aArgDataId );
    return argsDatas.hasKey( aArgDataId );
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public <V extends ITemporal<V>> ITimedList<V> getArgData( String aArgDataId ) {
    checkThread();
    Object retValue = argsDatas.findByKey( aArgDataId );
    return (retValue != null ? (ITimedList<V>)retValue : (ITimedList<V>)EMPTY_TIMED_LIST);
  }

  // ------------------------------------------------------------------------------------
  // SkAsynchronousQuery abstract implementation
  //
  @Override
  protected void doNextData( IStringMap<ITimedList<ITemporal<?>>> aValues, ESkQueryState aState ) {
    for( String k : aValues.keys() ) {
      ITimedListEdit<ITemporal<?>> v = argsDatas.findByKey( k );
      ITimedList<ITemporal<?>> nextData = aValues.getByKey( k );
      if( v == null ) {
        v = new TimedList<>( nextData.size() );
        argsDatas.put( k, v );
      }
      v.addAll( nextData );
    }
  }

}
