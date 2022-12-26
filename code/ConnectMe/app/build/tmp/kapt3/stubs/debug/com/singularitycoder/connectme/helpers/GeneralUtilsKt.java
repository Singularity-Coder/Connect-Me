package com.singularitycoder.connectme.helpers;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 2, d1 = {"\u0000\u00b0\u0001\n\u0000\n\u0002\u0010\u0011\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0006\n\u0002\u0010\u0007\n\u0002\u0010\u0004\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0015\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0018\u0002\n\u0000\u001a.\u0010\u000e\u001a\u00020\u000f2\b\u0010\u0010\u001a\u0004\u0018\u00010\u00112\u0006\u0010\u0012\u001a\u00020\u00022\u000e\b\u0006\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0014H\u0086\b\u00f8\u0001\u0000\u001a\u0006\u0010\u0015\u001a\u00020\u0016\u001a\u0006\u0010\u0017\u001a\u00020\u0016\u001a\u001c\u0010\u0018\u001a\u00020\u000f2\u0006\u0010\u0019\u001a\u00020\u00072\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0014\u001a\u0006\u0010\u001b\u001a\u00020\u001c\u001a\u000e\u0010\u001d\u001a\u00020\u000f2\u0006\u0010\u001e\u001a\u00020\u001f\u001a\u000e\u0010 \u001a\u0004\u0018\u00010!*\u0004\u0018\u00010\u001f\u001aJ\u0010\"\u001a\u00020\u000f\"\u0004\b\u0000\u0010#*\u00020$2\f\u0010%\u001a\b\u0012\u0004\u0012\u0002H#0&2\"\u0010\'\u001a\u001e\b\u0001\u0012\u0004\u0012\u0002H#\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0)\u0012\u0006\u0012\u0004\u0018\u00010*0(\u00f8\u0001\u0001\u00a2\u0006\u0002\u0010+\u001a\u0014\u0010,\u001a\u00020\u0016*\u00020\u001f2\b\b\u0001\u0010-\u001a\u00020\u0016\u001a \u0010.\u001a\u00020\u0002*\u0004\u0018\u00010\u00112\b\u0010\u0010\u001a\u0004\u0018\u00010\u00022\b\u0010/\u001a\u0004\u0018\u00010\u0002\u001a\n\u00100\u001a\u000201*\u000202\u001a\u0016\u00103\u001a\u0004\u0018\u000104*\u00020\u001f2\b\b\u0001\u00105\u001a\u00020\u0016\u001a,\u00106\u001a\u00020\u0011*\u00020\u001f2\b\b\u0002\u00107\u001a\u00020\u00022\n\b\u0002\u00108\u001a\u0004\u0018\u00010\u00022\n\b\u0002\u0010/\u001a\u0004\u0018\u00010\u0002\u001a\u0016\u00109\u001a\u0004\u0018\u00010:*\u00020\u001f2\u0006\u0010;\u001a\u00020<H\u0007\u001a\n\u0010=\u001a\u00020\u000f*\u00020>\u001a\f\u0010=\u001a\u00020\u000f*\u0004\u0018\u00010?\u001a\"\u0010@\u001a\u00020\u0011*\u00020\u001f2\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u00022\n\b\u0002\u0010/\u001a\u0004\u0018\u00010\u0002\u001a\n\u0010A\u001a\u00020\u000b*\u00020\u001f\u001a\n\u0010B\u001a\u00020\u000b*\u00020\u001f\u001a\n\u0010C\u001a\u00020\u000b*\u00020\u001f\u001a\u0012\u0010D\u001a\u00020\u000f*\u00020\u001f2\u0006\u0010E\u001a\u00020\u0002\u001a \u0010F\u001a\u00020\u000f*\u00020?2\u000e\b\u0004\u0010G\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0014H\u0086\b\u00f8\u0001\u0000\u001a\n\u0010H\u001a\u000201*\u000202\u001a\n\u0010I\u001a\u000201*\u000202\u001a\u0012\u0010J\u001a\u00020\u000f*\u00020\u001f2\u0006\u0010E\u001a\u00020\u0002\u001a\u0012\u0010K\u001a\u00020\u000f*\u00020\u001f2\u0006\u0010L\u001a\u00020\u0002\u001aC\u0010M\u001a\u00020\u000f*\u00020\f2\n\b\u0002\u0010N\u001a\u0004\u0018\u00010\u00162\b\b\u0002\u0010O\u001a\u00020\u00162\b\b\u0002\u0010P\u001a\u00020\u00162\b\b\u0002\u0010Q\u001a\u00020\u00162\b\b\u0002\u0010R\u001a\u00020\u0016\u00a2\u0006\u0002\u0010S\u001a\n\u0010T\u001a\u00020\u000f*\u00020U\u001a.\u0010V\u001a\u00020\u000f*\u00020\u001f2\u0006\u0010W\u001a\u00020<2\u0006\u0010X\u001a\u00020\u00022\u0006\u0010Y\u001a\u00020\u00022\n\b\u0002\u0010Z\u001a\u0004\u0018\u00010\u0002\u001a\f\u0010[\u001a\u00020\u000f*\u0004\u0018\u00010?\u001a\n\u0010\\\u001a\u00020\u000f*\u00020\u001f\u001a\u001a\u0010]\u001a\u00020\u000f*\u00020$2\u0006\u0010^\u001a\u00020_2\u0006\u0010`\u001a\u00020\u0002\u001aB\u0010a\u001a\u00020\u000f*\u00020\f2\u0006\u0010b\u001a\u00020\u00022\n\b\u0002\u0010c\u001a\u0004\u0018\u00010\f2\b\b\u0002\u0010\u0019\u001a\u00020\u00162\b\b\u0002\u0010d\u001a\u00020\u00022\u000e\b\u0002\u0010e\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0014\u001a\u001c\u0010f\u001a\u00020\u000f*\u00020\u001f2\u0006\u0010b\u001a\u00020\u00022\b\b\u0002\u0010\u0019\u001a\u00020\u0016\u001a\n\u0010g\u001a\u000201*\u000202\u001a\u001c\u0010h\u001a\u0004\u0018\u00010:*\u00020\f2\u0006\u0010i\u001a\u00020\u00162\u0006\u0010j\u001a\u00020\u0016\u001a\u0014\u0010k\u001a\u0004\u0018\u00010:*\u00020\f2\u0006\u0010l\u001a\u00020\u0016\u001a\n\u0010m\u001a\u00020\u0002*\u00020\u0007\u001a\u0015\u0010n\u001a\u00020\u0002*\u00020\u00072\u0006\u0010o\u001a\u00020pH\u0086\u0004\"\u0019\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001\u00a2\u0006\n\n\u0002\u0010\u0005\u001a\u0004\b\u0003\u0010\u0004\"\u0011\u0010\u0006\u001a\u00020\u00078F\u00a2\u0006\u0006\u001a\u0004\b\b\u0010\t\"\u0015\u0010\n\u001a\u00020\u000b*\u00020\f8F\u00a2\u0006\u0006\u001a\u0004\b\n\u0010\r\u0082\u0002\u000b\n\u0005\b\u009920\u0001\n\u0002\b\u0019\u00a8\u0006q"}, d2 = {"mainActivityPermissions", "", "", "getMainActivityPermissions", "()[Ljava/lang/String;", "[Ljava/lang/String;", "timeNow", "", "getTimeNow", "()J", "isKeyboardVisible", "", "Landroid/view/View;", "(Landroid/view/View;)Z", "deleteAllFilesFrom", "", "directory", "Ljava/io/File;", "withName", "onDone", "Lkotlin/Function0;", "deviceHeight", "", "deviceWidth", "doAfter", "duration", "task", "getDeviceSize", "Landroid/graphics/Point;", "showSettingsAlert", "context", "Landroid/content/Context;", "clipboard", "Landroid/content/ClipboardManager;", "collectLatestLifecycleFlow", "T", "Landroidx/appcompat/app/AppCompatActivity;", "flow", "Lkotlinx/coroutines/flow/Flow;", "collect", "Lkotlin/Function2;", "Lkotlin/coroutines/Continuation;", "", "(Landroidx/appcompat/app/AppCompatActivity;Lkotlinx/coroutines/flow/Flow;Lkotlin/jvm/functions/Function2;)V", "color", "colorRes", "customPath", "fileName", "dpToPx", "", "", "drawable", "Landroid/graphics/drawable/Drawable;", "drawableRes", "externalFilesDir", "rootDir", "subDir", "getVideoThumbnailBitmap", "Landroid/graphics/Bitmap;", "docUri", "Landroid/net/Uri;", "hideKeyboard", "Landroid/app/Activity;", "Landroid/widget/EditText;", "internalFilesDir", "isCameraPresent", "isLocationPermissionGranted", "isLocationToggleEnabled", "makeCall", "phoneNum", "onImeDoneClick", "callback", "pxToDp", "pxToSp", "sendSms", "sendWhatsAppMessage", "whatsAppPhoneNum", "setMargins", "all", "start", "top", "end", "bottom", "(Landroid/view/View;Ljava/lang/Integer;IIII)V", "setTransparentBackground", "Lcom/google/android/material/bottomsheet/BottomSheetDialogFragment;", "shareImageAndTextViaApps", "uri", "title", "subtitle", "intentTitle", "showKeyboard", "showPermissionSettings", "showScreen", "fragment", "Landroidx/fragment/app/Fragment;", "tag", "showSnackBar", "message", "anchorView", "actionBtnText", "action", "showToast", "spToPx", "toBitmapOf", "width", "height", "toBitmapWith", "defaultColor", "toIntuitiveDateTime", "toTimeOfType", "type", "Lcom/singularitycoder/connectme/helpers/DateType;", "app_debug"})
public final class GeneralUtilsKt {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String[] mainActivityPermissions = {"android.permission.READ_CONTACTS", "android.permission.CAMERA"};
    
