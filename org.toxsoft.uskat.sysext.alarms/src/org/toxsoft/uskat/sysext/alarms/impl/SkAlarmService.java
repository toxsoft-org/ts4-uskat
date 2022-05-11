package org.toxsoft.uskat.sysext.alarms.impl;

import static org.toxsoft.uskat.sysext.alarms.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.IMapEdit;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;
import org.toxsoft.uskat.sysext.alarms.addon.ISkBackendAddonAlarm;
import org.toxsoft.uskat.sysext.alarms.api.*;
import org.toxsoft.uskat.sysext.alarms.api.flacon.ISkAlarmFlacon;

import ru.uskat.common.ISkHardConstants;
import ru.uskat.common.dpu.impl.DpuObject;
import ru.uskat.core.api.users.ISkUser;
import ru.uskat.core.api.users.ISkUserService;
import ru.uskat.core.devapi.IDevCoreApi;
import ru.uskat.core.impl.AbstractSkService;
import ru.uskat.core.impl.SkUserService;

/**
 * Реализация службы {@link ISkAlarmService}.
 *
 * @author mvk
 */
public class SkAlarmService
    extends AbstractSkService
    implements ISkAlarmService {

  /**
   * Логин пользователя: "служба алармов"
   */
  public static final String ALARM_USER_LOGIN = ISkAlarmService.SERVICE_ID;

  /**
   * Пароль пользователя: "служба алармов"
   */
  private static final String ALARM_USER_PASSWD = "alarm.passwd" + String.valueOf( Math.random() ); //$NON-NLS-1$

  /**
   * Класс для реализации {@link ISkAlarmService#eventer()}.
   */
  class Eventer
      extends AbstractServiceEventsFiringSupport<ISkAlarmServiceListener>
      implements ISkAlarmEventsFiringSupport {

    private FilterFactoriesRegistry                                         alarmFilterRegistry =
        new FilterFactoriesRegistry();
    private IMapEdit<ISkAlarmServiceListener, IListEdit<IPolyFilterParams>> listeners           = new ElemMap<>();
    private IListEdit<ISkAlarm>                                             onAlarms            =
        new ElemLinkedList<>();
    private IListEdit<Pair<ISkAlarm, ISkAnnounceThreadHistoryItem>>         onAlarmsChanges     =
        new ElemLinkedList<>();

    Eventer() {
      SkAlarmUtils.registerAlarmFilters( alarmFilterRegistry );
    }

    @Override
    public void addListener( ISkAlarmServiceListener aListener ) {
      super.addListener( aListener );
      SkAlarmUtils.addListenerSelection( listeners, aListener, IPolyFilterParams.NULL );
    }

    @Override
    public void addListener( ISkAlarmServiceListener aListener, IPolyFilterParams aSelection ) {
      super.addListener( aListener );
      SkAlarmUtils.addListenerSelection( listeners, aListener, aSelection );
    }

    @Override
    public void removeListener( ISkAlarmServiceListener aListener ) {
      super.removeListener( aListener );
    }

    @Override
    protected boolean doIsPendingEvents() {
      return (onAlarms.size() > 0 || onAlarmsChanges.size() > 0);
    }

    @Override
    protected void doFirePendingEvents() {
      if( onAlarms.size() > 0 ) {
        for( ISkAlarmServiceListener l : listeners() ) {
          for( ISkAlarm alarm : onAlarms ) {
            try {
              for( IPolyFilterParams selection : listeners.getByKey( l ) ) {
                IPolyFilter filter = IPolyFilter.NONE;
                if( selection != IPolyFilterParams.NULL ) {
                  filter = alarmFilterRegistry.createPolyFilter( selection );
                }
                if( filter.accept( alarm ) ) {
                  l.onAlarm( alarm );
                  break;
                }
              }
            }
            catch( Exception ex ) {
              LoggerUtils.errorLogger().error( ex );
            }
          }
        }
        onAlarms.clear();
      }
      if( onAlarmsChanges.size() > 0 ) {
        for( ISkAlarmServiceListener l : listeners() ) {
          for( Pair<ISkAlarm, ISkAnnounceThreadHistoryItem> change : onAlarmsChanges ) {
            try {
              for( IPolyFilterParams selection : listeners.getByKey( l ) ) {
                IPolyFilter filter = IPolyFilter.NONE;
                if( selection != IPolyFilterParams.NULL ) {
                  filter = alarmFilterRegistry.createPolyFilter( selection );
                }
                ISkAlarm alarm = change.left();
                if( filter.accept( alarm ) ) {
                  l.onAlarmStateChanged( alarm, change.right() );
                  break;
                }
              }
            }
            catch( Exception ex ) {
              LoggerUtils.errorLogger().error( ex );
            }
          }
        }
        onAlarmsChanges.clear();
      }
    }

    @Override
    protected void doClearPendingEvents() {
      onAlarms.clear();
      onAlarmsChanges.clear();
    }

    void addOnAlarm( ISkAlarm aAlarm ) {
      onAlarms.add( aAlarm );
    }

    void addOnAlarmChange( ISkAlarm aAlarm, ISkAnnounceThreadHistoryItem aChange ) {
      onAlarmsChanges.add( new Pair<>( aAlarm, aChange ) );
    }
  }

  private final ISkBackendAddonAlarm backend;
  private final Eventer              eventer = new Eventer();

  /**
   * Конструктор службы.
   *
   * @param aCoreApi {@link IDevCoreApi} API ядра uskat
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public SkAlarmService( IDevCoreApi aCoreApi ) {
    super( ISkAlarmService.SERVICE_ID, aCoreApi );
    backend = aCoreApi.getBackendAddon( ISkBackendAddonAlarm.S5_BACKEND_ALARMS_ID, ISkBackendAddonAlarm.class );
    // Регистрация обработчиков сообщений бекенда
    aCoreApi.registerMessageHandler( SkAlarmMessageWhenOn.WHEN_ALARM_ON, //
        new SkAlarmMessageWhenOn() {

          @Override
          protected void doWhenAlarmOn( ISkAlarm aAlarm ) {
            eventer.pauseFiring();
            try {
              eventer.addOnAlarm( aAlarm );
            }
            finally {
              eventer.resumeFiring( true );
            }
          }
        } );
    aCoreApi.registerMessageHandler( SkAlarmMessageWhenStateChanged.WHEN_ALARM_STATE_CHANGED,
        new SkAlarmMessageWhenStateChanged() {

          @Override
          protected void doWhenAlarmStateChanged( ISkAlarm aAlarm, ISkAnnounceThreadHistoryItem aThreadItem ) {
            eventer.pauseFiring();
            try {
              eventer.addOnAlarmChange( aAlarm, aThreadItem );
            }
            finally {
              eventer.resumeFiring( true );
            }
          }
        } );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов класса AbstractSkService
  //
  @Override
  protected void doInit( ITsContextRo aArgs ) {
    ISkUserService us = coreApi().userService();
    // Создание системного пользователя управления тревогами
    if( us.find( ALARM_USER_LOGIN ) == null ) {
      IOptionSetEdit attrs = new OptionSet();
      attrs.setStr( ISkHardConstants.AID_NAME, STR_N_ALARM_USER );
      attrs.setStr( ISkHardConstants.AID_DESCRIPTION, STR_D_ALARM_USER );
      attrs.setStr( ISkUser.ATRID_PASSWORD, SkUserService.getPasswordHashCode( ALARM_USER_PASSWD ) );
      attrs.setBool( ISkUser.ATRID_IS_ENABLED, true );
      attrs.setBool( ISkUser.ATRID_IS_HIDDEN, false );
      us.defineUser( new DpuObject( new Skid( ISkUser.CLASS_ID, ALARM_USER_LOGIN ), attrs ) );
    }
  }

  @Override
  protected void doChangeBackendState( boolean aActive ) {
    // Установка/разрыв связи с сервером
    // nop
  }

  @Override
  protected void doClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса ISkAlarmService
  //
  @Override
  public IStridablesList<ISkAlarmDef> listAlarmDefs() {
    return backend.listAlarmDefs();
  }

  @Override
  public ISkAlarmDef findAlarmDef( String aAlarmDefId ) {
    TsNullArgumentRtException.checkNull( aAlarmDefId );
    return backend.findAlarmDef( aAlarmDefId );
  }

  @Override
  public void registerAlarmDef( ISkAlarmDef aAlarmDef ) {
    TsNullArgumentRtException.checkNull( aAlarmDef );
    backend.registerAlarmDef( aAlarmDef );
  }

  @Override
  public ISkAlarm generateAlarm( String aAlarmDefId, Skid aAuthorId, Skid aUserId, byte aSublevel,
      ISkAlarmFlacon aSkAlarmFlacon ) {
    TsNullArgumentRtException.checkNulls( aAlarmDefId, aAuthorId, aUserId, aSkAlarmFlacon );
    return backend.generateAlarm( aAlarmDefId, aAuthorId, aUserId, aSublevel, aSkAlarmFlacon );
  }

  @Override
  public void addAnnounceThreadHistoryItem( long aAlarmId, ISkAnnounceThreadHistoryItem aItem ) {
    TsNullArgumentRtException.checkNull( aItem );
    backend.addAnnounceThreadHistoryItem( aAlarmId, aItem );
  }

  @Override
  public ISkAlarmFlacon getAlarmFlacon( long aAlarmId ) {
    return backend.getAlarmFlacon( aAlarmId );
  }

  @Override
  public ITimedList<ISkAnnounceThreadHistoryItem> getAlarmHistory( long aAlarmId ) {
    return backend.getAlarmHistory( aAlarmId );
  }

  @Override
  public ITimedList<ISkAlarm> queryAlarms( ITimeInterval aTimeInterval, ITsCombiFilterParams aQueryParams ) {
    TsNullArgumentRtException.checkNulls( aTimeInterval, aQueryParams );
    return backend.queryAlarms( aTimeInterval, aQueryParams );
  }

  @Override
  public ISkAlarmEventsFiringSupport eventer() {
    return eventer;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
