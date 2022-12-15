package org.toxsoft.uskat.regref.lib;

import org.toxsoft.core.tslib.bricks.strid.IStridableParameterized;
import org.toxsoft.core.tslib.utils.errors.TsUnsupportedFeatureRtException;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoAttrInfo;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoLinkInfo;

/**
 * Описание параметра НСИ.
 * <p>
 * Параметр НСИ бывает двух видов: либо атрибутом, либо связью. Интерфейс расширяет {@link IStridableParameterized},
 * возвращая значения {@link #attrInfo()} или {@link #linkInfo()} в зависимости от вида параметра.
 *
 * @author goga
 */
public interface ISkRriParamInfo
    extends IStridableParameterized {

  /**
   * Определяет, является ли параметр связью или атрибутом.
   *
   * @return boolean - признак связи<br>
   *         <b>true</b> - параметр НСИ - это связь, описание возвращает метод {@link #linkInfo()};<br>
   *         <b>false</b> - параметр НСИ - это связь, описание возвращает метод {@link #attrInfo()}.
   */
  boolean isLink();

  /**
   * Возвращает описание параметра НСИ - атрибута.
   *
   * @return {@link IDtoAttrInfo} - описание атрибута
   * @throws TsUnsupportedFeatureRtException {@link #isLink()} = <code>true</code>
   */
  IDtoAttrInfo attrInfo();

  /**
   * Возвращает описание параметра НСИ - связи.
   *
   * @return {@link IDtoLinkInfo} - описание связи
   * @throws TsUnsupportedFeatureRtException {@link #isLink()} = <code>false</code>
   */
  IDtoLinkInfo linkInfo();

}
