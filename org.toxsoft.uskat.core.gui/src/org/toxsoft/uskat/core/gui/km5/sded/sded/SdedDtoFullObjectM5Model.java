package org.toxsoft.uskat.core.gui.km5.sded.sded;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sded.ISkSdedKm5SharedResources.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.m5.std.models.av.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.misc.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.*;
import org.toxsoft.uskat.core.gui.km5.sded.*;

/**
 * M5-model of the {@link IDtoFullObject}.
 * <p>
 * Note: model's LM provides {@link IDtoFullObject} with only self properties, without parents properties. Say, for
 * attributes {@link IDtoFullObject#attrs()} contains only attributes declared in this class.
 *
 * @author dima
 */
public class SdedDtoFullObjectM5Model
    extends KM5ConnectedModelBase<IDtoFullObject> {

  static final String FID_ATTRS  = "fid.attrs";  //$NON-NLS-1$
  static final String FID_LINKS  = "fid.links";  //$NON-NLS-1$
  static final String FID_RIVETS = "fid.rivets"; //$NON-NLS-1$
  static final String FID_CLOBS  = "fid.clobs";  //$NON-NLS-1$

  /**
   * Attribute {@link IDtoFullObject#skid() } String ID
   */
  // public M5AttributeFieldDef<IDtoFullObject> SKID = new M5AttributeFieldDef<>( FID_SKID, VALOBJ, //
  // TSID_KEEPER_ID, Skid.KEEPER_ID, //
  // OPID_EDITOR_FACTORY_NAME, ValedAvValobjSkidEditor.FACTORY_NAME //
  // ) {
  //
  // @Override
  // protected void doInit() {
  // setNameAndDescription( STR_N_PARAM_SKID, STR_D_PARAM_SKID );
  // setFlags( M5FF_INVARIANT | M5FF_COLUMN );
  // }
  //
  // protected IAtomicValue doGetFieldValue( IDtoFullObject aEntity ) {
  // return avValobj( aEntity.skid() );
  // }
  //
  // };

  /**
   * Attribute {@link IDtoFullObject#nmName()}.
   */
  public final IM5AttributeFieldDef<IDtoFullObject> NAME = new M5AttributeFieldDef<>( FID_NAME, DDEF_NAME ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_OBJECT_NAME, STR_D_OBJECT_NAME );
      setFlags( M5FF_COLUMN );
    }

    protected IAtomicValue doGetFieldValue( IDtoFullObject aEntity ) {
      return avStr( aEntity.nmName() );
    }

  };

  /**
   * Attribute {@link IDtoFullObject#description()}.
   */
  public final IM5AttributeFieldDef<IDtoFullObject> DESCRIPTION =
      new M5AttributeFieldDef<>( FID_DESCRIPTION, DDEF_DESCRIPTION ) {

        @Override
        protected void doInit() {
          setNameAndDescription( STR_N_OBJECT_DESCRIPTION, STR_D_OBJECT_DESCRIPTION );
          setFlags( M5FF_COLUMN );
        }

        protected IAtomicValue doGetFieldValue( IDtoFullObject aEntity ) {
          return avStr( aEntity.description() );
        }

      };

  /**
   * LM class for this model.
   * <p>
   * Allows only enumeration of classes, no editing is allowed.
   *
   * @author hazard157
   */
  class LifecycleManager
      extends M5LifecycleManager<IDtoFullObject, ISkConnection> {

    public LifecycleManager( IM5Model<IDtoFullObject> aModel, ISkConnection aMaster ) {
      super( aModel, true, true, true, true, aMaster );
      TsNullArgumentRtException.checkNull( aMaster );
    }

    // ------------------------------------------------------------------------------------
    // implementation
    //

    private IDtoFullObject makeDtoFullObject( IM5Bunch<IDtoFullObject> aValues ) {
      // String id = aValues.getAsAv( FID_CLASS_ID ).asString();
      // String parentId = aValues.getAsAv( FID_PARENT_ID ).asString();
      // IOptionSetEdit params = new OptionSet();
      // if( aValues.originalEntity() != null ) {
      // params.setAll( aValues.originalEntity().params() );
      // }
      // params.setStr( FID_NAME, aValues.getAsAv( FID_NAME ).asString() );
      // params.setStr( FID_DESCRIPTION, aValues.getAsAv( FID_DESCRIPTION ).asString() );
      // DtoClassInfo cinf = new DtoClassInfo( id, parentId, params );
      // cinf.attrInfos().setAll( SELF_ATTR_INFOS.getFieldValue( aValues ) );
      // cinf.rtdataInfos().setAll( SELF_RTDATA_INFOS.getFieldValue( aValues ) );
      // cinf.cmdInfos().setAll( SELF_CMD_INFOS.getFieldValue( aValues ) );
      // cinf.eventInfos().setAll( SELF_EVENT_INFOS.getFieldValue( aValues ) );
      // cinf.linkInfos().setAll( SELF_LINK_INFOS.getFieldValue( aValues ) );
      // cinf.rivetInfos().setAll( SELF_RIVET_INFOS.getFieldValue( aValues ) );
      //
      // // TODO SdedSkClassInfoM5LifecycleManager.makeDtoClassInfo()
      //
      // return cinf;
      return null;
    }

    @Override
    protected IDtoFullObject doCreate( IM5Bunch<IDtoFullObject> aValues ) {
      IDtoFullObject dtoClassInfo = makeDtoFullObject( aValues );
      return dtoClassInfo;
    }

    @Override
    protected IDtoFullObject doEdit( IM5Bunch<IDtoFullObject> aValues ) {
      IDtoFullObject dtoClassInfo = makeDtoFullObject( aValues );
      return dtoClassInfo;
    }

    @Override
    protected void doRemove( IDtoFullObject aEntity ) {
      // nop
    }

    @Override
    protected IList<IDtoFullObject> doListEntities() {
      IList<ISkClassInfo> skClassesList = master().coreApi().sysdescr().listClasses();
      IListEdit<IDtoFullObject> retVal = new ElemArrayList<>();
      for( ISkClassInfo skInf : skClassesList ) {
        // IDtoFullObject dtoInf = DtoClassInfo.createFromSk( skInf, true );
      }
      return retVal;
    }

  } // class LifecycleManager

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @param aClassInfo {@link ISkClassInfo} - описание класса
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SdedDtoFullObjectM5Model( ISkConnection aConn, ISkClassInfo aClassInfo ) {
    super( IKM5SdedConstants.MID_SDED_DTO_FULL_OBJECT, IDtoFullObject.class, aConn );
    addFieldDefs( NAME, DESCRIPTION );
    ISkClassProps<IDtoAttrInfo> attrInfoes = aClassInfo.attrs();
    ISkClassProps<IDtoLinkInfo> linkInfoes = aClassInfo.links();
    ISkClassProps<IDtoClobInfo> clobInfoes = aClassInfo.clobs();
    ISkClassProps<IDtoRivetInfo> rivetInfoes = aClassInfo.rivets();
    ElemArrayList<IM5FieldDef<IDtoFullObject, ?>> fieldDefs = new ElemArrayList<>();
    // На лету создаем модель полей атрибутов объекта
    createAttrFieldDefs( attrInfoes, fieldDefs );
    // Затем поля для отображения связей
    createLinkFieldDefs( linkInfoes, fieldDefs );
    // Затем поля для отображения clob
    createClobFieldDefs( clobInfoes, fieldDefs );
    // Затем поля для отображения заклепок
    createRivetFieldDefs( rivetInfoes, fieldDefs );
    // Поля созданные "на лету"
    addFieldDefs( fieldDefs );

    setPanelCreator( new SdedDtoFullObjectM5PanelCreator() );
  }

  /**
   * По описанию атрибутов объекта создает описание поля для M5
   *
   * @param aAttrInfoes - описания атрибутов
   * @param aFieldDefs - описания полей M5
   */
  private static void createAttrFieldDefs( ISkClassProps<IDtoAttrInfo> aAttrInfoes,
      ElemArrayList<IM5FieldDef<IDtoFullObject, ?>> aFieldDefs ) {
    M5MultiModownFieldDef<IDtoFullObject, IdValue> fd =
        new M5MultiModownFieldDef<>( FID_ATTRS, IdValueM5Model.MODEL_ID ) {

          @Override
          protected void doInit() {
            setNameAndDescription( STR_N_ATTRS, STR_D_ATTRS );
            setFlags( M5FF_DETAIL );
          }

          protected IList<IdValue> doGetFieldValue( IDtoFullObject aEntity ) {
            return IdValue.makeIdValuesCollFromOptionSet( aEntity.attrs() ).values();
          }

        };
    aFieldDefs.add( fd );
  }

  /**
   * По описанию связей объекта создает описание поля для M5
   *
   * @param aLinkInfoes - описания связей
   * @param aFieldDefs - описания полей M5
   */
  private static void createLinkFieldDefs( ISkClassProps<IDtoLinkInfo> aLinkInfoes,
      ElemArrayList<IM5FieldDef<IDtoFullObject, ?>> aFieldDefs ) {
    M5MultiModownFieldDef<IDtoFullObject, IMappedSkids> fd =
        new M5MultiModownFieldDef<>( FID_LINKS, MappedSkidsM5Model.M5MODEL_ID ) {

          @Override
          protected void doInit() {
            setNameAndDescription( STR_N_LINKS, STR_D_LINKS );
            setFlags( M5FF_DETAIL );
          }

          protected IList<IMappedSkids> doGetFieldValue( IDtoFullObject aEntity ) {
            IListEdit<IMappedSkids> retVal = new ElemArrayList<>();
            for( String key : aEntity.links().map().keys() ) {
              MappedSkids map = new MappedSkids();
              map.ensureSkidList( key, aEntity.links().map().getByKey( key ) );
              retVal.add( map );
            }
            return retVal;
          }

        };
    aFieldDefs.add( fd );
  }

  /**
   * По описанию заклепок объекта создает описание поля для M5
   *
   * @param aRivetInfoes - описания заклепок
   * @param aFieldDefs - описания полей M5
   */
  private static void createRivetFieldDefs( ISkClassProps<IDtoRivetInfo> aRivetInfoes,
      ElemArrayList<IM5FieldDef<IDtoFullObject, ?>> aFieldDefs ) {
    M5MultiModownFieldDef<IDtoFullObject, IMappedSkids> fd =
        new M5MultiModownFieldDef<>( FID_RIVETS, MappedSkidsM5Model.M5MODEL_ID ) {

          @Override
          protected void doInit() {
            setNameAndDescription( STR_N_RIVETS, STR_D_RIVETS );
            setFlags( M5FF_DETAIL );
          }

          protected IList<IMappedSkids> doGetFieldValue( IDtoFullObject aEntity ) {
            IListEdit<IMappedSkids> retVal = new ElemArrayList<>();
            for( String key : aEntity.rivets().map().keys() ) {
              MappedSkids map = new MappedSkids();
              map.ensureSkidList( key, aEntity.rivets().map().getByKey( key ) );
              retVal.add( map );
            }
            return retVal;
          }

        };
    aFieldDefs.add( fd );
  }

  /**
   * По описанию clobs объекта создает описание поля для M5
   *
   * @param aClobInfoes - описания clobs
   * @param aFieldDefs - описания полей M5
   */
  private static void createClobFieldDefs( ISkClassProps<IDtoClobInfo> aClobInfoes,
      ElemArrayList<IM5FieldDef<IDtoFullObject, ?>> aFieldDefs ) {
    M5MultiModownFieldDef<IDtoFullObject, IStringMap<String>> fd =
        new M5MultiModownFieldDef<>( FID_CLOBS, StringMapStringM5Model.M5MODEL_ID ) {

          @Override
          protected void doInit() {
            setNameAndDescription( STR_N_CLOBS, STR_D_CLOBS );
            setFlags( M5FF_DETAIL );
          }

          protected IList<IStringMap<String>> doGetFieldValue( IDtoFullObject aEntity ) {
            IListEdit<IStringMap<String>> retVal = new ElemArrayList<>();
            for( String key : aEntity.clobs().keys() ) {
              IStringMapEdit<String> map = new StringMap<>();
              map.put( key, aEntity.clobs().getByKey( key ) );
              retVal.add( map );
            }
            return retVal;
          }

        };
    aFieldDefs.add( fd );
  }

  /**
   * Создает описание полей связей. Различает обработку связей 1-1 и 1-много
   *
   * @param aLinkInfoes описание полей связей объекта
   * @param aFieldDefs список описаний полей для M5
   */
  // private void createLinkFieldDefs( ISkClassProps<IDtoLinkInfo> aLinkInfoes,
  // ElemArrayList<IM5FieldDef<IDtoFullObject, ?>> aFieldDefs ) {
  // for( IDtoLinkInfo linkInfo : aLinkInfoes.list() ) {
  // // Связи множественные и связи одиночные обрабатываем по разному
  // if( linkInfo.linkConstraint().maxCount() == 1 ) {
  // aFieldDefs.add( singleLinkFieldDef( linkInfo ) );
  // }
  // else {
  // aFieldDefs.add( multyLinkField( linkInfo ) );
  // }
  // }
  // }

  /**
   * Создает описание поля связи 1-1
   *
   * @param aLinkInfo описание связи
   * @return описание поля связи 1-1
   */
  private IM5FieldDef<IDtoFullObject, ISkObject> singleLinkFieldDef( IDtoLinkInfo aLinkInfo ) {
    M5SingleLookupFieldDef<IDtoFullObject, ISkObject> retVal =
        new M5SingleLookupFieldDef<>( aLinkInfo.id(), IGwHardConstants.GW_ROOT_CLASS_ID ) {

          @Override
          protected void doInit() {
            setNameAndDescription( aLinkInfo.nmName(), aLinkInfo.description() );
          }

          /**
           * Допускаем возможность того, что поле не инициализировано
           */
          @Override
          public boolean canUserSelectNull() {
            return false;
          }

          /**
           * Отображает список объектов по данной связи
           * <p>
           *
           * @param aEntity &lt;T&gt; - экземпляр моделированого объекта
           * @return String - отображаемый текст
           */
          @Override
          protected String doGetFieldValueName( IDtoFullObject aEntity ) {
            if( skConn() == null || skConn().state() != ESkConnState.ACTIVE ) {
              return TsLibUtils.EMPTY_STRING;
            }
            try {
              ISkLinkService ls = skConn().coreApi().linkService();
              ISkidList linkIds =
                  ls.getLinkFwd( new Skid( aEntity.classId(), aEntity.id() ), aLinkInfo.id() ).rightSkids();
              ISkObjectService os = skConn().coreApi().objService();
              IList<ISkObject> linkedObjs = os.getObjs( linkIds );
              return linkedObjs.isEmpty() ? TsLibUtils.EMPTY_STRING : linkedObjs.first().nmName();
            }
            catch( Exception ex ) {
              LoggerUtils.errorLogger().error( ex );
            }
            return TsLibUtils.EMPTY_STRING;
          }

          @Override
          protected ISkObject doGetFieldValue( IDtoFullObject aEntity ) {
            if( skConn() == null || skConn().state() != ESkConnState.ACTIVE ) {
              return ISkObject.NONE;
            }
            try {
              ISkLinkService ls = skConn().coreApi().linkService();
              ISkidList linkIds =
                  ls.getLinkFwd( new Skid( aEntity.classId(), aEntity.id() ), aLinkInfo.id() ).rightSkids();
              ISkObjectService os = skConn().coreApi().objService();
              IList<ISkObject> linkedObjs = os.getObjs( linkIds );
              if( linkedObjs.isEmpty() ) {
                return ISkObject.NONE;
              }
              return linkedObjs.first();
            }
            catch( Exception ex ) {
              LoggerUtils.errorLogger().error( ex );
              return ISkObject.NONE;
            }
          }

          @Override
          public IM5LookupProvider<ISkObject> lookupProvider() {
            return () -> baseListItems( aLinkInfo );
          }
        };
    retVal.setFlags( M5FF_COLUMN );
    return retVal;
  }

  /**
   * По описанию связи получить список связанных объектов
   *
   * @param aLinkInfo описание связи
   * @return список связанных объектов
   */
  IList<ISkObject> baseListItems( IDtoLinkInfo aLinkInfo ) {
    ISkObjectService os = skConn().coreApi().objService();
    IListEdit<ISkObject> lookupObjs = new ElemLinkedBundleList<>();
    for( String classId : aLinkInfo.rightClassIds() ) {
      lookupObjs.addAll( os.listObjs( classId, true ) );
    }
    return lookupObjs;
  }

  /**
   * Создает поле описание связи 1-много
   *
   * @param aLinkInfo описание связи
   * @return поле описание связи 1-много
   */
  private IM5FieldDef<IDtoFullObject, ?> multyLinkField( IDtoLinkInfo aLinkInfo ) {
    M5MultiLookupFieldDef<IDtoFullObject, ISkObject> retVal =
        new M5MultiLookupFieldDef<>( aLinkInfo.id(), IGwHardConstants.GW_ROOT_CLASS_ID ) {

          @Override
          protected void doInit() {
            setNameAndDescription( aLinkInfo.nmName(), aLinkInfo.description() );
          }

          /**
           * Отображает список объектов по данной связи
           * <p>
           *
           * @param aEntity &lt;T&gt; - экземпляр моделированого объекта
           * @return String - отображаемый текст
           */
          @Override
          protected String doGetFieldValueName( IDtoFullObject aEntity ) {
            if( skConn() == null || skConn().state() != ESkConnState.ACTIVE ) {
              return TsLibUtils.EMPTY_STRING;
            }
            try {
              ISkLinkService ls = skConn().coreApi().linkService();
              ISkidList linkIds =
                  ls.getLinkFwd( new Skid( aEntity.classId(), aEntity.id() ), aLinkInfo.id() ).rightSkids();
              ISkObjectService os = skConn().coreApi().objService();
              IList<ISkObject> linkedObjs = os.getObjs( linkIds );
              StringBuilder sb = new StringBuilder();
              for( ISkObject obj : linkedObjs ) {
                sb.append( obj.nmName() + ", " ); //$NON-NLS-1$
              }
              // выкусываем финальную запятую
              if( sb.length() > 0 ) {
                return sb.substring( 0, sb.length() - 2 );
              }
            }
            catch( Exception ex ) {
              LoggerUtils.errorLogger().error( ex );
            }
            return TsLibUtils.EMPTY_STRING;
          }

          @Override
          protected IList<ISkObject> doGetFieldValue( IDtoFullObject aEntity ) {
            if( skConn() == null || skConn().state() != ESkConnState.ACTIVE ) {
              return IList.EMPTY;
            }
            try {
              ISkLinkService ls = skConn().coreApi().linkService();
              ISkidList linkIds =
                  ls.getLinkFwd( new Skid( aEntity.classId(), aEntity.id() ), aLinkInfo.id() ).rightSkids();
              ISkObjectService os = skConn().coreApi().objService();
              IList<ISkObject> linkedObjs = os.getObjs( linkIds );
              if( linkedObjs.isEmpty() ) {
                return IList.EMPTY;
              }
              return linkedObjs;
            }
            catch( Exception ex ) {
              LoggerUtils.errorLogger().error( ex );
              return IList.EMPTY;
            }
          }

          @Override
          public IM5LookupProvider<ISkObject> lookupProvider() {
            return () -> baseListItems( aLinkInfo );
          }
        };
    retVal.setFlags( M5FF_COLUMN );
    return retVal;
  }

  // ------------------------------------------------------------------------------------
  // M5Model
  //

  @Override
  protected IM5LifecycleManager<IDtoFullObject> doCreateDefaultLifecycleManager() {
    return new LifecycleManager( this, skConn() );
  }

  @Override
  protected IM5LifecycleManager<IDtoFullObject> doCreateLifecycleManager( Object aMaster ) {
    return new LifecycleManager( this, ISkConnection.class.cast( aMaster ) );
  }

}
