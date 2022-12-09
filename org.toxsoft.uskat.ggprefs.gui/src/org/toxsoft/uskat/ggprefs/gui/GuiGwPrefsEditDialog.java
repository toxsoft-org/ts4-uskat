package org.toxsoft.uskat.ggprefs.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.ITsGuiContext;
import org.toxsoft.core.tsgui.bricks.ctx.impl.TsGuiContext;
import org.toxsoft.core.tsgui.dialogs.datarec.*;
import org.toxsoft.core.tsgui.panels.opsedit.IOptionSetPanel;
import org.toxsoft.core.tsgui.panels.opsedit.impl.OptionSetPanel;
import org.toxsoft.core.tsgui.utils.layout.BorderLayout;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.bricks.events.change.IGenericChangeListener;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.objserv.ISkObjectService;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.ggprefs.lib.IGuiGwPrefsConstants;
import org.toxsoft.uskat.ggprefs.lib.IGuiGwPrefsSection;

/**
 * Панель диалога редактирования настроечных опций объектов
 *
 * @author Max
 */
public class GuiGwPrefsEditDialog
    extends AbstractTsDialogPanel<IMap<Skid, IOptionSet>, ITsGuiContext> {

  /**
   * Идентификатор группы опций по умолчанию - группы, куда попадают описания без указанной группы.
   */
  private static final String DEFAULT_OPTS_GROUP_ID = "default.group"; //$NON-NLS-1$

  /**
   * Слушатель события изменения выбранного объекта.
   */
  private final SelectionAdapter objSelectionChangeListener = new SelectionAdapter() {

    @Override
    public void widgetSelected( SelectionEvent aEvent ) {
      whenObjectSelectionChanged();
    }

  };

  /**
   * Слушатель события изменения значений опций одной группы.
   */
  private final IGenericChangeListener groupValuesChangeListener =
      aSource -> whenGroupValuesChanged( (OptionSetPanel)aSource );

  /**
   * Раздел редактируемых настроек.
   */
  private final IGuiGwPrefsSection guiGwSection;

  /**
   * Контекст
   */
  private final ITsGuiContext context;

  /**
   * Соединение с сервером
   */
  private final ISkConnection connection;

  /**
   * Список идентификаторов редактируемых объектов
   */
  private ISkidList objsList;

  /**
   * Список описаний групп опций
   */
  private final IStridablesList<IDataDef> groupDefs;

  /**
   * Изменённые в диалоге значения опций (мапируются на редактируемые объекты)
   */
  private final IMapEdit<Skid, IOptionSet> changedValues = new ElemMap<>();

  /**
   * Панель отображения списка редактируемых объектов
   */
  private final List objsPanel;

  /**
   * Панель закладок редакторов групп опций объекта
   */
  private final TabFolder groupEditorsFolder;

  /**
   * Список редакторов групп опций объекта.
   */
  private final IListEdit<IOptionSetPanel> groupEditors = new ElemArrayList<>();

  /**
   * Конструктор.
   *
   * @param aContext - контекст.
   * @param aSkConn - соединение с сервером
   * @param aGuiGwSection - редактируемый раздел сервиса настроек
   * @param aGroupDefs - список описаний групп опций
   * @param aParent - родительский компонент
   * @param aOwnerDialog - диалог
   */
  protected GuiGwPrefsEditDialog( ITsGuiContext aContext, ISkConnection aSkConn, IGuiGwPrefsSection aGuiGwSection,
      IStridablesList<IDataDef> aGroupDefs, Composite aParent,
      TsDialog<IMap<Skid, IOptionSet>, ITsGuiContext> aOwnerDialog ) {
    super( aParent, aOwnerDialog );
    setLayout( new BorderLayout() );

    // service = aContext.get( ISkConnection.class ).coreApi().getService( ISkGuiGwPrefsService.SERVICE_ID );
    guiGwSection = aGuiGwSection;
    context = aContext;
    connection = aSkConn;
    groupDefs = aGroupDefs;

    SashForm sfMain = new SashForm( this, SWT.HORIZONTAL );
    sfMain.setLayoutData( BorderLayout.CENTER );

    // панель списка объектов
    objsPanel = new List( sfMain, SWT.SINGLE | SWT.BORDER );
    objsPanel.setLayoutData( BorderLayout.WEST );
    objsPanel.addSelectionListener( objSelectionChangeListener );

    // панель закладок для редактирования опций одного объекта в разных группах - одна группа на закладку
    groupEditorsFolder = new TabFolder( sfMain, SWT.CENTER );
    groupEditorsFolder.setLayoutData( BorderLayout.EAST );

    sfMain.setWeights( 250, 750 );
  }

  @Override
  protected void doSetDataRecord( IMap<Skid, IOptionSet> aData ) {
    // очистка
    objsPanel.removeAll();
    changedValues.clear();

    // инициализация списка объектов
    objsList = new SkidList( aData.keys() );

    ISkObjectService objService = connection.coreApi().objService();

    // инициализация панели списка объектов - загрузка отображаемых имен объектов
    for( int i = 0; i < objsList.size(); i++ ) {
      Skid b = objsList.get( i );
      String s = objService.get( b ).readableName();

      objsPanel.add( s );
    }

    // выбор первого объекта
    objsPanel.select( 0 );

    // после инициализации покажем редакторы групп опций первого объекта
    showObjectOptions( objsList.first() );

    this.layout( true, true );
  }

  @Override
  protected IMap<Skid, IOptionSet> doGetDataRecord() {
    // возвращаем только изменённые опции
    return changedValues;
  }

  /**
   * Реализация реакции на смену выделенного в списке объекта
   */
  private void whenObjectSelectionChanged() {
    // выделенный объект
    Skid selObj = getSelectedObject();
    showObjectOptions( selObj );
  }

  /**
   * Реализация реакции на изменение значений опций в конкретной панели редактирования группы
   *
   * @param aOpsedPanel PanelOptionSetEdit - панель редактирования группы опций
   */
  private void whenGroupValuesChanged( OptionSetPanel aOpsedPanel ) {
    Skid selObj = getSelectedObject();
    if( selObj != null ) {
      // изменённые значения
      IOptionSetEdit objChangedValues = new OptionSet();
      // если уже есть изменённые значения - сначала добавить их
      if( changedValues.hasKey( selObj ) ) {
        objChangedValues.addAll( changedValues.getByKey( selObj ) );
      }
      // добавить (переписать поверх) новые изменённые значения
      objChangedValues.addAll( aOpsedPanel.getEntity() );
      changedValues.put( selObj, objChangedValues );
    }
  }

  /**
   * Возвращает выделенный объект.
   *
   * @return Skid - выделенный объект.
   */
  private Skid getSelectedObject() {
    int selIndex = objsPanel.getSelectionIndex();
    if( selIndex >= 0 ) {
      return objsList.get( selIndex );
    }
    return null;
  }

  /**
   * Отображает закладки редактирования групп опций выбранного объекта - по одной закладке на группу опций
   *
   * @param aObj Skid - идентификатор выбранного объекта
   */
  private void showObjectOptions( Skid aObj ) {
    // очистка предыдущих редакторов

    for( int i = 0; i < groupEditors.size(); i++ ) {
      groupEditors.get( i ).setOptionDefs( IStridablesList.EMPTY );
      groupEditors.get( i ).getControl().dispose();
    }
    groupEditors.clear();
    for( int i = 0; i < groupEditorsFolder.getItemCount(); i++ ) {
      groupEditorsFolder.getItem( i ).dispose();
    }
    if( aObj == null ) {
      return;
    }

    // описание всех опций объекта
    IStridablesList<IDataDef> optionDefs = guiGwSection.listOptionDefs( aObj );
    // значения всех опций объекта
    IOptionSet optionVals = guiGwSection.getOptions( aObj );

    // заменить значения тех опций, которые уже были изменены в диалоге
    if( changedValues.hasKey( aObj ) ) {
      IOptionSetEdit mixedOptionVals = new OptionSet();
      mixedOptionVals.addAll( optionVals );
      mixedOptionVals.addAll( changedValues.getByKey( aObj ) );
      optionVals = mixedOptionVals;
    }

    // группировка описаний опций
    IStringMap<IStridablesListEdit<IDataDef>> groupedOptionDefs = getGroups( optionDefs );

    // для каждой группы - своя закладка
    for( int i = 0; i < groupedOptionDefs.keys().size(); i++ ) {
      // идентификатор группы
      String groupId = groupedOptionDefs.keys().get( i );

      // закладка группы (чуток кривовато, потому что одна закладка не удалялась)
      TabItem item = i < groupEditorsFolder.getItemCount() ? groupEditorsFolder.getItem( i )
          : new TabItem( groupEditorsFolder, SWT.NONE );

      // описание группы
      IDataDef groupDef = groupDefs.findByKey( groupId );

      // Установка наименования закладки
      item.setText( groupDef != null ? groupDef.nmName() : groupId );

      // Контекст редактора группы с настройками
      ITsGuiContext ctx = new TsGuiContext( context );

      // TODO: 2022-12-08 mvk ???
      // IOptionSetPanel.IS_EXCLUDE_CHECKBOXES_USED.setValue( ctx.params(), AV_FALSE );
      // IPanelOptionSetEdit.IS_RETAIN_UNKNOWN.setValue( ctx.params(), AV_TRUE );

      // редактор группы
      OptionSetPanel opsedPanel = new OptionSetPanel( ctx, false ); // aIsViewer = false
      groupEditors.add( opsedPanel );
      item.setControl( opsedPanel.createControl( groupEditorsFolder ) );

      // слушатель изменения значений группы
      opsedPanel.genericChangeEventer().addListener( groupValuesChangeListener );

      // установка описаний группы опций в редактор
      opsedPanel.setOptionDefs( groupedOptionDefs.getByKey( groupId ) );

      // Значения группы опций
      IOptionSetEdit groupVals = new OptionSet();
      for( String id : optionVals.keys() ) {
        if( groupedOptionDefs.getByKey( groupId ).hasKey( id ) ) {
          groupVals.setValue( id, optionVals.getValue( id ) );
        }
      }

      // установка значений группы опций в редактор
      opsedPanel.setEntity( groupVals );
    }
  }

  /**
   * Группирует опции в соответствии со значением параметра {@link IGuiGwPrefsConstants#OPID_TREE_PATH1}
   *
   * @param aOptionDefs IStridablesList - плоский список описаний опций.
   * @return IStringMap - разделённые на группы списки описаний опций.
   */
  private static IStringMap<IStridablesListEdit<IDataDef>> getGroups( IStridablesList<IDataDef> aOptionDefs ) {
    IStringMapEdit<IStridablesListEdit<IDataDef>> result = new StringMap<>();

    for( IDataDef oDef : aOptionDefs ) {
      String groupId = DEFAULT_OPTS_GROUP_ID;
      IAtomicValue groupIdVal = oDef.params().findValue( IGuiGwPrefsConstants.OPID_TREE_PATH1 );

      if( groupIdVal != null && groupIdVal.isAssigned() ) {
        groupId = groupIdVal.asString();
      }

      if( result.hasKey( groupId ) ) {
        result.getByKey( groupId ).add( oDef );
      }
      else {
        result.put( groupId, new StridablesList<>( oDef ) );
      }
    }

    return result;
  }

  /**
   * Вызывает диалог редактирования gui опций объектов - диалог состоит из: слева список реактируемых объектов - справа
   * набор закладок с полями редактирования опций - каждая закладка соответствует одной группе опций одного выбранного
   * объекта.
   *
   * @param aContext ITsGuiContext - контекст.
   * @param aCdi ITsDialogInfo - набор параметров диалога.
   * @param aSkConn соединение с сервером
   * @param aSection IGuiGwPrefsSection - раздел редактируемых опций.
   * @param aObjSkkeys ISkidList - список объектов (идентификаторов), опции которых редактируются.
   * @param aGroupDefs IStridablesList - список описаний групп опций объектов.
   * @return true - если была хоть одна изменённая и записанная на сервер опция, false - значение ни одной опции
   *         изменено не было
   */
  public static boolean edit( ITsGuiContext aContext, ITsDialogInfo aCdi, ISkConnection aSkConn,
      IGuiGwPrefsSection aSection, ISkidList aObjSkkeys, IStridablesList<IDataDef> aGroupDefs ) {
    TsNullArgumentRtException.checkNulls( aCdi, aSection, aContext, aObjSkkeys, aGroupDefs );
    IDialogPanelCreator<IMap<Skid, IOptionSet>, ITsGuiContext> creator = ( aParent,
        aOwnerDialog ) -> new GuiGwPrefsEditDialog( aContext, aSkConn, aSection, aGroupDefs, aParent, aOwnerDialog );
    TsDialog<IMap<Skid, IOptionSet>, ITsGuiContext> d = new TsDialog<>( aCdi, null, aContext, creator );

    // начальные данные - нужны только идентификаторы объектов, поэтому опции оставляем пустыми
    IMapEdit<Skid, IOptionSet> initData = new ElemMap<>();
    for( Skid objId : aObjSkkeys ) {
      initData.put( objId, IOptionSet.NULL );
    }
    d.setData( initData );

    // изменённые значения
    IMap<Skid, IOptionSet> changedValues = d.execData();
    boolean wasChanges = false;
    if( changedValues != null && !changedValues.isEmpty() ) {
      for( Skid skid : changedValues.keys() ) {
        // значения, которые будут записаны на сервер
        IOptionSetEdit newVals = new OptionSet();

        // добавляем все существующие на сервере значения
        newVals.addAll( aSection.getOptions( skid ) );

        // добавляем изменённые значения
        newVals.addAll( changedValues.getByKey( skid ) );

        // запись на сревер
        aSection.setOptions( skid, newVals );
      }

      wasChanges = true;
    }
    return wasChanges;
  }

  /**
   * Вызывает диалог редактирования gui опций одного объекта.
   * <p>
   * Не сохраняет изменения никуда, просто возвращает список измененных опции.
   *
   * @param aContext {@link ITsGuiContext} - контекст.
   * @param aSkConn {@link ISkConnection} - соединение.
   * @param aCdi {@link ITsDialogInfo} - набор параметров диалога.
   * @param aSection {@link IGuiGwPrefsSection} - раздел редактируемых опций.
   * @param aSkid {@link Skid} - идентификатор объекта
   * @param aGroupDefs {@link IStridablesList} - список описаний групп опций объектов.
   * @return {@link IOptionSet} - набор из только измененных опции, может быть пустым, но не <code>null</code>
   */
  public static IOptionSet editNoSave( ITsGuiContext aContext, ISkConnection aSkConn, ITsDialogInfo aCdi,
      IGuiGwPrefsSection aSection, Skid aSkid, IStridablesList<IDataDef> aGroupDefs ) {

    TsNullArgumentRtException.checkNulls( aCdi, aSection, aContext, aSkid, aGroupDefs );
    IDialogPanelCreator<IMap<Skid, IOptionSet>, ITsGuiContext> creator = ( aParent,
        aOwnerDialog ) -> new GuiGwPrefsEditDialog( aContext, aSkConn, aSection, aGroupDefs, aParent, aOwnerDialog );
    TsDialog<IMap<Skid, IOptionSet>, ITsGuiContext> d = new TsDialog<>( aCdi, null, aContext, creator );

    // начальные данные - нужны только идентификаторы объектов, поэтому опции оставляем пустыми
    IMapEdit<Skid, IOptionSet> initData = new ElemMap<>();
    initData.put( aSkid, IOptionSet.NULL );
    d.setData( initData );

    // изменённые значения
    IMap<Skid, IOptionSet> changedValues = d.execData();
    IOptionSet opset = changedValues.findByKey( aSkid );
    if( opset == null ) {
      return IOptionSet.NULL;
    }
    return opset;
  }

}
