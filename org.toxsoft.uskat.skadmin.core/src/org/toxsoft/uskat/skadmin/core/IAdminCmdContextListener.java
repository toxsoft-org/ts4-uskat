package org.toxsoft.uskat.skadmin.core;

/**
 * Слушатель контекста
 * 
 * @author mvk
 */
public interface IAdminCmdContextListener {

  /**
   * Событие контекста: добавлен параметр в контекст
   * 
   * @param aContext {@link IAdminCmdContext} контекст в который был добавлен параметр
   * @param aParamName String имя параметра
   */
  void onAddParam( IAdminCmdContext aContext, String aParamName );

  /**
   * Событие контекста: удаляется параметр из контекста
   * 
   * @param aContext {@link IAdminCmdContext} контекст из которого удаляется параметр
   * @param aParamName String имя параметра
   */
  void onRemovingParam( IAdminCmdContext aContext, String aParamName );

  /**
   * Событие контекста: удален параметр из контекста
   * 
   * @param aContext {@link IAdminCmdContext} контекст из которого удален параметр
   * @param aParamName String имя параметра
   */
  void onRemovedParam( IAdminCmdContext aContext, String aParamName );

  /**
   * Событие контекста: изменено значение параметра
   * 
   * @param aContext {@link IAdminCmdContext} контекст в котором было изменено значение параметра
   * @param aParamName String имя параметра
   */
  void onSetParamValue( IAdminCmdContext aContext, String aParamName );
}
