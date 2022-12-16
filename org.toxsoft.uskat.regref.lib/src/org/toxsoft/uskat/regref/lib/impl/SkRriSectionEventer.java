package org.toxsoft.uskat.regref.lib.impl;

import org.toxsoft.core.tslib.bricks.events.AbstractTsEventer;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedBundleList;
import org.toxsoft.core.tslib.coll.primtypes.IStringListEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.regref.lib.ISkRriSection;
import org.toxsoft.uskat.regref.lib.ISkRriSectionListener;

/**
 * Вспомогательный класс для реализации {@link ISkRriSection#eventer()}.
 *
 * @author goga
 */
class SkRriSectionEventer
    extends AbstractTsEventer<ISkRriSectionListener> {

  private final SkRriSection rriSection;

  private boolean            wasChangesInProps = false;
  private IStringListEdit    changedClassIds   = new StringArrayList();
  private IListEdit<SkEvent> events            = new ElemLinkedBundleList<>();

  public SkRriSectionEventer( SkRriSection aOwner ) {
    rriSection = aOwner;
  }

  @Override
  protected void doClearPendingEvents() {
    wasChangesInProps = false;
    changedClassIds.clear();
    events.clear();
  }

  @Override
  protected void doFirePendingEvents() {
    if( wasChangesInProps ) {
      fireSectionPropsChanged();
    }
    if( !changedClassIds.isEmpty() ) {
      for( String cid : changedClassIds ) {
        fireClassParamInfosChanged( cid );
      }
    }
    if( !events.isEmpty() ) {
      fireParamValuesChanged( events );
    }
  }

  @Override
  protected boolean doIsPendingEvents() {
    return wasChangesInProps || !changedClassIds.isEmpty() || events.isEmpty();
  }

  void fireSectionPropsChanged() {
    if( isFiringPaused() ) {
      wasChangesInProps = true;
      return;
    }
    for( ISkRriSectionListener l : listeners() ) {
      try {
        l.onSectionPropsChanged( rriSection );
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().error( ex );
      }
    }
  }

  void fireClassParamInfosChanged( String aClassId ) {
    if( isFiringPaused() ) {
      if( !changedClassIds.hasElem( aClassId ) ) {
        changedClassIds.add( aClassId );
      }
      return;
    }
    for( ISkRriSectionListener l : listeners() ) {
      try {
        l.onClassParamInfosChanged( rriSection, aClassId );
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().error( ex );
      }
    }
  }

  void fireParamValuesChanged( IList<SkEvent> aEvents ) {
    if( isFiringPaused() ) {
      events.addAll( aEvents );
      return;
    }
    for( ISkRriSectionListener l : listeners() ) {
      try {
        l.onParamValuesChanged( rriSection, new ElemArrayList<>( aEvents ) );
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().error( ex );
      }
    }
  }
}
