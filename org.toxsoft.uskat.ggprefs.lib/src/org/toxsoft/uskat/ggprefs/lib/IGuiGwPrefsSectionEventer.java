package org.toxsoft.uskat.ggprefs.lib;

import org.toxsoft.core.tslib.bricks.events.ITsPausabeEventsProducer;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.impl.SingleStringList;
import org.toxsoft.core.tslib.gw.gwid.EGwidKind;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.Skid;

/**
 * Работа с событиями от раздела {@link IGuiGwPrefsSection}.
 * <p>
 * <b>Пояснение к работе слушателей</b> <br>
 * Есть два типа слушателей:
 * <ul>
 * <li>{@link IGuiGwPrefsObjectListener} - общий слушатель изменения любой опции объекта;</li>
 * <li>{@link IGuiGwPrefsOptionListener} - частный слушатель изменения конкретной опции.</li>
 * </ul>
 * При изменении каких-либо опции, для каждого объекта, <b>сначала</b> вызываются частные слушатели, а <b>потом</b> -
 * общие. Мотивация существования частных слушателей заключается в том, что обработка изменения значении некторых опции
 * является "тяжелой" операцией, и не следует их делать если изменились только "легкие" опции. Например, опция
 * "background.image" требует загрузки изображения из файла, а "foreground.color" - ничего не требует кроме перерисовки.
 * В таком случае, при отсутствии частных слушателей подбор цвета стал бы очень ресурсоемкой операцией.
 * <p>
 * Правила использования слушателей следующее:
 * <ul>
 * <li>надо ставить общий слушатель {@link IGuiGwPrefsObjectListener} на опции объекта, обработать изменения всех опции,
 * и вызвать <code>redarw()</code>;</li>
 * <li>зарегистрировать слушатель методом {@link #addObjectListener(Gwid, IGuiGwPrefsObjectListener)};</li>
 * <li>если выяснется, что есть "тяжелая" опция, то создать частный слушатель {@link IGuiGwPrefsOptionListener}, и
 * перенести код обратки опции в него;</li>
 * <li>зарегистрировать слушатель методом {@link #addOptionListener(Gwid, IStringList, IGuiGwPrefsOptionListener)};</li>
 * <li>если "тяжелих" опции несколько, то лучше для каждой создать и зарегистрировать свой слушатель;</li>
 * <li>в частном слушателе <i>не надо вызывать rerdaw()</i>, ведь GUI гарантировано будет перерисовано общим
 * слушателем.</li>
 * </ul>
 *
 * @author goga
 */
public interface IGuiGwPrefsSectionEventer
    extends ITsPausabeEventsProducer {

  /**
   * Добавляет подписку на сообщения об изменении любых настроек объекта(ов).
   * <p>
   * В качестве аргумента <code>aGwid</code> может быть абстрактный или конкретный {@link Gwid} вида
   * {@link EGwidKind#GW_CLASS}. Если {@link Gwid#isAbstract()} = <code>true</code>, то слушатель
   * {@link IGuiGwPrefsObjectListener#onGuiGwPrefsBundleChanged(IGuiGwPrefsSection, Skid)} будет вызываться при
   * изменении настроек GUI любого объекта запрошенного класса (или наследника). Для конкретных {@link Gwid} - только
   * для конкретного объекта.
   *
   * @param aGwid {@link Gwid} - идентификатор интересующего класса или объекта
   * @param aListener {@link IGuiGwPrefsObjectListener} - регистрируемый слушатель
   */
  void addObjectListener( Gwid aGwid, IGuiGwPrefsObjectListener aListener );

  /**
   * Добавляет подписку на сообщения об изменении указанных настроек объекта(ов).
   * <p>
   * В качестве аргумента <code>aGwid</code> может быть абстрактный или конкретный {@link Gwid} вида
   * {@link EGwidKind#GW_CLASS}. Если {@link Gwid#isAbstract()} = <code>true</code>, то слушатель
   * {@link IGuiGwPrefsObjectListener#onGuiGwPrefsBundleChanged(IGuiGwPrefsSection, Skid)} будет вызываться при
   * изменении настроек GUI любого объекта запрошенного класса (или наследника). Для конкретных {@link Gwid} - только
   * для конкретного объекта.
   *
   * @param aGwid {@link Gwid} - ИД интересующего класса или объекта
   * @param aOptionIds {@link IStringList} - список ИДов интересущих опции
   * @param aListener {@link IGuiGwPrefsOptionListener} - регистрируемый слушатель
   */
  void addOptionListener( Gwid aGwid, IStringList aOptionIds, IGuiGwPrefsOptionListener aListener );

  /**
   * Удаляет все регистрации указанного слушателя.
   *
   * @param aListener {@link IGuiGwPrefsObjectListener} - удяляемый слушатель
   */
  void removeObjectListener( IGuiGwPrefsObjectListener aListener );

  /**
   * Удаляет все регистрации указанного слушателя.
   *
   * @param aListener {@link IGuiGwPrefsOptionListener} - удяляемый слушатель
   */
  void removeOptionListener( IGuiGwPrefsOptionListener aListener );

  // ------------------------------------------------------------------------------------
  // Инлайн методы для удобства
  //

  @SuppressWarnings( "javadoc" )
  default void addObjectListener( Skid aSkid, IGuiGwPrefsObjectListener aListener ) {
    addObjectListener( Gwid.createObj( aSkid ), aListener );
  }

  @SuppressWarnings( "javadoc" )
  default void addOptionListener( Gwid aGwid, String aOptionId, IGuiGwPrefsOptionListener aListener ) {
    addOptionListener( aGwid, new SingleStringList( aOptionId ), aListener );
  }

  @SuppressWarnings( "javadoc" )
  default void addOptionListener( Skid aSkid, String aOptionId, IGuiGwPrefsOptionListener aListener ) {
    addOptionListener( Gwid.createObj( aSkid ), new SingleStringList( aOptionId ), aListener );
  }

}
