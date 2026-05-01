package org.toxsoft.uskat.core.gui.km5.sded2.skobj;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * LM class to be used with {@link Sded2SkObjectM5Model}.
 * <p>
 * This LM does <b>not</b> has code to create or edit (only to remove and enumerate) objects of class
 * {@link #classId()}. The LM and model are intended to be used together with {@link Sded2SkObjectMpc} which has code to
 * add and edit objects using M5-models specific for the {@link #classId()}.
 *
 * @author hazard157
 */
public class Sded2SkObjectM5LifecycleManager
    extends M5LifecycleManager<ISkObject, ISkConnection>
    implements ISkConnected {

  private String classId;

  /**
   * Constructor.
   *
   * @param aModel {@link IM5Model}&lt;T&gt; - the model
   * @param aMaster &lt;{@link ISkConnection}&gt; - master object, may be <code>null</code>
   * @param aClassId String - ID of class to enumerate by {@link #itemsProvider()}
   * @throws TsNullArgumentRtException model is <code>null</code>
   */
  public Sded2SkObjectM5LifecycleManager( IM5Model<ISkObject> aModel, ISkConnection aMaster, String aClassId ) {
    super( aModel, true, true, true, true, aMaster );
    TsNullArgumentRtException.checkNull( aMaster );
    StridUtils.checkValidIdPath( aClassId );
    classId = aClassId;
  }

  // ------------------------------------------------------------------------------------
  // ISkConnected
  //

  @Override
  public ISkConnection skConn() {
    return master();
  }

  // ------------------------------------------------------------------------------------
  // M5LifecycleManager
  //

  @Override
  protected ValidationResult doBeforeRemove( ISkObject aEntity ) {
    return skObjServ().svs().validator().canRemoveObject( aEntity.skid() );
  }

  @Override
  protected void doRemove( ISkObject aEntity ) {
    skObjServ().removeObject( aEntity.skid() );
  }

  @Override
  protected IList<ISkObject> doListEntities() {
    return skObjServ().listObjs( classId, false );
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns class ID of objects to be listed.
   *
   * @return String - class ID
   */
  public String classId() {
    return classId;
  }

  /**
   * Sets class ID of objects to be listed.
   *
   * @param aClassId String - ID of class to enumerate by {@link #itemsProvider()}
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException argument is not an IDpath
   */
  public void setClassId( String aClassId ) {
    StridUtils.checkValidIdPath( aClassId );
    classId = aClassId;
  }

}
