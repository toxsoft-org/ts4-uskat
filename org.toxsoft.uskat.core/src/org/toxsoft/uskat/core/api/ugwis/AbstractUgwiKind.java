package org.toxsoft.uskat.core.api.ugwis;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.ugwis.helpers.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Basic implementation of {@link IUgwiKind}.
 *
 * @author hazard157
 * @param <T> - the UGWI content type
 */
public non-sealed abstract class AbstractUgwiKind<T>
    implements IUgwiKind {

  // FIXME make kind ICloseable ?

  private final IMapEdit<Class<?>, Object> helpersMap = new ElemMap<>();

  private final AbstractUgwiKindRegistrator<T> kindRegistrator;
  private final ISkCoreApi                     coreApi;

  /**
   * Constructor.
   *
   * @param aRegistrator {@link AbstractUgwiKindRegistrator} - the kind registrator
   * @param aCoreApi {@link ISkCoreApi} - the core API
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public AbstractUgwiKind( AbstractUgwiKindRegistrator<T> aRegistrator, ISkCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNulls( aRegistrator, aCoreApi );
    kindRegistrator = aRegistrator;
    coreApi = aCoreApi;
    helpersMap.put( IUgwiAvHelper.class, new UgwiAvHelperNone<>( this ) );
  }

  // ------------------------------------------------------------------------------------
  // IStridable
  //

  @Override
  public String id() {
    return kindRegistrator.id();
  }

  @Override
  public String nmName() {
    return kindRegistrator.nmName();
  }

  @Override
  public String description() {
    return kindRegistrator.description();
  }

  // ------------------------------------------------------------------------------------
  // IIconIdable
  //

  @Override
  public String iconId() {
    return kindRegistrator.iconId();
  }

  // ------------------------------------------------------------------------------------
  // IParameterized
  //

  @Override
  public IOptionSet params() {
    return kindRegistrator.params();
  }

  // ------------------------------------------------------------------------------------
  // package API
  //

  /**
   * Determines if this kind can be registered.
   * <p>
   * USkat core implementation private method, only to be called by
   * {@link SkCoreServUgwis#registerKind(AbstractUgwiKind)}.
   *
   * @param aUgwiService {@link SkCoreServUgwis} - the service to create UGWI kind in
   * @return boolean - <code>true</code> if kind can be registered
   */
  final public boolean papiCanRegister( SkCoreServUgwis aUgwiService ) {
    if( aUgwiService.listKinds().hasKey( id() ) ) {
      return false;
    }
    return doCanRegister( aUgwiService );
  }

  // ------------------------------------------------------------------------------------
  // API for subclasses
  //

  /**
   * Retruns the core API this kind is created for.
   *
   * @return {@link ISkCoreApi} - the core API
   */
  public ISkCoreApi coreApi() {
    return coreApi;
  }

  // ------------------------------------------------------------------------------------
  // IUgwiKind
  //

  @Override
  final public ValidationResult validateUgwi( Ugwi aUgwi ) {
    return kindRegistrator.validateUgwi( aUgwi );
  }

  @Override
  final public ValidationResult validateUgwi( String aNamespace, String aEssence ) {
    return kindRegistrator.validateUgwi( aNamespace, aEssence );
  }

  @Override
  final public Object findContent( Ugwi aUgwi ) {
    TsNullArgumentRtException.checkNull( aUgwi );
    TsIllegalArgumentRtException.checkFalse( aUgwi.kindId().equals( id() ) );
    return doFindContent( aUgwi );
  }

  @Override
  final public <H> H findHelper( Class<H> aHelperClass ) {
    TsNullArgumentRtException.checkNull( aHelperClass );
    Object helper = helpersMap.findByKey( aHelperClass );
    if( helper != null ) {
      return aHelperClass.cast( helper );
    }
    return null;
  }

  @Override
  final public <H> void registerHelper( Class<H> aHelperClass, H aHelper ) {
    TsNullArgumentRtException.checkNulls( aHelperClass, aHelper );
    TsItemAlreadyExistsRtException.checkTrue( helpersMap.hasKey( aHelperClass ) );
    helpersMap.put( aHelperClass, aHelperClass.cast( aHelper ) );
  }

  // ------------------------------------------------------------------------------------
  // To override/implement
  //

  /**
   * Implementation must find and return the content addressed by the UGWI.
   *
   * @param aUgwi {@link Ugwi} - the UGWI of the kind {@link #id()}
   * @return {@link Object} - the content or <code>null</code> if not found
   */
  protected abstract T doFindContent( Ugwi aUgwi );

  /**
   * Implementation may perform check if this kind can be registered by
   * {@link ISkUgwiService#registerKind(AbstractUgwiKind)}.
   * <p>
   * For example, implementation may check if {@link SkCoreServUgwis#coreApi()} has the service
   * <code>ISkXxxServce</code> needed to handle this kind of UGWI.
   * <p>
   * In the base class returns <code>true</code>, there is no need to call superclass method when overriding.
   *
   * @param aUgwiService {@link SkCoreServUgwis} - the UGWI service
   * @return boolean - <code>true</code> if kind can be registered
   */
  protected boolean doCanRegister( SkCoreServUgwis aUgwiService ) {
    return true;
  }

}
