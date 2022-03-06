package org.toxsoft.uskat.s5.server.statistics;

/**
 * Локализуемые ресурсы .
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  /**
   * {@link EStatisticFunc}
   */
  String E_RF_D_FIRST   = "Первое значение в интервале агрегации";
  String E_RF_N_FIRST   = "Первое";
  String E_RF_D_LAST    = "Последнее значение в интервале агрегации";
  String E_RF_N_LAST    = "Последнее";
  String E_RF_D_MIN     = "В последовательности значений, в интервале агрегации, определяется минимальное значение";
  String E_RF_N_MIN     = "Минимум";
  String E_RF_D_MAX     = "В последовательности значений, в интервале агрегации, определяется максимальное значение";
  String E_RF_N_MAX     = "Максимум";
  String E_RF_D_AVERAGE = "В последовательности значений, в интервале агрегации, определяется среднее значение";
  String E_RF_N_AVERAGE = "Среднее";
  String E_RF_D_SUMMA   = "В последовательности значений, в интервале агрегации, определяется сумма значений";
  String E_RF_N_SUMMA   = "Сумма";
  String E_RF_D_COUNT   = "В последовательности значений, в интервале агрегации, определяется количество значений";
  String E_RF_N_COUNT   = "Количество";

  /**
   * {@link EStatisticInterval}
   */
  String E_SI_D_ALL    = Messages.getString( "IS5Resources.E_SI_D_ALL" );    //$NON-NLS-1$
  String E_SI_N_ALL    = Messages.getString( "IS5Resources.E_SI_N_ALL" );    //$NON-NLS-1$
  String E_SI_D_SECOND = Messages.getString( "IS5Resources.E_SI_D_SECOND" ); //$NON-NLS-1$
  String E_SI_N_SECOND = Messages.getString( "IS5Resources.E_SI_N_SECOND" ); //$NON-NLS-1$
  String E_SI_D_MINUTE = Messages.getString( "IS5Resources.E_SI_D_MINUTE" ); //$NON-NLS-1$
  String E_SI_N_MINUTE = Messages.getString( "IS5Resources.E_SI_N_MINUTE" ); //$NON-NLS-1$
  String E_SI_D_HOUR   = Messages.getString( "IS5Resources.E_SI_D_HOUR" );   //$NON-NLS-1$
  String E_SI_N_HOUR   = Messages.getString( "IS5Resources.E_SI_N_HOUR" );   //$NON-NLS-1$
  String E_SI_D_DAY    = Messages.getString( "IS5Resources.E_SI_D_DAY" );    //$NON-NLS-1$
  String E_SI_N_DAY    = Messages.getString( "IS5Resources.E_SI_N_DAY" );    //$NON-NLS-1$
  String E_SI_D_MONTH  = "Месяц";                                            //$NON-NLS-1$
  String E_SI_N_MONTH  = "За последний месяц работы";                        //$NON-NLS-1$
  String E_SI_D_YEAR   = "Год";                                              //$NON-NLS-1$
  String E_SI_N_YEAR   = "За последний год работы";                          //$NON-NLS-1$
}
