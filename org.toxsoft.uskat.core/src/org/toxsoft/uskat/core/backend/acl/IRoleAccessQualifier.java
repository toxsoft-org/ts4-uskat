package org.toxsoft.uskat.core.backend.acl;

import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.api.sysdescr.*;

/**
 * Qualifies access rights of the particular role.
 * <p>
 * Most backend addons may and should use {@link #gwidQualifier()} to determine if particular user/role may perform
 * requested action. However, for the sake of optimizations Sk-class and Sk-object qualifiers are declared.
 *
 * @author hazard157
 */
public interface IRoleAccessQualifier {

  /**
   * Returns access rights qualifier to the specified Sk-class.
   * <p>
   * Implementation of this interface guarantees that both qualifiers {@link #classInfoQualifier()} and
   * {@link #gwidQualifier()} returns the same access rights for the class represented as {@link ISkClassInfo} or
   * abstract GWID.
   *
   * @return {@link ISkAccessQualifier}&lt;{@link ISkClassInfo}&gt; - access qualifier, never is <code>null</code>
   */
  ISkAccessQualifier<ISkClassInfo> classInfoQualifier();

  /**
   * Returns access rights qualifier to the Sk-object specified by it's SKID.
   * <p>
   * Implementation of this interface guarantees that both qualifiers {@link #objectQualifier()} and
   * {@link #gwidQualifier()} returns the same access rights for the Sk-object represented as {@link Skid} or concrete
   * GWID.
   *
   * @return {@link ISkAccessQualifier}&lt;{@link Skid}&gt; - access qualifier, never is <code>null</code>
   */
  ISkAccessQualifier<Skid> objectQualifier();

  /**
   * Returns access rights qualifier to the entity specified by it's GWID.
   * <p>
   * General form of the GWID is:<br>
   * <code>classId[STRID_ID]$<b>prop_sect_id</b>(PROP_ID)$<b>sub_prop_sect_id</b>(SUB_PROP_ID)</code>
   * <p>
   * When no property (and hence sub-property) GWID refers either to the Sk-class (an abstract GWID) or to the Sk-object
   * (a concrete GWID). For abstract GWIDs this qualifier has same behaviour as as the class qualifier
   * {@link #classInfoQualifier()}. For concrete GWIDs - the same as {@link #objectQualifier()}.
   * <p>
   * When the property/sub-property is specified, by definition such entity may have equal or less rights than
   * corresponding Sk-class (for abstract GWIDs) or Sk-object (for concrete GWIDs). For example, attribute of the
   * read-only object never can have write rights.
   * <p>
   * Note about multi-GWID arguments. Any multi-GWID is considered as a kind of query "what access rights has such set
   * of GWIDs?". Answer to this query is very conservative, pessimistic: qualifier returns least access rights found in
   * the set of GWIDs.
   * <p>
   * TODO describe more details about multi-GWID queries!
   *
   * @return {@link ISkAccessQualifier}&lt;{@link Gwid}&gt; - access qualifier, never is <code>null</code>
   */
  ISkAccessQualifier<Gwid> gwidQualifier();

}
