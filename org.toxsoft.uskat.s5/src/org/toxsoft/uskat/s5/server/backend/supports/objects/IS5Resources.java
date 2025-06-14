package org.toxsoft.uskat.s5.server.backend.supports.objects;

/**
 * Локализуемые ресурсы реализации.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  String STR_D_BACKEND_OBJECTS  = Messages.getString( "IS5Resources.STR_D_BACKEND_OBJECTS" );
  String METHOD_CREATING_RIVETS = "creatingRivets";
  String METHOD_UPDATING_RIVETS = "updatingRivets";
  String METHOD_REMOVING_RIVETS = "removingRivets";

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_READ_OBJ_BY_SKID_SQL_START  = Messages.getString( "IS5Resources.MSG_READ_OBJ_BY_SKID_SQL_START" );
  String MSG_READ_OBJ_BY_SKID_SQL_FINISH = Messages.getString( "IS5Resources.MSG_READ_OBJ_BY_SKID_SQL_FINISH" );

  String MSG_READ_OBJ_BY_CLASSID_SQL_START  = Messages.getString( "IS5Resources.MSG_READ_OBJ_BY_CLASSID_SQL_START" );
  String MSG_READ_OBJ_BY_CLASSID_SQL_FINISH = Messages.getString( "IS5Resources.MSG_READ_OBJ_BY_CLASSID_SQL_FINISH" );
  String MSG_WRITE_OBJECTES                 = Messages.getString( "IS5Resources.MSG_WRITE_OBJECTES" );

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_OBJECT_IMPL_NOT_FOUND         = Messages.getString( "IS5Resources.ERR_OBJECT_IMPL_NOT_FOUND" );
  String ERR_OBJECT_CONSTRUCTOR_NOT_FOUND1 = Messages.getString( "IS5Resources.ERR_OBJECT_CONSTRUCTOR_NOT_FOUND1" );
  String ERR_OBJECT_CONSTRUCTOR_NOT_FOUND2 = Messages.getString( "IS5Resources.ERR_OBJECT_CONSTRUCTOR_NOT_FOUND2" );
  String ERR_SQL_EXECUTE_UNEXPECTED        = Messages.getString( "IS5Resources.ERR_SQL_EXECUTE_UNEXPECTED" );
  String ERR_READ_JDBC_UNEXPECTED          = Messages.getString( "IS5Resources.ERR_READ_JDBC_UNEXPECTED" );
  String ERR_READ_UNEXPECTED               = Messages.getString( "IS5Resources.ERR_READ_UNEXPECTED" );

  String ERR_CREATE_OBJ_UNEXPECTED = Messages.getString( "IS5Resources.ERR_CREATE_OBJ_UNEXPECTED" );

  String ERR_NO_DEFAULT_VALUE            = Messages.getString( "IS5Resources.ERR_NO_DEFAULT_VALUE" );
  String ERR_CANT_CHANGE_CLASS_WITH_OBJS = Messages.getString( "IS5Resources.ERR_CANT_CHANGE_CLASS_WITH_OBJS" );
  String ERR_ATTR_NOT_HAVE_DEFAULT_VALUE = Messages.getString( "IS5Resources.ERR_ATTR_NOT_HAVE_DEFAULT_VALUE" );
  String ERR_CANT_CHANGE_IMPL_AND_ATTRS  = Messages.getString( "IS5Resources.ERR_CANT_CHANGE_IMPL_AND_ATTRS" );
  String ERR_CHANGE_OBJECT_IMPL          = Messages.getString( "IS5Resources.ERR_CHANGE_OBJECT_IMPL" );

  String ERR_CANT_REMOVE_HAS_RIVET_REVS  =
      "%s: prohibition of deleting an object that is in the rivets of other objects: \n%s";
  String ERR_RIVERT_REVS_ALREADY_EXIST         =
      "%s(...): rightObj %s, rivetId = %s, leftObj =%s: rivet rev already exist.";
  String ERR_RIVET_REVS_RIGHT_OBJ_NOT_FOUND    = "%s(...): right obj %s is not found";
  String ERR_RIVET_REVS_LEFT_OBJ_NOT_FOUND     =
      "%s(...): rightObj %s, rivetId = %s, leftObj =%s: leftObj is not found";
  String ERR_RIVET_REVS_EDITOR_CLASS_NOT_FOUND = "openRivetRevsEditor(...): %s: rivetClassId %s is not found";
  String ERR_RIVET_REVS_EDITOR_RIVET_NOT_FOUND = "openRivetRevsEditor(...): %s: rivetId %s is not found";
}
