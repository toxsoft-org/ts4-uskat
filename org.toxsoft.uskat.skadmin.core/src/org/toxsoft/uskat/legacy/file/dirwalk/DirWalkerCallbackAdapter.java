package org.toxsoft.uskat.legacy.file.dirwalk;

import java.io.File;

/**
 * Заглушка для методов интерфейса {@link IDirWalkerCallback}.
 *
 * @author goga
 */
public class DirWalkerCallbackAdapter
    implements IDirWalkerCallback {

  /**
   * Конструктор.
   */
  public DirWalkerCallbackAdapter() {
    // Пустой конструктор
  }

  @Override
  public void beforeStart( File aStartDir )
      throws DirWalkerCanceledException {
    // Ничего не делает
  }

  @Override
  public boolean dirEnter( File aDir, File[] aFilesToBeProcessed, File[] aSubDirs )
      throws DirWalkerCanceledException {
    return false;
    // Ничего не делает
  }

  @Override
  public void processStart( File aDir, int aCount )
      throws DirWalkerCanceledException {
    // Ничего не делает
  }

  @Override
  public void processFile( File aDir, File aFile )
      throws DirWalkerCanceledException {
    // Ничего не делает
  }

  @Override
  public void processFinish( File aDir )
      throws DirWalkerCanceledException {
    // Ничего не делает
  }

  @Override
  public void dirExit( File aDir )
      throws DirWalkerCanceledException {
    // Ничего не делает
  }

  @Override
  public void afterFinish( boolean aWasCancelled ) {
    // Ничего не делает
  }

}
