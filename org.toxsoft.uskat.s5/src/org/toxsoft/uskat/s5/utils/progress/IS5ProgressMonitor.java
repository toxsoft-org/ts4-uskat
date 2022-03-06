package org.toxsoft.uskat.s5.utils.progress;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullObjectErrorRtException;

/**
 * Интерфейс монитора прогресса длительных операций
 * <p>
 * Повторяет org.core.eclipse.runtime.IProgressMonitor чтобы не устанавливать зависимость от пакетов eclipse
 *
 * @author mvk
 */
public interface IS5ProgressMonitor {

  /**
   * Несуществующий монитор
   */
  IS5ProgressMonitor NULL = new InternalNullConnectionProgressMonitor();

  /**
   * Запуск задачи
   *
   * @param aTaskName String имя задачи
   * @param aTotalSteps int общее количество этапов выполнения задачи
   */
  void beginTask( String aTaskName, int aTotalSteps );

  /**
   * Изменение имени задачи
   *
   * @param aTaskName String имя задачи
   */
  void setTaskName( String aTaskName );

  /**
   * Установка имени подзадачи
   *
   * @param aSubTask String имя подзадачи
   */
  void subTask( String aSubTask );

  /**
   * Установка номера выполненного этапа задачи
   *
   * @param aProgressStep int этап задачи
   */
  void worked( int aProgressStep );

  /**
   * Оповещение о выполнении задачи
   */
  void done();

  /**
   * Оповещение об отмене задачи
   *
   * @return boolean <b>true</b> задача отменена;<b>false</b> требуется продолжение выполнения задачи.
   */
  boolean isCanceled();

  /**
   * Установка значения требования об отмене задачи
   *
   * @param aCanceled <b>true</b> отменить задачу;<b>false</b> продолжить задачу.
   */
  void setCanceled( boolean aCanceled );

  /**
   * TODO: ???
   *
   * @param arg0 double
   */
  void internalWorked( double arg0 );
}

/**
 * Реализация несуществующего описания соединения {@link IS5ProgressMonitor#NULL}.
 */
class InternalNullConnectionProgressMonitor
    implements IS5ProgressMonitor, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Метод корректно восстанавливает сериализированный {@link IS5ProgressMonitor#NULL}.
   *
   * @return Object объект {@link IS5ProgressMonitor#NULL}
   * @throws ObjectStreamException это обявление, оно тут не выбрасывается
   */
  @SuppressWarnings( { "static-method" } )
  private Object readResolve()
      throws ObjectStreamException {
    return IS5ProgressMonitor.NULL;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов IS5ProgressMonitor
  //
  @Override
  public void beginTask( String aTaskName, int aTotalSteps ) {
    // nop
  }

  @Override
  public void setTaskName( String aTaskName ) {
    // nop
  }

  @Override
  public void subTask( String aSubTask ) {
    // nop
  }

  @Override
  public void worked( int aProgressStep ) {
    // nop
  }

  @Override
  public void done() {
    // nop
  }

  @Override
  public boolean isCanceled() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public void setCanceled( boolean aCanceled ) {
    // nop
  }

  @Override
  public void internalWorked( double arg0 ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов Object
  //
  @Override
  public int hashCode() {
    return TsLibUtils.INITIAL_HASH_CODE;
  }

  @Override
  public boolean equals( Object obj ) {
    return obj == this;
  }

  @Override
  public String toString() {
    return IS5ProgressMonitor.class.getSimpleName() + ".NULL"; //$NON-NLS-1$
  }
}
