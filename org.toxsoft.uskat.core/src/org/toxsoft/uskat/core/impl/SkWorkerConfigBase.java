package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.valobj.*;
import org.toxsoft.uskat.core.devapi.*;

/**
 * {@link ISkWorkerConfig} base implementation.
 *
 * @author mvk
 */
public class SkWorkerConfigBase
    extends Stridable
    implements ISkWorkerConfig {

  /**
   * Value-object registration identifier for {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = SkWorkerConfigBase.class.getSimpleName();

  /**
   * Keeper singleton.
   */
  public static final IEntityKeeper<ISkWorkerConfig> KEEPER =
      new AbstractEntityKeeper<>( ISkWorkerConfig.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, ISkWorkerConfig aEntity ) {
          aSw.writeAsIs( aEntity.id() );
          // aSw.writeSeparatorChar();
          // SkidListKeeper.KEEPER.write( aSw, aEntity.trackchainIds() );
        }

        @Override
        protected ISkWorkerConfig doRead( IStrioReader aSr ) {
          SkWorkerConfigBase retValue = new SkWorkerConfigBase( aSr.readIdPath() );
          // aSr.ensureSeparatorChar();
          // retValue.setTrackchainIds( SkidListKeeper.KEEPER.read( aSr ) );

          return retValue;
        }

      };

  /**
   * Формат текстового представления {@link SkWorkerConfigBase}
   */
  // private static final String TO_STRING_FORMAT = "%s, [%s]"; //$NON-NLS-1$
  private static final String TO_STRING_FORMAT = "%s"; //$NON-NLS-1$

  // private ISkidList trackchainIds;

  /**
   * Конструктор
   *
   * @param aId String идентификатор шлюза (ИД-путь)
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aId не ИД-путь или не ИД-имя
   */
  public SkWorkerConfigBase( String aId ) {
    super( aId );
  }

  // ------------------------------------------------------------------------------------
  // public API
  //

  // ------------------------------------------------------------------------------------
  // ISkWorkerConfig

  // ------------------------------------------------------------------------------------
  // Object
  //
  @Override
  public String toString() {
    return String.format( TO_STRING_FORMAT, id() );
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    // result = TsLibUtils.PRIME * result + trackchainIds.hashCode();
    return result;
  }

  @Override
  public boolean equals( Object aObject ) {
    if( this == aObject ) {
      return true;
    }
    if( aObject == null ) {
      return false;
    }
    if( getClass() != aObject.getClass() ) {
      return false;
    }
    if( !super.equals( aObject ) ) {
      return false;
    }
    // INmRouteWorkerConfig other = (INmRouteWorkerConfig)aObject;

    // if( !trackchainIds.equals( other.trackchainIds() ) ) {
    // return false;
    // }
    return true;
  }
}
