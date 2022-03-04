package org.toxsoft.uskat.concurrent;

/**
 * Синхронизация доступа к {@link ISkMnemoSchemeService} (декоратор)
 *
 * @author mvk TODO: сделать когда появится ISkMnemoSchemeService
 */
public final class S5SynchronizedMnemoSchemeService {

}
// public final class S5SynchronizedMnemoSchemeService
// extends S5SynchronizedService<ISkMnemoSchemeService>
// implements ISkMnemoSchemeService {
//
// /**
// * Конструктор
// *
// * @param aConnection {@link S5SynchronizedConnection} защищенное соединение
// * @throws TsNullArgumentRtException аругмент = null
// * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
// */
// public S5SynchronizedMnemoSchemeService( S5SynchronizedConnection aConnection ) {
// this( (ISkMnemoSchemeService)aConnection.getUnsynchronizedService( ISkMnemoSchemeService.SERVICE_ID ),
// aConnection.mainLock() );
// aConnection.addService( this );
// }
//
// /**
// * Конструктор
// *
// * @param aTarget {@link ISkOneWsService} защищаемый ресурс
// * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
// * @throws TsNullArgumentRtException любой аргумент = null
// */
// public S5SynchronizedMnemoSchemeService( ISkMnemoSchemeService aTarget, ReentrantReadWriteLock aLock ) {
// super( aTarget, aLock );
// }
//
// // ------------------------------------------------------------------------------------
// // S5SynchronizedResource
// //
// @Override
// protected void doChangeTarget( ISkMnemoSchemeService aPrevTarget, ISkMnemoSchemeService aNewTarget,
// ReentrantReadWriteLock aNewLock ) {
// // nop
// }
//
// // ------------------------------------------------------------------------------------
// // ISkMnemoSchemeService
// //
// @Override
// public void defineMnemoscheme( Gwid aMnemoGwid, IMnemoSchemeCfg aCfg ) {
// lockWrite( this );
// try {
// target().defineMnemoscheme( aMnemoGwid, aCfg );
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public IMnemoSchemeCfg readMnemoscheme( Gwid aMnemoGwid ) {
// lockWrite( this );
// try {
// return target().readMnemoscheme( aMnemoGwid );
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public boolean isMnemoscheme( Gwid aMnemoGwid ) {
// lockWrite( this );
// try {
// return target().isMnemoscheme( aMnemoGwid );
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public IList<Gwid> listAllMnemoschemes() {
// lockWrite( this );
// try {
// return target().listAllMnemoschemes();
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public void removeMnemoscheme( Gwid aMnemoGwid ) {
// lockWrite( this );
// try {
// target().removeMnemoscheme( aMnemoGwid );
// }
// finally {
// unlockWrite( this );
// }
// }
//
// }
