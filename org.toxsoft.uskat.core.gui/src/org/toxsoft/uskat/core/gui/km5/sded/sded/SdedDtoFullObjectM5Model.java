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
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.*;
import org.toxsoft.uskat.core.gui.km5.sded.*;
import org.toxsoft.uskat.core.impl.dto.*;

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
   * Attribute {@link IDtoFullObject#id()}
   */
  public final M5AttributeFieldDef<IDtoFullObject> ID = new M5AttributeFieldDef<>( FID_ID, DT_STRING ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_OBJECT_STRID, STR_OBJECT_STRID_D );
      setDefaultValue( DEFAULT_ID_AV );
      setFlags( M5FF_INVARIANT );
    }

    @Override
    protected IAtomicValue doGetFieldValue( IDtoFullObject aEntity ) {
      return avStr( aEntity.id() );
    }

  };

  /**
   * Attribute {@link IDtoFullObject#nmName()}.
   */
  public final IM5AttributeFieldDef<IDtoFullObject> NAME = new M5AttributeFieldDef<>( FID_NAME, DDEF_NAME ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_OBJECT_NAME, STR_OBJECT_NAME_D );
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
          setNameAndDescription( STR_OBJECT_DESCRIPTION, STR_OBJECT_DESCRIPTION_D );
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
      IOptionSetEdit attrs = new OptionSet();

      // обрабатываем новые имя и описание
      attrs.setStr( FID_NAME, aValues.getAsAv( FID_NAME ).asString() );
      attrs.setStr( FID_DESCRIPTION, aValues.getAsAv( FID_DESCRIPTION ).asString() );
      // обрабатываем новые значения атрибутов
      IList<IdValue> attrVals = aValues.getAs( FID_ATTRS, IList.class );
      IdValue.fillOptionSetFromIdValuesColl( attrVals, attrs );

      IDtoObject dtoObj = new DtoObject( aValues.originalEntity().skid(), attrs, IStringMap.EMPTY );

      // создаем карту связей
      IList<LinkIdSkidList> linkVals = aValues.getAs( FID_LINKS, IList.class );
      MappedSkids links = new MappedSkids();
      LinkIdSkidList.fillMappedSkidFromLinkIdSkidList( linkVals, links );
      // TODO implements CLOB support
      DtoFullObject retVal = new DtoFullObject( dtoObj, IStringMap.EMPTY, links.map() );
      // создаем карту заклепок
      IList<LinkIdSkidList> rivetVals = aValues.getAs( FID_RIVETS, IList.class );
      MappedSkids rivets = new MappedSkids();
      LinkIdSkidList.fillMappedSkidFromLinkIdSkidList( rivetVals, rivets );
      retVal.rivets().map().putAll( rivets.map() );
      return retVal;
    }

    @Override
    protected IDtoFullObject doCreate( IM5Bunch<IDtoFullObject> aValues ) {
      IDtoFullObject dtoFullObject = makeDtoFullObject( aValues );
      DtoFullObject.defineFullObject( master().coreApi(), dtoFullObject );
      return dtoFullObject;
    }

    @Override
    protected IDtoFullObject doEdit( IM5Bunch<IDtoFullObject> aValues ) {
      IDtoFullObject dtoFullObject = makeDtoFullObject( aValues );
      DtoFullObject.defineFullObject( master().coreApi(), dtoFullObject );
      return dtoFullObject;
    }

    @Override
    protected void doRemove( IDtoFullObject aEntity ) {
      // nop
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
    addFieldDefs( ID, NAME, DESCRIPTION );
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
            setNameAndDescription( STR_ATTRS, STR_ATTRS_D );
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

    M5MultiModownFieldDef<IDtoFullObject, LinkIdSkidList> fd =
        new M5MultiModownFieldDef<>( FID_LINKS, LinkIdSkidListM5Model.MODEL_ID ) {

          @Override
          protected void doInit() {
            setNameAndDescription( STR_LINKS, STR_LINKS_D );
            setFlags( M5FF_DETAIL );
          }

          protected IList<LinkIdSkidList> doGetFieldValue( IDtoFullObject aEntity ) {
            return LinkIdSkidList.makeLinkIdSkidListCollFromMappedSkid( (MappedSkids)aEntity.links() ).values();
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
    M5MultiModownFieldDef<IDtoFullObject, LinkIdSkidList> fd =
        new M5MultiModownFieldDef<>( FID_RIVETS, LinkIdSkidListM5Model.MODEL_ID ) {

          @Override
          protected void doInit() {
            setNameAndDescription( STR_RIVETS, STR_RIVETS_D );
            setFlags( M5FF_DETAIL );
          }

          protected IList<LinkIdSkidList> doGetFieldValue( IDtoFullObject aEntity ) {
            return LinkIdSkidList.makeLinkIdSkidListCollFromMappedSkid( (MappedSkids)aEntity.rivets() ).values();
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
            setNameAndDescription( STR_CLOBS, STR_CLOBS_D );
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
