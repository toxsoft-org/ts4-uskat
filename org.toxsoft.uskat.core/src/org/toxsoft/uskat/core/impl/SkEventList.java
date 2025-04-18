package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.evserv.*;

/**
 * {@link ISkEventList} editable implementation.
 *
 * @author hazard157
 * @author mvk
 */
public class SkEventList
    extends TimedList<SkEvent>
    implements ISkEventList {

  private static final long serialVersionUID = 1L;

  /**
   * Registered keeper ID.
   */
  public static final String KEEPER_ID = "SkEvents"; //$NON-NLS-1$

  /**
   * Keeper singleton.
   */
  public static final IEntityKeeper<SkEventList> KEEPER =
      new AbstractEntityKeeper<>( SkEventList.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, SkEventList aEntity ) {
          SkEvent.KEEPER.writeColl( aSw, aEntity, false );
        }

        @Override
        protected SkEventList doRead( IStrioReader aSr ) {
          SkEventList coll = new SkEventList();
          SkEvent.KEEPER.readColl( aSr, coll );
          return coll;
        }
      };

  /**
   * Keeper singleton.
   */
  public static final IEntityKeeper<SkEventList> KEEPER_INDENTED =
      new AbstractEntityKeeper<>( SkEventList.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, SkEventList aEntity ) {
          SkEvent.KEEPER.writeColl( aSw, aEntity, false );
        }

        @Override
        protected SkEventList doRead( IStrioReader aSr ) {
          SkEventList coll = new SkEventList();
          SkEvent.KEEPER.readColl( aSr, coll );
          return coll;
        }
      };

  /**
   * Constructor.
   */
  public SkEventList() {
  }

  /**
   * Constructor.
   * <p>
   * Create a list with a specified capacity.
   *
   * @param aBundleCapacity int - number of elements in bundle
   * @throws TsIllegalArgumentRtException aBundleCapacity is out of range
   */
  public SkEventList( int aBundleCapacity ) {
    super( aBundleCapacity );
  }

  /**
   * Constructor with initialization by array events.
   *
   * @param aElems &lt;SkEvent&gt;[] - specified array
   * @throws TsNullArgumentRtException argument or any it's element = <code>null</code>
   */
  public SkEventList( SkEvent... aElems ) {
    this();
    setAll( aElems );
  }

  // ------------------------------------------------------------------------------------
  // TimedList override
  //

  @Override
  public SkEventList selectInterval( ITimeInterval aTimeInterval ) {
    TsNullArgumentRtException.checkNull( aTimeInterval );
    SkEventList ll = new SkEventList();
    if( isEmpty() ) {
      return ll;
    }
    int index1 = findFirstOrAfter( aTimeInterval.startTime() );
    int index2 = findLastOrBefore( aTimeInterval.endTime() );
    for( int i = index1; i <= index2; i++ ) {
      ll.add( get( i ) );
    }
    return ll;
  }

  @Override
  public SkEventList selectExtendedInterval( ITimeInterval aTimeInterval ) {
    TsNullArgumentRtException.checkNull( aTimeInterval );
    SkEventList ll = new SkEventList();
    if( isEmpty() ) {
      return ll;
    }
    int index1 = findFirstOrBefore( aTimeInterval.startTime() );
    int index2 = findLastOrAfter( aTimeInterval.endTime() );
    for( int i = index1; i <= index2; i++ ) {
      ll.add( get( i ) );
    }
    return ll;
  }

  @Override
  public SkEventList selectAfter( long aTimestamp ) {
    SkEventList ll = new SkEventList();
    if( isEmpty() ) {
      return ll;
    }
    int index = findFirstOrAfter( aTimestamp );
    for( int i = index, count = size(); i < count; i++ ) {
      ll.add( get( i ) );
    }
    return ll;
  }

  @Override
  public SkEventList selectBefore( long aTimestamp ) {
    SkEventList ll = new SkEventList();
    if( isEmpty() ) {
      return ll;
    }
    int index = findLastOrBefore( aTimestamp );
    for( int i = 0; i <= index; i++ ) {
      ll.add( get( i ) );
    }
    return ll;
  }

}
