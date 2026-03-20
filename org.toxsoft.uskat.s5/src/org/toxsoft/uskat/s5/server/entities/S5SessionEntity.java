package org.toxsoft.uskat.s5.server.entities;

import java.sql.*;

import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.s5.server.backend.supports.objects.*;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.*;

import jakarta.persistence.*;

/**
 * Реализация интерфейса {@link IDtoObject} способная маппироваться на таблицу базы данных и предназначеная для
 * сохранения объектов {@link ISkSession}
 *
 * @author mvk
 */
@Entity
public final class S5SessionEntity
    extends S5ObjectEntity {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор копирования (для сохранения объекта в базу данных)
   *
   * @param aSource {@link IDtoObject} исходные данные объекта
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5SessionEntity( IDtoObject aSource ) {
    super( aSource );
  }

  /**
   * Конструктор объекта из текущей записи курсора jdbc (чтение объекта из базы данных)
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5SessionEntity( ResultSet aResultSet ) {
    super( aResultSet );
  }

  /**
   * Конструктор по первичному ключу
   *
   * @param aId {@link S5ObjectID} первичный составной ключ
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5SessionEntity( S5ObjectID aId ) {
    super( aId );
  }

  /**
   * Конструктор по умолчанию для JPA (PojoInstantiator)
   */
  S5SessionEntity() {
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
   * @return {@link S5SessionEntity} PRIMARY-ключ
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static S5SessionEntity createPrimaryKey( Skid aSkid ) {
    TsNullArgumentRtException.checkNull( aSkid );
    return new S5SessionEntity( new S5ObjectID( aSkid ) );
  }
}
