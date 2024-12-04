package org.toxsoft.uskat.core.gui.km5.sded.sded;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tsgui.valed.api.IValedControlConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.api.sysdescr.ESkClassPropKind.*;
import static org.toxsoft.uskat.core.gui.km5.sded.ISkSdedKm5SharedResources.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.valed.controls.av.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.*;

/**
 * M5-model of the {@link IStringMap}.
 *
 * @author dima
 */
@SuppressWarnings( "rawtypes" )
public class StringMapStringM5Model
    extends KM5ConnectedModelBase<IStringMap> {

  /**
   * ID of model.
   */
  public static String M5MODEL_ID = "sded.mid.StringMap_String"; //$NON-NLS-1$

  /**
   * ID of field map value.
   */
  public String FID_MAP_VALUE_STR = "mapValueStr"; //$NON-NLS-1$

  /**
   * ID of field of map key
   */
  public static final String FID_MAP_KEY = "mapKey"; //$NON-NLS-1$

  /**
   * Attribute map key
   */
  public M5AttributeFieldDef<IStringMap<String>> CLOB_KEY = new M5AttributeFieldDef<>( FID_MAP_KEY, EAtomicType.STRING, //
      TSID_NAME, STR_CLOB_KEY, //
      TSID_DESCRIPTION, STR_CLOB_KEY_D, //
      OPID_EDITOR_FACTORY_NAME, ValedAvStringText.FACTORY_NAME ) {

    @Override
    protected void doInit() {
      setFlags( M5FF_COLUMN | M5FF_READ_ONLY );
    }

    protected IAtomicValue doGetFieldValue( IStringMap<String> aEntity ) {
      String key = aEntity.keys().first();
      return avStr( key );
    }

  };

  /**
   * Attribute string value
   */
  public M5AttributeFieldDef<IStringMap<String>> CLOB_VALUE =
      new M5AttributeFieldDef<>( FID_MAP_VALUE_STR, EAtomicType.STRING, //
          TSID_NAME, STR_CLOB_VAL, //
          TSID_DESCRIPTION, STR_CLOB_VAL_D, //
          OPID_EDITOR_FACTORY_NAME, ValedAvStringText.FACTORY_NAME ) {

        @Override
        protected void doInit() {
          setFlags( M5FF_COLUMN );
        }

        protected IAtomicValue doGetFieldValue( IStringMap<String> aEntity ) {
          String val = aEntity.getByKey( aEntity.keys().first() );
          return avStr( val );
        }

      };

  /**
   * LM for this model.
   *
   * @author dima
   */
  class LifecycleManager
      extends M5LifecycleManager<IStringMap, ISkConnection> {

    public LifecycleManager( IM5Model<IStringMap> aModel, ISkConnection aMaster ) {
      super( aModel, false, true, false, false, aMaster );
    }

    private IStringMap<String> makeStringMap( IM5Bunch<IStringMap> aValues ) {
      String clobKey = aValues.originalEntity().keys().first();
      String clobValue = aValues.getAsAv( FID_MAP_VALUE_STR ).asString();

      StringMap<String> retVal = new StringMap<>();
      retVal.put( clobKey, clobValue );
      return retVal;
    }

    @Override
    protected IStringMap<String> doCreate( IM5Bunch<IStringMap> aValues ) {
      return makeStringMap( aValues );
    }

    @Override
    protected IStringMap<String> doEdit( IM5Bunch<IStringMap> aValues ) {
      IStringMap<String> retVal = makeStringMap( aValues );
      // master().coreApi().linkService().setLink( retVal );
      return retVal;
    }

    @Override
    protected void doRemove( IStringMap aEntity ) {
      // nop
    }

  }

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public StringMapStringM5Model( ISkConnection aConn ) {
    super( M5MODEL_ID, IStringMap.class, aConn );
    setNameAndDescription( CLOB.nmName(), CLOB.description() );
    addFieldDefs( CLOB_KEY, CLOB_VALUE );
  }

  @Override
  protected IM5LifecycleManager<IStringMap> doCreateDefaultLifecycleManager() {
    return new LifecycleManager( this, skConn() );
  }

  @Override
  protected IM5LifecycleManager<IStringMap> doCreateLifecycleManager( Object aMaster ) {
    return getLifecycleManager( null );
  }

}
