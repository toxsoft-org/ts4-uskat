package org.toxsoft.uskat.core.impl.dto;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;

import java.io.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * {@link IDtoObject} implementation.
 *
 * @author hazard157
 */
public final class DtoObject
    implements IDtoObject, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Синглтон хранителя.
   */
  public static final IEntityKeeper<IDtoObject> KEEPER =
      new AbstractEntityKeeper<>( IDtoObject.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, IDtoObject aEntity ) {
          Skid.KEEPER.write( aSw, aEntity.skid() );
          aSw.writeSeparatorChar();
          OptionSetKeeper.KEEPER.write( aSw, aEntity.attrs() );
          aSw.writeSeparatorChar();
          MappedSkids.KEEPER.write( aSw, aEntity.rivets() );
        }

        @Override
        protected IDtoObject doRead( IStrioReader aSr ) {
          Skid skid = Skid.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          OptionSet attrs = (OptionSet)OptionSetKeeper.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          MappedSkids ms = (MappedSkids)MappedSkids.KEEPER.read( aSr );
          return new DtoObject( 0, skid, attrs, ms );
        }
      };

  private final Skid           skid;
  private final IOptionSetEdit attrs;
  private final MappedSkids    rivets;

  /**
   * Constructor.
   *
   * @param aSkid {@link Skid} - object skid
   * @param aAttrs {@link IOptionSet} - attributes values
   * @param aRivets {@link IStringMap}&lt;{@link ISkidList}&gt; - rivets
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public DtoObject( Skid aSkid, IOptionSet aAttrs, IStringMap<ISkidList> aRivets ) {
    skid = TsNullArgumentRtException.checkNull( aSkid );
    attrs = new OptionSet();
    attrs.addAll( aAttrs );
    rivets = new MappedSkids();
    rivets.setAll( aRivets );
  }

  /**
   * Private constructor for keeper.
   *
   * @param aFoo int - unsed argument for unique constructor signature
   * @param aSkid {@link Skid} - object skid
   * @param aAttrs {@link OptionSet} - attributes values
   * @param aRivets {@link MappedSkids} - reivets values
   */
  private DtoObject( int aFoo, Skid aSkid, OptionSet aAttrs, MappedSkids aRivets ) {
    skid = aSkid;
    attrs = aAttrs;
    rivets = aRivets;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IDtoObject
  //

  @Override
  public Skid skid() {
    return skid;
  }

  @Override
  public IOptionSetEdit attrs() {
    return attrs;
  }

  @Override
  public MappedSkids rivets() {
    return rivets;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public String toString() {
    return skid.toString();
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof DtoObject that ) {
      return skid.equals( that.skid ) && attrs.equals( that.attrs ) && rivets.equals( that.rivets );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = INITIAL_HASH_CODE;
    result = PRIME * result + skid.hashCode();
    result = PRIME * result + attrs.hashCode();
    result = PRIME * result + rivets.hashCode();
    return result;
  }

}
