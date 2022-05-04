package org.toxsoft.uskat.s5.legacy;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.bricks.strio.impl.StrioUtils;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.TsLibUtils;

/**
 * {@link ISkLinksMap} editable implementation.
 *
 * @author hazard157
 */
public class SkLinksMap
    implements ISkLinksMap {

  /**
   * Keeper ID.
   */
  public static final String KEEPER_ID = "SkLinksMap"; //$NON-NLS-1$

  private static final class Keeper
      extends AbstractEntityKeeper<ISkLinksMap> {

    private final boolean indent;

    Keeper( boolean aIndenting ) {
      super( ISkLinksMap.class, EEncloseMode.NOT_IN_PARENTHESES, null );
      indent = aIndenting;
    }

    @Override
    protected void doWrite( IStrioWriter aSw, ISkLinksMap aEntity ) {
      StrioUtils.writeStringMap( aSw, TsLibUtils.EMPTY_STRING, aEntity.map(), SkidListKeeper.KEEPER, indent );
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    @Override
    protected ISkLinksMap doRead( IStrioReader aSr ) {
      SkLinksMap slMap = new SkLinksMap();
      StrioUtils.readStringMap( aSr, TsLibUtils.EMPTY_STRING, SkidListKeeper.KEEPER, (IStringMapEdit)slMap.mapEdit() );
      return slMap;
    }

  }

  /**
   * Keeper singleton (this is non-indenting keeper).
   * <p>
   * Note: read va;lue may be safely casted to the editable {@link SkLinksMap}.
   */
  public static final IEntityKeeper<ISkLinksMap> KEEPER = new Keeper( false );

  /**
   * Keeper singleton (this is keeper indents written text).
   * <p>
   * Note: read va;lue may be safely casted to the editable {@link SkLinksMap}.
   */
  public static final IEntityKeeper<ISkLinksMap> KEEPER_IDENTED = new Keeper( true );

  private final IStringMapEdit<SkidList> map = new StringMap<>();

  /**
   * Constructor.
   */
  public SkLinksMap() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // ISkLinksMap
  //

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IStringMap<ISkidList> map() {
    return (IStringMap)map;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns editable reference to the {@link #map()}.
   * <p>
   * Note: editable map contains editable {@link SkidList} implementation of SKIDs list.
   *
   * @return {@link IStringMapEdit}&lt;{@link SkidList}&gt; - the editable map
   */
  public IStringMapEdit<SkidList> mapEdit() {
    return map;
  }

}
