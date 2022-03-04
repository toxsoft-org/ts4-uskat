package org.toxsoft.uskat.concurrent;

/**
 * Синхронизация доступа к {@link ISkRefbookService} (декоратор)
 *
 * @author mvk TODO: доделать когда появиться ISkRefbookService
 */
public final class S5SynchronizedRefbookService {

}
// public final class S5SynchronizedRefbookService
// extends S5SynchronizedService<ISkRefbookService>
// implements ISkRefbookService {
//
// private final S5SynchronizedEventer<ISkRefbookServiceListener> eventer;
// private final S5SynchronizedValidationSupport<ISkRefbookServiceValidator> svs;
//
// /**
// * Конструктор
// *
// * @param aConnection {@link S5SynchronizedConnection} защищенное соединение
// * @throws TsNullArgumentRtException аругмент = null
// * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
// */
// public S5SynchronizedRefbookService( S5SynchronizedConnection aConnection ) {
// this( (ISkRefbookService)aConnection.getUnsynchronizedService( ISkRefbookService.SERVICE_ID ),
// aConnection.mainLock() );
// aConnection.addService( this );
// }
//
// /**
// * Конструктор
// *
// * @param aTarget {@link ISkRefbookService} защищаемый ресурс
// * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
// * @throws TsNullArgumentRtException любой аргумент = null
// */
// public S5SynchronizedRefbookService( ISkRefbookService aTarget, ReentrantReadWriteLock aLock ) {
// super( aTarget, aLock );
// eventer = new S5SynchronizedEventer<>( target().eventer(), lock() );
// svs = new S5SynchronizedValidationSupport<>( aTarget.svs(), aLock );
// }
//
// // ------------------------------------------------------------------------------------
// // S5SynchronizedResource
// //
// @Override
// protected void doChangeTarget( ISkRefbookService aPrevTarget, ISkRefbookService aNewTarget,
// ReentrantReadWriteLock aNewLock ) {
// eventer.changeTarget( aNewTarget.eventer(), aNewLock );
// svs.changeTarget( aNewTarget.svs(), aNewLock );
// }
//
// // ------------------------------------------------------------------------------------
// // ISkRefbookService
// //
// @Override
// public ISkRefbook findRefbook( String aRefbookId ) {
// lockWrite( this );
// try {
// return target().findRefbook( aRefbookId );
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public ISkRefbook findRefbookByItemClassId( String aRefbookItemClassId ) {
// lockWrite( this );
// try {
// return target().findRefbookByItemClassId( aRefbookItemClassId );
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public IStridablesList<ISkRefbook> listRefbooks() {
// lockWrite( this );
// try {
// return target().listRefbooks();
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public ISkRefbook defineRefbook( IDpuRefbookInfo aDpuRefbookInfo ) {
// lockWrite( this );
// try {
// return target().defineRefbook( aDpuRefbookInfo );
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public void removeRefbook( String aRefbookId ) {
// lockWrite( this );
// try {
// target().removeRefbook( aRefbookId );
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public ISkRefbookHistory history() {
// lockWrite( this );
// try {
// return target().history();
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public IServiceValidationSupport<ISkRefbookServiceValidator> svs() {
// return svs;
// }
//
// @Override
// public IServiceEventsFiringSupport<ISkRefbookServiceListener> eventer() {
// return eventer;
// }
// }
