package org.toxsoft.uskat.regref.lib.impl;

import java.io.Serializable;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.coll.IMapEdit;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.Skop;
import org.toxsoft.uskat.regref.lib.ISkRriParamValues;

/**
 * Неизменяемая руеализация {@link ISkRriParamValues}.
 *
 * @author mvk
 */
public class SkRriParamValues
    implements ISkRriParamValues, Serializable {

  private static final long serialVersionUID = 157157L;

  private final IMapEdit<Skop, IAtomicValue> attrs;
  private final IMapEdit<Skop, ISkidList>    links;

  private transient ISkidList objSkids = null;

  private transient IMap<Skid, IStringMap<IAtomicValue>> attrsByObj = null;
  private transient IMap<Skid, IStringMap<ISkidList>>    linksByObj = null;

  /**
   * Конструктор.
   *
   * @param aAttrs {@link IMap}&lt;{@link Skid},{@link IAtomicValue}&gt; - карта "объект/параметр" - "значение
   *          параметра"
   * @param aLinks {@link IMap}&lt;{@link Skid},{@link ISkidList}&gt; - карта "объект/параметр" - "ИДы связанных
   *          объектов"
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public SkRriParamValues( IMap<Skop, IAtomicValue> aAttrs, IMap<Skop, ISkidList> aLinks ) {
    TsNullArgumentRtException.checkNull( aAttrs );
    TsNullArgumentRtException.checkNull( aLinks );
    attrs = new ElemMap<>();
    attrs.putAll( aAttrs );
    links = new ElemMap<>();
    links.putAll( aLinks );
  }

  /**
   * Конструктор.
   */
  public SkRriParamValues() {
    attrs = new ElemMap<>();
    links = new ElemMap<>();
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkRriParamValues
  //

  @Override
  public IMapEdit<Skop, IAtomicValue> attrParams() {
    return attrs;
  }

  @Override
  public IMapEdit<Skop, ISkidList> linkParams() {
    return links;
  }

  @Override
  public ISkidList listObjSkids() {
    if( objSkids == null ) {
      SkidList ll = new SkidList();
      for( Skop skop : attrs.keys() ) {
        if( !ll.hasElem( skop.skid() ) ) {
          ll.add( skop.skid() );
        }
      }
      for( Skop skop : links.keys() ) {
        if( !ll.hasElem( skop.skid() ) ) {
          ll.add( skop.skid() );
        }
      }
      objSkids = ll;
    }
    return objSkids;
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IStringMap<IAtomicValue> getAttrParamsOfObj( Skid aObjId ) {
    TsNullArgumentRtException.checkNull( aObjId );
    if( attrsByObj == null ) {
      attrsByObj = new ElemMap<>();
    }
    IStringMap<IAtomicValue> retValue = attrsByObj.findByKey( aObjId );
    if( retValue == null ) {
      retValue = new StringMap<>();
      for( Skop skop : attrs.keys() ) {
        if( skop.skid().equals( aObjId ) ) {
          ((IStringMapEdit<IAtomicValue>)retValue).put( skop.propId(), attrs.getByKey( skop ) );
        }
      }
      ((IMapEdit)attrsByObj).put( aObjId, retValue );
    }
    return retValue;
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IStringMap<ISkidList> getLinkParamsOfObj( Skid aObjId ) {
    TsNullArgumentRtException.checkNull( aObjId );
    if( linksByObj == null ) {
      linksByObj = new ElemMap<>();
    }
    IStringMap<ISkidList> retValue = linksByObj.findByKey( aObjId );
    if( retValue == null ) {
      retValue = new StringMap<>();
      for( Skop skop : links.keys() ) {
        if( skop.skid().equals( aObjId ) ) {
          ((IStringMapEdit<ISkidList>)retValue).put( skop.propId(), links.getByKey( skop ) );
        }
      }
      ((IMapEdit)linksByObj).put( aObjId, retValue );
    }
    return retValue;
  }

}
