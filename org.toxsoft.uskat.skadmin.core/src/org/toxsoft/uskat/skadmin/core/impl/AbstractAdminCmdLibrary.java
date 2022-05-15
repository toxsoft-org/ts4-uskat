package org.toxsoft.uskat.skadmin.core.impl;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.skadmin.core.impl.IAdminResources.*;

import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.core.*;

/**
 * Абстрактная библиотека команд
 *
 * @author mvk
 */
public abstract class AbstractAdminCmdLibrary
    implements IAdminCmdLibrary {

  private IStringMapEdit<IAdminCmd>   cmdMap     = new StringMap<>();
  private IListEdit<IAdminCmdDef>     cmdList    = new ElemLinkedList<>();
  private ILogger                     logger     = getLogger( getClass() );
  private IListEdit<AbstractAdminCmd> clonedCmds = new ElemLinkedList<>();

  /**
   * Признак завершения работы плагина
   */
  private boolean closed;

  /**
   * Текущий контекст выполнения команд
   */
  private IAdminCmdContext context;

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IAdminCmdLibrary
  //
  @Override
  public void close() {
    TsIllegalStateRtException.checkTrue( closed, ERR_CLOSED );
    // Обработка завершения наследниками
    doClose();
    closed = true;
  }

  @Override
  public boolean isClosed() {
    return closed;
  }

  @Override
  public IAdminCmdDef findCommand( String aCmdId ) {
    StridUtils.checkValidIdPath( aCmdId );
    IAdminCmd cmd = cmdMap.findByKey( aCmdId );
    if( cmd == null ) {
      // Команда не существует
      return null;
    }
    return cmd.cmdDef();
  }

  @Override
  public IList<IAdminCmdDef> availableCmds() {
    return cmdList;
  }

  @Override
  public IAdminCmdContext context() {
    return context;
  }

  @Override
  public void setContext( IAdminCmdContext aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    context = aContext;
    for( IAdminCmd cmd : cmdMap.values() ) {
      cmd.setContext( aContext );
    }
    for( IAdminCmd cmd : clonedCmds ) {
      cmd.setContext( aContext );
    }
  }

  @Override
  public IAdminCmdResult exec( String aCmdId, IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    TsNullArgumentRtException.checkNulls( aCmdId, aArgValues, aCallback );
    TsIllegalStateRtException.checkTrue( closed, ERR_CLOSED );
    AbstractAdminCmd cmd = (AbstractAdminCmd)cmdMap.findByKey( aCmdId );
    TsItemNotFoundRtException.checkNull( cmd, ERR_CMD_NOT_FOUND, aCmdId );
    try {
      // Для обработки команды создается клон команды, чтобы обеспечить вложенность вызовов при выполнении скриптов с
      // помощью команды batch
      AbstractAdminCmd cloneCmd = (AbstractAdminCmd)cmd.clone();
      clonedCmds.add( cloneCmd );
      try {
        return cloneCmd.exec( aArgValues, aCallback );
      }
      finally {
        clonedCmds.remove( cloneCmd );
      }
    }
    catch( CloneNotSupportedException e ) {
      throw new TsInternalErrorRtException( e );
    }
  }

  @Override
  public IList<IPlexyValue> getPossibleValues( String aCmdId, String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    TsIllegalStateRtException.checkTrue( closed, ERR_CLOSED );
    IAdminCmd cmd = cmdMap.findByKey( aCmdId );
    TsItemNotFoundRtException.checkNull( cmd, ERR_CMD_NOT_FOUND, aCmdId );
    try {
      return cmd.getPossibleValues( aArgId, aArgValues );
    }
    catch( RuntimeException e ) {
      logger.error( e );
      return IList.EMPTY;
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IAdminCmdLibrary
  //
  @Override
  public void init() {
    TsIllegalStateRtException.checkTrue( closed, ERR_CLOSED );
    // Обработка инициализации наследниками
    doInit();
  }

  // ------------------------------------------------------------------------------------
  // API для наследников
  //
  /**
   * Возвращает журнал работы библиотеки
   *
   * @return {@link ILogger} журнал работы
   */
  protected ILogger logger() {
    return logger;
  }

  /**
   * Шаблонный метод: инициализировать работу плагина
   * <p>
   * Создать и зарегистрировать команды плагина
   */
  protected abstract void doInit();

  /**
   * Шаблонный метод: завершить работу плагина
   * <p>
   */
  protected abstract void doClose();

  /**
   * Добавить команду для выполнения
   *
   * @param aCmd {@link IAdminCmd} - команда
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException у идентификатор команды и ее алиас пустая строка
   * @throws TsIllegalArgumentRtException идентификатор команды или ее алиас должен быть ИД-путем или пустой строкой
   * @throws TsItemAlreadyExistsRtException команда уже существует
   */
  protected void addCmd( IAdminCmd aCmd ) {
    TsNullArgumentRtException.checkNull( aCmd );
    TsNullArgumentRtException.checkNull( aCmd.cmdDef() );
    IAdminCmdDef cmdDef = aCmd.cmdDef();
    String cmdId = cmdDef.id();
    String cmdAlias = cmdDef.alias();
    checkCmd( cmdId );
    checkCmd( cmdAlias );
    boolean noCmdId = cmdId.equals( EMPTY_STRING ) && cmdAlias.equals( EMPTY_STRING );
    TsIllegalArgumentRtException.checkTrue( noCmdId, ERR_CMD_NOT_ID, cmdDef.id() );
    if( !cmdId.equals( EMPTY_STRING ) ) {
      cmdMap.put( cmdId, aCmd );
    }
    if( !cmdAlias.equals( EMPTY_STRING ) ) {
      cmdMap.put( cmdAlias, aCmd );
    }
    cmdList.add( aCmd.cmdDef() );
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
  /**
   * Проверяет существование аргумента в списке уже зарегистрированных аргументов
   *
   * @param aCmdId String - идентификатор команды или ее алиас
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор команды или ее алиас должен быть ИД-путем или пустой строкой
   * @throws TsItemAlreadyExistsRtException команда уже существует
   */
  private void checkCmd( String aCmdId ) {
    TsNullArgumentRtException.checkNull( aCmdId );
    if( !aCmdId.equals( EMPTY_STRING ) ) {
      boolean valid = StridUtils.isValidIdPath( aCmdId );
      TsIllegalArgumentRtException.checkFalse( valid, ERR_CMD_ID_MUST_PATH, getName(), aCmdId );
    }
    TsItemAlreadyExistsRtException.checkTrue( cmdMap.hasKey( aCmdId ), ERR_CMD_ALREADY_EXIST, aCmdId );
  }
}
