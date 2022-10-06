package org.toxsoft.uskat.legacy.file;

/**
 * Локализуемые ресурсы работы с файловыми объектами.
 *
 * @author goga
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  /**
   * {@link ConvoyFileManager}
   */
  String FMT_INFO_CREATED_DEFAULT_FILE_CONVOY =
      Messages.getString( "IS5Resources.FMT_INFO_CREATED_DEFAULT_FILE_CONVOY" ); //$NON-NLS-1$
  String FMT_INFO_CREATED_DEFAULT_DIR_CONVOY  =
      Messages.getString( "IS5Resources.FMT_INFO_CREATED_DEFAULT_DIR_CONVOY" );  //$NON-NLS-1$

  /**
   * {@link FileUtils}
   */
  String FMT_ERR_PARENT_ARG_MUST_BE_DIRS = Messages.getString( "IS5Resources.FMT_ERR_PARENT_ARG_MUST_BE_DIRS" );
  String FMT_ERR_FILE_NOT_EXISTS         = Messages.getString( "IS5Resources.FMT_ERR_FILE_NOT_EXISTS" );
  String MSG_ERR_EMPTY_FILE_NAME         = Messages.getString( "IS5Resources.MSG_ERR_EMPTY_FILE_NAME" );
  String FMT_ERR_PATH_IS_NOT_FILE        = Messages.getString( "IS5Resources.FMT_ERR_PATH_IS_NOT_FILE" );
  String FMT_ERR_FILE_NOT_READABLE       = Messages.getString( "IS5Resources.FMT_ERR_FILE_NOT_READABLE" );
  String FMT_ERR_FILE_NOT_WRITEABLE      = Messages.getString( "IS5Resources.FMT_ERR_FILE_NOT_WRITEABLE" );
  String FMT_ERR_DIR_NOT_EXISTS          = Messages.getString( "IS5Resources.FMT_ERR_DIR_NOT_EXISTS" );
  String FMT_ERR_PATH_IS_NOT_DIRECTORY   = Messages.getString( "IS5Resources.FMT_ERR_PATH_IS_NOT_DIRECTORY" );
  String FMT_ERR_DIR_NOT_READABLE        = Messages.getString( "IS5Resources.FMT_ERR_DIR_NOT_READABLE" );
  String FMT_ERR_DIR_NOT_WRITEABLE       = Messages.getString( "IS5Resources.FMT_ERR_DIR_NOT_WRITEABLE" );
  String FMT_ERR_CANT_UNIQUE_FILE        = Messages.getString( "IS5Resources.FMT_ERR_CANT_UNIQUE_FILE" )
      + FileUtils.MAX_UNIQUE_FILE_NAME_PREFIXES + Messages.getString( "IS5Resources.FMT_ERR_CANT_UNIQUE_FILE___1" );

  /**
   * {@link EFileSystemType}
   */
  String E_EFS_TYPE_OTHER   = Messages.getString( "IS5Resources.E_EFS_TYPE_OTHER" );
  String E_EFS_TYPE_UNIX    = Messages.getString( "IS5Resources.E_EFS_TYPE_UNIX" );
  String E_EFS_TYPE_WINDOWS = Messages.getString( "IS5Resources.E_EFS_TYPE_WINDOWS" );

  /**
   * {@link TsFileFilter}
   */
  String MSG_FILTER_DIRS_DESCR       = Messages.getString( "IS5Resources.MSG_FILTER_DIRS_DESCR" );
  String MSG_FILTER_ALLDIRS_DESCR    = Messages.getString( "IS5Resources.MSG_FILTER_ALLDIRS_DESCR" );
  String MSG_FILTER_FILES_DESCR      = Messages.getString( "IS5Resources.MSG_FILTER_FILES_DESCR" );
  String MSG_FILTER_ALLFILES_DESCR   = Messages.getString( "IS5Resources.MSG_FILTER_ALLFILES_DESCR" );
  String MSG_FILTER_UNHIDDEN_DESCR   = Messages.getString( "IS5Resources.MSG_FILTER_UNHIDDEN_DESCR" );
  String MSG_FILTER_EVERYTHING_DESCR = Messages.getString( "IS5Resources.MSG_FILTER_EVERYTHING_DESCR" );

  /**
   * {@link EFileIoErrorKind}
   */
  String ERR_MSG_FILEIO_GENERAL_ERROR   = Messages.getString( "IS5Resources.ERR_MSG_FILEIO_GENERAL_ERROR" );   //$NON-NLS-1$
  String ERR_MSG_FILEIO_INV_NAME        = Messages.getString( "IS5Resources.ERR_MSG_FILEIO_INV_NAME" );        //$NON-NLS-1$
  String ERR_MSG_FILEIO_NOT_FOUND       = Messages.getString( "IS5Resources.ERR_MSG_FILEIO_NOT_FOUND" );       //$NON-NLS-1$
  String ERR_MSG_FILEIO_NO_READ_RIGHTS  = Messages.getString( "IS5Resources.ERR_MSG_FILEIO_NO_READ_RIGHTS" );  //$NON-NLS-1$
  String ERR_MSG_FILEIO_NO_WRITE_RIGHTS = Messages.getString( "IS5Resources.ERR_MSG_FILEIO_NO_WRITE_RIGHTS" ); //$NON-NLS-1$
  String ERR_MSG_FILEIO_NOT_A_FILE      = Messages.getString( "IS5Resources.ERR_MSG_FILEIO_NOT_A_FILE" );      //$NON-NLS-1$
  String ERR_MSG_FILEIO_NOT_A_DIR       = Messages.getString( "IS5Resources.ERR_MSG_FILEIO_NOT_A_DIR" );       //$NON-NLS-1$
  String ERR_MSG_ALREADY_EXISTS         = Messages.getString( "IS5Resources.ERR_MSG_ALREADY_EXISTS" );         //$NON-NLS-1$
  String ERR_MSG_CANT_CREATE            = Messages.getString( "IS5Resources.ERR_MSG_CANT_CREATE" );            //$NON-NLS-1$
  String ERR_MSG_CLOSE_FAIL             = Messages.getString( "IS5Resources.ERR_MSG_CLOSE_FAIL" );             //$NON-NLS-1$
  String ERR_MSG_IO_EXCEPTION           = Messages.getString( "IS5Resources.ERR_MSG_IO_EXCEPTION" );           //$NON-NLS-1$

}
