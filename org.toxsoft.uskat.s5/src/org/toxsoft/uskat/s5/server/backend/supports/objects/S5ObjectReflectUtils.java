package org.toxsoft.uskat.s5.server.backend.supports.objects;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.IS5Resources.*;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;

import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.IDtoObject;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;

/**
 * Вспомогательные методы пакета для работы с отражением java
 *
 * @author mvk
 */
class S5ObjectReflectUtils {

  /**
   * Возвращает конструктор создания объекта через курсор jdbc {@link ResultSet}.
   *
   * @param aObjectImplClassName String полное имя класса реализации объекта, наследник {@link S5ObjectEntity}
   * @return {@link Constructor} конструктор с параметром ({@link ResultSet} )
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException не найден класс реализации объекта
   * @throws TsIllegalArgumentRtException не найден метод конструктора с параметром {@link ResultSet}
   */
  @SuppressWarnings( "unchecked" )
  static Constructor<S5ObjectEntity> getConstructorByResultSet( String aObjectImplClassName ) {
    TsNullArgumentRtException.checkNull( aObjectImplClassName );
    // Определение класса реализации объекта
    Class<S5ObjectEntity> objectImplClass;
    try {
      objectImplClass = (Class<S5ObjectEntity>)Class.forName( aObjectImplClassName );
    }
    catch( ClassNotFoundException e ) {
      // Не найден класс реализации
      throw new TsIllegalArgumentRtException( ERR_OBJECT_IMPL_NOT_FOUND, aObjectImplClassName, cause( e ) );
    }
    try {
      Constructor<S5ObjectEntity> retValue = objectImplClass.getDeclaredConstructor( ResultSet.class );
      retValue.setAccessible( true );
      return retValue;
    }
    catch( NoSuchMethodException | SecurityException e ) {
      // Не найден класс реализации
      throw new TsIllegalArgumentRtException( ERR_OBJECT_CONSTRUCTOR_NOT_FOUND1, aObjectImplClassName, cause( e ) );
    }
  }

  /**
   * Возвращает конструктор копирования объекта через исходное {@link IDtoObject}.
   *
   * @param aObjectImplClass Class класс реализации объекта, наследник {@link S5ObjectEntity}
   * @return {@link Constructor} конструктор с параметром ({@link IDtoObject} )
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException не найден класс реализации объекта
   * @throws TsIllegalArgumentRtException не найден метод конструктора с параметром {@link IDtoObject}
   */
  static Constructor<S5ObjectEntity> getConstructorBySource( Class<S5ObjectEntity> aObjectImplClass ) {
    TsNullArgumentRtException.checkNull( aObjectImplClass );
    try {
      Constructor<S5ObjectEntity> retValue = aObjectImplClass.getDeclaredConstructor( IDtoObject.class );
      retValue.setAccessible( true );
      return retValue;
    }
    catch( NoSuchMethodException | SecurityException e ) {
      // Не найден класс реализации
      throw new TsIllegalArgumentRtException( ERR_OBJECT_CONSTRUCTOR_NOT_FOUND2, aObjectImplClass.getName(),
          cause( e ) );
    }
  }

  /**
   * Возвращает класс реализации объектов
   *
   * @param aClassInfo {@link ISkClassInfo} описание класса
   * @param aImplByIds {@link IStringMapEdit}&lt;Class&gt; карта классов реализаций объектов по описаниям классов. <br>
   *          Ключ: описание класса;<br>
   *          Значение: Класс реализации объекта.
   * @return Class&lt{@link S5ObjectEntity}&gt; класс реализации объекта
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  static Class<S5ObjectEntity> getObjectImplClass( ISkClassInfo aClassInfo,
      IStringMapEdit<Class<S5ObjectEntity>> aImplByIds ) {
    TsNullArgumentRtException.checkNulls( aClassInfo, aImplByIds );
    Class<S5ObjectEntity> retValue = aImplByIds.findByKey( aClassInfo.id() );
    if( retValue != null ) {
      return retValue;
    }
    // Класс реализации хранения значений объекта
    String objectImplClassName = OP_OBJECT_IMPL_CLASS.getValue( aClassInfo.params() ).asString();
    try {
      retValue = (Class<S5ObjectEntity>)Class.forName( objectImplClassName );
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
   * @param aConstructor {@link Constructor}&lt;{@link S5ObjectEntity}&gt; конструктор объекта
   * @param aSource {@link IDtoObject} исходное описание объекта
   * @return {@link S5ObjectEntity} созданный объект
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static S5ObjectEntity createObjectEntity( Constructor<S5ObjectEntity> aConstructor, IDtoObject aSource ) {
    TsNullArgumentRtException.checkNulls( aConstructor, aSource );
    try {
      return aConstructor.newInstance( aSource );
    }
    catch( Throwable e ) {
      // Неожиданная ошибка создания объекта класса
      throw new TsInternalErrorRtException( e, ERR_CREATE_OBJ_UNEXPECTED, aSource.skid(), cause( e ) );
    }
  }
}
