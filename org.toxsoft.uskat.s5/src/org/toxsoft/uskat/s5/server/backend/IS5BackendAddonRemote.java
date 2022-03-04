package org.toxsoft.uskat.s5.server.backend;

import javax.ejb.Remote;

import org.toxsoft.core.tslib.bricks.strid.IStridable;

/**
 * Удаленный доступ к расширению backend предоставляемый s5-сервером
 *
 * @author mvk
 */
@Remote
public interface IS5BackendAddonRemote
    extends IStridable {
  // nop
}
