package com.singularitycoder.connectme.helpers;

import java.lang.System;

/**
 * State holder for location permissions. Properties are implemented as State objects so that they
 * trigger a recomposition when the value changes (if the value is read within a Composable scope).
 * This also implements the behavior for requesting location permissions and updating the internal
 * state afterward.
 *
 * This class should be initialized in `onCreate()` of the Activity. Sample usage:
 *
 * ```
 * override fun onCreate(savedInstanceState: Bundle?) {
 *    super.onCreate(savedInstanceState)
 *
 *    val locationPermissionState = LocationPermissionState(this) { permissionState ->
 *        if (permissionState.accessFineLocationGranted) {
 *            // Do something requiring precise location permission
 *        }
 *    }
 *
 *    setContent {
 *        Button(
 *            onClick = { locationPermissionState.requestPermissions() }
 *        ) {
 *            Text("Click Me")
 *        }
 *    }
 * }
 * ```
 */
@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\u0010\u0011\n\u0002\u0010\u000e\n\u0002\b\u0007\u0018\u00002\u00020\u0001B!\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0000\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\u0002\u0010\u0007J\u0006\u0010\u0019\u001a\u00020\tJ\u0006\u0010\u001a\u001a\u00020\u0006J\u0006\u0010\u001b\u001a\u00020\tJ\b\u0010\u001c\u001a\u00020\u0006H\u0002R\u001e\u0010\n\u001a\u00020\t2\u0006\u0010\b\u001a\u00020\t@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u001e\u0010\r\u001a\u00020\t2\u0006\u0010\b\u001a\u00020\t@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\fR\u001e\u0010\u000f\u001a\u00020\t2\u0006\u0010\b\u001a\u00020\t@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\fR\u001e\u0010\u0011\u001a\u00020\t2\u0006\u0010\b\u001a\u00020\t@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0000\u0012\u0004\u0012\u00020\u00060\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00160\u00150\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0017\u001a\u00020\t2\u0006\u0010\b\u001a\u00020\t@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\f\u00a8\u0006\u001d"}, d2 = {"Lcom/singularitycoder/connectme/helpers/LocationPermissionState;", "", "activity", "Landroidx/activity/ComponentActivity;", "onResult", "Lkotlin/Function1;", "", "(Landroidx/activity/ComponentActivity;Lkotlin/jvm/functions/Function1;)V", "<set-?>", "", "accessCoarseLocationGranted", "getAccessCoarseLocationGranted", "()Z", "accessCoarseLocationNeedsRationale", "getAccessCoarseLocationNeedsRationale", "accessFineLocationGranted", "getAccessFineLocationGranted", "accessFineLocationNeedsRationale", "getAccessFineLocationNeedsRationale", "permissionLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "", "", "showDegradedExperience", "getShowDegradedExperience", "hasPermission", "requestPermissions", "shouldShowRationale", "updateState", "app_debug"})
public final class LocationPermissionState {
    private final androidx.activity.ComponentActivity activity = null;
    private final kotlin.jvm.functions.Function1<com.singularitycoder.connectme.helpers.LocationPermissionState, kotlin.Unit> onResult = null;
    
    /**
     * Whether permission was granted to access approximate location.
     */
    private boolean accessCoarseLocationGranted = false;
    
    /**
     * Whether to show a rationale for permission to access approximate location.
     */
    private boolean accessCoarseLocationNeedsRationale = false;
    
    /**
     * Whether permission was granted to access precise location.
     */
    private boolean accessFineLocationGranted = false;
    
    /**
     * Whether to show a rationale for permission to access precise location.
     */
    private boolean accessFineLocationNeedsRationale = false;
    
    /**
     * Whether to show a degraded experience (set after the permission is denied).
     */
    private boolean showDegradedExperience = false;
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String[]> permissionLauncher = null;
    
    public LocationPermissionState(@org.jetbrains.annotations.NotNull()
    androidx.activity.ComponentActivity activity, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.singularitycoder.connectme.helpers.LocationPermissionState, kotlin.Unit> onResult) {
        super();
    }
    
    public final boolean getAccessCoarseLocationGranted() {
        return false;
    }
    
    public final boolean getAccessCoarseLocationNeedsRationale() {
        return false;
    }
    
    public final boolean getAccessFineLocationGranted() {
        return false;
    }
    
    public final boolean getAccessFineLocationNeedsRationale() {
        return false;
    }
    
    public final boolean getShowDegradedExperience() {
        return false;
    }
    
    private final void updateState() {
    }
    
    /**
     * Launch the permission request. Note that this may or may not show the permission UI if the
     * permission has already been granted or if the user has denied permission multiple times.
     */
    public final void requestPermissions() {
    }
    
    public final boolean hasPermission() {
        return false;
    }
    
    public final boolean shouldShowRationale() {
        return false;
    }
}