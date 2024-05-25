package org.toxsoft.uskat.core.utils.ugwi;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;

/**
 * The read-only interface to the list of UGWIs.
 *
 * @author hazard157
 */
public interface IUgwiList {

  /**
   * Always empty unmodifiable instance.
   */
  IUgwiList EMPTY = new InternalEmptyUgwiList();

  /**
   * Returns the UGWIs in this list.
   *
   * @return {@link IList}&lt;{@link Ugwi}&gt; - items in this list
   */
  IList<Ugwi> items();

  /**
   * Returns the kind IDs of UGWIs in the list.
   *
   * @return {@link IStringList} - collected kind IDs
   */
  IStringList listKindIds();

  /**
   * Returns UGWI of specified kind.
   *
   * @param aKindId String - the kind ID
   * @return {@link IList}&lt;{@link Ugwi}&gt; - UGWIs extracted from {@link #items()}
   */
  IList<Ugwi> listByKindId( String aKindId );

}

class InternalEmptyUgwiList
    implements IUgwiList {

  @Override
  public IList<Ugwi> items() {
    return IList.EMPTY;
  }

  @Override
  public IStringList listKindIds() {
    return IStringList.EMPTY;
  }

  @Override
  public IList<Ugwi> listByKindId( String aKindId ) {
    return IList.EMPTY;
  }

}
