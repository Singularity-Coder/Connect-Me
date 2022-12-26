package com.singularitycoder.connectme.helpers;

import java.lang.System;

/**
 * Service which manages turning location updates on and off. UI clients should bind to this service
 * to access this functionality.
 *
 * This service can be started the usual way (i.e. startService), but it will also start itself when
 * the first client binds to it. Thereafter it will manage its own lifetime as follows:
 *  - While there are any bound clients, the service remains started in the background. If it was
 *    in the foreground, it will exit the foreground, cancelling any ongoing notification.
 *  - When there are no bound clients and location updates are on, the service moves to the
 *    foreground and shows an ongoing notification with the latest location.
 *  - When there are no bound clients and location updates are off, the service stops itself.
 */
@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000b\b\u0007\u0018\u0000 -2\u00020\u0001:\u0002-.B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u0002J\b\u0010\u001a\u001a\u00020\u001bH\u0002J\b\u0010\u001c\u001a\u00020\u001bH\u0002J\b\u0010\u001d\u001a\u00020\u001bH\u0002J\b\u0010\u001e\u001a\u00020\u001bH\u0002J\b\u0010\u001f\u001a\u00020\u0006H\u0002J\b\u0010 \u001a\u00020\u001bH\u0002J\u0010\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020$H\u0016J\u0012\u0010%\u001a\u00020\u001b2\b\u0010#\u001a\u0004\u0018\u00010$H\u0016J\"\u0010&\u001a\u00020\u00042\b\u0010#\u001a\u0004\u0018\u00010$2\u0006\u0010\'\u001a\u00020\u00042\u0006\u0010(\u001a\u00020\u0004H\u0016J\u0012\u0010)\u001a\u00020\u00062\b\u0010#\u001a\u0004\u0018\u00010$H\u0016J\u0012\u0010*\u001a\u00020\u001b2\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u0002J\u0006\u0010+\u001a\u00020\u001bJ\u0006\u0010,\u001a\u00020\u001bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0012\u0010\u0007\u001a\u00060\bR\u00020\u0000X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001e\u0010\t\u001a\u00020\n8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u001e\u0010\u000f\u001a\u00020\u00108\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R\u000e\u0010\u0015\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006/"}, d2 = {"Lcom/singularitycoder/connectme/helpers/ForegroundLocationService;", "Landroidx/lifecycle/LifecycleService;", "()V", "bindCount", "", "isForeground", "", "localBinder", "Lcom/singularitycoder/connectme/helpers/ForegroundLocationService$LocalBinder;", "locationPreferences", "Lcom/singularitycoder/connectme/helpers/locationData/LocationPreferences;", "getLocationPreferences", "()Lcom/singularitycoder/connectme/helpers/locationData/LocationPreferences;", "setLocationPreferences", "(Lcom/singularitycoder/connectme/helpers/locationData/LocationPreferences;)V", "locationRepository", "Lcom/singularitycoder/connectme/helpers/locationData/LocationRepository;", "getLocationRepository", "()Lcom/singularitycoder/connectme/helpers/locationData/LocationRepository;", "setLocationRepository", "(Lcom/singularitycoder/connectme/helpers/locationData/LocationRepository;)V", "started", "buildNotification", "Landroid/app/Notification;", "location", "Landroid/location/Location;", "createNotificationChannel", "", "enterForeground", "exitForeground", "handleBind", "isBound", "manageLifetime", "onBind", "Landroid/os/IBinder;", "intent", "Landroid/content/Intent;", "onRebind", "onStartCommand", "flags", "startId", "onUnbind", "showNotification", "startLocationUpdates", "stopLocationUpdates", "Companion", "LocalBinder", "app_debug"})
@dagger.hilt.android.AndroidEntryPoint()
public final class ForegroundLocationService extends androidx.lifecycle.LifecycleService {
    @javax.inject.Inject()
    public com.singularitycoder.connectme.helpers.locationData.LocationRepository locationRepository;
    @javax.inject.Inject()
    public com.singularitycoder.connectme.helpers.locationData.LocationPreferences locationPreferences;
    private final com.singularitycoder.connectme.helpers.ForegroundLocationService.LocalBinder localBinder = null;
    private int bindCount = 0;
    private boolean started = false;
    private boolean isForeground = false;
    @org.jetbrains.annotations.NotNull()
    private static final com.singularitycoder.connectme.helpers.ForegroundLocationService.Companion Companion = null;
    @java.lang.Deprecated()
    public static final long UNBIND_DELAY_MILLIS = 2000L;
    @java.lang.Deprecated()
    public static final int NOTIFICATION_ID = 1;
    @org.jetbrains.annotations.NotNull()
    @java.lang.Deprecated()
    public static final java.lang.String NOTIFICATION_CHANNEL_ID = "LocationUpdates";
    @org.jetbrains.annotations.NotNull()
    @java.lang.Deprecated()
    public static final java.lang.String ACTION_STOP_UPDATES = "com.singularitycoder.connectme.ACTION_STOP_UPDATES";
    
    public ForegroundLocationService() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.singularitycoder.connectme.helpers.locationData.LocationRepository getLocationRepository() {
        return null;
    }
    
    public final void setLocationRepository(@org.jetbrains.annotations.NotNull()
    com.singularitycoder.connectme.helpers.locationData.LocationRepository p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.singularitycoder.connectme.helpers.locationData.LocationPreferences getLocationPreferences() {
        return null;
    }
    
    public final void setLocationPreferences(@org.jetbrains.annotations.NotNull()
    com.singularitycoder.connectme.helpers.locationData.LocationPreferences p0) {
    }
    
    private final boolean isBound() {
        return false;
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public android.os.IBinder onBind(@org.jetbrains.annotations.NotNull()
    android.content.Intent intent) {
        return null;
    }
    
    @java.lang.Override()
    public void onRebind(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
    }
    
    private final void handleBind() {
    }
    
    @java.lang.Override()
    public boolean onUnbind(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
        return false;
    }
    
    private final void manageLifetime() {
    }
    
    private final void exitForeground() {
    }
    
    private final void enterForeground() {
    }
    
    private final void showNotification(android.location.Location location) {
    }
    
    private final void createNotificationChannel() {
    }
    
    private final android.app.Notification buildNotification(android.location.Location location) {
        return null;
    }
    
    public final void startLocationUpdates() {
    }
    
    public final void stopLocationUpdates() {
    }
    
    /**
     * Binder which provides clients access to the service.
     */
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0080\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0003\u001a\u00020\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/singularitycoder/connectme/helpers/ForegroundLocationService$LocalBinder;", "Landroid/os/Binder;", "(Lcom/singularitycoder/connectme/helpers/ForegroundLocationService;)V", "getService", "Lcom/singularitycoder/connectme/helpers/ForegroundLocationService;", "app_debug"})
    public final class LocalBinder extends android.os.Binder {
        
        public LocalBinder() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.singularitycoder.connectme.helpers.ForegroundLocationService getService() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0000\b\u0082\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/singularitycoder/connectme/helpers/ForegroundLocationService$Companion;", "", "()V", "ACTION_STOP_UPDATES", "", "NOTIFICATION_CHANNEL_ID", "NOTIFICATION_ID", "", "UNBIND_DELAY_MILLIS", "", "app_debug"})
    static final class Companion {
        
        private Companion() {
            super();
        }
    }
}