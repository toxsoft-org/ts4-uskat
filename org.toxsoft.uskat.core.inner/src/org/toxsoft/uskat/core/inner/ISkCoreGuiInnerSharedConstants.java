package org.toxsoft.uskat.core.inner;

import org.toxsoft.core.tslib.utils.gui.*;

/**
 * GUI-related constants to be shared between GUI and LIB plugins.
 * <p>
 * <code><b>org.toxsoft.uskat.core</b></code> itself is not a GUI library. However some entities from library are
 * assumed to have GUI representation when used in GUI environment. This interface lists icon and other graphical entity
 * IDs for such entities. <code><b>org.toxsoft.uskat.core.inner</b></code> does <b>not</b> contains icons/entities
 * itself, just IDs.
 * <p>
 * Other applicable constants may be found in {@link ITsLibInnerSharedConstants}.
 *
 * @author hazard157
 * @see ITsLibInnerSharedConstants
 */
@SuppressWarnings( "javadoc" )
public interface ISkCoreGuiInnerSharedConstants {

  // ------------------------------------------------------------------------------------
  // Class ID selector VALEDs

  String SKCGC_VALED_CLASS_ID_SELECTOR                          = "ts.valed.ClassIdSelector";           //$NON-NLS-1$
  String SKCGC_VALED_CLASS_IDS_LIST_SELECTOR                    = "ts.valed.ClassIdsListSelector";      //$NON-NLS-1$
  String SKCGC_VALED_AV_CLASS_ID_SELECTOR                       = "ts.valed.AvClassIdSelector";         //$NON-NLS-1$
  String SKCGC_VALED_AV_CLASS_IDS_LIST_SELECTOR                 = "ts.valed.AvClassIdsListSelector";    //$NON-NLS-1$
  String SKCGC_VALED_OPID_CLASS_SELECTOR_SINGLE_BASE_CLASS_ID   = "ts.valed.opid.SingleBaseClassId";    //$NON-NLS-1$
  String SKCGC_VALED_OPID_CLASS_SELECTOR_CLASS_IDS_LIST         = "ts.valed.opid.ClassIdsList";         //$NON-NLS-1$
  String SKCGC_VALED_OPID_CLASS_SELECTOR_IS_SUBCLASSES_INCLUDED = "ts.valed.opid.IsSubclassesIncluded"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // SKID selector VALEDs

  String SKCGC_VALED_SKID_SELECTOR                             = "ts.valed.SkidSelector";                               //$NON-NLS-1$
  String SKCGC_VALED_SKID_LIST_SELECTOR                        = "ts.valed.SkidListSelector";                           //$NON-NLS-1$
  String SKCGC_VALED_AV_SKID_SELECTOR                          = "ts.valed.AvSkidSelector";                             //$NON-NLS-1$
  String SKCGC_VALED_AV_SKID_LIST_SELECTOR                     = "ts.valed.AvSkidListSelector";                         //$NON-NLS-1$
  String SKCGC_VALED_OPID_SKID_SELECTOR_SINGLE_BASE_CLASS_ID   = SKCGC_VALED_OPID_CLASS_SELECTOR_SINGLE_BASE_CLASS_ID;
  String SKCGC_VALED_OPID_SKID_SELECTOR_CLASS_IDS_LIST         = SKCGC_VALED_OPID_CLASS_SELECTOR_CLASS_IDS_LIST;
  String SKCGC_VALED_OPID_SKID_SELECTOR_IS_SUBCLASSES_INCLUDED = SKCGC_VALED_OPID_CLASS_SELECTOR_IS_SUBCLASSES_INCLUDED;

  // ------------------------------------------------------------------------------------
  // GWID selector VALEDs

