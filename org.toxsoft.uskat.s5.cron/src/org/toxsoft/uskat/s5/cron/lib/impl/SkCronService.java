package org.toxsoft.uskat.s5.cron.lib.impl;

import static org.toxsoft.uskat.s5.cron.lib.ISkCronHardConstants.*;

import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.events.AbstractTsEventer;
import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.api.objserv.IDtoFullObject;
import org.toxsoft.uskat.core.devapi.IDevCoreApi;
import org.toxsoft.uskat.core.impl.AbstractSkService;
import org.toxsoft.uskat.core.impl.dto.DtoFullObject;
import org.toxsoft.uskat.s5.cron.lib.*;

/**
 * Реализация службы {@link ISkCronService}.
 *
 * @author mvk
 */
public class SkCronService
    extends AbstractSkService
    implements ISkCronService {

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkCronService::new;

  /**
   * Класс для реализации {@link ISkCronService#eventer()}.
   */
  class Eventer
      extends AbstractTsEventer<ISkCronServiceListener> {

    private SkidList onScheduleIds = null;

    @Override
    protected boolean doIsPendingEvents() {
      return false;
    }

    @Override
    protected void doFirePendingEvents() {
      reallyFire();
    }

    private void reallyFire() {
      if( onScheduleIds != null ) {
        for( ISkCronServiceListener l : listeners() ) {
          try {
            l.onScheduled( onScheduleIds );
          }
          catch( Exception ex ) {
            LoggerUtils.errorLogger().error( ex );
          }
        }
        onScheduleIds = null;
      }
    }

    @Override
    protected void doClearPendingEvents() {
      onScheduleIds = null;
    }

    void fireOnScheduledEvent( ISkidList aScheduleIds ) {
      if( onScheduleIds == null ) {
        onScheduleIds = new SkidList();
      }
      onScheduleIds.addAll( aScheduleIds );
      if( !isPendingEvents() ) {
        reallyFire();
      }
    }
  }

  private final Eventer                    eventer           = new Eventer();
  private final ClassClaimingCoreValidator claimingValidator = new ClassClaimingCoreValidator();

  /**
   * Конструктор службы.
   *
   * @param aCoreApi {@link IDevCoreApi} API ядра uskat
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public SkCronService( IDevCoreApi aCoreApi ) {
    super( ISkCronService.SERVICE_ID, aCoreApi );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов класса AbstractSkService
  //
  @Override
  protected void doInit( ITsContextRo aArgs ) {
    // create class for ISkSchedule
    sysdescr().defineClass( CLSINF_SCHEDULE );
    objServ().registerObjectCreator( CLSID_SCHEDULE, SkSchedule.CREATOR );
    // claim on self classes
    sysdescr().svs().addValidator( claimingValidator );
    objServ().svs().addValidator( claimingValidator );
    linkService().svs().addValidator( claimingValidator );
    clobService().svs().addValidator( claimingValidator );
  }

  @Override
  protected void doClose() {
    // nop
  }

  @Override
  protected boolean doIsClassClaimedByService( String aClassId ) {
    // Заявленные классы службы
    return switch( aClassId ) {
      case ISkSchedule.CLASS_ID -> true;
      default -> false;
    };
  }

  @Override
  protected boolean onBackendMessage( GenericMessage aMessage ) {
    return false;
  }

  @Override
  protected void onBackendActiveStateChanged( boolean aIsActive ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса ISkCronService
  //
  @Override
  public IStridablesList<ISkSchedule> listSchedules() {
    // aIncludeSubclasses = true
    IList<ISkSchedule> ll = objServ().listObjs( CLSID_SCHEDULE, true );
    return new StridablesList<>( ll );
  }

  @Override
  public ISkSchedule findSchedule( String aScheduleId ) {
    return objServ().find( new Skid( CLSID_SCHEDULE, aScheduleId ) );
  }

  @Override
  public ISkSchedule defineSchedule( IDtoFullObject aDtoSchedule ) {
    pauseCoreValidation();
    try {
      ISkSchedule skSchedule = DtoFullObject.defineFullObject( coreApi(), aDtoSchedule );
      return skSchedule;
    }
    finally {
      resumeCoreValidation();
    }
  }

  @Override
  public void removeSchedule( String aScheduleId ) {
    ISkSchedule skSchedule = findSchedule( aScheduleId );
    if( skSchedule == null ) {
      return;
    }
    pauseCoreValidation();
    try {
      objServ().removeObject( skSchedule.skid() );
    }
    finally {
      resumeCoreValidation();
    }
  }

  @Override
  public ITsEventer<ISkCronServiceListener> eventer() {
    return eventer;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  @SuppressWarnings( "unused" )
  private IBaCrone backend() {
    return coreApi().backend().findBackendAddon( IBaCrone.ADDON_ID, IBaCrone.class );
  }

  private void pauseCoreValidation() {
    sysdescr().svs().pauseValidator( claimingValidator );
    objServ().svs().pauseValidator( claimingValidator );
    linkService().svs().pauseValidator( claimingValidator );
    clobService().svs().pauseValidator( claimingValidator );
  }

  private void resumeCoreValidation() {
    sysdescr().svs().resumeValidator( claimingValidator );
    objServ().svs().resumeValidator( claimingValidator );
    linkService().svs().resumeValidator( claimingValidator );
    clobService().svs().resumeValidator( claimingValidator );
  }
}
