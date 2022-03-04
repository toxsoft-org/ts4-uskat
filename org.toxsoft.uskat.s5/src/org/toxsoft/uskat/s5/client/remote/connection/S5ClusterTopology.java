package org.toxsoft.uskat.s5.client.remote.connection;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.basis.ITsCollection;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;

/**
 * Топология кластеров системы
 *
 * @author mvk
 */
public final class S5ClusterTopology
    implements Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "S5ClusterTopology"; //$NON-NLS-1$

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<S5ClusterTopology> KEEPER =
      new AbstractEntityKeeper<>( S5ClusterTopology.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @SuppressWarnings( "unchecked" )
        @Override
        protected void doWrite( IStrioWriter aSw, S5ClusterTopology aEntity ) {
          S5ClusterNodeInfo.KEEPER.writeColl( aSw, (ITsCollection<S5ClusterNodeInfo>)(Object)aEntity.nodes(), false );
        }

        @SuppressWarnings( "unchecked" )
        @Override
        protected S5ClusterTopology doRead( IStrioReader aSr ) {
          IListEdit<S5ClusterNodeInfo> coll = new ElemArrayList<>();
          S5ClusterNodeInfo.KEEPER.readColl( aSr, coll );
          return new S5ClusterTopology( (IList<IS5ClusterNodeInfo>)(Object)coll );
        }
      };

  private final IListEdit<IS5ClusterNodeInfo> nodes;

  /**
   * Конструктор
   */
  public S5ClusterTopology() {
    this( IList.EMPTY );
  }

  /**
   * Конструктор
   *
   * @param aNodes {@link IList}&lt;{@link IS5ClusterNodeInfo}&gt; список узлов кластера
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5ClusterTopology( IList<IS5ClusterNodeInfo> aNodes ) {
    TsNullArgumentRtException.checkNull( aNodes );
    nodes = new ElemArrayList<>( aNodes );
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Установить информацию о топологии
   *
   * @param aClusterTopology {@link S5ClusterTopology} информация о топологии
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void setAll( S5ClusterTopology aClusterTopology ) {
    TsNullArgumentRtException.checkNull( aClusterTopology );
    nodes.setAll( aClusterTopology.nodes() );
  }

  /**
   * Возвращает список узлов кластеров
   *
   * @return {@link IList}&lt;{@link IS5ClusterNodeInfo}&gt; список узлов
   */
  public IList<IS5ClusterNodeInfo> nodes() {
    return nodes;
  }

  // ------------------------------------------------------------------------------------
  // Реализация Object
  //
  @Override
  public String toString() {
    return toString( this );
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + nodes.hashCode();
    return result;
  }

  @Override
  public boolean equals( Object aObject ) {
    if( this == aObject ) {
      return true;
    }
    if( aObject == null ) {
      return false;
    }
    if( getClass() != aObject.getClass() ) {
      return false;
    }
    S5ClusterTopology other = (S5ClusterTopology)aObject;
    if( !nodes.equals( other.nodes ) ) {
      return false;
    }
    return true;
  }

  // ------------------------------------------------------------------------------------
  // Открытые вспомогательные методы
  //
  /**
   * Возвращает текстовое представление топологии кластеров
   *
   * @param aTopology {@link S5ClusterTopology} топология кластеров
   * @return String текстовое представление топологии
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static String toString( S5ClusterTopology aTopology ) {
    TsNullArgumentRtException.checkNull( aTopology );
    StringBuilder retValue = new StringBuilder();
    for( int index = 0, n = aTopology.nodes().size(); index < n; index++ ) {
      retValue.append( aTopology.nodes().get( index ) );
      if( index + 1 < n ) {
        retValue.append( ',' );
      }
    }
    return retValue.toString();
  }
}