    public static final void showSnackBar(@org.jetbrains.annotations.NotNull()
    android.view.View $this$showSnackBar, @org.jetbrains.annotations.NotNull()
    java.lang.String message, @org.jetbrains.annotations.Nullable()
    android.view.View anchorView, int duration, @org.jetbrains.annotations.NotNull()
    java.lang.String actionBtnText, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> action) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final android.graphics.Point getDeviceSize() {
        return null;
    }
    
    public static final int deviceWidth() {
        return 0;
    }
    
    public static final int deviceHeight() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String customPath(@org.jetbrains.annotations.Nullable()
    java.io.File $this$customPath, @org.jetbrains.annotations.Nullable()
    java.lang.String directory, @org.jetbrains.annotations.Nullable()
    java.lang.String fileName) {
        return null;
    }
    
    /**
     * /data/user/0/com.singularitycoder.audioweb/files
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.io.File internalFilesDir(@org.jetbrains.annotations.NotNull()
    android.content.Context $this$internalFilesDir, @org.jetbrains.annotations.Nullable()
    java.lang.String directory, @org.jetbrains.annotations.Nullable()
    java.lang.String fileName) {
        return null;
    }
    
    /**
     * /storage/emulated/0/Android/data/com.singularitycoder.audioweb/files
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.io.File externalFilesDir(@org.jetbrains.annotations.NotNull()
    android.content.Context $this$externalFilesDir, @org.jetbrains.annotations.NotNull()
    java.lang.String rootDir, @org.jetbrains.annotations.Nullable()
    java.lang.String subDir, @org.jetbrains.annotations.Nullable()
    java.lang.String fileName) {
        return null;
    }
    
    public static final void deleteAllFilesFrom(@org.jetbrains.annotations.Nullable()
    java.io.File directory, @org.jetbrains.annotations.NotNull()
    java.lang.String withName, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDone) {
    }
    
    public static final boolean isCameraPresent(@org.jetbrains.annotations.NotNull()
    android.content.Context $this$isCameraPresent) {
        return false;
    }
    
    public static final long getTimeNow() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String toIntuitiveDateTime(long $this$toIntuitiveDateTime) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String toTimeOfType(long $this$toTimeOfType, @org.jetbrains.annotations.NotNull()
    com.singularitycoder.connectme.helpers.DateType type) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String[] getMainActivityPermissions() {
        return null;
    }
    
    public static final boolean isLocationPermissionGranted(@org.jetbrains.annotations.NotNull()
    android.content.Context $this$isLocationPermissionGranted) {
        return false;
    }
    
    public static final void showPermissionSettings(@org.jetbrains.annotations.NotNull()
    android.content.Context $this$showPermissionSettings) {
    }
    
    public static final void showToast(@org.jetbrains.annotations.NotNull()
    android.content.Context $this$showToast, @org.jetbrains.annotations.NotNull()
    java.lang.String message, int duration) {
    }
    
    public static final void doAfter(long duration, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> task) {
    }
    
    public static final float dpToPx(@org.jetbrains.annotations.NotNull()
    java.lang.Number $this$dpToPx) {
        return 0.0F;
    }
    
    public static final float pxToDp(@org.jetbrains.annotations.NotNull()
    java.lang.Number $this$pxToDp) {
        return 0.0F;
    }
    
    public static final float spToPx(@org.jetbrains.annotations.NotNull()
    java.lang.Number $this$spToPx) {
        return 0.0F;
    }
    
    public static final float pxToSp(@org.jetbrains.annotations.NotNull()
    java.lang.Number $this$pxToSp) {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.Nullable()
    @androidx.annotation.RequiresApi(value = android.os.Build.VERSION_CODES.O_MR1)
    public static final android.graphics.Bitmap getVideoThumbnailBitmap(@org.jetbrains.annotations.NotNull()
    android.content.Context $this$getVideoThumbnailBitmap, @org.jetbrains.annotations.NotNull()
    android.net.Uri docUri) {
        return null;
    }
    
    public static final void shareImageAndTextViaApps(@org.jetbrains.annotations.NotNull()
    android.content.Context $this$shareImageAndTextViaApps, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String subtitle, @org.jetbrains.annotations.Nullable()
    java.lang.String intentTitle) {
    }
    
    public static final void makeCall(@org.jetbrains.annotations.NotNull()
    android.content.Context $this$makeCall, @org.jetbrains.annotations.NotNull()
    java.lang.String phoneNum) {
    }
    
    public static final void sendSms(@org.jetbrains.annotations.NotNull()
    android.content.Context $this$sendSms, @org.jetbrains.annotations.NotNull()
    java.lang.String phoneNum) {
    }
    
    public static final void sendWhatsAppMessage(@org.jetbrains.annotations.NotNull()
    android.content.Context $this$sendWhatsAppMessage, @org.jetbrains.annotations.NotNull()
    java.lang.String whatsAppPhoneNum) {
    }
    
    public static final void setTransparentBackground(@org.jetbrains.annotations.NotNull()
    com.google.android.material.bottomsheet.BottomSheetDialogFragment $this$setTransparentBackground) {
    }
    
    public static final int color(@org.jetbrains.annotations.NotNull()
    android.content.Context $this$color, @androidx.annotation.ColorRes()
    int colorRes) {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public static final android.graphics.drawable.Drawable drawable(@org.jetbrains.annotations.NotNull()
    android.content.Context $this$drawable, @androidx.annotation.DrawableRes()
    int drawableRes) {
        return null;
    }
    
    public static final void showScreen(@org.jetbrains.annotations.NotNull()
    androidx.appcompat.app.AppCompatActivity $this$showScreen, @org.jetbrains.annotations.NotNull()
    androidx.fragment.app.Fragment fragment, @org.jetbrains.annotations.NotNull()
    java.lang.String tag) {
    }
    
    public static final void setMargins(@org.jetbrains.annotations.NotNull()
    android.view.View $this$setMargins, @org.jetbrains.annotations.Nullable()
    java.lang.Integer all, int start, int top, int end, int bottom) {
    }
    
    public static final void onImeDoneClick(@org.jetbrains.annotations.NotNull()
    android.widget.EditText $this$onImeDoneClick, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> callback) {
    }
    
    /**
     * If layout inflated already
     */
    @org.jetbrains.annotations.Nullable()
    public static final android.graphics.Bitmap toBitmapWith(@org.jetbrains.annotations.NotNull()
    android.view.View $this$toBitmapWith, int defaultColor) {
        return null;
    }
    
