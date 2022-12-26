package com.singularitycoder.connectme.helpers;

import java.lang.System;

/**
 * ServiceConnection that provides access to a [ForegroundLocationService].
 */
@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rH\u0016J\u0010\u0010\u000e\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bH\u0016R\"\u0010\u0005\u001a\u0004\u0018\u00010\u00042\b\u0010\u0003\u001a\u0004\u0018\u00010\u0004@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u000f"}, d2 = {"Lcom/singularitycoder/connectme/helpers/ForegroundLocationServiceConnection;", "Landroid/content/ServiceConnection;", "()V", "<set-?>", "Lcom/singularitycoder/connectme/helpers/ForegroundLocationService;", "service", "getService", "()Lcom/singularitycoder/connectme/helpers/ForegroundLocationService;", "onServiceConnected", "", "name", "Landroid/content/ComponentName;", "binder", "Landroid/os/IBinder;", "onServiceDisconnected", "app_debug"})
public final class ForegroundLocationServiceConnection implements android.content.ServiceConnection {
    @org.jetbrains.annotations.Nullable()
    private com.singularitycoder.connectme.helpers.ForegroundLocationService service;
    
    @javax.inject.Inject()
    public ForegroundLocationServiceConnection() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.singularitycoder.connectme.helpers.ForegroundLocationService getService() {
        return null;
    }
    
    @java.lang.Override()
    public void onServiceConnected(@org.jetbrains.annotations.NotNull()
    android.content.ComponentName name, @org.jetbrains.annotations.NotNull()
    android.os.IBinder binder) {
    }
    
    @java.lang.Override()
    public void onServiceDisconnected(@org.jetbrains.annotations.NotNull()
    android.content.ComponentName name) {
    }
}