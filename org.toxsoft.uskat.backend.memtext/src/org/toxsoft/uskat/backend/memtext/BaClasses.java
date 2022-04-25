package org.toxsoft.uskat.backend.memtext;

import static org.toxsoft.uskat.backend.memtext.IBackendMemtextConstants.*;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * {@link IBaClasses} implementation.
 *
 * @author hazard157
 */
public class BaClasses
    extends AbstractBackendAddon
    implements IBaClasses {

  private final IStridablesListEdit<IDtoClassInfo> classInfos = new StridablesList<>();

  BaClasses( AbstractSkBackendMemtext aOwner ) {
    super( aOwner, TEXT_SECT_NAME_CLASSES );
  }

  // ------------------------------------------------------------------------------------
  // BackendAddonBase
  //

  @Override
  public void close() {
    // nop
  }

  @Override
  public void clear() {
    classInfos.clear();
  }

  @Override
  protected void doWrite( IStrioWriter aSw ) {
    // TODO BaClasses.dowrite()
  }

  @Override
  protected void doRead( IStrioReader aSr ) {
    // TODO BaClasses.doread()
  }

  // ------------------------------------------------------------------------------------
  // IBaClasses
  //

  @Override
  public IStridablesList<IDtoClassInfo> readClassInfos() {
    // TODO BaClasses.readClassInfos()
    return IStridablesList.EMPTY;
  }

  @Override
  public void writeClassInfos( IStringList aRemoveClassIds, IStridablesList<IDtoClassInfo> aUpdateClassInfos ) {
    // TODO BaClasses.writeClassInfos()
  }

}