    /**
     * When layout not inflated yet. Measure the view first before extracting the bitmap.
     * Else the width and height will be 0. Which means u cant do view.width & view.height.
     * If unsure of width and height specify MeasureSpec.UNSPECIFIED
     */
    @org.jetbrains.annotations.Nullable()
    public static final android.graphics.Bitmap toBitmapOf(@org.jetbrains.annotations.NotNull()
    android.view.View $this$toBitmapOf, int width, int height) {
        return null;
    }
    
    public static final <T extends java.lang.Object>void collectLatestLifecycleFlow(@org.jetbrains.annotations.NotNull()
    androidx.appcompat.app.AppCompatActivity $this$collectLatestLifecycleFlow, @org.jetbrains.annotations.NotNull()
    kotlinx.coroutines.flow.Flow<? extends T> flow, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super T, ? super kotlin.coroutines.Continuation<? super kotlin.Unit>, ? extends java.lang.Object> collect) {
    }
    
    public static final void showSettingsAlert(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public static final android.content.ClipboardManager clipboard(@org.jetbrains.annotations.Nullable()
    android.content.Context $this$clipboard) {
        return null;
    }
    
    /**
     * Request focus before showing keyboard - editText.requestFocus()
     */
    public static final void showKeyboard(@org.jetbrains.annotations.Nullable()
    android.widget.EditText $this$showKeyboard) {
    }
    
    /**
     * Request focus before hiding keyboard - editText.requestFocus()
     */
    public static final void hideKeyboard(@org.jetbrains.annotations.Nullable()
    android.widget.EditText $this$hideKeyboard) {
    }
    
    public static final void hideKeyboard(@org.jetbrains.annotations.NotNull()
    android.app.Activity $this$hideKeyboard) {
    }
    
    public static final boolean isKeyboardVisible(@org.jetbrains.annotations.NotNull()
    android.view.View $this$isKeyboardVisible) {
        return false;
    }
    
    public static final boolean isLocationToggleEnabled(@org.jetbrains.annotations.NotNull()
    android.content.Context $this$isLocationToggleEnabled) {
        return false;
    }
}