package org.toxsoft.uskat.regref.lib.impl;

import org.toxsoft.core.tslib.bricks.strid.impl.StridableParameterizedSer;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoAttrInfo;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoLinkInfo;
import org.toxsoft.uskat.regref.lib.ISkRriParamInfo;

/**
 * Реализация {@link ISkRriParamInfo}
 *
 * @author mvk
 */
class SkRriParamInfo
    extends StridableParameterizedSer
    implements ISkRriParamInfo {

  private static final long serialVersionUID = 4997500209361567648L;

  private final IDtoAttrInfo attrInfo;
  private final IDtoLinkInfo linkInfo;

  /**
   * Конструктор параметра НСИ типа 'атрибут'
   *
   * @param aAttrInfo {@link IDtoAttrInfo} описание атрибута
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор не ИД-путь
   */
  SkRriParamInfo( IDtoAttrInfo aAttrInfo ) {
    super( aAttrInfo.id(), aAttrInfo.params() );
    attrInfo = TsNullArgumentRtException.checkNull( aAttrInfo );
    linkInfo = null;
  }

  /**
   * Конструктор параметра НСИ типа 'связь'
   *
   * @param aLinkInfo {@link IDtoAttrInfo} описание атрибута
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор не ИД-путь
   */
  SkRriParamInfo( IDtoLinkInfo aLinkInfo ) {
    super( aLinkInfo.id(), aLinkInfo.params() );
    attrInfo = null;
    linkInfo = TsNullArgumentRtException.checkNull( aLinkInfo );
  }

  // ------------------------------------------------------------------------------------
  // IRriParamInfo
  //

  @Override
  public boolean isLink() {
    return (linkInfo != null);
  }

  @Override
  public IDtoAttrInfo attrInfo() {
    TsUnsupportedFeatureRtException.checkTrue( isLink() );
    return attrInfo;
  }

  @Override
  public IDtoLinkInfo linkInfo() {
    TsUnsupportedFeatureRtException.checkFalse( isLink() );
    return linkInfo;
  }
}
