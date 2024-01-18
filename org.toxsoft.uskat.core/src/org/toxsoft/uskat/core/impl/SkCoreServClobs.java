package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.backend.ISkBackendHardConstant.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.events.AbstractTsEventer;
import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.bricks.validator.ITsValidationSupport;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.bricks.validator.impl.AbstractTsValidationSupport;
import org.toxsoft.core.tslib.bricks.validator.impl.TsValidationFailedRtException;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedBundleList;
import org.toxsoft.core.tslib.gw.gwid.EGwidKind;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.api.clobserv.*;
import org.toxsoft.uskat.core.api.objserv.ISkObjectService;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.core.backend.api.BaMsgClobsChanged;
import org.toxsoft.uskat.core.devapi.IDevCoreApi;

/**
 * {@link ISkClobService} implementation.
 *
 * @author hazard157
 */
public class SkCoreServClobs
    extends AbstractSkCoreService
    implements ISkClobService {

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkCoreServClobs::new;

  /**
   * {@link ISkClobService#eventer()} implementation.
   *
   * @author hazard157
   */
  class Eventer
      extends AbstractTsEventer<ISkClobServiceListener> {

    private final IListEdit<Gwid> changedClobGwids = new ElemLinkedBundleList<>();

    @Override
    protected boolean doIsPendingEvents() {
      return !changedClobGwids.isEmpty();
    }

    @Override
    protected void doFirePendingEvents() {
      while( !changedClobGwids.isEmpty() ) {
        reallyFireEvent( changedClobGwids.removeByIndex( 0 ) );
      }
    }

    @Override
    protected void doClearPendingEvents() {
      changedClobGwids.clear();
    }

    private void reallyFireEvent( Gwid aGwid ) {
      for( ISkClobServiceListener l : listeners() ) {
        l.onClobChanged( coreApi(), aGwid );
      }
    }

    public void fireClobsChangedEvent( Gwid aGwid ) {
      if( isFiringPaused() ) {
        // put changed GWID at the end of the list
        changedClobGwids.remove( aGwid );
        changedClobGwids.add( aGwid );
        return;
      }
      reallyFireEvent( aGwid );
    }

  }

  /**
   * The service validator {@link ISkObjectService#svs()} implementation.
   *
   * @author hazard157
   */
  static class ValidationSupport
      extends AbstractTsValidationSupport<ISkClobServiceValidator>
      implements ISkClobServiceValidator {

    @Override
    public ISkClobServiceValidator validator() {
      return this;
    }

    // ------------------------------------------------------------------------------------
    // ISkClobServiceValidator
    //

    @Override
    public ValidationResult canWriteClob( Gwid aGwid, String aClob ) {
      TsNullArgumentRtException.checkNulls( aGwid, aClob );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkClobServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canWriteClob( aGwid, aClob ) );
      }
      return vr;
    }

  }

  @SuppressWarnings( "boxing" )
  private final ISkClobServiceValidator builtinValidator = ( aGwid, aClob ) -> {
    // check if GWID is valid
    if( aGwid.kind() != EGwidKind.GW_CLOB ) {
      return ValidationResult.error( FMT_ERR_NON_CLOB_GWID, aGwid.toString() );
    }
    if( aGwid.isAbstract() || aGwid.isMulti() ) {
      return ValidationResult.error( FMT_ERR_NON_CLOB_GWID, aGwid.toString() );
    }
    ISkClassInfo cinf = coreApi().sysdescr().findClassInfo( aGwid.classId() );
    if( cinf == null ) {
      return ValidationResult.error( FMT_ERR_CLOB_CLASS_NOT_EXIST, aGwid.classId() );
    }
    if( !cinf.clobs().list().hasKey( aGwid.propId() ) ) {
      return ValidationResult.error( FMT_ERR_CLOB_NOT_EXIST, aGwid.propId(), aGwid.classId() );
    }
    // check if CLOB's object exists
    if( objServ().find( aGwid.skid() ) == null ) {
      return ValidationResult.error( FMT_ERR_NO_OBJ_OF_CLOB, aGwid.skid().toString(), aGwid.toString() );
    }
    // check if CLOB size is less than ISkBackendHardConstant.OPDEF_SKBI_MAX_CLOB_LENGTH
    int maxLen = OPDEF_SKBI_MAX_CLOB_LENGTH.getValue( coreApi().openArgs().params() ).asInt();
    if( aClob.length() > maxLen ) {
      return ValidationResult.error( FMT_ERR_CLOB_TOO_LONG, aClob.length(), maxLen );
    }
    return ValidationResult.SUCCESS;
  };

  final ValidationSupport validationSupport = new ValidationSupport();

  final Eventer eventer = new Eventer();

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  SkCoreServClobs( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
    validationSupport.addValidator( builtinValidator );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkCoreService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    // nop
  }

  @Override
  protected void doClose() {
    eventer.clearListenersList();
    eventer.resetPendingEvents();
  }

  @Override
  protected boolean onBackendMessage( GenericMessage aMessage ) {
    return switch( aMessage.messageId() ) {
      case BaMsgClobsChanged.MSG_ID -> {
        Gwid clobGwid = BaMsgClobsChanged.BUILDER.getClobGwid( aMessage );
        eventer.fireClobsChangedEvent( clobGwid );
        yield true;
      }
      default -> false;
    };
  }

  // ------------------------------------------------------------------------------------
  // ISkClobService
  //

  @Override
  public String readClob( Gwid aGwid ) {
    checkThread();
    TsIllegalArgumentRtException.checkNull( aGwid );
    TsIllegalArgumentRtException.checkTrue( aGwid.isAbstract() || aGwid.isMulti() );
    TsIllegalArgumentRtException.checkTrue( aGwid.kind() != EGwidKind.GW_CLOB );
    TsItemNotFoundRtException.checkNull( objServ().find( aGwid.skid() ) );
    String clob = ba().baClobs().readClob( aGwid );
    return (clob != null) ? clob : TsLibUtils.EMPTY_STRING;
  }

  @Override
  public void writeClob( Gwid aGwid, String aClob ) {
    checkThread();
    TsValidationFailedRtException.checkError( validationSupport.canWriteClob( aGwid, aClob ) );
    try {
      ba().baClobs().writeClob( aGwid, aClob );
    }
    catch( Exception ex ) {
      throw new TsIoRtException( ex, FMT_ERR_CLOB_TO_BACKEND, aGwid.toString() );
    }
  }

  @Override
  public ITsValidationSupport<ISkClobServiceValidator> svs() {
    return validationSupport;
  }

  @Override
  public ITsEventer<ISkClobServiceListener> eventer() {
    return eventer;
  }

}
