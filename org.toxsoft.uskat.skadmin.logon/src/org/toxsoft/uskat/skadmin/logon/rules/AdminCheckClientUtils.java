package org.toxsoft.uskat.skadmin.logon.rules;

import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.skadmin.logon.rules.IAdminResources.*;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.Calendar;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.bricks.strio.chario.ICharOutputStream;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.CharOutputStreamWriter;
import org.toxsoft.core.tslib.bricks.strio.impl.StrioWriter;
import org.toxsoft.core.tslib.bricks.validator.IValResList;
import org.toxsoft.core.tslib.bricks.validator.impl.ValResList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.utils.errors.TsIoRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.common.sessions.IS5SessionInfo;
import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;
import org.toxsoft.uskat.s5.server.statistics.*;

/**
 * Вспомогательные методы для проверки сессий клиентов
 *
 * @author mvk
 */
public class AdminCheckClientUtils {

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Сформировать новый файл контроля сессий клиентов
   *
   * @param aFile File файл для сохранения правил проверки клиентов
   * @param aSessions {@link IList} список сессий клиентов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void writeCheckFile( File aFile, IList<IS5SessionInfo> aSessions ) {
    TsNullArgumentRtException.checkNulls( aFile, aSessions );
    // Формирование списка правил
    IListEdit<IAdminCheckClientRule> rules = new ElemArrayList<>( aSessions.size() );
    for( IS5SessionInfo session : aSessions ) {
      AdminCheckClientRule rule = new AdminCheckClientRule();
      rule.setType( EClientRuleType.MAY_BE );
      rule.setStartTime( -1 );
      rule.setEndTime( -1 );
      rule.setIp( session.remoteAddress() );
      rule.setPort( session.remotePort() );
      rule.setLogin( session.login() );
      rule.addClientFeatureIds( new StringArrayList( "s5box" ) ); //$NON-NLS-1$
      for( EStatisticInterval interval : EStatisticInterval.values() ) {
        rule.setSendedMin( interval, -1 );
        rule.setReceivedMin( interval, -1 );
        rule.setQueriesMin( interval, -1 );
        if( interval == EStatisticInterval.ALL ) {
          rule.setSendedMax( interval, -1 );
          rule.setReceivedMax( interval, -1 );
          rule.setQueriesMax( interval, -1 );
          rule.setErrorsMax( interval, -1 );
          continue;
        }
        rule.setSendedMax( interval, 0 );
        rule.setReceivedMax( interval, 0 );
        rule.setQueriesMax( interval, 0 );
        rule.setErrorsMax( interval, 0 );
      }
      rules.add( rule );
    }
    try( FileWriter fw = new FileWriter( aFile ) ) {
      ICharOutputStream chOut = new CharOutputStreamWriter( fw );
      IStrioWriter sw = new StrioWriter( chOut );
      // Запись комментария
      sw.writeAsIs( MSG_COMMENT );
      // Каждое правило на новой строке
      AdminCheckClientRuleKeeper.KEEPER.writeColl( sw, rules, true );
    }
    catch( IOException e ) {
      throw new TsIoRtException( e );
    }
  }

