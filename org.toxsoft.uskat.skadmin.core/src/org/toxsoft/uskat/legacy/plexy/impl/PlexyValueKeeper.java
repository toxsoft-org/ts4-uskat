package org.toxsoft.uskat.legacy.plexy.impl;

import static org.toxsoft.uskat.legacy.plexy.impl.ISkResources.*;

import java.io.*;
import java.nio.ByteBuffer;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AtomicValueKeeper;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.impl.StrioUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNotAllEnumsUsedRtException;
import org.toxsoft.uskat.legacy.plexy.*;

/**
 * Хранитель значений объектов типа {@link IPlexyValue}.
 * <p>
 * Обратите внимание, что невозможно соханить значение вида {@link EPlexyKind#isReference()}, если объекты класса
 * {@link IPlexyType#refClass()} не являются сериализуемым (не реализует {@link Serializable}).
 *
 * @author hazard157
 */
public class PlexyValueKeeper
    extends AbstractEntityKeeper<IPlexyValue> {

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static IEntityKeeper<IPlexyValue> KEEPER = new PlexyValueKeeper();

  private PlexyValueKeeper() {
    super( IPlexyValue.class, EEncloseMode.ENCLOSES_BASE_CLASS, IPlexyValue.NULL );
  }

  @Override
  protected void doWrite( IStrioWriter aSw, IPlexyValue aEntity ) {
    PlexyTypeKeeper.KEEPER.write( aSw, aEntity.type() );
    aSw.writeSeparatorChar();
    switch( aEntity.type().kind() ) {
      case SINGLE_VALUE:
        AtomicValueKeeper.KEEPER.write( aSw, aEntity.singleValue() );
        break;
      case VALUE_LIST:
        StrioUtils.writeCollection( aSw, TsLibUtils.EMPTY_STRING, aEntity.valueList(), AtomicValueKeeper.KEEPER );
        break;
      case OPSET:
        OptionSetKeeper.KEEPER.write( aSw, aEntity.getOpset() );
        break;
      case SINGLE_REF:
        if( aEntity.singleRef() instanceof Serializable ) {
          Serializable o = (Serializable)aEntity.singleRef();
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          try( ObjectOutputStream oos = new ObjectOutputStream( baos ) ) {
            oos.writeObject( o );
          }
          catch( IOException e ) {
            throw new StrioRtException( e );
          }
          IAtomicValue av = AvUtils.avValobj( ByteBuffer.wrap( baos.toByteArray() ) );
          AtomicValueKeeper.KEEPER.write( aSw, av );
        }
        else {
          throw new StrioRtException( MSG_ERR_CANT_WRITE_NON_SERIALIZABLE_OBJ_REF );
        }
        break;
      case REF_LIST:
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try( ObjectOutputStream oos = new ObjectOutputStream( baos ) ) {
          oos.writeObject( aEntity.refList().toArray() );
        }
        catch( IOException e ) {
          throw new StrioRtException( e );
        }
        IAtomicValue av = AvUtils.avValobj( ByteBuffer.wrap( baos.toByteArray() ) );
        AtomicValueKeeper.KEEPER.write( aSw, av );
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }

  @Override
  protected IPlexyValue doRead( IStrioReader aSr ) {
    IPlexyType type = PlexyTypeKeeper.KEEPER.read( aSr );
    aSr.ensureSeparatorChar();
    switch( type.kind() ) {
      case SINGLE_VALUE:
        return PlexyValueUtils.pvSingleValue( AtomicValueKeeper.KEEPER.read( aSr ) );
      case VALUE_LIST:
        IList<IAtomicValue> avs = StrioUtils.readCollection( aSr, TsLibUtils.EMPTY_STRING, AtomicValueKeeper.KEEPER );
        return PlexyValueUtils.pvValueList( type, avs );
      case OPSET:
        return PlexyValueUtils.pvOpset( OptionSetKeeper.KEEPER.read( aSr ) );
      case SINGLE_REF: {
        IAtomicValue av = AtomicValueKeeper.KEEPER.read( aSr );
        StrioRtException.checkTrue( av.atomicType() != EAtomicType.VALOBJ, MSG_ERR_CANT_READ_NON_VALOBJ_REF );
        Object ref;
        ByteBuffer bb = av.asValobj();
        try( ObjectInputStream ois = new ObjectInputStream( new ByteArrayInputStream( bb.array() ) ) ) {
          ref = ois.readObject();
        }
        catch( IOException e ) {
          throw new StrioRtException( e );
        }
        catch( ClassNotFoundException e ) {
          throw new StrioRtException( e, MSG_ERR_CANT_FIND_OBJ_REF_CLASS );
        }
        return PlexyValueUtils.pvSingleRef( type, ref );
      }
      case REF_LIST: {
        IAtomicValue av = AtomicValueKeeper.KEEPER.read( aSr );
        StrioRtException.checkTrue( av.atomicType() != EAtomicType.VALOBJ, MSG_ERR_CANT_READ_NON_VALOBJ_REF );
        Object[] refArray;
        ByteBuffer bb = av.asValobj();
        try( ObjectInputStream ois = new ObjectInputStream( new ByteArrayInputStream( bb.array() ) ) ) {
          refArray = (Object[])ois.readObject();
        }
        catch( IOException e ) {
          throw new StrioRtException( e );
        }
        catch( ClassNotFoundException e ) {
          throw new StrioRtException( e, MSG_ERR_CANT_FIND_OBJ_REF_CLASS );
        }
        return PlexyValueUtils.pvRefList( type, new ElemArrayList<>( refArray ) );
      }
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }

}
