package org.toxsoft.uskat.ggprefs.gui;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;

import org.toxsoft.core.tsgui.m5.IM5FieldDef;
import org.toxsoft.core.tsgui.m5.model.impl.M5AttributeFieldDef;
import org.toxsoft.core.tsgui.m5.model.impl.M5Model;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.uskat.ggprefs.lib.IGuiGwPrefsSection;

/**
 * Модель, используемая при редактировании списка опций
 *
 * @author Max
 */
public class GuiGwPrefsEditModel
    extends M5Model<Skid> {

  /**
   * Идентификатор модели.
   */
  public static final String MODEL_ID = "ru.sitrol.fgdp.lib.core.ggprefs.impl.GuiGwPrefsEditModel"; //$NON-NLS-1$

  private IGuiGwPrefsSection prefsSection;

  /**
   * Конструктор модели редактирования опций gui объекта
   *
   * @param aPrefsSection - редактируемый раздел
   * @param aObjSkid - идентификатор gui объекта
   */
  public GuiGwPrefsEditModel( IGuiGwPrefsSection aPrefsSection, Skid aObjSkid ) {
    super( MODEL_ID, Skid.class );

    setNameAndDescription( "Опции объекта", "Опции объекта для редактирования" ); //$NON-NLS-1$//$NON-NLS-2$

    // ISkGuiGwPrefsService ps = aCoreApi.getService( ISkGuiGwPrefsService.SERVICE_ID );
    // prefsSection = ps.getSection( aGuiGwSectionId );

    prefsSection = aPrefsSection;

    IStridablesList<IDataDef> optDefs = prefsSection.listOptionDefs( aObjSkid );

    IListEdit<IM5FieldDef<?, ?>> fDefs = new ElemArrayList<>();

    // формируем поля - по одному на опцию
    for( IDataDef optDef : optDefs ) {
      fDefs.add( createAttrFieldDef( optDef ) );
    }

    addFieldDefs( fDefs.toArray( new IM5FieldDef[0] ) );

  }

  private IM5FieldDef<?, ?> createAttrFieldDef( IDataDef optDef ) {

    M5AttributeFieldDef<Skid> fDef = new M5AttributeFieldDef<>( optDef.id(), optDef.atomicType() ) {

      @Override
      protected IAtomicValue doGetFieldValue( Skid aEntity ) {

        IOptionSet values = prefsSection.getOptions( aEntity );

        return values.getValue( optDef.id() );

      }

    };

    fDef.setNameAndDescription( optDef.nmName(), optDef.description() );
    fDef.setDefaultValue( optDef.defaultValue() );
    fDef.setFlags( M5FF_COLUMN );
    return fDef;
  }

}
