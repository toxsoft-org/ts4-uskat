package org.toxsoft.uskat.s5.server.entities;

import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectID.*;

import java.sql.ResultSet;

import javax.persistence.*;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.core.api.linkserv.IDtoLinkFwd;
import org.toxsoft.uskat.s5.server.backend.supports.links.S5LinkFwdEntity;

/**
 * Реализация интерфейса {@link IDtoLinkFwd} способная маппироваться на таблицу базы данных и предназначеная для
 * сохранения прямых связей {@link ISkSession}
 *
 * @author mvk
 */
@Entity
public final class S5SessionLinkFwdEntity
    extends S5LinkFwdEntity {

  private static final long serialVersionUID = 157157L;

  /**
   * Левый объект связи (определяется в наследнике)
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
      foreignKey = @ForeignKey( name = "S5SessionLinkFwdEntity_ManyToOne_S5SessionEntity" ) )
  private S5SessionEntity leftObj;

  /**
   * Конструктор копирования (для сохранения связи объекта в базу данных)
   *
   * @param aSource {@link IDtoLinkFwd} исходная связь с объектами
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5SessionLinkFwdEntity( IDtoLinkFwd aSource ) {
    super( aSource );
    leftObj = S5SessionEntity.createPrimaryKey( aSource.leftSkid() );
  }

  /**
   * Конструктор объекта из текущей записи курсора jdbc (чтение объекта из базы данных)
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5SessionLinkFwdEntity( ResultSet aResultSet ) {
    super( aResultSet );
  }

  /**
   * Конструктор по умолчанию для JPA (PojoInstantiator)
   */
  S5SessionLinkFwdEntity() {
    super();
  }
}