  /**
   * Провести проверку сессий клиентов
   *
   * @param aTime ZonedDateTime время (мсек от начала эпохи) валидации
   * @param aFile File файл для сохранения правил проверки клиентов
   * @param aSessions {@link IList}&lt;{@link IS5SessionInfo}&gt; список сессий клиентов
   * @param aUnsatisfied {@link IListEdit}&lt;{@link IS5SessionInfo}&gt; список не выполненных правил
   * @return {@link IList}&lt;{@link IValResList}&gt; список валидаций для каждой сессии. Порядок соответствует порядку
   *         aSessions
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static IList<IValResList> validation( ZonedDateTime aTime, File aFile, IList<IS5SessionInfo> aSessions,
      IListEdit<IAdminCheckClientRule> aUnsatisfied ) {
    TsNullArgumentRtException.checkNulls( aFile, aSessions, aUnsatisfied );
    // Время сервера в милисекндах (mvk: ??? зона учитывается ???)
    long serverTime = aTime.toInstant().toEpochMilli();
    // Внутрисуточное время
    // int intradayTime = intradayTime( aTime );
    int intradayTime = intradayTime( serverTime );
    // Чтение правил в файл.
    IList<IAdminCheckClientRule> rules = AdminCheckClientRuleKeeper.KEEPER.readColl( aFile );
    // Список текущих правил
    IListEdit<IAdminCheckClientRule> activeRules = new ElemArrayList<>( rules.size() );
    // Список правил требующих обязательного выполнения
    IListEdit<IAdminCheckClientRule> unsatisfiedRules = new ElemArrayList<>( rules.size() );
    // Формирование списков текущих обязательных правил
    for( int index = 0, n = rules.size(); index < n; index++ ) {
      IAdminCheckClientRule rule = rules.get( index );
      int startTime = rule.startTime();
      int endTime = rule.endTime();
      if( startTime >= 0 && startTime > intradayTime ) {
        // Правило действует позже указанного времени
        continue;
      }
      if( endTime >= 0 && endTime < intradayTime ) {
        // Правило действует раньше указанного времени
        continue;
      }
      // Активное правило
      activeRules.add( rule );
      if( rule.type() == EClientRuleType.MUST_BE ) {
        // Обязательное правило
        unsatisfiedRules.add( rule );
      }
    }
    // Валидация
    IListEdit<IValResList> retValue = new ElemArrayList<>( aSessions.size() );
    for( int index = 0, n = aSessions.size(); index < n; index++ ) {
      IS5SessionInfo session = aSessions.get( index );
      long workTime = serverTime - session.openTime();
      ValResList resultList = new ValResList();
      retValue.add( resultList );
      // Удаление из обязательных правил все что совпадает с найденной сессией
      removeSatisfiedRules( session, unsatisfiedRules );
      // Поиск правила для проверки
      IAdminCheckClientRule rule = findRule( activeRules, session );
      if( rule == null ) {
        // Для клиента нет правил проверки
        continue;
      }
      // Обработка статистики
      IS5Statistic statistic = session.statistics();
      for( IS5StatisticInterval interval1 : statistic.intervals() ) {
        if( workTime < interval1.milli() ) {
          // Клиент еще не наработал статистику для данного интервала
          continue;
        }
        String id = interval1.id();
        EStatisticInterval predefInterval = EStatisticInterval.findByIdOrNull( id );
        if( predefInterval == null ) {
          // Неизвестный интервал
          continue;
        }
        IOptionSet params = statistic.params( predefInterval );
        int sended = 0;
        int recevied = 0;
        int errors = 0;
        if( params.hasKey( IS5ServerHardConstants.STAT_SESSION_SENDED.id() ) ) {
          sended = params.getInt( IS5ServerHardConstants.STAT_SESSION_SENDED.id() );
        }
        if( params.hasKey( IS5ServerHardConstants.STAT_SESSION_RECEVIED.id() ) ) {
          recevied = params.getInt( IS5ServerHardConstants.STAT_SESSION_RECEVIED.id() );
        }
        if( params.hasKey( IS5ServerHardConstants.STAT_SESSION_ERRORS.id() ) ) {
          errors = params.getInt( IS5ServerHardConstants.STAT_SESSION_ERRORS.id() );
        }

        if( rule.sendedMin( predefInterval ) >= 0 && rule.sendedMin( predefInterval ) > sended ) {
          // Мало отправлено пакетов клиенту
          resultList.add( warn( "%s[low sended=%d]", id, Integer.valueOf( sended ) ) ); //$NON-NLS-1$
        }
        if( rule.sendedMax( predefInterval ) >= 0 && rule.sendedMax( predefInterval ) < sended ) {
          // Много отправлено пакетов клиенту
          resultList.add( warn( "%s[high sended=%d]", id, Integer.valueOf( sended ) ) ); //$NON-NLS-1$
        }
        if( rule.receivedMin( predefInterval ) >= 0 && rule.receivedMin( predefInterval ) > recevied ) {
          // Мало получено пакетов от клиента
          resultList.add( warn( "%s[low received=%d]", id, Integer.valueOf( recevied ) ) ); //$NON-NLS-1$
        }
        if( rule.receivedMax( predefInterval ) >= 0 && rule.receivedMax( predefInterval ) < recevied ) {
          // Много получено пакетов от клиента
          resultList.add( warn( "%s[high received=%d]", id, Integer.valueOf( recevied ) ) ); //$NON-NLS-1$
        }
        // if( rule.queriesMin( interval ) >= 0 && rule.queriesMin( interval ) > statistic.queries( interval ) ) {
        // // Мало запросов от клиента
        // resultList.add( warn( "%s[low queries=%d]", id, Integer.valueOf( statistic.queries( interval ) ) ) );
        // //$NON-NLS-1$
        // }
        // if( rule.queriesMax( interval ) >= 0 && rule.queriesMax( interval ) < statistic.queries( interval ) ) {
        // // Много запросов от клиента
        // resultList.add( warn( "%s[high queries=%d]", id, Integer.valueOf( statistic.queries( interval ) ) ) );
        // //$NON-NLS-1$
        // }
        if( rule.errorsMax( predefInterval ) >= 0 && rule.errorsMax( predefInterval ) < errors ) {
          // Много ошибок от клиента
          resultList.add( warn( "%s[high errors=%d]", id, Integer.valueOf( errors ) ) ); //$NON-NLS-1$
        }
      }
    }
    aUnsatisfied.addAll( unsatisfiedRules );
    return retValue;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает количество секунд с начала суток по указанной метке времени
   *
   * @param aTimestamp long метка времени (мсек с начала эпохи)
   * @return int количество секунд от начала суток
   */
  private static int intradayTime( long aTimestamp ) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis( aTimestamp );
    int hours = calendar.get( Calendar.HOUR_OF_DAY );
    int mins = calendar.get( Calendar.MINUTE );
    int seconds = calendar.get( Calendar.SECOND );
    return 60 * (60 * hours + mins) + seconds;
  }

  /**
   * Удаление из списка правил, всех правил которые были выполнены сессией пользователя
   *
   * @param aSession {@link IS5SessionInfo} сессия клиента
   * @param aRules {@link IListEdit}&lt; {@link IAdminCheckClientRule}&gt; редактируемый список правил
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void removeSatisfiedRules( IS5SessionInfo aSession, IListEdit<IAdminCheckClientRule> aRules ) {
    TsNullArgumentRtException.checkNulls( aSession, aRules );
    for( int index = aRules.size() - 1; index >= 0; index-- ) {
      IAdminCheckClientRule rule = aRules.get( index );
      if( !rule.ip().equals( EMPTY_STRING ) && !rule.ip().equals( aSession.remoteAddress() ) ) {
        // Не соответствие ip-адреса
        continue;
      }
      if( rule.port() >= 0 && rule.port() != aSession.remotePort() ) {
        // Не соответствие порта
        continue;
      }
      if( !rule.login().equals( EMPTY_STRING ) && !rule.login().equals( aSession.login() ) ) {
        // Не соответствие логина клиента
        continue;
      }
      // Несоответствие типа клиента
      if( rule.clientFeatureIds().size() > 0 && !hasFeaturesInSession( rule.clientFeatureIds(), aSession ) ) {
        // Не соответствие особенностей клиента
        continue;
      }
      // Правило выполнено
      aRules.removeByIndex( index );
    }
  }

  /**
   * Проводит поиск правила которое точнее всего определяется сессией клиента
   *
   * @param aRules {@link IList}&lt; {@link IAdminCheckClientRule}&gt; список правил
   * @param aSession {@link IS5SessionInfo} сессия клиента
   * @return {@link IAdminCheckClientRule} найденное правило. null: правило не найдено
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static IAdminCheckClientRule findRule( IList<IAdminCheckClientRule> aRules, IS5SessionInfo aSession ) {
    TsNullArgumentRtException.checkNulls( aRules, aSession );
    IAdminCheckClientRule retValue = null;
    for( int index = 0, n = aRules.size(); index < n; index++ ) {
      IAdminCheckClientRule rule = aRules.get( index );
      if( !rule.ip().equals( EMPTY_STRING ) && !rule.ip().equals( aSession.remoteAddress() ) ) {
        // Не соответствие ip-адреса
        continue;
      }
      if( rule.port() >= 0 && rule.port() != aSession.remotePort() ) {
        // Не соответствие порта
        continue;
      }
      if( !rule.login().equals( EMPTY_STRING ) && !rule.login().equals( aSession.login() ) ) {
        // Не соответствие логина клиента
        continue;
      }
      if( rule.clientFeatureIds().size() > 0 && !hasFeaturesInSession( rule.clientFeatureIds(), aSession ) ) {
        // Не соответствие особенностей клиента
        continue;
      }
      if( retValue == null ) {
        // Найдено первое подходящее правило
        retValue = rule;
        continue;
      }
      // Если найдено несколько подходящих правил, то выбор делается по следующему ранжиру:
      // 1. Более точное определение ip-адреса
      // 2. Более точное определение типа клиента
      // 3. Более точное определение логина
      // 4. Более точное определение порта
      // 5. Более короткое время действия правила
      if( retValue.ip().equals( EMPTY_STRING ) && !rule.ip().equals( EMPTY_STRING ) ) {
        retValue = rule;
        continue;
      }
      if( retValue.clientFeatureIds().size() == 0 && rule.clientFeatureIds().size() > 0 ) {
        retValue = rule;
        continue;
      }
      if( retValue.login().equals( EMPTY_STRING ) && !rule.login().equals( EMPTY_STRING ) ) {
        retValue = rule;
        continue;
      }
      if( retValue.port() < 0 && rule.port() >= 0 ) {
        retValue = rule;
        continue;
      }
      int duration0 =
          (retValue.endTime() < 0 ? 24 * 60 * 60 : retValue.endTime()) - Math.max( retValue.startTime(), 0 );
      int duration1 = (rule.endTime() < 0 ? 24 * 60 * 60 : rule.endTime()) - Math.max( rule.startTime(), 0 );
      if( duration0 > duration1 ) {
        retValue = rule;
        continue;
      }
    }
    return retValue;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Проверяет есть ли хотя бы одна особенность в сессии клиента
   *
   * @param aFeatureIds {@link IStringList} список идентификаторов (ИД-пути) особенностей
   * @param aSession {@link IS5SessionInfo} сессия клиента
   * @return <b>true</b> особенности найдены; <b>false</b> особенности не найдены
   */
  private static boolean hasFeaturesInSession( IStringList aFeatureIds, IS5SessionInfo aSession ) {
    // TsNullArgumentRtException.checkNulls( aFeatureIds, aSession );
    // IList<ITypedOptionSetEdit<IFeatureOptions>> sessionFeatures = IClientInfoOptions.FEATURES.get( aSession.client()
    // );
    // for( ITypedOptionSetEdit<IFeatureOptions> feature : sessionFeatures ) {
    // if( aFeatureIds.hasElem( IFeatureOptions.VALUE.getValue( feature ).asString() ) == true ) {
    // return true;
    // }
    // }
    return false;
  }

}
