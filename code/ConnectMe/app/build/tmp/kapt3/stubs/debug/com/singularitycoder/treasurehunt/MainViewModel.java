package com.singularitycoder.treasurehunt;

import java.lang.System;

@dagger.hilt.android.lifecycle.HiltViewModel()
@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u00012\u00020\u0002B\u001f\b\u0007\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\b\u00a2\u0006\u0002\u0010\tJ)\u0010\u0011\u001a\u00020\u00122\u000e\u0010\u0013\u001a\n \u0015*\u0004\u0018\u00010\u00140\u00142\u000e\u0010\u0016\u001a\n \u0015*\u0004\u0018\u00010\u00170\u0017H\u0096\u0001J\u0019\u0010\u0018\u001a\u00020\u00122\u000e\u0010\u0013\u001a\n \u0015*\u0004\u0018\u00010\u00140\u0014H\u0096\u0001J\b\u0010\u0019\u001a\u00020\u0012H\u0002J\b\u0010\u001a\u001a\u00020\u0012H\u0002J\u0006\u0010\u001b\u001a\u00020\u0012R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\r\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000e0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001c"}, d2 = {"Lcom/singularitycoder/treasurehunt/MainViewModel;", "Landroidx/lifecycle/ViewModel;", "Landroid/content/ServiceConnection;", "locationRepository", "Lcom/singularitycoder/connectme/helpers/locationData/LocationRepository;", "locationPreferences", "Lcom/singularitycoder/connectme/helpers/locationData/LocationPreferences;", "serviceConnection", "Lcom/singularitycoder/connectme/helpers/ForegroundLocationServiceConnection;", "(Lcom/singularitycoder/connectme/helpers/locationData/LocationRepository;Lcom/singularitycoder/connectme/helpers/locationData/LocationPreferences;Lcom/singularitycoder/connectme/helpers/ForegroundLocationServiceConnection;)V", "isReceivingLocationUpdates", "Lkotlinx/coroutines/flow/StateFlow;", "", "lastLocation", "Landroid/location/Location;", "getLastLocation", "()Lkotlinx/coroutines/flow/StateFlow;", "onServiceConnected", "", "p0", "Landroid/content/ComponentName;", "kotlin.jvm.PlatformType", "p1", "Landroid/os/IBinder;", "onServiceDisconnected", "startLocationUpdates", "stopLocationUpdates", "toggleLocationUpdates", "app_debug"})
public final class MainViewModel extends androidx.lifecycle.ViewModel implements android.content.ServiceConnection {
    private final com.singularitycoder.connectme.helpers.locationData.LocationPreferences locationPreferences = null;
    private final com.singularitycoder.connectme.helpers.ForegroundLocationServiceConnection serviceConnection = null;
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isReceivingLocationUpdates = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<android.location.Location> lastLocation = null;
    
    @javax.inject.Inject()
    public MainViewModel(@org.jetbrains.annotations.NotNull()
    com.singularitycoder.connectme.helpers.locationData.LocationRepository locationRepository, @org.jetbrains.annotations.NotNull()
    com.singularitycoder.connectme.helpers.locationData.LocationPreferences locationPreferences, @org.jetbrains.annotations.NotNull()
    com.singularitycoder.connectme.helpers.ForegroundLocationServiceConnection serviceConnection) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<android.location.Location> getLastLocation() {
        return null;
    }
    
    public final void toggleLocationUpdates() {
    }
    
    private final void startLocationUpdates() {
    }
    
    private final void stopLocationUpdates() {
    }
    
    @java.lang.Override()
    public void onServiceConnected(android.content.ComponentName p0, android.os.IBinder p1) {
    }
    
    @java.lang.Override()
    public void onServiceDisconnected(android.content.ComponentName p0) {
    }
}