package org.toxsoft.uskat.s5.server.backend.supports.links;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.IS5Resources.*;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;

import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.linkserv.IDtoLinkFwd;
import org.toxsoft.uskat.core.api.linkserv.IDtoLinkRev;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;

/**
 * Вспомогательные методы пакета для работы с отражением java
 *
 * @author mvk
 */
class S5LinksReflectUtils {

  /**
   * Возвращает конструктор создания ПРЯМОЙ связи объекта {@link S5LinkFwdEntity} через курсор jdbc {@link ResultSet}.
   *
   * @param aLinkImplClassName String полное имя класса реализации, наследник {@link S5LinkFwdEntity}
   * @return {@link Constructor} конструктор с параметром ({@link ResultSet} )
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException не найден класс реализации связи объекта
   * @throws TsIllegalArgumentRtException не найден метод конструктора с параметром {@link ResultSet}
   */
  @SuppressWarnings( "unchecked" )
  static Constructor<S5LinkFwdEntity> getConstructorLinkFwdByResultSet( String aLinkImplClassName ) {
    TsNullArgumentRtException.checkNull( aLinkImplClassName );
    // Определение класса реализации объекта
    Class<S5LinkFwdEntity> linkImplClass;
    try {
      linkImplClass = (Class<S5LinkFwdEntity>)Class.forName( aLinkImplClassName );
    }
    catch( ClassNotFoundException e ) {
      // Не найден класс реализации связи объекта
      throw new TsIllegalArgumentRtException( MSG_ERR_LINK_IMPL_NOT_FOUND, aLinkImplClassName, cause( e ) );
    }
    try {
      Constructor<S5LinkFwdEntity> retValue = linkImplClass.getDeclaredConstructor( ResultSet.class );
      retValue.setAccessible( true );
      return retValue;
    }
    catch( NoSuchMethodException | SecurityException e ) {
      // Не найден класс реализации
      throw new TsIllegalArgumentRtException( MSG_ERR_LINK_FWD_CONSTRUCTOR_NOT_FOUND1, aLinkImplClassName, cause( e ) );
    }
  }

  /**
   * Возвращает конструктор создания ОБРАТНОЙ связи объекта {@link S5LinkRevEntity} через курсор jdbc {@link ResultSet}.
   *
   * @param aLinkImplClassName String полное имя класса реализации, наследник {@link S5LinkRevEntity}
   * @return {@link Constructor} конструктор с параметром ({@link ResultSet} )
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException не найден класс реализации связи объекта
   * @throws TsIllegalArgumentRtException не найден метод конструктора с параметром {@link ResultSet}
   */
  @SuppressWarnings( "unchecked" )
  static Constructor<S5LinkRevEntity> getConstructorLinkRevByResultSet( String aLinkImplClassName ) {
    TsNullArgumentRtException.checkNull( aLinkImplClassName );
    // Определение класса реализации объекта
    Class<S5LinkRevEntity> linkImplClass;
    try {
      linkImplClass = (Class<S5LinkRevEntity>)Class.forName( aLinkImplClassName );
    }
    catch( ClassNotFoundException e ) {
      // Не найден класс реализации связи объекта
      throw new TsIllegalArgumentRtException( MSG_ERR_LINK_IMPL_NOT_FOUND, aLinkImplClassName, cause( e ) );
    }
    try {
      Constructor<S5LinkRevEntity> retValue = linkImplClass.getDeclaredConstructor( ResultSet.class );
      retValue.setAccessible( true );
      return retValue;
    }
    catch( NoSuchMethodException | SecurityException e ) {
      // Не найден класс реализации
      throw new TsIllegalArgumentRtException( MSG_ERR_LINK_REV_CONSTRUCTOR_NOT_FOUND1, aLinkImplClassName, cause( e ) );
    }
  }

