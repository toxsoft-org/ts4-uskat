package org.toxsoft.uskat.core.api.ugwis;

import java.util.concurrent.locks.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.api.ugwis.kinds.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Helper static methods of UGWI handling.
 * <p>
 * All methods are thread-safe.
 *
 * @author hazard157, vs
 */
public class SkUgwiUtils {

  private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

  private static final SynchronizedStridablesList<AbstractUgwiKind<?>> ugwiKindsList =
      new SynchronizedStridablesList<>( new StridablesList<>(), lock );

  static {
    ugwiKindsList.add( UgwiKindSkAttr.INSTANCE );
    ugwiKindsList.add( UgwiKindSkRtdata.INSTANCE );
    ugwiKindsList.add( UgwiKindSkCmd.INSTANCE );
    ugwiKindsList.add( UgwiKindSkSkid.INSTANCE );
    ugwiKindsList.add( UgwiKindSkLink.INSTANCE );
    ugwiKindsList.add( UgwiKindSkRivet.INSTANCE );
    // TODO add all built-in registrators
  }

  /**
   * Core handler to register all Sk-connection bound {@link ISkUgwiKind} when connection opens.
   * <p>
   * Note: not for users! Field is public for {@link SkCoreUtils} to access it.
   */
  public static final ISkCoreExternalHandler ugwisRegistrationHandler = aCoreApi -> {
    ISkUgwiService uServ = ISkUgwiService.class.cast( aCoreApi.services().findByKey( ISkUgwiService.SERVICE_ID ) );
    if( uServ != null ) {
      for( AbstractUgwiKind<?> uk : ugwiKindsList ) {
        AbstractSkUgwiKind<?> skuk = uk.createUgwiKind( aCoreApi );
        uServ.registerKind( skuk );
      }
    }
  };

  /**
   * Returns all registered UGWI kinds.
   *
   * @return {@link IStridablesList}&lt;{@link AbstractUgwiKind}&gt; - copy of the UGWI kinds list
   */
  public static IStridablesList<AbstractUgwiKind<?>> listUgwiKinds() {
    return ugwiKindsList.copyTo( null );
  }

  /**
   * Registers kind registrator to be used with all connections.
   * <p>
   * Does nothing if the king with same ID was already registered.
   *
   * @param aCreator {@link AbstractUgwiKind} - the creator
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static void registerUgwiKind( AbstractUgwiKind<?> aCreator ) {
    TsNullArgumentRtException.checkNull( aCreator );
    lock.writeLock().lock();
    try {
      if( !ugwiKindsList.hasElem( aCreator ) ) {
        ugwiKindsList.add( aCreator );
      }
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Возвращает признак существования сущности, на которую указывает {@link Ugwi}.<br>
   * Имеет смысл для тех {@link Ugwi}, которые ссылаются на одну сущность, например объект, атрибут, данное и т.п.
   *
   * @param aUgwi {@link Ugwi} - ИД сущности
   * @param aCoreApi {@link ISkCoreApi} - API сервера
   * @return <b>true</b> - сущность есть<br>
   *         <b>false</b> - сущность отсутствует
   */
  public static boolean isEntityExists( Ugwi aUgwi, ISkCoreApi aCoreApi ) {

    return switch( aUgwi.kindId() ) {
      case UgwiKindSkAttr.KIND_ID -> UgwiKindSkAttr.isEntityExists( aUgwi, aCoreApi );
      case UgwiKindSkRtdata.KIND_ID -> UgwiKindSkRtdata.isEntityExists( aUgwi, aCoreApi );
      case UgwiKindSkSkid.KIND_ID -> UgwiKindSkSkid.isEntityExists( aUgwi, aCoreApi );
      case UgwiKindSkLink.KIND_ID -> UgwiKindSkLink.isEntityExists( aUgwi, aCoreApi );
      case UgwiKindSkRivet.KIND_ID -> UgwiKindSkRivet.isEntityExists( aUgwi, aCoreApi );
      case UgwiKindSkCmd.KIND_ID -> UgwiKindSkCmd.isEntityExists( aUgwi, aCoreApi );
      default -> throw new TsNotAllEnumsUsedRtException();
    };
  }

  /**
   * Возвращает {@link Ugwi} из набора значений по идентификатору.<br>
   * Не порождает исключений - в случае отсутствия возвращает <code>null</code>
   *
   * @param aPropId String - ИД опции
   * @param aValues {@link IOptionSet} - набор значений
   * @return {@link Ugwi} - Ugwi или <code>null</code>
   */
  public static Ugwi findUgwi( String aPropId, IOptionSet aValues ) {
    if( aValues.hasKey( aPropId ) ) {
      IAtomicValue av = aValues.getByKey( aPropId );
      if( av.isAssigned() ) {
        return av.asValobj();
      }
    }
    return null;
  }

  /**
   * По возможности осуществляет преобразование из {@link Gwid} в {@link Ugwi}.
   *
   * @param aGwid {@link Gwid} - ИД корневой sk-сущности
   * @return {@link Ugwi} - ИД sk-сущности
   */
  public static Ugwi ofGwid( Gwid aGwid ) {
    return switch( aGwid.kind() ) {
      case GW_CLASS -> UgwiKindSkClassInfo.makeUgwi( aGwid.classId() );
      case GW_ATTR -> UgwiKindSkAttr.makeUgwi( aGwid.skid(), aGwid.propId() );
      case GW_RIVET -> UgwiKindSkRivet.makeUgwi( aGwid.skid(), aGwid.propId() );
      case GW_RTDATA -> UgwiKindSkRtdata.makeUgwi( aGwid.skid(), aGwid.propId() );
      case GW_LINK -> UgwiKindSkLink.makeUgwi( aGwid.skid(), aGwid.propId() );
      case GW_CMD -> UgwiKindSkCmd.makeUgwi( aGwid.skid(), aGwid.propId() );
      case GW_CLOB, GW_CMD_ARG, GW_EVENT, GW_EVENT_PARAM -> throw new TsNotAllEnumsUsedRtException();
      default -> throw new TsNotAllEnumsUsedRtException();
    };
  }

  /**
   * No subclasses.
   */
  private SkUgwiUtils() {
    // nop
  }

}