  String SKCGC_VALED_GWID_SELECTOR                             = "ts.valed.GwidSelector";                               //$NON-NLS-1$
  String SKCGC_VALED_GWID_LIST_SELECTOR                        = "ts.valed.GwidListSelector";                           //$NON-NLS-1$
  String SKCGC_VALED_AV_GWID_SELECTOR                          = "ts.valed.AvGwidSelector";                             //$NON-NLS-1$
  String SKCGC_VALED_AV_GWID_LIST_SELECTOR                     = "ts.valed.AvGwidListSelector";                         //$NON-NLS-1$
  String SKCGC_VALED_OPID_GWID_SELECTOR_SINGLE_BASE_CLASS_ID   = SKCGC_VALED_OPID_CLASS_SELECTOR_SINGLE_BASE_CLASS_ID;
  String SKCGC_VALED_OPID_GWID_SELECTOR_CLASS_IDS_LIST         = SKCGC_VALED_OPID_CLASS_SELECTOR_CLASS_IDS_LIST;
  String SKCGC_VALED_OPID_GWID_SELECTOR_IS_SUBCLASSES_INCLUDED = SKCGC_VALED_OPID_CLASS_SELECTOR_IS_SUBCLASSES_INCLUDED;
  String SKCGC_VALED_OPID_GWID_SELECTOR_IS_ABSTRACT_GWID       = "ts.valed.opid.IsAbstractGwid";                        //$NON-NLS-1$
  String SKCGC_VALED_OPID_GWID_SELECTOR_GWID_SELECTION_OPTION  = "ts.valed.opid.GwidSelectionOption";                   //$NON-NLS-1$
  String SKCGC_VALED_OPID_GWID_SELECTOR_SINGLE_GWID_KIND_ID    = "ts.valed.opid.SingleGwidKindId";                      //$NON-NLS-1$
  String SKCGC_VALED_OPID_GWID_SELECTOR_GWID_KIND_IDS_LIST     = "ts.valed.opid.GwidKindIdsList";                       //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // FIXME UGWI selection VALEDs
  //

  // String SKCGC_VALED_UGWI_SELECTOR = "ts.valed.UgwiSelector"; //$NON-NLS-1$
  // String SKCGC_VALED_UGWI_LIST_SELECTOR = "ts.valed.UgwiListSelector"; //$NON-NLS-1$
  // String SKCGC_VALED_AV_UGWI_SELECTOR = "ts.valed.AvUgwiSelector"; //$NON-NLS-1$
  // String SKCGC_VALED_AV_UGWI_LIST_SELECTOR = "ts.valed.AvUgwiListSelector"; //$NON-NLS-1$
  // String SKCGC_VALED_GWID_AS_UGWI_SELECTOR = "ts.valed.GwidAsUgwiSelector"; //$NON-NLS-1$
  // String SKCGC_VALED_GWID_AS_UGWI_LIST_SELECTOR = "ts.valed.GwidAsUgwiListSelector"; //$NON-NLS-1$
  // String SKCGC_VALED_AV_GWID_AS_UGWI_SELECTOR = "ts.valed.AvGwidAsUgwiSelector"; //$NON-NLS-1$
  // String SKCGC_VALED_AV_GWID_AS_UGWI_LIST_SELECTOR = "ts.valed.AvGwidAsUgwiListSelector"; //$NON-NLS-1$
  // String SKCGC_VALED_OPID_UGWI_SELECTOR_SINGLE_UGWI_KIND_ID = "ts.valed.opid.SingleUgwiKindId"; //$NON-NLS-1$
  // String SKCGC_VALED_OPID_UGWI_SELECTOR_UGWI_KIND_IDS_LIST = "ts.valed.opid.UgwiKindIdsList"; //$NON-NLS-1$

  // UGWI -> classID
  // UGWI -> SKID
  // UGWI -> GWID

  // FIXME other UGWIs

  // ------------------------------------------------------------------------------------
  // old - before 2026-01-02
  //

  String SKCGC_VALED_UGWI_SELECTOR                          = "ts.valed.UgwiSelector";                         //$NON-NLS-1$
  String SKCGC_VALED_AV_UGWI_SELECTOR                       = "ts.valed.AvValobjUgwiSelector";                 //$NON-NLS-1$
  String SKCGC_VALED_UGWI_SELECTOR_OPID_SINGLE_UGWI_KIND_ID = "ts.valed.UgwiSelector.option.SingleUgwiKindId"; //$NON-NLS-1$
  String SKCGC_VALED_UGWI_SELECTOR_OPID_UGWI_KIND_IDS_LIST  = "ts.valed.UgwiSelector.option.UgwiKindIdsList";  //$NON-NLS-1$

  String SKCGC_VALED_CONCRETE_GWID_EDITOR_NAME                = "ts.valed.ConcreteGwidEditor";                 //$NON-NLS-1$
  String SKCGC_VALED_CONCRETE_GWID_EDITOR_NAME_OPID_GWID_KIND = "ts.valed.ConcreteGwidEditor.option.GwidKind"; //$NON-NLS-1$
  String SKCGC_VALED_AV_CONCRETE_GWID_EDITOR_NAME             = "ts.valed.AvValobjConcreteGwidEditor";         //$NON-NLS-1$
  String SKCGC_VALED_AV_UGWI_SELECTOR_TEXT_AND_BUTTON         = "ts.valed.AvValobjUgwiSelectorTextAndButton";  //$NON-NLS-1$

}
