package org.toxsoft.uskat.s5.utils.collections;

import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AtomicValueKeeper;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;

/**
 * Keeper of the {@link IAtomicValueMap} interface instances.
 * <p>
 * Values returned by <code>read()</code> methods may be safely casted to editable {@link IOptionSetEdit} and even more
 * - to the class {@link AtomicValueMap}.
 *
 * @author hazard157
 */
public class AtomicValueMapKeeper
    extends AbstractEntityKeeper<IAtomicValueMap> {

  /**
   * Value-object registration identifier for {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "AtomicValueMap"; //$NON-NLS-1$

  /**
   * Keeper singleton.
   */
  public static final IEntityKeeper<IAtomicValueMap> KEEPER = new AtomicValueMapKeeper( false );

  /**
   * Indented keeper singleton.
   */
  public static final IEntityKeeper<IAtomicValueMap> KEEPER_INDENTED = new AtomicValueMapKeeper( true );

  private final boolean indented;

  private AtomicValueMapKeeper( boolean aIndented ) {
    super( IAtomicValueMap.class, EEncloseMode.ENCLOSES_KEEPER_IMPLEMENTATION, null );
    indented = aIndented;
  }

  @Override
  protected void doWrite( IStrioWriter aSw, IAtomicValueMap aEntity ) {
    aSw.writeChar( CHAR_SET_BEGIN );
    // empty option set
    if( aEntity.isEmpty() ) {
      aSw.writeChar( CHAR_SET_END );
      return;
    }
    if( indented ) {
      aSw.incNewLine();
    }
    // write values in form "id = value"
    IStringList ids = aEntity.keys();
    for( int i = 0, n = ids.size(); i < n; i++ ) {
      String name = ids.get( i );
      IAtomicValue value = aEntity.getByKey( name );
      aSw.writeAsIs( name );
      aSw.writeChar( CHAR_EQUAL );
      AtomicValueKeeper.KEEPER.write( aSw, value );
      if( i < n - 1 ) {
        aSw.writeChar( CHAR_ITEM_SEPARATOR );
        if( indented ) {
          aSw.writeEol();
        }
      }
    }
    if( indented ) {
      aSw.decNewLine();
    }
    aSw.writeChar( CHAR_SET_END );
  }

  @Override
  protected IAtomicValueMap doRead( IStrioReader aSr ) {
    AtomicValueMap map = new AtomicValueMap();
    if( aSr.readSetBegin() ) {
      do {
        String id = aSr.readIdPath();
        aSr.ensureChar( CHAR_EQUAL );
        IAtomicValue value = AtomicValueKeeper.KEEPER.read( aSr );
        map.put( id, value );
      } while( aSr.readSetNext() );
    }
    return map;
  }

}
