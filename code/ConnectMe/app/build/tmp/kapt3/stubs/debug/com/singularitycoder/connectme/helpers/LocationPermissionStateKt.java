package com.singularitycoder.connectme.helpers;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 2, d1 = {"\u0000 \n\u0000\n\u0002\u0010\u0011\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u001a\u0014\u0010\u0004\u001a\u00020\u0005*\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0002H\u0000\u001a\u0014\u0010\b\u001a\u00020\u0005*\u00020\t2\u0006\u0010\u0007\u001a\u00020\u0002H\u0000\"\u0016\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0003\u00a8\u0006\n"}, d2 = {"locationPermissions", "", "", "[Ljava/lang/String;", "hasPermission", "", "Landroid/content/Context;", "permission", "shouldShowRationaleFor", "Landroid/app/Activity;", "app_debug"})
public final class LocationPermissionStateKt {
    private static final java.lang.String[] locationPermissions = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"};
    
    public static final boolean hasPermission(@org.jetbrains.annotations.NotNull()
    android.content.Context $this$hasPermission, @org.jetbrains.annotations.NotNull()
    java.lang.String permission) {
        return false;
    }
    
    public static final boolean shouldShowRationaleFor(@org.jetbrains.annotations.NotNull()
    android.app.Activity $this$shouldShowRationaleFor, @org.jetbrains.annotations.NotNull()
    java.lang.String permission) {
        return false;
    }
}