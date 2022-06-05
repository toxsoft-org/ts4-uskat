package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.uskat.core.api.evserv.*;

/**
 * {@link ISkEventList} editable implementation.
 *
 * @author hazard157
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
    super();
  }

}
