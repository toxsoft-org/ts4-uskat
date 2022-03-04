package org.toxsoft.uskat.concurrent;

/**
 * Синхронизация доступа к {@link ISkOneWsService} (декоратор)
 *
 * @author mvk TODO: доделать когда появится ISkOneWsService
 */
public final class S5SynchronizedOneWsService {

}

// public final class S5SynchronizedOneWsService
// extends S5SynchronizedService<ISkOneWsService>
// implements ISkOneWsService {
//
// /**
// * Конструктор
// *
// * @param aConnection {@link S5SynchronizedConnection} защищенное соединение
// * @throws TsNullArgumentRtException аругмент = null
// * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
// */
// public S5SynchronizedOneWsService( S5SynchronizedConnection aConnection ) {
// this( (ISkOneWsService)aConnection.getUnsynchronizedService( ISkOneWsService.SERVICE_ID ), aConnection.mainLock() );
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
// public S5SynchronizedOneWsService( ISkOneWsService aTarget, ReentrantReadWriteLock aLock ) {
// super( aTarget, aLock );
// }
//
// // ------------------------------------------------------------------------------------
// // S5SynchronizedResource
// //
// @Override
// protected void doChangeTarget( ISkOneWsService aPrevTarget, ISkOneWsService aNewTarget,
// ReentrantReadWriteLock aNewLock ) {
// // nop
// }
//
// // ------------------------------------------------------------------------------------
// // ISkOneWsService
// //
// @Override
// public IStridablesList<IOneWsProfile> listProfiles() {
// lockWrite( this );
// try {
// return target().listProfiles();
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public IOneWsProfile findProfile( String aProfileId ) {
// lockWrite( this );
// try {
// return target().findProfile( aProfileId );
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public IOneWsProfile getUserProfile( String aUserId ) {
// lockWrite( this );
// try {
// return target().getUserProfile( aUserId );
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public IOneWsProfile getDefaultProfile() {
// lockWrite( this );
// try {
// return target().getDefaultProfile();
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public IStringList listProfileUsers( String aProfileId ) {
// lockWrite( this );
// try {
// return target().listProfileUsers( aProfileId );
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public IStridablesList<IStridable> listKnownAbilityKinds() {
// lockWrite( this );
// try {
// return target().listKnownAbilityKinds();
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public IStridablesList<IOneWsAbility> listKnownAbilities() {
// lockWrite( this );
// try {
// return target().listKnownAbilities();
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public void setDefaultProfile( String aProfileId ) {
// lockWrite( this );
// try {
// target().setDefaultProfile( aProfileId );
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public void setUserProfile( String aUserId, String aProfileId ) {
// lockWrite( this );
// try {
// target().setUserProfile( aUserId, aProfileId );
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public <T extends IOneWsProfile> IOneWsProfileEditor<T> defineProfile( String aProfileId ) {
// lockWrite( this );
// try {
// return target().defineProfile( aProfileId );
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public void removeProfile( String aProfileId ) {
// lockWrite( this );
// try {
// target().removeProfile( aProfileId );
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public void defineAbilityKind( IStridable aKind ) {
// lockWrite( this );
// try {
// target().defineAbilityKind( aKind );
// }
// finally {
// unlockWrite( this );
// }
// }
//
// @Override
// public void defineAbility( IOneWsAbility aAbility ) {
// lockWrite( this );
// try {
// target().defineAbility( aAbility );
// }
// finally {
// unlockWrite( this );
// }
// }
//
// }
