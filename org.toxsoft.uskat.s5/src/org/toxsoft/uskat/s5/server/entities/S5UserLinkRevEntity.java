package org.toxsoft.uskat.s5.server.entities;

import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectID.*;

import java.sql.ResultSet;

import javax.persistence.*;

import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.supports.links.S5LinkRevEntity;

import ru.uskat.common.dpu.IDpuLinkRev;
import ru.uskat.core.api.users.ISkUser;

/**
 * Реализация интерфейса {@link IDpuLinkRev} способная маппироваться на таблицу базы данных и предназначеная для
 * сохранения обратных связей {@link ISkUser}
 *
 * @author mvk
 */
@Entity
public final class S5UserLinkRevEntity
    extends S5LinkRevEntity {

  private static final long serialVersionUID = 157157L;

  /**
   * Правый объект связи (определяется в наследнике)
   */
  @ManyToOne( targetEntity = S5UserEntity.class, optional = false, fetch = FetchType.LAZY )
  @JoinColumns( value = { @JoinColumn( name = FIELD_CLASSID, //
      insertable = false,
      updatable = false,
      nullable = false ),
      @JoinColumn( name = FIELD_STRID, //
          insertable = false,
          updatable = false,
          nullable = false ) },
      foreignKey = @ForeignKey( name = "S5UserLinkRevEntity_ManyToOne_S5UserEntity" ) )
  protected S5UserEntity rightObj;

  /**
   * Конструктор с заданными параметрами (для сохранения связи объекта в базу данных)
   *
   * @param aRightSkid {@link Skid} идентификатор правого объекта связи
   * @param aLinkClassId String идентификатор класса в котором определена связь
   * @param aLinkId String строковый идентификатор связи
   * @param aLeftSkids {@link ISkidList} список идентификаторов левых объектов связи
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5UserLinkRevEntity( Skid aRightSkid, String aLinkClassId, String aLinkId, ISkidList aLeftSkids ) {
    super( aRightSkid, aLinkClassId, aLinkId, aLeftSkids );
    rightObj = S5UserEntity.createPrimaryKey( aRightSkid );
  }

  /**
   * Конструктор копирования (для сохранения связи объекта в базу данных)
   *
   * @param aSource {@link IDpuLinkRev} исходная связь
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5UserLinkRevEntity( IDpuLinkRev aSource ) {
    super( aSource );
    rightObj = S5UserEntity.createPrimaryKey( aSource.rightSkid() );
  }

  /**
   * Конструктор объекта из текущей записи курсора jdbc (чтение объекта из базы данных)
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5UserLinkRevEntity( ResultSet aResultSet ) {
    super( aResultSet );
  }

  /**
   * Конструктор по умолчанию для JPA (PojoInstantiator)
   */
  S5UserLinkRevEntity() {
    super();
  }
}
