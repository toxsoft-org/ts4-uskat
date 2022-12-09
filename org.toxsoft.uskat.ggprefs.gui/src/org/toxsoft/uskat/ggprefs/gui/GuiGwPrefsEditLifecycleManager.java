package org.toxsoft.uskat.ggprefs.gui;

import org.toxsoft.core.tsgui.m5.IM5Bunch;
import org.toxsoft.core.tsgui.m5.IM5Model;
import org.toxsoft.core.tsgui.m5.model.impl.M5LifecycleManager;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.ggprefs.lib.IGuiGwPrefsSection;

/**
 * Менеджер жц опций гуи объекта
 *
 * @author Max
 */
public class GuiGwPrefsEditLifecycleManager
    extends M5LifecycleManager<Skid, IGuiGwPrefsSection> {

  /**
   * Конструктор.
   *
   * @param aModel {@link IM5Model} - модель сущностей
   * @param aMaster {@link IGuiGwPrefsSection} - мастер-объект
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public GuiGwPrefsEditLifecycleManager( IM5Model<Skid> aModel, IGuiGwPrefsSection aMaster ) {
    super( aModel, false, true, false, false, aMaster );
    TsNullArgumentRtException.checkNulls( aModel, aMaster );

  }

  @Override
  protected Skid doEdit( IM5Bunch<Skid> aValues ) {

    IStridablesList<IDataDef> optDefs = master().listOptionDefs( aValues.originalEntity() );

    IOptionSetEdit optValues = new OptionSet();

    for( IDataDef optDef : optDefs ) {
      optValues.setValue( optDef.id(), aValues.getAsAv( optDef.id() ) );
    }

    master().setOptions( aValues.originalEntity(), optValues );

    return aValues.originalEntity();
  }

}