  /**
   * Возвращает конструктор копирования ПРЯМОЙ связи объекта через исходное {@link IDtoLinkFwd}.
   *
   * @param aLinkFwdImplClass Class класс реализации ПРЯМОЙ связи объекта, наследник {@link S5LinkFwdEntity}
   * @return {@link Constructor} конструктор с параметром ({@link IDtoLinkFwd} )
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException не найден класс реализации объекта
   * @throws TsIllegalArgumentRtException не найден метод конструктора с параметром {@link IDtoLinkFwd}
   */
  static Constructor<S5LinkFwdEntity> getConstructorLinkFwdBySource( Class<S5LinkFwdEntity> aLinkFwdImplClass ) {
    TsNullArgumentRtException.checkNull( aLinkFwdImplClass );
    try {
      Constructor<S5LinkFwdEntity> retValue = aLinkFwdImplClass.getDeclaredConstructor( IDtoLinkFwd.class );
      retValue.setAccessible( true );
      return retValue;
    }
    catch( NoSuchMethodException | SecurityException e ) {
      // Не найден класс реализации
      throw new TsIllegalArgumentRtException( MSG_ERR_LINK_FWD_CONSTRUCTOR_NOT_FOUND2, aLinkFwdImplClass.getName(),
          cause( e ) );
    }
  }

  /**
   * Возвращает конструктор копирования ОБРАТНОЙ связи объекта через исходное {@link IDtoLinkRev}.
   *
   * @param aLinkRevImplClass Class класс реализации ОБРАТНоЙ связи объекта, наследник {@link S5LinkRevEntity}
   * @return {@link Constructor} конструктор с параметром ({@link IDtoLinkRev} )
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException не найден класс реализации объекта
   * @throws TsIllegalArgumentRtException не найден метод конструктора с параметром {@link IDtoLinkRev}
   */
  static Constructor<S5LinkRevEntity> getConstructorLinkRevBySource( Class<S5LinkRevEntity> aLinkRevImplClass ) {
    TsNullArgumentRtException.checkNull( aLinkRevImplClass );
    try {
      Constructor<S5LinkRevEntity> retValue = aLinkRevImplClass.getDeclaredConstructor( IDtoLinkRev.class );
      retValue.setAccessible( true );
      return retValue;
    }
    catch( NoSuchMethodException | SecurityException e ) {
      // Не найден класс реализации
      throw new TsIllegalArgumentRtException( MSG_ERR_LINK_REV_CONSTRUCTOR_NOT_FOUND2, aLinkRevImplClass.getName(),
          cause( e ) );
    }
  }

  /**
   * Возвращает конструктор копирования ОБРАТНОЙ связи объекта через заданные параметры.
   *
   * @param aLinkRevImplClass Class класс реализации ОБРАТНоЙ связи объекта, наследник {@link S5LinkRevEntity}
   * @return {@link Constructor} конструктор с параметром ({@link IDtoLinkRev} )
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException не найден класс реализации объекта
   * @throws TsIllegalArgumentRtException не найден метод конструктора с параметром {@link IDtoLinkRev}
   */
  static Constructor<S5LinkRevEntity> getConstructorLinkRevByParams( Class<S5LinkRevEntity> aLinkRevImplClass ) {
    TsNullArgumentRtException.checkNull( aLinkRevImplClass );
    try {
      Constructor<S5LinkRevEntity> retValue =
          aLinkRevImplClass.getDeclaredConstructor( Skid.class, String.class, String.class, ISkidList.class );
      retValue.setAccessible( true );
      return retValue;
    }
    catch( NoSuchMethodException | SecurityException e ) {
      // Не найден класс реализации
      throw new TsIllegalArgumentRtException( MSG_ERR_LINK_REV_CONSTRUCTOR_NOT_FOUND3, aLinkRevImplClass.getName(),
          cause( e ) );
    }
  }

