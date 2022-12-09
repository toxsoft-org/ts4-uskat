package org.toxsoft.uskat.ggprefs.gui;

import org.toxsoft.core.tsgui.bricks.ctx.ITsGuiContext;
import org.toxsoft.core.tsgui.dialogs.datarec.ITsDialogInfo;
import org.toxsoft.core.tsgui.m5.IM5Domain;
import org.toxsoft.core.tsgui.m5.gui.M5GuiUtils;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.objserv.ISkObject;
import org.toxsoft.uskat.core.api.objserv.ISkObjectService;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.ggprefs.lib.IGuiGwPrefsSection;

/**
 * Вспомогательные методы для редактирования настроек GUI.
 *
 * @author goga
 */
public class GuiGwPrefsUtils {

  /**
   * Выводит диалог редактирования настроек GUI указанных объектов.
   *
   * @param aContext {@link ITsGuiContext} - контекст
   * @param aCdi {@link ITsDialogInfo} - конфигурация диалогового окна
   * @param aSkConn {@link ISkConnection} - соединение, в котором находится объект
   * @param aSection {@link IGuiGwPrefsSection} - используемый раздел
   * @param aObjSkids {@link ISkidList} - идентификаторы объектов, чьи настройки визуализации будут отредатированы
   * @param aGroupDefs {@link IStridablesList} - список описаний групп, к которым могут относится редактируемые опции.
   *          Если описание для группы редактируемых опций не найдено в этом списке - группа считается безымянной и
   *          редактируется "по умолчанию". Опции без указания группы - редактируется как одна безымянная группа "по
   *          умолчанию"
   * @return boolean - признак, что пользователь сделал изменения, а не отказался от правок
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException нет такого раздела {@link IGuiGwPrefsSection}
   * @throws TsItemNotFoundRtException нет такого объекта {@link ISkObject}
   */
  public static boolean editGuiGwPrefs( ITsGuiContext aContext, ITsDialogInfo aCdi, ISkConnection aSkConn,
      IGuiGwPrefsSection aSection, ISkidList aObjSkids, IStridablesList<IDataDef> aGroupDefs ) {
    TsNullArgumentRtException.checkNulls( aContext, aCdi, aSkConn, aSection, aObjSkids );

    return GuiGwPrefsEditDialog.edit( aContext, aCdi, aSkConn, aSection, aObjSkids, aGroupDefs );
  }

  /**
   * Выводит диалог редактирования настроек GUI одного указанного объекта.
   *
   * @param aContext {@link ITsGuiContext} - контекст
   * @param aCdi {@link ITsDialogInfo} - конфигурация диалогового окна
   * @param aSkConn {@link ISkConnection} - соединение, в котором находится объект
   * @param aSection {@link IGuiGwPrefsSection} - используемый раздел
   * @param aObjSkid {@link Skid} - идентификаторы объекта, чьи настройки визуализации будут отредатированы
   * @param aGroupDefs {@link IStridablesList} - список описаний групп, к которым могут относится редактируемые опции.
   *          Если описание для группы редактируемых опций не найдено в этом списке - группа считается безымянной и
   *          редактируется "по умолчанию". Опции без указания группы - редактируется как одна безымянная группа "по
   *          умолчанию"
   * @return boolean - признак, что пользователь сделал изменения, а не отказался от правок
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException нет такого раздела {@link IGuiGwPrefsSection}
   * @throws TsItemNotFoundRtException нет такого объекта {@link ISkObject}
   */
  public static boolean editGuiGwPrefs( ITsGuiContext aContext, ITsDialogInfo aCdi, ISkConnection aSkConn,
      IGuiGwPrefsSection aSection, Skid aObjSkid, IStridablesList<IDataDef> aGroupDefs ) {
    TsNullArgumentRtException.checkNulls( aContext, aCdi, aSkConn, aSection, aObjSkid );
    ISkObjectService os = aSkConn.coreApi().objService();
    os.get( aObjSkid ); // проверка на несуществущие объекты
    // ISkGuiGwPrefsService ps = aSkConn.coreApi().getService( ISkGuiGwPrefsService.SERVICE_ID );
    // if( !ps.listSections().idList().hasElem( aGuiGwSectionId ) ) {
    // throw new TsItemNotFoundRtException();
    // }
    IGuiGwPrefsSection prefsSection = aSection;// ps.getSection( aGuiGwSectionId );

    GuiGwPrefsEditModel model = new GuiGwPrefsEditModel( prefsSection, aObjSkid );

    ISkConnection skConn = aContext.get( ISkConnection.class );
    IM5Domain d = skConn.scope().get( IM5Domain.class );
    d.initTemporaryModel( model );

    GuiGwPrefsEditLifecycleManager manager = new GuiGwPrefsEditLifecycleManager( model, prefsSection );

    return M5GuiUtils.askEdit( aContext, model, aObjSkid, aCdi, manager ) != null;

    // TODO реализовать GuiGwPrefsUtils.editGuiGwPrefs()
    // throw new TsUnderDevelopmentRtException( "GuiGwPrefsUtils.editGuiGwPrefs()" );

  }

}
