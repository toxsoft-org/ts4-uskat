package org.toxsoft.uskat.core.api.users.acl;

import org.toxsoft.uskat.core.backend.acl.*;

/**
 * USkat support for role-based access rights to the data.
 * <p>
 * All the data in USkat belongs to the "Green World" entities, that is any data is denoted by GWID - Green World
 * IDentifier. These are class (meta-data), objects and their properties such as attributes, RTdata, commands, etc. Any
 * extension to these entities like refbooks, mnemoschemes, RRI, etc are denoted by UGWI (Universal Green World
 * Identifier). However any UGWI entities are mapped (implemented by) on GWID entities.
 * <p>
 * USkat defines access rights to any particular GWID as {@link ESkAccess}: GWID may be inaccessible, may be just read
 * or read and modified (written). Any role in the system has it's own set of the access rights to the GWIDs.
 * <p>
 * TODO about roles
 *
 * @author hazard157
 */
public interface IAccessRightsManager {

  // nop
}
