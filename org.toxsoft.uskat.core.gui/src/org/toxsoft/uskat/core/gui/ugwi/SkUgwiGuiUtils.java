package org.toxsoft.uskat.core.gui.ugwi;

import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.api.ugwis.*;
import org.toxsoft.uskat.core.api.ugwis.kinds.*;
import org.toxsoft.uskat.core.gui.*;
import org.toxsoft.uskat.core.gui.ugwi.gui.*;
import org.toxsoft.uskat.core.gui.ugwi.kinds.*;

/**
 * UGWI GUI support utilities.
 *
 * @author hazard157
 */
public class SkUgwiGuiUtils {

  /**
   * Core handler to register all {@link IUgwiKindGuiHelper} when connection opens.
   * <p>
   * Note: not for users! Field is public for {@link QuantSkCoreGui} to access it.
   */
  @SuppressWarnings( { "unchecked", "rawtypes" } )
  public static final ISkCoreExternalHandler guiHelpersRegistrator = aCoreApi -> {
    ISkUgwiService uServ = aCoreApi.ugwiService();
    ISkUgwiKind uk;
    uk = uServ.listKinds().getByKey( UgwiKindSkAttr.KIND_ID );
    uk.registerHelper( IUgwiKindGuiHelper.class, new UgwiGuiHelperSkAttr( (AbstractSkUgwiKind)uk ) );
    uk = uServ.listKinds().getByKey( UgwiKindSkCmd.KIND_ID );
    uk.registerHelper( IUgwiKindGuiHelper.class, new UgwiGuiHelperSkCmd( (AbstractSkUgwiKind)uk ) );
    uk = uServ.listKinds().getByKey( UgwiKindSkLink.KIND_ID );
    uk.registerHelper( IUgwiKindGuiHelper.class, new UgwiGuiHelperSkLink( (AbstractSkUgwiKind)uk ) );
    uk = uServ.listKinds().getByKey( UgwiKindSkSkid.KIND_ID );
    uk.registerHelper( IUgwiKindGuiHelper.class, new UgwiGuiHelperSkSkid( (AbstractSkUgwiKind)uk ) );
    uk = uServ.listKinds().getByKey( UgwiKindSkRivet.KIND_ID );
    // uk.registerHelper( IUgwiKindGuiHelper.class, new UgwiGuiHelperSkRivet( (AbstractSkUgwiKind)uk ) );
    uk = uServ.listKinds().getByKey( UgwiKindSkRtdata.KIND_ID );
    uk.registerHelper( IUgwiKindGuiHelper.class, new UgwiGuiHelperSkRtdata( (AbstractSkUgwiKind)uk ) );

    // TODO register all known GUI helpers
  };

  // private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  //
  // private static final IStringMapEdit<IListEdit<IRegistrator<?>>> helperRegistrators = new StringMap<>();
  //
  // static {
  // addRegistrator( UgwiKindSkAttr.KIND_ID, UgwiGuiHelperSkAttr.REGISTRATOR );
  // // TODO add all built-in helper registrators
  // }
  //
  // /**
  // * Returns helper registrators.
  // *
  // * @return {@link IStringMap}&lt;{@link IList}&lt;{@link IRegistrator}&gt;&gt; - map "kindID" - "registrators list"
  // */
  // public static IStringMap<IList<IRegistrator<?>>> getHelperRegistratorsMap() {
  // lock.readLock().lock();
  // try {
  // IStringMapEdit<IList<IRegistrator<?>>> map = new StringMap<>();
  // for( String ugwiKindId : helperRegistrators.keys() ) {
  // IList<IRegistrator<?>> ll = new ElemArrayList<>( helperRegistrators.getByKey( ugwiKindId ) );
  // map.put( ugwiKindId, ll );
  // }
  // return map;
  // }
  // finally {
  // lock.readLock().unlock();
  // }
  // }
  //
  // /**
  // * Add registrator to the UGWI kind.
  // *
  // * @param aUgwiKindId String - the UGWI kind ID
  // * @param aRegistrator {@link IRegistrator} - helpers registrator
  // * @throws TsNullArgumentRtException any argument = <code>null</code>
  // * @throws TsIllegalArgumentRtException ID is not an IDpath
  // */
  // public static void addRegistrator( String aUgwiKindId, IRegistrator<?> aRegistrator ) {
  // StridUtils.checkValidIdPath( aUgwiKindId );
  // TsNullArgumentRtException.checkNull( aRegistrator );
  // lock.writeLock().lock();
  // try {
  // IListEdit<IRegistrator<?>> ll = helperRegistrators.findByKey( aUgwiKindId );
  // if( ll == null ) {
  // ll = new ElemArrayList<>();
  // helperRegistrators.put( aUgwiKindId, ll );
  // }
  // if( !ll.hasElem( aRegistrator ) ) {
  // ll.add( aRegistrator );
  // }
  // }
  // finally {
  // lock.writeLock().unlock();
  // }
  // }

  /**
   * No subclasses.
   */
  private SkUgwiGuiUtils() {
    // nop
  }
}
