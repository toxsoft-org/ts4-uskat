package org.toxsoft.uskat.s5.server.entities;

import java.sql.ResultSet;

import javax.persistence.Entity;

import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.objserv.IDtoObject;
import org.toxsoft.uskat.core.api.users.ISkUser;
import org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectEntity;
import org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectID;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.S5ClassEntity;

/**
 * Реализация интерфейса {@link IDtoObject} способная маппироваться на таблицу базы данных и предназначеная для
 * сохранения объектов {@link ISkUser}
 *
 * @author mvk
 */
@Entity
public final class S5UserEntity
    extends S5ObjectEntity {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор копирования (для сохранения объекта в базу данных)
   *
   * @param aSource {@link IDtoObject} исходные данные объекта
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5UserEntity( IDtoObject aSource ) {
    super( aSource );
  }

  /**
   * Конструктор объекта из текущей записи курсора jdbc (чтение объекта из базы данных)
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5UserEntity( ResultSet aResultSet ) {
    super( aResultSet );
  }

  /**
   * Конструктор по первичному ключу
   *
   * @param aId {@link S5ObjectID} первичный составной ключ
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5UserEntity( S5ObjectID aId ) {
    super( aId );
  }

  /**
   * Конструктор по умолчанию для JPA (PojoInstantiator)
   */
  S5UserEntity() {
    super();
  }

  // ------------------------------------------------------------------------------------
  // Служебные методы
  //
  /**
   * Создание PRIMARY ключа на описание класса в базе данных
   * <p>
   * Ключ используется для организации связанности в базе данных, например, между таблицами {@link S5ObjectEntity} и
   * {@link S5ClassEntity}
   *
   * @param aSkid {@link Skid} идентификатор объекта
   * @return {@link S5UserEntity} PRIMARY-ключ
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static S5UserEntity createPrimaryKey( Skid aSkid ) {
    TsNullArgumentRtException.checkNull( aSkid );
    return new S5UserEntity( new S5ObjectID( aSkid ) );
  }
}
