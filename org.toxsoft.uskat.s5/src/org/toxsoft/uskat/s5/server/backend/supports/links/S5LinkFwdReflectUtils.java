package org.toxsoft.uskat.s5.server.backend.supports.links;

import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;

import java.lang.reflect.*;
import java.sql.*;

import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;

/**
 * Вспомогательные методы пакета для работы с отражением java
 *
 * @author mvk
 */
class S5LinkFwdReflectUtils {

  /**
   * Возвращает конструктор создания прямой связи через курсор jdbc {@link ResultSet}.
   *
   * @param aLinkImplClassName String полное имя класса реализации прямой связи, наследник {@link S5LinkFwdEntity}
   * @return {@link Constructor} конструктор с параметром ({@link ResultSet} )
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException не найден класс реализации прямой связи
   * @throws TsIllegalArgumentRtException не найден метод конструктора с параметром {@link ResultSet}
   */
  @SuppressWarnings( "unchecked" )
  static Constructor<S5LinkFwdEntity> getConstructorByResultSet( String aLinkImplClassName ) {
    TsNullArgumentRtException.checkNull( aLinkImplClassName );
    // Определение класса реализации прямой связи
    Class<S5LinkFwdEntity> linkImplClass;
    try {
      linkImplClass = (Class<S5LinkFwdEntity>)Class.forName( aLinkImplClassName );
    }
    catch( ClassNotFoundException e ) {
      throw new TsInternalErrorRtException( e );
    }
    try {
      Constructor<S5LinkFwdEntity> retValue = linkImplClass.getDeclaredConstructor( ResultSet.class );
      retValue.setAccessible( true );
      return retValue;
    }
    catch( NoSuchMethodException | SecurityException e ) {
      throw new TsInternalErrorRtException( e );
    }
  }

  /**
   * Возвращает конструктор копирования прямой связи через исходное {@link IDtoLinkFwd}.
   *
   * @param aLinkImplClass Class класс реализации прямой связи, наследник {@link S5LinkFwdEntity}
   * @return {@link Constructor} конструктор с параметром ({@link IDtoLinkFwd} )
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException не найден класс реализации объекта
   * @throws TsIllegalArgumentRtException не найден метод конструктора с параметром {@link IDtoLinkFwd}
   */
  static Constructor<S5LinkFwdEntity> getConstructorBySource( Class<S5LinkFwdEntity> aLinkImplClass ) {
    TsNullArgumentRtException.checkNull( aLinkImplClass );
    try {
      Constructor<S5LinkFwdEntity> retValue = aLinkImplClass.getDeclaredConstructor( IDtoLinkFwd.class );
      retValue.setAccessible( true );
      return retValue;
    }
    catch( NoSuchMethodException | SecurityException e ) {
      throw new TsInternalErrorRtException( e );
    }
  }

  /**
   * Возвращает класс реализации прямых связей.
   *
   * @param aClassInfo {@link ISkClassInfo} описание класса
   * @param aImplByIds {@link IStringMapEdit}&lt;Class&gt; карта классов реализаций прямой связи по описаниям классов.
   *          <br>
   *          Ключ: описание класса;<br>
   *          Значение: Класс реализации прямой связи
   * @return Class&lt{@link S5LinkFwdEntity}&gt; класс реализации прямой связи
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  static Class<S5LinkFwdEntity> getLinkImplClass( ISkClassInfo aClassInfo,
      IStringMapEdit<Class<S5LinkFwdEntity>> aImplByIds ) {
    TsNullArgumentRtException.checkNulls( aClassInfo, aImplByIds );
    Class<S5LinkFwdEntity> retValue = aImplByIds.findByKey( aClassInfo.id() );
    if( retValue != null ) {
      return retValue;
    }
    // Класс реализации хранения прямой связи
    String linkImplClassName = OP_FWD_LINK_IMPL_CLASS.getValue( aClassInfo.params() ).asString();
    try {
      retValue = (Class<S5LinkFwdEntity>)Class.forName( linkImplClassName );
      aImplByIds.put( aClassInfo.id(), retValue );
      return retValue;
    }
    catch( ClassNotFoundException e ) {
      throw new TsInternalErrorRtException( e );
    }
  }

  /**
   * Создает объект способный быть сохранен в базе данных
   *
   * @param aConstructor {@link Constructor}&lt;{@link S5LinkFwdEntity}&gt; конструктор объекта
   * @param aSource {@link IDtoLinkFwd} исходное описание прямой связи
   * @return {@link S5LinkFwdEntity} созданная прямая связь
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static S5LinkFwdEntity createLinkEntity( Constructor<S5LinkFwdEntity> aConstructor, IDtoLinkFwd aSource ) {
    TsNullArgumentRtException.checkNulls( aConstructor, aSource );
    try {
      return aConstructor.newInstance( aSource );
    }
    catch( Throwable e ) {
      // Неожиданная ошибка создания прямой связи класса
      throw new TsInternalErrorRtException( e );
    }
  }
}
