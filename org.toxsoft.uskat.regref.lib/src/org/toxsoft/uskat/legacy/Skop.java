package org.toxsoft.uskat.legacy;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.bricks.strid.more.IdPair;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Идентификация свойства конкретного объекта.
 * <p>
 * В отличие от <code>Cod</code> этот класс для идентификации объекта использует {@link Skid}, а не <code>long</code>
 * идентификатор объекта. Таким образом, экземпляры этого класса:
 * <ul>
 * <li>можно создавать самому, в том числе в виде <code>static final</code> констант;</li>
 * <li>можно хранить перманентно, имея в виду, что strid идентификаторы объектов перманентны;</li>
 * <li>облегчают отладку благодаря осмысленному представлению {@link #toString()}.</li>
 * </ul>
 * <p>
 * Это неизменяемый класс.
 *
 * @author goga
 */
public final class Skop
    implements Serializable, Comparable<Skop> {

  private static final long serialVersionUID = 157157L;

  /**
   * Value-object keeper identifier.
   */
  public static final String KEEPER_ID = "Skop"; //$NON-NLS-1$

  /**
   * Экземпляр-синголтон хранителя.
   */
  public static final IEntityKeeper<Skop> KEEPER =
      new AbstractEntityKeeper<>( Skop.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, Skop aEntity ) {
          Skid.KEEPER.write( aSw, aEntity.skid() );
          aSw.writeSeparatorChar();
          aSw.writeAsIs( aEntity.propId() );
        }

        @Override
        protected Skop doRead( IStrioReader aSr ) {
          Skid skid = Skid.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          String propId = aSr.readIdPath();
          return new Skop( skid, propId );
        }
      };

  private final Skid   skid;
  private final String propId;

  /**
   * Конструктор.
   *
   * @param aSkid {@link Skid} - скид (идентификатор) объекта
   * @param aPropId String - идентификар (ИД-путь) свойства объекта
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsIllegalArgumentRtException идентификатор свойства не ИД-имя
   */
  public Skop( Skid aSkid, String aPropId ) {
    skid = TsNullArgumentRtException.checkNull( aSkid );
    propId = StridUtils.checkValidIdPath( aPropId );
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса Object
  //

  @Override
  public String toString() {
    return skid.toString() + IdPair.CHAR_SEPARATOR + propId;
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof Skop that ) {
      return this.skid.equals( that.skid ) && this.propId.equals( that.propId );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + skid.hashCode();
    result = TsLibUtils.PRIME * result + propId.hashCode();
    return result;
  }

  // ------------------------------------------------------------------------------------
  // Comparable
  //

  @Override
  public int compareTo( Skop aThat ) {
    if( aThat == null ) {
      throw new NullPointerException();
    }
    int c = this.skid.compareTo( aThat.skid );
    if( c == 0 ) {
      c = this.propId.compareTo( propId );
    }
    return c;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Возвращает скид.
   *
   * @return {@link Skid} - скид (идентификатор) объекта
   */
  public Skid skid() {
    return skid;
  }

  /**
   * Возвращает идентификар свойства объекта.
   *
   * @return String - идентификар (ИД-путь) свойства объекта
   */
  public String propId() {
    return propId;
  }

}
