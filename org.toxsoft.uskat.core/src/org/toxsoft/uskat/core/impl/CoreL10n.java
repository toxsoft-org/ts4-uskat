package org.toxsoft.uskat.core.impl;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;
import static org.toxsoft.core.tslib.coll.impl.TsCollectionsUtils.*;
import static org.toxsoft.uskat.core.impl.ISkCoreConfigConstants.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.chario.*;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.files.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * Реализация {@link ICoreL10n}.
 * <p>
 * В директории {@link ISkCoreConfigConstants#OPDEF_L10N_FILES_DIR} локализатор ищет директорию заданной локали
 * {@link ISkCoreConfigConstants#OPDEF_LOCALE}, и из него считывает файлы локализации. Для каждого типа сущностей
 * (классы, объекты и т.п.) загружаются все файлы, совпадающие по имени с соответствующим шаблоном
 * <code>PTRN_XXX</code>. Файлы обрабатываются в сортированном по имени порядке, так что более поздние данные
 * локализации имеют приоритет над более ранними.
 * <p>
 * Подробности про хранение и принципы работы локализатиора описани в коде этого класса, по месту.
 *
 * @author hazard157
 */
class CoreL10n
    implements ICoreL10n {

  // ------------------------------------------------------------------------------------
  // В директориипо имени локали localeFilesRoot находятся следующие файлы, в которых хранятся соответствующие L10nItem
  // Любое количество файлов, совпадающих с шаблоном, будут обработаны
  private static final Pattern PTRN_CLASSES_FILES = Pattern.compile( "classes.*\\.txt" ); //$NON-NLS-1$
  private static final Pattern PTRN_OBJECTS_FILES = Pattern.compile( "objects.*\\.txt" ); //$NON-NLS-1$

  /**
   * Элемент локализации одной сущности.
   * <p>
   * Содержит три строки. Имя и описание соответствуют таковым у {@link IStridable}. А вот {@link #entityIdString()} -
   * это не ИД-путь, а текстовое представление для идентификации сущности. В зависимости от сущности имеет следующий
   * смысл:
   * <ul>
   * <li>для {@link IDtoClassInfo} и его свойств (типа {@link IDtoAttrInfo} - это {@link Gwid} в каноническом текстовом
   * виде {@link Gwid#toString()}. Для сохранения/загрузки используется {@link Gwid#KEEPER};</li>
   * <li>для {@link IDtoObject} - это {@link Skid} в каноническом текстовом виде {@link Skid#toString()}. Для
   * сохранения/загрузки используется {@link Skid#KEEPER}.</li>
   * </ul>
   * Элементы хранятся в текстовом файле по одному элементу на стороку. Формат строки следующий:
   * <p>
   * <code><b>EntityIdString "name" "description"</b></code>
   * <p>
   * При этом:
   * <ul>
   * <li>Пустые строки (только из пробелов в смысле {@link IStrioReader#getSpaceChars()}) и стандартные комментарии
   * {@link IStrioReader} игнорируются;</li>
   * <li>EntityIdString хрантися как есть, без кавычек;</li>
   * <li>пробелы игнорируются, но между тремя элементами должен быть хотя бы один пробел в смысле
   * {@link IStrioReader#getSpaceChars()};</li>
   * <li>как имя, так и описание являются опциональными, если они пропущены, то в элементе они инициализируются
   * <code>null</code>, а при локализации такие строки остаются не локализованными. Таким образом, отличается пустая
   * строка ("") и отсутсвие локализации;</li>
   * <li>учтите, что если указана толька один текст в кавычках, то он интерпретируется как name, то есть, нельзя задать
   * только description, не задав name.</li>
   * </ul>
   *
   * @author hazard157
   */
  static class L10nItem {

    private final String entityIdString;
    private final String name;
    private final String description;

    public L10nItem( String aEntityIdString, String aName, String aDescription ) {
      entityIdString = aEntityIdString;
      name = aName;
      description = aDescription;
    }

    /**
     * Считывает очередной элемент из входного потока.
     *
     * @param aSr {@link IStrioReader} - входной поток
     * @return {@link L10nItem} - считанный элемент или <code>null</code> если поток закончился
     * @throws StrioRtException нарушение формата строки
     */
    static L10nItem read( IStrioReader aSr ) {
      if( isEof( aSr ) ) {
        return null;
      }
      String rId = readEntityIdString( aSr );
      String rName = null;
      String rDescr = null;
      if( !isEof( aSr ) ) {
        if( !isEol( aSr ) ) {
          rName = aSr.readQuotedString();
          if( !isEol( aSr ) ) {
            rDescr = aSr.readQuotedString();
          }
        }
      }
      return new L10nItem( rId, rName, rDescr );
    }

    private static String readEntityIdString( IStrioReader aSr ) {
      StringBuilder sb = new StringBuilder();
      while( true ) {
        char ch = aSr.peekChar( EStrioSkipMode.SKIP_NONE );
        if( aSr.isSpaceChar( ch ) || ch == CHAR_EOF ) {
          break;
        }
        sb.append( aSr.nextChar() );
      }
      return sb.toString();
    }

    private static boolean isEol( IStrioReader aSr ) {
      char ch = aSr.peekChar( EStrioSkipMode.SKIP_COMMENTS );
      return ch == CHAR_EOL;
    }

    private static boolean isEof( IStrioReader aSr ) {
      char ch = aSr.peekChar( EStrioSkipMode.SKIP_COMMENTS );
      return ch == CHAR_EOF;
    }

    void update( IOptionSetEdit aOps ) {
      if( name != null ) {
        DDEF_NAME.setValue( aOps, avStr( name ) );
      }
      if( description != null ) {
        DDEF_DESCRIPTION.setValue( aOps, avStr( description ) );
      }
    }

    public String entityIdString() {
      return entityIdString;
    }

    public String name() {
      return name;
    }

    public String description() {
      return description;
    }

    @Override
    public String toString() {
      return String.format( "%s: %s --- %s", entityIdString, name, description ); //$NON-NLS-1$
    }
  }

  /**
   * Локаль, которая задана в опции {@link ISkCoreConfigConstants#OPDEF_LOCALE}.
   */
  private final Locale locale;

  /**
   * Корневая директория локализатора, которая задана в опции {@link ISkCoreConfigConstants#OPDEF_L10N_FILES_DIR}.
   */
  private final File l10nFilesRoot;

  /**
   * Подкаталог в {@link #l10nFilesRoot}, соответствующий локали {@link #locale} или <code>null</code>.
   * <p>
   * Имя подкаталога формируется как "<b>ln_CN</b>", где <b>ln</b> = {@link Locale#getLanguage()}, а <b>CN</b> =
   * {@link Locale#getCountry()} текущей локали {@link #locale}. Допускатеся короткое имя только "<b>ln</b>". При
   * нескольких директории (например, "en", "en_GB", "en_US", то сначала выбирается более длинное имя, а потом только по
   * языку, более которкое имя).
   * <p>
   * Значение <code>null</code> является признаком того, что локализация неработоспосбна.
   */
  private final File localeFilesRoot;

  /**
   * Признак включения процесса локализации.
   * <p>
   * Если {@link #localeFilesRoot} = <code>null</code>, то всегда <code>false</code>.
   */
  private boolean isL10n;

  // ------------------------------------------------------------------------------------
  // загруженные в конструкторе данные локализации
  // Коллекции расчитаны на количество эелемнтов, которые будут локализованы, а не на общее количество в системе
  private final IMapEdit<Gwid, L10nItem> ldClassesMap =
      new ElemMap<>( getMapBucketsCount( estimateOrder( 1_000 ) ), getListInitialCapacity( estimateOrder( 1_000 ) ) );
  private final IMapEdit<Skid, L10nItem> ldObjsMap    =
      new ElemMap<>( getMapBucketsCount( estimateOrder( 10_000 ) ), getListInitialCapacity( estimateOrder( 10_000 ) ) );

  /**
   * Конструктор.
   * <p>
   * Получает те же аргументы, что и {@link ISkConnection#open(ITsContextRo)}.
   *
   * @param aArgs {@link ITsContextRo} - аргументы установления соединения
   */
  public CoreL10n( ITsContextRo aArgs ) {
    locale = aArgs.params().getValue( OPDEF_LOCALE.id(), avValobj( Locale.getDefault() ) ).asValobj();
    String strRootDir = OPDEF_L10N_FILES_DIR.getValue( aArgs.params() ).asString();
    l10nFilesRoot = new File( strRootDir );
    // проверим наличие нужной директории локали
    if( TsFileUtils.isDirReadable( l10nFilesRoot ) ) {
      // сначала пробуем длинное имя директория, потом короткое
      String longName = locale.getLanguage() + "_" + locale.getCountry(); //$NON-NLS-1$
      String shortName = locale.getLanguage();
      File f = new File( l10nFilesRoot, longName );
      if( !TsFileUtils.isDirReadable( f ) ) {
        f = new File( l10nFilesRoot, shortName );
      }
      if( TsFileUtils.isDirReadable( f ) ) {
        localeFilesRoot = f;
      }
      else {
        localeFilesRoot = null;
        LoggerUtils.errorLogger().warning( FMT_WARN_L10N_NO_LOCALE_DIR, longName, shortName );
      }
    }
    else {
      localeFilesRoot = null;
      LoggerUtils.errorLogger().warning( FMT_WARN_L10N_NO_ROOT_DIR, l10nFilesRoot.getAbsolutePath() );
    }
    // загрузим данные локализации
    if( localeFilesRoot != null ) {
      loadLocalizationData();
      isL10n = true;
    }
    else {
      isL10n = false;
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //

  /**
   * Считывает данные локализации из файлов во внутренные коллекции <code>ldXxx</code>.
   * <p>
   * Прилюбыч ошибках только логирует сообщения, но не выбрасывает исключение.
   */
  private void loadLocalizationData() {
    IListEdit<L10nItem> ll = new ElemLinkedBundleList<>( getListInitialCapacity( estimateOrder( 10_000 ) ), true );
    // загрузка локализации описания классов
    for( File f : listMatchingFiles( PTRN_CLASSES_FILES ) ) {
      loadItemsFromFile( f, ll );
    }
    for( L10nItem item : ll ) {
      try {
        Gwid gwid = Gwid.of( item.entityIdString() );
        ldClassesMap.put( gwid, item );
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().warning( ex, FMT_WARN_L10N_INV_SDC_GWID_STR, item.entityIdString() );
      }
    }
    ll.clear();
    // загрузка локализации объектов
    for( File f : listMatchingFiles( PTRN_OBJECTS_FILES ) ) {
      loadItemsFromFile( f, ll );
    }
    for( L10nItem item : ll ) {
      try {
        Skid skid = Skid.KEEPER.str2ent( item.entityIdString() );
        ldObjsMap.put( skid, item );
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().warning( ex, FMT_WARN_L10N_INV_OBJ_SKID_STR, item.entityIdString() );
      }
    }
  }

  /**
   * Возвращает сортированный список файлов из {@link #localeFilesRoot}, удовлетворяющих шаблону имени.
   *
   * @param aFileNamePattern {@link Pattern} - шаблон имени файла
   * @return {@link IList}&ltFile&gt; - список файлов
   */
  private IList<File> listMatchingFiles( Pattern aFileNamePattern ) {
    File[] array = localeFilesRoot.listFiles( TsFileFilter.FF_FILES );
    if( array == null || array.length == 0 ) {
      return IList.EMPTY;
    }
    IListBasicEdit<File> ll = new SortedElemLinkedBundleList<>();
    for( File f : array ) {
      if( aFileNamePattern.matcher( f.getName() ).matches() ) {
        ll.add( f );
      }
    }
    return ll;
  }

  /**
   * Загружает элементы локализации из указанного файла
   * <p>
   * В случае ошибки в процессе чтения файла (в том числе, при отсутсвии файла), не выбрасывает исключение, а логирует
   * сообщение в журнал.
   *
   * @param aFile String - файл
   * @param aItems {@link IListEdit}&lt;{@link L10nItem}&gt; - список для загрузки считанных элементов
   */
  private static void loadItemsFromFile( File aFile, IListEdit<L10nItem> aItems ) {
    if( !TsFileUtils.isFileReadable( aFile ) ) {
      LoggerUtils.errorLogger().warning( FMT_WARN_L10N_BAD_FILE, aFile.getName() );
      return;
    }
    L10nItem lastItem = null;
    try( ICharInputStreamCloseable chIn = new CharInputStreamFile( aFile ) ) {
      IStrioReader sr = new StrioReader( chIn );
      L10nItem item;
      while( (item = L10nItem.read( sr )) != null ) {
        aItems.add( item );
        lastItem = item;
      }
    }
    catch( Exception ex ) {
      if( lastItem != null ) {
        LoggerUtils.errorLogger().info( FMT_LAST_READ_ITEM, lastItem.entityIdString(), lastItem.name() );
      }
      else {
        LoggerUtils.errorLogger().info( MSG_NO_ITEMS_READ_YET );
      }
      LoggerUtils.errorLogger().error( ex, FMT_ERR_L10N_LOADING_FILE, aFile.getName() );
    }
  }

  // приведение типов в этом методе - хак, но что делать...
  private void internalL10nClassInfo( DtoClassInfo aCinf ) {
    Gwid g;
    L10nItem l10;
    // сам класс
    g = Gwid.createClass( aCinf.id() );
    l10 = ldClassesMap.findByKey( g );
    if( l10 != null ) {
      l10.update( aCinf.params() );
    }
    // атрибуты
    for( IDtoAttrInfo inf : aCinf.attrInfos() ) {
      g = Gwid.createAttr( aCinf.id(), inf.id() );
      l10 = ldClassesMap.findByKey( g );
      if( l10 != null ) {
        l10.update( ((DtoAttrInfo)inf).params() );
      }
    }
    // связи
    for( IDtoLinkInfo inf : aCinf.linkInfos() ) {
      g = Gwid.createLink( aCinf.id(), inf.id() );
      l10 = ldClassesMap.findByKey( g );
      if( l10 != null ) {
        l10.update( ((DtoLinkInfo)inf).params() );
      }
    }
    // РВ-данные
    for( IDtoRtdataInfo inf : aCinf.rtdataInfos() ) {
      g = Gwid.createRtdata( aCinf.id(), inf.id() );
      l10 = ldClassesMap.findByKey( g );
      if( l10 != null ) {
        l10.update( ((DtoRtdataInfo)inf).params() );
      }
    }
    // команды
    for( IDtoCmdInfo inf : aCinf.cmdInfos() ) {
      g = Gwid.createCmd( aCinf.id(), inf.id() );
      l10 = ldClassesMap.findByKey( g );
      if( l10 != null ) {
        l10.update( ((DtoCmdInfo)inf).params() );
      }
      // аргументы команды
      for( IDataDef dd : inf.argDefs() ) {
        g = Gwid.createCmdArg( aCinf.id(), inf.id(), dd.id() );
        l10 = ldClassesMap.findByKey( g );
        if( l10 != null ) {
          l10.update( ((DataDef)dd).params() );
        }
      }
    }
    // команды
    for( IDtoEventInfo inf : aCinf.eventInfos() ) {
      g = Gwid.createEvent( aCinf.id(), inf.id() );
      l10 = ldClassesMap.findByKey( g );
      if( l10 != null ) {
        l10.update( ((DtoEventInfo)inf).params() );
      }
      // аргументы команды
      for( IDataDef dd : inf.paramDefs() ) {
        g = Gwid.createEventParam( aCinf.id(), inf.id(), dd.id() );
        l10 = ldClassesMap.findByKey( g );
        if( l10 != null ) {
          l10.update( ((DataDef)dd).params() );
        }
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // ICoreL10n
  //

  @Override
  public Locale locale() {
    return locale;
  }

  @Override
  public boolean isL10nOn() {
    return isL10n;
  }

  @Override
  public void setL10n( boolean aLocalization ) {
    if( localeFilesRoot != null ) {
      isL10n = aLocalization;
    }
  }

  @Override
  public IStridablesList<IDtoClassInfo> l10nClassInfos( IStridablesList<IDtoClassInfo> aClassInfoes ) {
    if( !isL10n || aClassInfoes.isEmpty() ) {
      return aClassInfoes;
    }
    IStridablesListEdit<IDtoClassInfo> ll = new StridablesList<>();
    for( IDtoClassInfo dto : aClassInfoes ) {
      DtoClassInfo cinf = DtoClassInfo.createDeepCopy( dto );
      internalL10nClassInfo( cinf );
      ll.add( cinf );
    }
    return ll;
  }

  @Override
  public IDtoObject l10nObject( IDtoObject aObject ) {
    if( !isL10n || aObject == null ) {
      return aObject;
    }
    L10nItem l10 = ldObjsMap.findByKey( aObject.skid() );
    if( l10 == null ) {
      return aObject;
    }
    DtoObject obj = new DtoObject( aObject.skid(), aObject.attrs(), aObject.rivets().map() );
    l10.update( obj.attrs() );
    return obj;
  }

  @Override
  public IList<IDtoObject> l10nObjectsList( IList<IDtoObject> aObjects ) {
    if( !isL10n || aObjects.isEmpty() ) {
      return aObjects;
    }
    IListEdit<IDtoObject> ll = new ElemArrayList<>( aObjects.size() );
    for( IDtoObject dpu : aObjects ) {
      L10nItem l10 = ldObjsMap.findByKey( dpu.skid() );
      if( l10 != null ) {
        DtoObject obj = new DtoObject( dpu.skid(), dpu.attrs(), dpu.rivets().map() );
        l10.update( obj.attrs() );
        ll.add( obj );
      }
      else {
        ll.add( dpu );
      }
    }
    return ll;
  }

}
