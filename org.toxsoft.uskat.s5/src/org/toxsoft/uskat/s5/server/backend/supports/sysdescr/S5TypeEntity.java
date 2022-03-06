package org.toxsoft.uskat.s5.server.backend.supports.sysdescr;

import static org.toxsoft.uskat.s5.server.backend.supports.sysdescr.S5ClassesSQL.*;

import javax.persistence.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.common.dpu.IDpuSdTypeInfo;
import ru.uskat.common.dpu.impl.IDpuHardConstants;

/**
 * Реализация интерфейса {@link IDpuSdTypeInfo} способная маппироваться на таблицу базы данных
 *
 * @author mvk
 */
@Entity
@NamedQueries( { @NamedQuery( name = QUERY_NAME_GET_TYPES, query = QUERY_GET_TYPES ), } )
public class S5TypeEntity
    extends S5DpuBaseEntity
    implements IDpuSdTypeInfo {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор копирования
   *
   * @param aSource {@link IDpuSdTypeInfo} исходное описание типа
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5TypeEntity( IDpuSdTypeInfo aSource ) {
    super( aSource.id(), aSource.params() );
  }

  /**
   * Конструктор по умолчанию (требование hibernate/JPA)
   */
  S5TypeEntity() {
    super( TsLibUtils.EMPTY_STRING, TsLibUtils.EMPTY_STRING, TsLibUtils.EMPTY_STRING );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IDpuSdTypeInfo
  //
  @Override
  public EAtomicType atomicType() {
    return IDpuHardConstants.OP_ATOMIC_TYPE.getValue( params() ).asValobj();
  }
}
