package com.singularitycoder.connectme.helpers.locationData;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001:\u0001\u0014B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0011\u001a\u00020\u0012H\u0007J\u0006\u0010\u0013\u001a\u00020\u0012R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0012\u0010\n\u001a\u00060\u000bR\u00020\u0000X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00070\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u000eR\u0019\u0010\u000f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000e\u00a8\u0006\u0015"}, d2 = {"Lcom/singularitycoder/connectme/helpers/locationData/LocationRepository;", "", "fusedLocationProviderClient", "Lcom/google/android/gms/location/FusedLocationProviderClient;", "(Lcom/google/android/gms/location/FusedLocationProviderClient;)V", "_isReceivingUpdates", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_lastLocation", "Landroid/location/Location;", "callback", "Lcom/singularitycoder/connectme/helpers/locationData/LocationRepository$Callback;", "isReceivingLocationUpdates", "Lkotlinx/coroutines/flow/StateFlow;", "()Lkotlinx/coroutines/flow/StateFlow;", "lastLocation", "getLastLocation", "startLocationUpdates", "", "stopLocationUpdates", "Callback", "app_debug"})
@javax.inject.Singleton()
public final class LocationRepository {
    private final com.google.android.gms.location.FusedLocationProviderClient fusedLocationProviderClient = null;
    private final com.singularitycoder.connectme.helpers.locationData.LocationRepository.Callback callback = null;
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isReceivingUpdates = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isReceivingLocationUpdates = null;
    private final kotlinx.coroutines.flow.MutableStateFlow<android.location.Location> _lastLocation = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<android.location.Location> lastLocation = null;
    
    @javax.inject.Inject()
    public LocationRepository(@org.jetbrains.annotations.NotNull()
    com.google.android.gms.location.FusedLocationProviderClient fusedLocationProviderClient) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isReceivingLocationUpdates() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<android.location.Location> getLastLocation() {
        return null;
    }
    
    @android.annotation.SuppressLint(value = {"MissingPermission"})
    public final void startLocationUpdates() {
    }
    
    public final void stopLocationUpdates() {
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0082\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0016\u00a8\u0006\u0007"}, d2 = {"Lcom/singularitycoder/connectme/helpers/locationData/LocationRepository$Callback;", "Lcom/google/android/gms/location/LocationCallback;", "(Lcom/singularitycoder/connectme/helpers/locationData/LocationRepository;)V", "onLocationResult", "", "result", "Lcom/google/android/gms/location/LocationResult;", "app_debug"})
    final class Callback extends com.google.android.gms.location.LocationCallback {
        
        public Callback() {
            super();
        }
        
        @java.lang.Override()
        public void onLocationResult(@org.jetbrains.annotations.NotNull()
        com.google.android.gms.location.LocationResult result) {
        }
    }
}