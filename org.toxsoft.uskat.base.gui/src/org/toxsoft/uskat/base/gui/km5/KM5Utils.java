package org.toxsoft.uskat.base.gui.km5;

/**
 * Вспомогательные методы как для пользователей, так и для реализации подсистемы SM5.
 *
 * @author goga
 */
public class KM5Utils {

  /**
   * Создает экземпляр {@link IKM5Support}.
   *
   * @return {@link IKM5Support} - созданный экземпляр
   */
  public static final IKM5Support createKM5Support() {
    return new KM5Support();
  }

  // /**
  // * Определяет, является ли аргумент идентификатором обычного атрибута.
  // * <p>
  // * Обычным считается аторибут, значения которого задается пользователем с помощью
  // * {@link IBsObjectEditor#setAttrValue(String, IAtomicValue)}. К обычным относятся все атрибуты, кроме специальных -
  // * {@link ISkObject#ATTR_ID_CLASSID}, {@link ISkObject#ATTR_ID_STRID}, {@link ISkObject#ATTR_ID_OBJID},
  // * {@link ISkObject#ATTR_ID_VISNAME}.
  // *
  // * @param aAttrId String - проверемый идентификатор
  // * @return boolean - призак обычного атрибута
  // * @throws TsNullArgumentRtException любой аргумент = null
  // */
  // public static boolean isCommonAttr( String aAttrId ) {
  // TsNullArgumentRtException.checkNull( aAttrId );
  // switch( aAttrId ) {
  // case ATTR_ID_CLASSID:
  // case ATTR_ID_STRID:
  // case ATTR_ID_OBJID:
  // case ATTR_ID_VISNAME:
  // return false;
  // default:
  // return true;
  // }
  // }

  // /**
  // * Возвращает Java-класс, реализующий S5-объект указанного S5-класса.
  // *
  // * @param <T> - конкретный класс моделируемой S5-сущности
  // * @param aClassInfo {@link ISkClassInfo} - описание S5-класса
  // * @return {@link Class} - Java-класс реализующий S5-объект
  // * @throws TsNullArgumentRtException любой аргумент = null
  // * @throws TsItemNotFoundRtException не науден нужны класс
  // * @throws TsIllegalStateRtException Java-класс не наследник {@link ISkObject}
  // */
  // @SuppressWarnings( "unchecked" )
  // public static <T extends ISkObject> Class<T> getBsObjectImplementationClass( ISkClassInfo aClassInfo ) {
  // TsNullArgumentRtException.checkNull( aClassInfo );
  // Class<?> rawClass;
  // try {
  // rawClass = Class.forName( aClassInfo.javaClassName() );
  // }
  // catch( ClassNotFoundException ex ) {
  // throw new TsItemNotFoundRtException( ex, FMT_ERR_NO_BSOBJ_JCLASS, aClassInfo.id(), aClassInfo.javaClassName() );
  // }
  // if( ISkObject.class.isAssignableFrom( rawClass ) ) {
  // return (Class<T>)rawClass;
  // }
  // throw new TsIllegalStateRtException( FMT_ERR_INV_BSOBJ_JCLASS, aClassInfo.id(), rawClass.getName(),
  // ISkObject.class.getSimpleName() );
  // }

  /**
   * Запрет на создание экземпляров.
   */
  private KM5Utils() {
    // nop
  }

}
