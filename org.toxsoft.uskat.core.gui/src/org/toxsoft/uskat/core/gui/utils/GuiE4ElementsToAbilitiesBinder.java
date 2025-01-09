package org.toxsoft.uskat.core.gui.utils;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.mws.services.e4helper.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.api.users.ability.*;
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Class binds E4 GUI items visibility to the {@link ISkAbility} allowance state.
 * <p>
 * Usage:
 * <ul>
 * <li>create the instance of this class;</li>
 * <li>bind GUI elements to ability state with methods <code>bindXxx()</code>;</li>
 * <li>register instance with {@link SkCoreUtils#registerCoreApiHandler(ISkCoreExternalHandler)}.</li>
 * </ul>
 *
 * @author hazard157
 */
public final class GuiE4ElementsToAbilitiesBinder
    implements ISkCoreExternalHandler {

  /**
   * Which type of E4 GUI elements can be bind to ability.
   *
   * @author hazard157
   */
  enum EGuiE4ElementType {
    PERSPECTIVE,
    UIPART,
    MENU_ITEM,
    TOOL_ITEM
  }

  /**
   * An item of binding.
   *
   * @author hazard157
   * @param abilityId String - the ability ID
   * @param elementType {@link EGuiE4ElementType} - the GUI element type
   * @param elementId String - - the GUI element type
   * @param isInverted boolean - invert binding action
   */
  static record AbilityGuiBinding ( String abilityId, EGuiE4ElementType elementType, String elementId,
      boolean isInverted ) {

    AbilityGuiBinding( String abilityId, EGuiE4ElementType elementType, String elementId, boolean isInverted ) {
      StridUtils.checkValidIdPath( abilityId );
      TsNullArgumentRtException.checkNulls( elementType, elementId );
      this.abilityId = abilityId;
      this.elementType = elementType;
      this.elementId = elementId;
      this.isInverted = isInverted;
    }

  }

  private final ITsGuiContext                tsContext;
  private final IListEdit<AbilityGuiBinding> bindingsList = new ElemArrayList<>();

  /**
   * Constructor.
   */
  public GuiE4ElementsToAbilitiesBinder( ITsGuiContext aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    tsContext = aContext;
  }

  // ------------------------------------------------------------------------------------
  // ISkCoreExternalHandler
  //

  @Override
  public void processSkCoreInitialization( IDevCoreApi aCoreApi ) {
    ISkAbilityManager abMan = aCoreApi.userService().abilityManager();
    ITsE4Helper e4Helper = tsContext.get( ITsE4Helper.class );
    for( AbilityGuiBinding b : bindingsList ) {
      boolean visible = abMan.isAbilityAllowed( b.abilityId );
      if( b.isInverted ) {
        visible = !visible;
      }
      switch( b.elementType ) {
        case PERSPECTIVE: {
          e4Helper.setPrerspectiveVisible( b.elementId, visible );
          break;
        }
        case UIPART: {
          e4Helper.setUipartVisible( b.elementId, visible );
          break;
        }
        case MENU_ITEM: {
          e4Helper.setMenuItemVisible( b.elementId, visible );
          break;
        }
        case TOOL_ITEM: {
          e4Helper.setToolItemVisible( b.elementId, visible );
          break;
        }
        default:
          throw new IllegalArgumentException( b.elementType.name() );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Binds perspective to the ability.
   * <p>
   * If ability is disabled for current role of the Sk-connection, perspective will be invisible.
   *
   * @param aAbilityId String - the ability ID
   * @param aPerspectiveId String - the perspective ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ability ID is not an IDpath
   */
  public void bindPerspective( String aAbilityId, String aPerspectiveId ) {
    bindPerspective( aAbilityId, aPerspectiveId, false );
  }

  /**
   * Binds perspective to the ability.
   * <p>
   * Normally if ability is disabled for current role of the Sk-connection, perspective will be invisible. However if
   * <code>aInverted</code> argument is <code>true</code>, perspective will be disabled if ability is enabled.
   *
   * @param aAbilityId String - the ability ID
   * @param aPerspectiveId String - the perspective ID
   * @param aInverted boolean - the flag to invert visibility
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ability ID is not an IDpath
   */
  public void bindPerspective( String aAbilityId, String aPerspectiveId, boolean aInverted ) {
    AbilityGuiBinding b = new AbilityGuiBinding( aAbilityId, EGuiE4ElementType.PERSPECTIVE, aPerspectiveId, aInverted );
    bindingsList.add( b );
  }

  /**
   * Binds UIpart to the ability.
   * <p>
   * If ability is disabled for current role of the Sk-connection, UIpart will be invisible.
   *
   * @param aAbilityId String - the ability ID
   * @param aUipartId String - the UIpart ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ability ID is not an IDpath
   */
  public void bindUipart( String aAbilityId, String aUipartId ) {
    bindUipart( aAbilityId, aUipartId, false );
  }

  /**
   * Binds UIpart to the ability.
   * <p>
   * Normally if ability is disabled for current role of the Sk-connection, UIpart will be invisible. However if
   * <code>aInverted</code> argument is <code>true</code>, UIpart will be disabled if ability is enabled.
   *
   * @param aAbilityId String - the ability ID
   * @param aUipartId String - the UIpart ID
   * @param aInverted boolean - the flag to invert visibility
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ability ID is not an IDpath
   */
  public void bindUipart( String aAbilityId, String aUipartId, boolean aInverted ) {
    AbilityGuiBinding b = new AbilityGuiBinding( aAbilityId, EGuiE4ElementType.UIPART, aUipartId, aInverted );
    bindingsList.add( b );
  }

  /**
   * Binds menu element to the ability.
   * <p>
   * If ability is disabled for current role of the Sk-connection, menu element will be invisible.
   *
   * @param aAbilityId String - the ability ID
   * @param aMenuElementId String - the menu element ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ability ID is not an IDpath
   */
  public void bindMenuElement( String aAbilityId, String aMenuElementId ) {
    bindMenuElement( aAbilityId, aMenuElementId, false );
  }

  /**
   * Binds menu element to the ability.
   * <p>
   * Normally if ability is disabled for current role of the Sk-connection, menu element will be invisible. However if
   * <code>aInverted</code> argument is <code>true</code>, menu element will be disabled if ability is enabled.
   *
   * @param aAbilityId String - the ability ID
   * @param aMenuElementId String - the menu element ID
   * @param aInverted boolean - the flag to invert visibility
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ability ID is not an IDpath
   */
  public void bindMenuElement( String aAbilityId, String aMenuElementId, boolean aInverted ) {
    AbilityGuiBinding b = new AbilityGuiBinding( aAbilityId, EGuiE4ElementType.MENU_ITEM, aMenuElementId, aInverted );
    bindingsList.add( b );
  }

  /**
   * Binds tool item to the ability.
   * <p>
   * If ability is disabled for current role of the Sk-connection, tool item will be invisible.
   *
   * @param aAbilityId String - the ability ID
   * @param aToolItemId String - the tool item ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ability ID is not an IDpath
   */
  public void bindToolItem( String aAbilityId, String aToolItemId ) {
    bindToolItem( aAbilityId, aToolItemId, false );
  }

  /**
   * Binds tool item to the ability.
   * <p>
   * Normally if ability is disabled for current role of the Sk-connection, tool item will be invisible. However if
   * <code>aInverted</code> argument is <code>true</code>, tool item will be disabled if ability is enabled.
   *
   * @param aAbilityId String - the ability ID
   * @param aToolItemId String - the tool item ID
   * @param aInverted boolean - the flag to invert visibility
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ability ID is not an IDpath
   */
  public void bindToolItem( String aAbilityId, String aToolItemId, boolean aInverted ) {
    AbilityGuiBinding b = new AbilityGuiBinding( aAbilityId, EGuiE4ElementType.TOOL_ITEM, aToolItemId, aInverted );
    bindingsList.add( b );
  }

}
