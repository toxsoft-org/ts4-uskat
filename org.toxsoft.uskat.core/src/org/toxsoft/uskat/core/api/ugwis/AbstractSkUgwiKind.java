package org.toxsoft.uskat.core.api.ugwis;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Basic implementation of {@link ISkUgwiKind}.
 *
 * @author hazard157
 * @param <T> - the UGWI content type
 */
public non-sealed abstract class AbstractSkUgwiKind<T>
    implements ISkUgwiKind {

  // FIXME make kind ICloseable ?

  private final IMapEdit<Class<?>, Object> helpersMap = new ElemMap<>();

  private final AbstractUgwiKind<T> ugwiKind;
  private final ISkCoreApi          coreApi;

  /**
   * Constructor.
   *
   * @param aStaticKind {@link AbstractUgwiKind} - the kind registrator
   * @param aCoreApi {@link ISkCoreApi} - the core API
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public AbstractSkUgwiKind( AbstractUgwiKind<T> aStaticKind, ISkCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNulls( aStaticKind, aCoreApi );
    ugwiKind = aStaticKind;
    coreApi = aCoreApi;
  }

  // ------------------------------------------------------------------------------------
  // IStridable
  //

  @Override
  public String id() {
    return ugwiKind.id();
  }

  @Override
  public String nmName() {
    return ugwiKind.nmName();
  }

  @Override
  public String description() {
    return ugwiKind.description();
  }

  // ------------------------------------------------------------------------------------
  // IIconIdable
  //

  @Override
  public String iconId() {
    return ugwiKind.iconId();
  }

  // ------------------------------------------------------------------------------------
  // IParameterized
  //

  @Override
  public IOptionSet params() {
    return ugwiKind.params();
  }

  // ------------------------------------------------------------------------------------
  // package API
  //

  /**
   * Determines if this kind can be registered.
   * <p>
   * USkat core implementation private method, only to be called by
   * {@link SkCoreServUgwis#registerKind(AbstractSkUgwiKind)}.
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
  // ISkUgwiKind
  //

  @Override
  public ISkCoreApi coreApi() {
    return coreApi;
  }

  @Override
  public IUgwiKind ugwiKind() {
    return ugwiKind;
  }

  @Override
  final public Object findContent( Ugwi aUgwi ) {
    TsNullArgumentRtException.checkNull( aUgwi );
    TsIllegalArgumentRtException.checkFalse( aUgwi.kindId().equals( id() ) );
    return doFindContent( aUgwi );
  }

  @Override
  final public boolean isNaturalAtomicValue( Ugwi aUgwi ) {
    TsNullArgumentRtException.checkNull( aUgwi );
    TsIllegalArgumentRtException.checkFalse( aUgwi.kindId().equals( id() ) );
    return doIsNaturalAtomicValue( aUgwi );
  }

  @Override
  final public IAtomicValue getAtomicValue( Ugwi aUgwi ) {
    TsNullArgumentRtException.checkNull( aUgwi );
    TsIllegalArgumentRtException.checkFalse( aUgwi.kindId().equals( id() ) );
    IAtomicValue av = doFindAtomicValue( aUgwi );
    return av != null ? av : IAtomicValue.NULL;
  }

  @Override
  final public IDataType getAtomicValueDataType( Ugwi aUgwi ) {
    TsNullArgumentRtException.checkNull( aUgwi );
    TsIllegalArgumentRtException.checkFalse( aUgwi.kindId().equals( id() ) );
    IDataType dt = doGetAtomicValueDataType( aUgwi );
    return dt != null ? dt : IAvMetaConstants.DDEF_NONE;
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
   * Implementation must perform check if this kind can be registered by
   * {@link ISkUgwiService#registerKind(AbstractSkUgwiKind)}.
   * <p>
   * For example, implementation may check if {@link SkCoreServUgwis#coreApi()} has the service
   * <code>ISkXxxServce</code> needed to handle this kind of UGWI.
   *
   * @param aUgwiService {@link SkCoreServUgwis} - the UGWI service
   * @return boolean - <code>true</code> if kind can be registered
   */
  protected abstract boolean doCanRegister( SkCoreServUgwis aUgwiService );

  /**
   * Implementation must return the content as an atomic value.
   *
   * @param aUgwi {@link Ugwi} - the UGWI of this kind
   * @return boolean - <code>true</code> UGWI content may be represented as atomic value
   */
  protected abstract boolean doIsNaturalAtomicValue( Ugwi aUgwi );

  /**
   * Implementation must return the content as an atomic value.
   * <p>
   * Method may return null if the content does not exists.
   *
   * @param aUgwi {@link Ugwi} - the UGWI of this kind
   * @return {@link IAtomicValue} - content as atomic value or {@link IAtomicValue#NULL} or <code>null</code>
   */
  protected abstract IAtomicValue doFindAtomicValue( Ugwi aUgwi );

  /**
   * Implementation must return the data type.
   * <p>
   * Method may return null if the meta information about does not exists.
   *
   * @param aUgwi {@link Ugwi} - the UGWI of this kind
   * @return {@link IDataType} - the meta information or {@link IAvMetaConstants#DDEF_NONE} or <code>null</code>
   */
  protected abstract IDataType doGetAtomicValueDataType( Ugwi aUgwi );

}
