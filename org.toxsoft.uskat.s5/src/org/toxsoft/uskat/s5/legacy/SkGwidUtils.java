package org.toxsoft.uskat.s5.legacy;

import org.toxsoft.core.tslib.gw.gwid.EGwidKind;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassHierarchyExplorer;

/**
 * Вспомогательные методы обработки gwids
 * <p>
 * FIXME как-то точит не к месту, может перенести в Gwid Manager
 *
 * @author mvk
 */
public class SkGwidUtils {

  /**
   * Проверяет может команда с указанным идентификатором {@link Gwid} обработана исполнителем
   *
   * @param aHierarchy {@link ISkClassHierarchyExplorer} поставщик информации об иерархии классов
   * @param aGwidFilter {@link Gwid} идентификатор команды определяющий правило фильтрации исполняемых команд
   * @param aCmdGwid {@link Gwid} идентификатор исполняемой команды
   * @return boolean <b>true</b> команда исполняется; <b>false</b> команда не исполняется.
   * @throws TsNullArgumentRtException любой (кроме aClassInfoManager) аргумент = null
   */
  public static boolean acceptableCmd( ISkClassHierarchyExplorer aHierarchy, Gwid aGwidFilter, Gwid aCmdGwid ) {
    TsNullArgumentRtException.checkNulls( aHierarchy, aGwidFilter, aCmdGwid );
    EGwidKind kind = aGwidFilter.kind();
    if( kind == EGwidKind.GW_CLASS ) {
      if( aGwidFilter.isAbstract() || aGwidFilter.isStridMulti() ) {
        // Все события всех объектов указанного класса (и наследников)
        String classId = aGwidFilter.classId();
        String classId2 = aCmdGwid.classId();
        return aHierarchy.isAssignableFrom( classId, classId2 );
      }
      // Все события указанного объекта
      Skid skid = aGwidFilter.skid();
      Skid skid2 = aCmdGwid.skid();
      boolean stridMulti = aGwidFilter.isStridMulti();
      return (stridMulti || skid.equals( skid2 ));
    }
    if( kind == EGwidKind.GW_CMD ) {
      if( aGwidFilter.isAbstract() || aGwidFilter.isStridMulti() ) {
        // Указанное событие всех объектов указанного класса (и наследников)
        String classId = aGwidFilter.classId();
        String cmdId = aGwidFilter.propId();
        boolean cmdMulti = aGwidFilter.isPropMulti();
        String classId2 = aCmdGwid.classId();
        String cmdId2 = aCmdGwid.propId();
        return ((cmdMulti || cmdId.equals( cmdId2 )) && //
            aHierarchy.isAssignableFrom( classId, classId2 ));
      }
      // Указанное событие указанного объекта
      Skid skid = aGwidFilter.skid();
      Skid skid2 = aCmdGwid.skid();
      boolean stridMulti = aGwidFilter.isStridMulti();
      String cmdId = aGwidFilter.propId();
      String cmdId2 = aCmdGwid.propId();
      boolean cmdMulti = aGwidFilter.isPropMulti();
      return ((cmdMulti || cmdId.equals( cmdId2 )) && //
          (stridMulti || skid.equals( skid2 )));
    }
    return false;
  }

  /**
   * Проверяет может ли событие быть принято обработчиком с указанным идентификатором {@link Gwid}
   *
   * @param aHierarchy {@link ISkClassHierarchyExplorer} поставщик информации об иерархии классов
   * @param aGwid {@link Gwid} идентификатор опредяемый фильтрацию принимаемых событий
   * @param aEvent {@link SkEvent} событие
   * @return boolean <b>true</b> событие может быть передано; <b>false</b> событие не может быть передано
   * @throws TsNullArgumentRtException любой (кроме aClassInfoManager) аргумент = null
   */
  public static boolean acceptableEvent( ISkClassHierarchyExplorer aHierarchy, Gwid aGwid, SkEvent aEvent ) {
    TsNullArgumentRtException.checkNulls( aHierarchy, aGwid, aEvent );
    EGwidKind kind = aGwid.kind();
    if( kind == EGwidKind.GW_CLASS ) {
      if( aGwid.isAbstract() || aGwid.isStridMulti() ) {
        // Все события всех объектов указанного класса (и наследников)
        String classId = aGwid.classId();
        String classId2 = aEvent.eventGwid().classId();
        return aHierarchy.isAssignableFrom( classId, classId2 );
      }
      // Все события указанного объекта
      Skid skid = aGwid.skid();
      Skid skid2 = aEvent.eventGwid().skid();
      boolean stridMulti = aGwid.isMulti();
      return (stridMulti || skid.equals( skid2 ));
    }
    if( kind == EGwidKind.GW_EVENT ) {
      if( aGwid.isAbstract() || aGwid.isStridMulti() ) {
        // Указанное событие всех объектов указанного класса (и наследников)
        String classId = aGwid.classId();
        String eventId = aGwid.propId();
        boolean eventMulti = aGwid.isPropMulti();
        String classId2 = aEvent.eventGwid().classId();
        String eventId2 = aEvent.eventGwid().propId();
        return ((eventMulti || eventId.equals( eventId2 )) && //
            aHierarchy.isAssignableFrom( classId, classId2 ));
      }
      // Указанное событие указанного объекта
      Skid skid = aGwid.skid();
      Skid skid2 = aEvent.eventGwid().skid();
      boolean stridMulti = aGwid.isMulti();
      String eventId = aGwid.propId();
      String eventId2 = aEvent.eventGwid().propId();
      boolean eventMulti = aGwid.isPropMulti();
      return ((eventMulti || eventId.equals( eventId2 )) && //
          (stridMulti || skid.equals( skid2 )));
    }
    return false;
  }
}
