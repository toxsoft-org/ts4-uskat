package org.toxsoft.uskat.alarms.s5.supports;

import java.io.Serializable;

import javax.persistence.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.uskat.alarms.lib.flacon.ISkAlarmFlacon;

/**
 * Реализация хранимого флакона.
 *
 * @author dima
 */
@Entity
public class S5AlarmFlaconEntity
    implements ISkAlarmFlacon, Serializable {

  private static final long serialVersionUID = 157157L;
  @Id
  @GeneratedValue
  @Column( name = "flacon_id" )
  private Long              flaconId;
  /**
   * Содержимое флакона.
   */
  @Column( name = "content" )
  private String            content;

  /**
   * Конструктор без параметров
   */
  protected S5AlarmFlaconEntity() {
    // nop
  }

  /**
   * Конструктор по {@link ISkAlarmFlacon}
   *
   * @param aSkAlarmFlacon оригинальный флакон
   */
  public S5AlarmFlaconEntity( ISkAlarmFlacon aSkAlarmFlacon ) {
    content = OptionSetKeeper.KEEPER.ent2str( aSkAlarmFlacon.params() );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ISkAlarmFlacon
  //
  @Override
  public IOptionSet params() {
    return OptionSetKeeper.KEEPER.str2ent( content );
  }
}
