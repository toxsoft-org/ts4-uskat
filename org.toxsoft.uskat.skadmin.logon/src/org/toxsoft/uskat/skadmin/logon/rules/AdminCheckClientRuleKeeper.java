package org.toxsoft.uskat.skadmin.logon.rules;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.std.StringListKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.uskat.s5.server.statistics.EStatisticInterval;

/**
 * Хранитель объектов типа {@link IAdminCheckClientRule}.
 *
 * @author mvk
 */
public class AdminCheckClientRuleKeeper
    extends AbstractEntityKeeper<IAdminCheckClientRule> {

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static IEntityKeeper<IAdminCheckClientRule> KEEPER = new AdminCheckClientRuleKeeper();

  private AdminCheckClientRuleKeeper() {
    // Авт.обрамление скобками, null объект не используется
    super( IAdminCheckClientRule.class, EEncloseMode.ENCLOSES_BASE_CLASS, null );
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса AbstractEntityKeeper
  //

  @Override
  protected void doWrite( IStrioWriter aSw, IAdminCheckClientRule aEntity ) {
    // Тип правила
    aSw.writeAsIs( aEntity.type().id() );
    aSw.writeSeparatorChar();
    // Начало действия правила
    aSw.writeInt( aEntity.startTime() );
    aSw.writeSeparatorChar();
    // Завершение действия правила
    aSw.writeInt( aEntity.endTime() );
    aSw.writeSeparatorChar();
    // ip-адрес
    aSw.writeAsIs( aEntity.ip() );
    aSw.writeSeparatorChar();
    // Порт
    aSw.writeInt( aEntity.port() );
    aSw.writeSeparatorChar();
    // login
    aSw.writeAsIs( aEntity.login() );
    aSw.writeSeparatorChar();
    // Особенности клиента
    StringListKeeper.KEEPER.write( aSw, aEntity.clientFeatureIds() );
    aSw.writeSeparatorChar();
    for( EStatisticInterval interval : EStatisticInterval.values() ) {
      aSw.writeAsIs( "[" ); //$NON-NLS-1$
      // sendedMin
      aSw.writeInt( aEntity.sendedMin( interval ) );
      aSw.writeSeparatorChar();
      // sendedMax
      aSw.writeInt( aEntity.sendedMax( interval ) );
      aSw.writeSeparatorChar();
      // receivedMin
      aSw.writeInt( aEntity.receivedMin( interval ) );
      aSw.writeSeparatorChar();
      // receivedMax
      aSw.writeInt( aEntity.receivedMax( interval ) );
      aSw.writeSeparatorChar();
      // queriesMin
      aSw.writeInt( aEntity.queriesMin( interval ) );
      aSw.writeSeparatorChar();
      // queriesMax
      aSw.writeInt( aEntity.queriesMax( interval ) );
      aSw.writeSeparatorChar();
      // errorsMax
      aSw.writeInt( aEntity.errorsMax( interval ) );
      aSw.writeAsIs( "]" ); //$NON-NLS-1$
      aSw.writeSeparatorChar();
      if( interval == EStatisticInterval.DAY ) {
        break;
      }
    }
  }

  @Override
  protected IAdminCheckClientRule doRead( IStrioReader aSr ) {
    AdminCheckClientRule rule = new AdminCheckClientRule();
    // Тип правила
    rule.setType( EClientRuleType.findById( aSr.readIdName() ) );
    aSr.ensureSeparatorChar();
    // Начало действия правила
    rule.setStartTime( aSr.readInt() );
    aSr.ensureSeparatorChar();
    // Завершение действия правила
    rule.setEndTime( aSr.readInt() );
    aSr.ensureSeparatorChar();
    // ip-адрес
    rule.setIp( aSr.readUntilDelimiter() );
    aSr.ensureSeparatorChar();
    // Порт
    rule.setPort( aSr.readInt() );
    aSr.ensureSeparatorChar();
    // login
    rule.setLogin( aSr.readUntilDelimiter() );
    aSr.ensureSeparatorChar();
    // Особенности клиента
    rule.addClientFeatureIds( StringListKeeper.KEEPER.read( aSr ) );
    aSr.ensureSeparatorChar();
    for( EStatisticInterval interval : EStatisticInterval.values() ) {
      aSr.ensureChar( '[' );
      // sendedMin
      rule.setSendedMin( interval, aSr.readInt() );
      aSr.ensureSeparatorChar();
      // sendedMax
      rule.setSendedMax( interval, aSr.readInt() );
      aSr.ensureSeparatorChar();
      // receivedMin
      rule.setReceivedMin( interval, aSr.readInt() );
      aSr.ensureSeparatorChar();
      // receivedMax
      rule.setReceivedMax( interval, aSr.readInt() );
      aSr.ensureSeparatorChar();
      // queriesMin
      rule.setQueriesMin( interval, aSr.readInt() );
      aSr.ensureSeparatorChar();
      // queriesMax
      rule.setQueriesMax( interval, aSr.readInt() );
      aSr.ensureSeparatorChar();
      // errorsMax
      rule.setErrorsMax( interval, aSr.readInt() );
      aSr.ensureChar( ']' );
      aSr.ensureSeparatorChar();
      if( interval == EStatisticInterval.DAY ) {
        break;
      }
    }
    return rule;
  }

}
