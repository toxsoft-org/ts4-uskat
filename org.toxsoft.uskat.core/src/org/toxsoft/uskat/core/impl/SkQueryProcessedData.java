package org.toxsoft.uskat.core.impl;

import static org.toxsoft.core.tslib.bricks.strid.idgen.UuidStridGenerator.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.strid.idgen.IStridGenerator;
import org.toxsoft.core.tslib.bricks.strid.idgen.UuidStridGenerator;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.hqserv.IDtoQueryParam;
import org.toxsoft.uskat.core.api.hqserv.ISkQueryProcessedData;

/**
 * {@link ISkQueryProcessedData} implementation.
 *
 * @author mvk
 */
public final class SkQueryProcessedData
    extends SkAsynchronousQuery
    implements ISkQueryProcessedData {

  private static final String          QUERY_ID_PREFIX = "ProcessedDataQuery";                                    //$NON-NLS-1$
  private static final IStridGenerator uuidGenerator   = new UuidStridGenerator( createState( QUERY_ID_PREFIX ) );

  private final IStringMapEdit<IDtoQueryParam>                       args      = new StringMap<>();
  private final IStringMapEdit<ITimedListEdit<ITemporalAtomicValue>> argsDatas = new StringMap<>();

  /**
   * Constructor
   *
   * @param aService {@link SkCoreServHistQueryService} the query service
   * @param aOptions {@link IOptionSet} - optional query execution parameters.
   * @throws TsNullArgumentRtException any argurment = null
   */
  public SkQueryProcessedData( SkCoreServHistQueryService aService, IOptionSet aOptions ) {
    super( aService, uuidGenerator.nextId(), aOptions );
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
    TsNullArgumentRtException.checkNull( aArgs );
    args.setAll( aArgs );
    backend().prepareQuery( queryId(), args );
  }

  @Override
  public boolean isArgDataReady( String aArgDataId ) {
    TsNullArgumentRtException.checkNull( aArgDataId );
    return argsDatas.hasKey( aArgDataId );
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public <V extends ITemporal<V>> ITimedList<V> getArgData( String aArgDataId ) {
    return (ITimedList<V>)argsDatas.getByKey( aArgDataId );
  }

  // ------------------------------------------------------------------------------------
  // SkAsynchronousQuery abstract implementation
  //
  @Override
  protected void doNextData( IStringMap<ITimedList<ITemporalAtomicValue>> aValues, boolean aFinished ) {
    for( String k : aValues.keys() ) {
      ITimedListEdit<ITemporalAtomicValue> v = argsDatas.findByKey( k );
      if( v == null ) {
        v = new TimedList<>();
        argsDatas.put( k, v );
      }
      v.addAll( aValues.getByKey( k ) );
    }
  }

}
