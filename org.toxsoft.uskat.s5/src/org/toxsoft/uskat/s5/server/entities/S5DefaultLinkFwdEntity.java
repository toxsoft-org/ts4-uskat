package org.toxsoft.uskat.s5.server.entities;

import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectID.*;

import java.sql.ResultSet;

import javax.persistence.*;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.supports.links.S5LinkFwdEntity;

import ru.uskat.common.dpu.IDpuLinkFwd;

/**
 * Реализация интерфейса {@link IDpuLinkFwd} способная маппироваться на таблицу базы данных и предназначеная для
 * сохранения прямых связей объектов по умолчанию
 *
 * @author mvk
 */
@Entity
public final class S5DefaultLinkFwdEntity
    extends S5LinkFwdEntity {

  private static final long serialVersionUID = 157157L;

  /**
   * Левый объект связи (определяется в наследнике)
   */
  @ManyToOne( targetEntity = S5DefaultObjectEntity.class, optional = false, fetch = FetchType.LAZY )
  @JoinColumns( value = { @JoinColumn( name = FIELD_CLASSID, //
      insertable = false,
      updatable = false,
      nullable = false ),
      @JoinColumn( name = FIELD_STRID, //
          insertable = false,
          updatable = false,
          nullable = false ) },
      foreignKey = @ForeignKey( name = "S5DefaultLinkFwdEntity_ManyToOne_S5DefaultObjectEntity" ) )
  private S5DefaultObjectEntity leftObj;

  /**
   * Конструктор копирования (для сохранения связи объекта в базу данных)
   *
   * @param aSource {@link IDpuLinkFwd} исходная связь с объектами
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5DefaultLinkFwdEntity( IDpuLinkFwd aSource ) {
    super( aSource );
    leftObj = S5DefaultObjectEntity.createPrimaryKey( aSource.leftSkid() );
  }

  /**
   * Конструктор объекта из текущей записи курсора jdbc (чтение объекта из базы данных)
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5DefaultLinkFwdEntity( ResultSet aResultSet ) {
    super( aResultSet );
  }

  /**
   * Конструктор по умолчанию для JPA (PojoInstantiator)
   */
  S5DefaultLinkFwdEntity() {
    super();
  }
}