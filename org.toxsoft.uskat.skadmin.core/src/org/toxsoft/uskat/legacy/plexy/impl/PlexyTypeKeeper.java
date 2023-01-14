package org.toxsoft.uskat.legacy.plexy.impl;

import org.toxsoft.core.tslib.av.impl.DataType;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.utils.errors.TsNotAllEnumsUsedRtException;
import org.toxsoft.uskat.legacy.plexy.EPlexyKind;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;

/**
 * Хранитель значений объектов типа {@link IPlexyType}.
 *
 * @author hazard157
 */
public class PlexyTypeKeeper
    extends AbstractEntityKeeper<IPlexyType> {

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static IEntityKeeper<IPlexyType> KEEPER = new PlexyTypeKeeper();

  private PlexyTypeKeeper() {
    super( IPlexyType.class, EEncloseMode.ENCLOSES_BASE_CLASS, IPlexyType.NONE );
  }

  @Override
  protected void doWrite( IStrioWriter aSw, IPlexyType aEntity ) {
    aSw.writeAsIs( aEntity.kind().id() );
    switch( aEntity.kind() ) {
      case SINGLE_VALUE:
      case VALUE_LIST:
        aSw.writeSeparatorChar();
        DataType.KEEPER.write( aSw, aEntity.dataType() );
        break;
      case OPSET:
        break;
      case SINGLE_REF:
      case REF_LIST:
        aSw.writeSeparatorChar();
        aSw.writeQuotedString( aEntity.refClass().getCanonicalName() );
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }

  @Override
  protected IPlexyType doRead( IStrioReader sr ) {
    EPlexyKind kind = EPlexyKind.findById( sr.readIdName() );
    switch( kind ) {
      case SINGLE_VALUE:
        sr.ensureSeparatorChar();
        return PlexyValueUtils.ptSingleValue( DataType.KEEPER.read( sr ) );
      case VALUE_LIST:
        sr.ensureSeparatorChar();
        return PlexyValueUtils.ptValueList( DataType.KEEPER.read( sr ) );
      case OPSET:
        return PlexyValueUtils.ptOpset();
      case SINGLE_REF:
        sr.ensureSeparatorChar();
        Class<?> cls1;
        try {
          cls1 = Class.forName( sr.readQuotedString() );
        }
        catch( ClassNotFoundException e ) {
          throw new StrioRtException( e );
        }
        return PlexyValueUtils.ptSingleRef( cls1 );
      case REF_LIST:
        sr.ensureSeparatorChar();
        Class<?> cls2;
        try {
          cls2 = Class.forName( sr.readQuotedString() );
        }
        catch( ClassNotFoundException e ) {
          throw new StrioRtException( e );
        }
        return PlexyValueUtils.ptRefList( cls2 );
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }

}
