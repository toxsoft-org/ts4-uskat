package org.toxsoft.uskat.core.impl.helpers;

import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.txtmatch.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * Helper class to implement {@link ISkSysdescrValidator}.
 *
 * @author hazard157
 */
public class ClassClaimingSysdescrValidator
    implements ISkSysdescrValidator {

  private final String             serviceId;
  private final IList<TextMatcher> rules;

  /**
   * Constructor.
   *
   * @param aServiceId String - ID of the service that claims classes
   * @param aRules {@link IList}&lt;{@link TextMatcher}&gt; - list of claiming rules
   */
  public ClassClaimingSysdescrValidator( String aServiceId, IList<TextMatcher> aRules ) {
    serviceId = StridUtils.checkValidIdPath( aServiceId );
    rules = new ElemArrayList<>( aRules );
  }

  // ------------------------------------------------------------------------------------
  // ISkSysdescrValidator
  //

  @Override
  public ValidationResult canCreateClass( IDtoClassInfo aNewClassInfo ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult canEditClass( IDtoClassInfo aNewClassInfo, ISkClassInfo aOldClassInfo ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult canRemoveClass( String aClassId ) {
    // TODO Auto-generated method stub
    return null;
  }

}
