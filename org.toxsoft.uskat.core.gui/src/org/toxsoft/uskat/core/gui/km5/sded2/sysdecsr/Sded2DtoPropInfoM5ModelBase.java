package org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tsgui.m5.gui.mpc.IMultiPaneComponentConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr.ISkResources.*;

import java.time.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.m5.std.fields.*;
import org.toxsoft.core.tsgui.m5.valeds.multimodown.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.*;

/**
 * M5-mode base for all entities extending {@link IDtoClassPropInfoBase}.
 *
 * @author hazard157
 * @param <T> - modeled entity type
 */
public class Sded2DtoPropInfoM5ModelBase<T extends IDtoClassPropInfoBase>
    extends KM5ConnectedModelBase<T> {

  /**
   * Attribute {@link IDtoClassPropInfoBase#id()}.
   */
  public final IM5AttributeFieldDef<T> ID = new M5StdFieldDefId<>() {
    @Override
    protected void doInit() {
      setNameAndDescription( STR_PROP_ID, STR_PROP_ID_D );
      setFlags( M5FF_COLUMN | M5FF_INVARIANT );
    }
  };

  /**
   * Attribute {@link IDtoClassPropInfoBase#nmName()}.
   */
  public final IM5AttributeFieldDef<T> NAME = new M5StdFieldDefName<>() {
    @Override
    protected void doInit() {
      setNameAndDescription( STR_PROP_NAME, STR_PROP_NAME_D );
      setFlags( M5FF_COLUMN );
    }
  };

  /**
   * Attribute {@link IDtoClassPropInfoBase#description()}.
   */
  public final IM5AttributeFieldDef<T> DESCRIPTION = new M5StdFieldDefDescription<>() {
    @Override
    protected void doInit() {
      setNameAndDescription( STR_PROP_DESCRIPTION, STR_PROP_DESCRIPTION_D );
      setFlags( M5FF_DETAIL );
    }
  };

  /**
   * Base class for lifecycle management.
   * <p>
   * LM for class properties is intended to work with field editor {@link ValedMultiModownEditor} which edits list of
   * properties in the memory (hold by VALED itself). So LM simply creates/edits instances of the
   * <code>IDtoXxxInfo</code> like any value-object like {@link IAtomicValue} or {@link LocalDate}.
   * <p>
   * No enumeration is supported - initial list of <code>IDtoXxxInfo</code> is retrieved by
   * {@link IM5FieldDef#getFieldValue(IM5Bunch)}, edited by in-memory list of VALED and used by lifecycle manager of the
   * {@link Sded2SkClassInfoM5Model}.
   *
   * @author hazard157
   */
  protected class LmBase
      extends M5LifecycleManager<T, Object> {

    public LmBase( IM5Model<T> aModel ) {
      super( aModel, true, true, true, false, null );
    }

    @Override
    protected ValidationResult doBeforeCreate( IM5Bunch<T> aValues ) {
      String id = aValues.getAsAv( FID_ID ).asString();
      if( !StridUtils.isValidIdPath( id ) ) {
        return ValidationResult.error( FMT_ERR_ID_NOT_IDPATH, id );
      }
      return ValidationResult.SUCCESS;
    }

    @Override
    protected ValidationResult doBeforeEdit( IM5Bunch<T> aValues ) {
      String id = aValues.getAsAv( FID_ID ).asString();
      if( !StridUtils.isValidIdPath( id ) ) {
        return ValidationResult.error( FMT_ERR_ID_NOT_IDPATH, id );
      }
      return ValidationResult.SUCCESS;
    }

  }

  private final ESkClassPropKind propKind;

  /**
   * Constructor.
   *
   * @param aId String - model ID
   * @param aEntityClass {@link Class}&lt;T&gt; - modeled entity type
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   */
  protected Sded2DtoPropInfoM5ModelBase( String aId, Class<T> aEntityClass, ESkClassPropKind aKind,
      ISkConnection aConn ) {
    super( aId, aEntityClass, aConn );
    propKind = TsNullArgumentRtException.checkNull( aKind );
    addFieldDefs( ID, NAME, DESCRIPTION );
    setPanelCreator( new M5DefaultPanelCreator<>() {
      protected IM5CollectionPanel<T> doCreateCollEditPanel( ITsGuiContext aContext, IM5ItemsProvider<T> aItemsProvider,
          IM5LifecycleManager<T> aLifecycleManager ) {
        OPDEF_IS_ADD_COPY_ACTION.setValue( aContext.params(), AV_TRUE );
        return super.doCreateCollEditPanel( aContext, aItemsProvider, aLifecycleManager );
      }
    } );
  }

  // ------------------------------------------------------------------------------------
  // API for subclasses
  //

  protected ESkClassPropKind propKind() {
    return propKind;
  }

}