  /**
   * Возвращает класс реализации ПРЯМОЙ связи объектов
   *
   * @param aClassInfo {@link ISkClassInfo} описание класса
   * @param aImplByIds {@link IStringMapEdit}&lt;Class&gt; карта классов реализаций прямых связей объектов. <br>
   *          Ключ: описание класса;<br>
   *          Значение: Класс реализации ПРЯМОЙ связи объекта.
   * @return Class&lt;{@link S5LinkFwdEntity}&gt; класс реализации связи объекта
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static Class<S5LinkFwdEntity> getLinkFwdImplClass( ISkClassInfo aClassInfo,
      IStringMapEdit<Class<S5LinkFwdEntity>> aImplByIds ) {
    TsNullArgumentRtException.checkNull( aClassInfo );
    Class<S5LinkFwdEntity> retValue = aImplByIds.findByKey( aClassInfo.id() );
    if( retValue != null ) {
      return retValue;
    }
    retValue = getLinkFwdImplClass( aClassInfo );
    aImplByIds.put( aClassInfo.id(), retValue );
    return retValue;
  }

  /**
   * Возвращает класс реализации ПРЯМОЙ связи объектов
   *
   * @param aClassInfo {@link ISkClassInfo} описание класса
   * @param aImplByIds {@link IStringMapEdit}&lt;Class&gt; карта классов реализаций обратных связей объектов. <br>
   *          Ключ: описание класса;<br>
   *          Значение: Класс реализации ОБРАТНОЙ связи объекта.
   * @return Class&lt;{@link S5LinkRevEntity}&gt; класс реализации связи объекта
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static Class<S5LinkRevEntity> getLinkRevImplClass( ISkClassInfo aClassInfo,
      IStringMapEdit<Class<S5LinkRevEntity>> aImplByIds ) {
    TsNullArgumentRtException.checkNull( aClassInfo );
    Class<S5LinkRevEntity> retValue = aImplByIds.findByKey( aClassInfo.id() );
    if( retValue != null ) {
      return retValue;
    }
    retValue = getLinkRevImplClass( aClassInfo );
    aImplByIds.put( aClassInfo.id(), retValue );
    return retValue;
  }

  /**
   * Возвращает класс реализации ПРЯМОЙ связи объектов
   *
   * @param aClassInfo {@link ISkClassInfo} описание класса
   * @return Class&lt;{@link S5LinkFwdEntity}&gt; класс реализации связи объекта
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static Class<S5LinkFwdEntity> getLinkFwdImplClass( ISkClassInfo aClassInfo ) {
    TsNullArgumentRtException.checkNull( aClassInfo );
    // Класс реализации хранения значений объекта
    String linkImplClassName = OP_FWD_LINK_IMPL_CLASS.getValue( aClassInfo.params() ).asString();
    return getLinkFwdImplClass( linkImplClassName );
  }

  /**
   * Возвращает класс реализации ПРЯМОЙ связи объектов
   *
   * @param aLinkImplClassName String полное имя java-класса реализации прямой связи
   * @return Class&lt;{@link S5LinkFwdEntity}&gt; класс реализации связи объекта
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  static Class<S5LinkFwdEntity> getLinkFwdImplClass( String aLinkImplClassName ) {
    TsNullArgumentRtException.checkNull( aLinkImplClassName );
    try {
      return (Class<S5LinkFwdEntity>)Class.forName( aLinkImplClassName );
    }
    catch( ClassNotFoundException e ) {
      throw new TsInternalErrorRtException( e );
    }
  }

  /**
   * Возвращает класс реализации ОБРАТНОЙ связи объектов
   *
   * @param aClassInfo {@link ISkClassInfo} описание класса
   * @return Class&lt;{@link S5LinkRevEntity}&gt; класс реализации связи объекта
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static Class<S5LinkRevEntity> getLinkRevImplClass( ISkClassInfo aClassInfo ) {
    TsNullArgumentRtException.checkNull( aClassInfo );
    // Класс реализации хранения значений объекта
    String linkImplClassName = OP_REV_LINK_IMPL_CLASS.getValue( aClassInfo.params() ).asString();
    return getLinkRevImplClass( linkImplClassName );
  }

  /**
   * Возвращает класс реализации ОБРАТНОЙ связи объектов
   *
   * @param aLinkImplClassName String полное имя java-класса реализации обратной связи
   * @return Class&lt;{@link S5LinkRevEntity}&gt; класс реализации связи объекта
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  static Class<S5LinkRevEntity> getLinkRevImplClass( String aLinkImplClassName ) {
    TsNullArgumentRtException.checkNull( aLinkImplClassName );
    try {
      return (Class<S5LinkRevEntity>)Class.forName( aLinkImplClassName );
    }
    catch( ClassNotFoundException e ) {
      throw new TsInternalErrorRtException( e );
    }
  }

  /**
   * Создает ПРЯМУЮ связь объекта способную быть сохраненной в базе данных
   *
   * @param aConstructor {@link Constructor}&lt;{@link S5LinkFwdEntity}&gt; конструктор связи объекта
   * @param aSource {@link IDtoLinkFwd} исходная связь с объектами
   * @return {@link S5LinkFwdEntity} созданная связь объекта
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static S5LinkFwdEntity createLinkFwdEntity( Constructor<S5LinkFwdEntity> aConstructor, IDtoLinkFwd aSource ) {
    TsNullArgumentRtException.checkNulls( aConstructor, aSource );
    try {
      return aConstructor.newInstance( aSource );
    }
    catch( Throwable e ) {
      // Неожиданная ошибка создания объекта класса
      throw new TsInternalErrorRtException( e, MSG_ERR_CREATE_LINK_UNEXPECTED, aSource.linkId(), aSource.leftSkid(),
          cause( e ) );
    }
  }

  /**
   * Создает ОБРАТНУЮ связь объекта способную быть сохраненной в базе данных
   *
   * @param aConstructor {@link Constructor}&lt;{@link S5LinkRevEntity}&gt; конструктор связи объекта
   * @param aSource {@link IDtoLinkRev} исходная связь с объектами
   * @return {@link S5LinkRevEntity} созданная связь объекта
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static S5LinkRevEntity createLinkRevEntity( Constructor<S5LinkRevEntity> aConstructor, IDtoLinkRev aSource ) {
    TsNullArgumentRtException.checkNulls( aConstructor, aSource );
    try {
      return aConstructor.newInstance( aSource );
    }
    catch( Throwable e ) {
      // Неожиданная ошибка создания объекта класса
      throw new TsInternalErrorRtException( e, MSG_ERR_CREATE_LINK_UNEXPECTED, aSource.linkId(), aSource.rightSkid(),
          cause( e ) );
    }
  }

  /**
   * Создает ОБРАТНУЮ связь объекта способную быть сохраненной в базе данных
   *
   * @param aConstructor {@link Constructor}&lt;{@link S5LinkRevEntity}&gt; конструктор связи объекта
   * @param aRightSkid {@link Skid} идентификатор правого объекта связи
   * @param aLinkClassId String идентификатор класса в котором определена связь
   * @param aLinkId String строковый идентификатор связи
   * @param aRightSkids {@link ISkidList} список идентификаторов правых объектов связи
   * @return {@link S5LinkRevEntity} созданная связь объекта
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static S5LinkRevEntity createLinkRevEntity( Constructor<S5LinkRevEntity> aConstructor, Skid aRightSkid,
      String aLinkClassId, String aLinkId, ISkidList aRightSkids ) {
    TsNullArgumentRtException.checkNulls( aConstructor, aRightSkid, aLinkClassId, aLinkId, aRightSkids );
    try {
      return aConstructor.newInstance( aRightSkid, aLinkClassId, aLinkId, aRightSkids );
    }
    catch( Throwable e ) {
      // Неожиданная ошибка создания объекта класса
      throw new TsInternalErrorRtException( e, MSG_ERR_CREATE_LINK_UNEXPECTED, aLinkId, aRightSkid, cause( e ) );
    }
  }
}
