package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AtomicValueKeeper;
import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.CharInputStreamString;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.CharOutputStreamAppendable;
import org.toxsoft.core.tslib.bricks.strio.impl.StrioReader;
import org.toxsoft.core.tslib.bricks.strio.impl.StrioWriter;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.coll.IMapEdit;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.rtdserv.ISkRtdataService;

/**
 * {@link IBaRtdata} message builder: new values of current data received.
 *
 * @author hazard157
 */
public class BaMsgRtdataCurrData
    extends AbstractBackendMessageBuilder {

  /**
   * ID of the message.
   */
  public static final String MSG_ID = "CurrData"; //$NON-NLS-1$

  /**
   * Singletone intance.
   */
  public static final BaMsgRtdataCurrData INSTANCE = new BaMsgRtdataCurrData();

  private static final String ARGID_NEW_VALUES = "NewValues"; //$NON-NLS-1$

  BaMsgRtdataCurrData() {
    super( ISkRtdataService.SERVICE_ID, MSG_ID );
    defineArgNonValobj( ARGID_NEW_VALUES, EAtomicType.STRING, true );
  }

  /**
   * Creates the message instance.
   *
   * @param aNewValues {@link IMap}&lt;{@link Gwid},{@link IAtomicValue}&gt; - map "RTdata GWID" - "current value"
   * @return {@link GtMessage} - created instance of the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public GtMessage makeMessage( IMap<Gwid, IAtomicValue> aNewValues ) {
    StringBuilder sb = new StringBuilder();
    IStrioWriter sw = new StrioWriter( new CharOutputStreamAppendable( sb ) );
    sw.writeChar( CHAR_ARRAY_BEGIN );
    for( int i = 0, count = aNewValues.size(); i < count; i++ ) {
      Gwid g = aNewValues.keys().get( i );
      IAtomicValue v = aNewValues.values().get( i );
      Gwid.KEEPER.write( sw, g );
      sw.writeSeparatorChar();
      AtomicValueKeeper.KEEPER.write( sw, v );
      if( i < count - 1 ) {
        sw.writeSeparatorChar();
      }
    }
    sw.writeChar( CHAR_ARRAY_END );
    return makeMessageVarargs( ARGID_NEW_VALUES, sb.toString() );
  }

  /**
   * Extracts new current RTdata values argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link IMap}&lt;{@link Gwid},{@link IAtomicValue}&gt; - map "RTdata GWID" - "current value"
   */
  public IMap<Gwid, IAtomicValue> getNewValues( GenericMessage aMsg ) {
    String s = getArg( aMsg, ARGID_NEW_VALUES ).asString();
    IMapEdit<Gwid, IAtomicValue> map = new ElemMap<>();
    IStrioReader sr = new StrioReader( new CharInputStreamString( s ) );
    if( sr.readArrayBegin() ) {
      do {
        Gwid g = Gwid.KEEPER.read( sr );
        sr.ensureSeparatorChar();
        IAtomicValue v = AtomicValueKeeper.KEEPER.read( sr );
        map.put( g, v );
      } while( sr.readArrayNext() );
    }
    return map;
  }

}
