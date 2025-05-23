package org.toxsoft.uskat.s5.server.entities;

import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectID.*;

import java.sql.ResultSet;

import javax.persistence.*;

import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.core.api.linkserv.IDtoLinkRev;
import org.toxsoft.uskat.s5.server.backend.supports.links.S5LinkRevEntity;

/**
 * Реализация интерфейса {@link IDtoLinkRev} способная маппироваться на таблицу базы данных и предназначеная для
 * сохранения обратных связей {@link ISkSession}
 *
 * @author mvk
 */
@Entity
public final class S5SessionLinkRevEntity
    extends S5LinkRevEntity {

  private static final long serialVersionUID = 157157L;

  /**
   * Правый объект связи (определяется в наследнике)
   */
  @ManyToOne( targetEntity = S5SessionEntity.class, optional = false, fetch = FetchType.LAZY )
  @JoinColumns( value = { @JoinColumn( name = FIELD_CLASSID, //
      insertable = false,
      updatable = false,
      nullable = false ),
      @JoinColumn( name = FIELD_STRID, //
          insertable = false,
          updatable = false,
          nullable = false ) },
      foreignKey = @ForeignKey( name = "S5SessionLinkRevEntity_ManyToOne_S5SessionEntity" ) )
  protected S5SessionEntity rightObj;

  /**
   * Конструктор с заданными параметрами (для сохранения связи объекта в базу данных)
   *
   * @param aRightSkid {@link Skid} идентификатор правого объекта связи
   * @param aLinkClassId String идентификатор класса в котором определена связь
   * @param aLinkId String строковый идентификатор связи
   * @param aLeftSkids {@link ISkidList} список идентификаторов левых объектов связи
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5SessionLinkRevEntity( Skid aRightSkid, String aLinkClassId, String aLinkId, ISkidList aLeftSkids ) {
    super( aRightSkid, aLinkClassId, aLinkId, aLeftSkids );
    rightObj = S5SessionEntity.createPrimaryKey( aRightSkid );
  }

  /**
   * Конструктор копирования (для сохранения связи объекта в базу данных)
   *
   * @param aSource {@link IDtoLinkRev} исходная связь
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5SessionLinkRevEntity( IDtoLinkRev aSource ) {
    super( aSource );
    rightObj = S5SessionEntity.createPrimaryKey( aSource.rightSkid() );
  }

  /**
   * Конструктор объекта из текущей записи курсора jdbc (чтение объекта из базы данных)
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5SessionLinkRevEntity( ResultSet aResultSet ) {
    super( aResultSet );
  }

  /**
   * Конструктор по умолчанию для JPA (PojoInstantiator)
   */
  S5SessionLinkRevEntity() {
    super();
  }
}
