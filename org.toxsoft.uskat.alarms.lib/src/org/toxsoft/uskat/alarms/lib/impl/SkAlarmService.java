package org.toxsoft.uskat.alarms.lib.impl;

import static org.toxsoft.uskat.alarms.lib.impl.ISkResources.*;

import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.events.AbstractTsEventer;
import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.bricks.filter.*;
import org.toxsoft.core.tslib.bricks.filter.impl.TsCombiFilter;
import org.toxsoft.core.tslib.bricks.filter.impl.TsFilterFactoriesRegistry;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.IMapEdit;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.gw.skid.SkidList;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;
import org.toxsoft.uskat.alarms.lib.*;
import org.toxsoft.uskat.alarms.lib.flacon.ISkAlarmFlacon;
import org.toxsoft.uskat.core.ISkHardConstants;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.devapi.IDevCoreApi;
import org.toxsoft.uskat.core.impl.AbstractSkService;
import org.toxsoft.uskat.core.impl.dto.DtoFullObject;

/**
 * Реализация службы {@link ISkAlarmService}.
 *
 * @author mvk
 */
public class SkAlarmService
    extends AbstractSkService
    implements ISkAlarmService {

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkAlarmService::new;

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
      extends AbstractTsEventer<ISkAlarmServiceListener>
      implements ISkAlarmEventer {

    private static final ITsFilterFactoriesRegistry<ISkAlarm>                  FILTER_REGISTRY =
        new TsFilterFactoriesRegistry<>( ISkAlarm.class );
    private IMapEdit<ISkAlarmServiceListener, IListEdit<ITsCombiFilterParams>> listeners       = new ElemMap<>();
    private IListEdit<ISkAlarm>                                                onAlarms        = new ElemLinkedList<>();
    private IListEdit<Pair<ISkAlarm, ISkAlarmThreadHistoryItem>>               onAlarmsChanges = new ElemLinkedList<>();

    static {
      SkAlarmUtils.registerAlarmFilters( FILTER_REGISTRY );
    }

    @Override
    public void addListener( ISkAlarmServiceListener aListener ) {
      super.addListener( aListener );
      SkAlarmUtils.addListenerFilter( listeners, aListener, ITsCombiFilterParams.ALL );
      backend().setAlarmFilters( SkAlarmUtils.getListenerFilters( listeners ) );
    }

    @Override
    public void addListener( ISkAlarmServiceListener aListener, ITsCombiFilterParams aFilter ) {
      super.addListener( aListener );
      SkAlarmUtils.addListenerFilter( listeners, aListener, aFilter );
      backend().setAlarmFilters( SkAlarmUtils.getListenerFilters( listeners ) );
    }

    @Override
    public void removeListener( ISkAlarmServiceListener aListener ) {
      super.removeListener( aListener );
      SkAlarmUtils.removeListenerFilter( listeners, aListener );
      backend().setAlarmFilters( SkAlarmUtils.getListenerFilters( listeners ) );
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
              for( ITsCombiFilterParams filterParams : listeners.getByKey( l ) ) {
                ITsFilter<ISkAlarm> filter = TsCombiFilter.create( filterParams, FILTER_REGISTRY );
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
          for( Pair<ISkAlarm, ISkAlarmThreadHistoryItem> change : onAlarmsChanges ) {
            try {
              for( ITsCombiFilterParams filterParams : listeners.getByKey( l ) ) {
                ITsFilter<ISkAlarm> filter = TsCombiFilter.create( filterParams, FILTER_REGISTRY );
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

    void fireOnAlarm( ISkAlarm aAlarm ) {
      onAlarms.add( aAlarm );
      doFirePendingEvents();
    }

    void fireOnAlarmChange( ISkAlarm aAlarm, ISkAlarmThreadHistoryItem aChange ) {
      onAlarmsChanges.add( new Pair<>( aAlarm, aChange ) );
      doFirePendingEvents();
    }
  }

  private final Eventer eventer = new Eventer();

  /**
   * Конструктор службы.
   *
   * @param aCoreApi {@link IDevCoreApi} API ядра uskat
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public SkAlarmService( IDevCoreApi aCoreApi ) {
    super( ISkAlarmService.SERVICE_ID, aCoreApi );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов класса AbstractSkService
  //
  @Override
  protected void doInit( ITsContextRo aArgs ) {
    ISkUserService userService = coreApi().userService();
    // Создание системного пользователя управления тревогами
    if( userService.findUser( ALARM_USER_LOGIN ) == null ) {
      DtoFullObject dtoUser = new DtoFullObject( new Skid( ISkUser.CLASS_ID, ALARM_USER_LOGIN ) );
      dtoUser.attrs().setStr( ISkHardConstants.AID_NAME, STR_N_ALARM_USER );
      dtoUser.attrs().setStr( ISkHardConstants.AID_DESCRIPTION, STR_D_ALARM_USER );
      dtoUser.attrs().setBool( ISkUserServiceHardConstants.ATRID_USER_IS_ENABLED, true );
      dtoUser.attrs().setBool( ISkUserServiceHardConstants.ATRID_USER_IS_HIDDEN, false );
      dtoUser.links().map().put( ISkUserServiceHardConstants.LNKID_USER_ROLES,
          new SkidList( ISkUserServiceHardConstants.SKID_ROLE_ROOT ) );
      userService.createUser( dtoUser, ALARM_USER_PASSWD );
    }
  }

  @Override
  protected void doClose() {
    // nop
  }

  @Override
  protected boolean onBackendMessage( GenericMessage aMessage ) {
    switch( aMessage.messageId() ) {
      case SkAlarmMsgIsOn.MSG_ID: {
        ISkAlarm alarm = SkAlarmMsgIsOn.INSTANCE.getAlarm( aMessage );
        eventer.fireOnAlarm( alarm );
        return true;
      }
      case SkAlarmMsgStateChanged.MSG_ID: {
        ISkAlarm alarm = SkAlarmMsgStateChanged.INSTANCE.getAlarm( aMessage );
        ISkAlarmThreadHistoryItem item = SkAlarmMsgStateChanged.INSTANCE.getThreadHistoryItem( aMessage );
        eventer.fireOnAlarmChange( alarm, item );
        return true;
      }
      default:
        return false;
    }
  }

  @Override
  protected void onBackendActiveStateChanged( boolean aIsActive ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса ISkAlarmService
  //
  @Override
  public IStridablesList<ISkAlarmDef> listAlarmDefs() {
    return backend().listAlarmDefs();
  }

  @Override
  public ISkAlarmDef findAlarmDef( String aAlarmDefId ) {
    TsNullArgumentRtException.checkNull( aAlarmDefId );
    return backend().findAlarmDef( aAlarmDefId );
  }

  @Override
  public void registerAlarmDef( ISkAlarmDef aAlarmDef ) {
    TsNullArgumentRtException.checkNull( aAlarmDef );
    backend().registerAlarmDef( aAlarmDef );
  }

  @Override
  public ISkAlarm generateAlarm( String aAlarmDefId, Skid aAuthorId, Skid aUserId, byte aSublevel,
      ISkAlarmFlacon aSkAlarmFlacon ) {
    TsNullArgumentRtException.checkNulls( aAlarmDefId, aAuthorId, aUserId, aSkAlarmFlacon );
    return backend().generateAlarm( aAlarmDefId, aAuthorId, aUserId, aSublevel, aSkAlarmFlacon );
  }

  @Override
  public void addThreadHistoryItem( long aAlarmId, ISkAlarmThreadHistoryItem aItem ) {
    TsNullArgumentRtException.checkNull( aItem );
    backend().addThreadHistoryItem( aAlarmId, aItem );
  }

  @Override
  public ITimedList<ISkAlarm> queryAlarms( ITimeInterval aTimeInterval, ITsCombiFilterParams aFilter ) {
    TsNullArgumentRtException.checkNulls( aTimeInterval, aFilter );
    return backend().queryAlarms( aTimeInterval, aFilter );
  }

  @Override
  public ISkAlarmEventer eventer() {
    return eventer;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  private IBaAlarms backend() {
    return coreApi().backend().findBackendAddon( IBaAlarms.ADDON_ID, IBaAlarms.class );
  }
}
