package org.toxsoft.uskat.core.devapi;

import java.util.*;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.impl.*;

// TODO TRANSLATE

/**
 * The USkat core entities localization.
 * <p>
 * Локализация работает на уровне между бекендом {@link ISkBackend} и ядром {@link ISkCoreApi}. То есть, бекенд
 * возвращает не локализованные сущности, а в ядро они попадают локализованными. Сущности, доступные по
 * {@link ISkCoreApi} - локализованы.
 * <p>
 * Локализатор создается при создании {@link SkCoreApi} и вместе с ним заверщает работу. Соответсвенно, все настроечные
 * параметры и ссылки он получает в конструкторе, в том числе, заданным и неизменным остается {@link #locale()}.
 *
 * @author hazard157
 */
public interface ICoreL10n {

  /**
   * Returns the locale used for translation.
   *
   * @return {@link Locale} - the locale, never is <code>null</code>
   */
  Locale locale();

  /**
   * Determines if localization is turned on.
   * <p>
   * Localization may be turned off using the {@link #setL10n(boolean)} method, as well as by incorrect settings, or
   * lack of resources for the requested locale or for other reasons, localization may not work at all. Note that even
   * if localization works, for any core entity (class, object, and etc.) translation may be missing.
   *
   * @return boolean - <code>true</code> if localization is turned on
   */
  boolean isL10nOn();

  // TODO TRANSLATE

  /**
   * Включает или отключает локализацию.
   * <p>
   * Если все настроено верно, то локализация включена по умолчанию. Если же локализация не настроена, то применение
   * этого метода игнорируется.
   * <p>
   * Причины отключения локализации могут быть разные. Но одна из основных в том, что если бекенд сам поддерживает
   * локализацию, или знает, что хранит данные в нужной локали, может отключить ядерную локализацию. Например, для
   * повышения производительности. Также полезно можеет быть отключить локализацию для отладки.
   * <p>
   * Учтите, что включени/выключение локализации влияет только на те сущности, к которым пройзойдет обращение через
   * бекенд. Уже загруженные сущности останутся без изменений.
   *
   * @param aLocalization boolean - признак включения локализации
   */
  void setL10n( boolean aLocalization );

  /**
   * Localizes class descriptions.
   * <p>
   * Localizes {@link IDtoClassInfo} supplied by the backend addon {@link IBaClasses}.
   * <p>
   * If localization is turend off ({@link #isL10nOn()} = <code>false</code>), then returns the argument.
   *
   * @param aClassInfoes {@link IStridablesList}&lt;{@link IDtoClassInfo}&gt; - classes lsit
   * @return {@link IStridablesList}&lt;{@link IDtoClassInfo}&gt; - localized classes list
   */
  IStridablesList<IDtoClassInfo> l10nClassInfos( IStridablesList<IDtoClassInfo> aClassInfoes );

  /**
   * Localizes one object.
   * <p>
   * Localizes {@link IDtoObject} supplied by the backend addon {@link IBaObjects}.
   * <p>
   * If localization is turned off ({@link #isL10nOn()} = <code>false</code>), then returns the argument.
   * <p>
   * Two different methods {@link #l10nObject(IDtoObject)} and {@link #l10nObjectsList(IList)} exits for optimization
   * purposes.
   *
   * @param aObject {@link IDtoObject} - object, may be <code>null</code>
   * @return {@link IDtoObject} - localized object
   */
  IDtoObject l10nObject( IDtoObject aObject );

  /**
   * Localizes many objects at once.
   * <p>
   * Localizes {@link IDtoObject} supplied by the backend addon {@link IBaObjects}.
   * <p>
   * If localization is turned off ({@link #isL10nOn()} = <code>false</code>), then returns the argument.
   * <p>
   * Two different methods {@link #l10nObject(IDtoObject)} and {@link #l10nObjectsList(IList)} exists for optimization
   * purposes.
   *
   * @param aObjects {@link IStridablesList}&lt;{@link IDtoObject}&gt; - list of objects
   * @return {@link IStridablesList}&lt;{@link IDtoObject}&gt; - localized objects
   */
  IList<IDtoObject> l10nObjectsList( IList<IDtoObject> aObjects );

}
