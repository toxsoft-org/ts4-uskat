package org.toxsoft.uskat.s5.utils.jobs;

import static org.toxsoft.core.log4j.Logger.*;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;

/**
 * Оболочка для серверной задачи
 *
 * @author mvk
 */
public class S5ServerJobWrapper
    implements IS5ServerJob {

  private final boolean safe;
  private IS5ServerJob  job;
  private final ILogger logger;

  /**
   * Конструктор
   *
   * @param aJob {@link IS5ServerJob} задача
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5ServerJobWrapper( IS5ServerJob aJob ) {
    this( aJob, false, getLogger( S5ServerJobWrapper.class ) );
  }

  /**
   * Конструктор
   *
   * @param aJob {@link IS5ServerJob} задача
   * @param aSafe boolean <b>true</b> продолжать выполнение задачи при возникновении ошибок
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5ServerJobWrapper( IS5ServerJob aJob, boolean aSafe ) {
    this( aJob, aSafe, getLogger( S5ServerJobWrapper.class ) );
  }

  /**
   * Конструктор
   *
   * @param aJob {@link IS5ServerJob} задача
   * @param aLogger {@link ILogger} журнал задачи
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5ServerJobWrapper( IS5ServerJob aJob, ILogger aLogger ) {
    this( aJob, false, aLogger );
  }

  /**
   * Конструктор
   *
   * @param aJob {@link IS5ServerJob} задача
   * @param aSafe boolean <b>true</b> продолжать выполнение задачи при возникновении ошибок
   * @param aLogger {@link ILogger} журнал задачи
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5ServerJobWrapper( IS5ServerJob aJob, boolean aSafe, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aJob, aLogger );
    job = aJob;
    safe = aSafe;
    logger = aLogger;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5ServerJob
  //
  @Override
  public void doJobPrepare() {
    if( job == null ) {
      return;
    }
    try {
      job.doJobPrepare();
    }
    catch( Throwable e ) {
      logger.error( e );
      if( !safe ) {
        job = null;
      }
    }
  }

  @Override
  public void doJob() {
    if( job == null ) {
      return;
    }
    try {
      job.doJob();
    }
    catch( Throwable e ) {
      logger.error( e );
      if( !safe ) {
        job = null;
      }
    }
  }

  @Override
  public boolean completed() {
    if( job == null ) {
      return true;
    }
    try {
      boolean completed = job.completed();
      if( completed ) {
        job = null;
      }
      return completed;
    }
    catch( Throwable e ) {
      logger.error( e );
      if( !safe ) {
        job = null;
        return true;
      }
    }
    return false;
  }

  @Override
  public void close() {
    if( job == null ) {
      return;
    }
    try {
      job.close();
    }
    catch( Throwable e ) {
      logger.error( e );
    }
    job = null;
  }
}
