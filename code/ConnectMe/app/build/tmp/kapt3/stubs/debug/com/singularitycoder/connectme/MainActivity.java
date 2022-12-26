package com.singularitycoder.connectme;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0011\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u000e\b\u0007\u0018\u00002\u00020\u0001:\u00015B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010#\u001a\u00020$H\u0002J\b\u0010%\u001a\u00020$H\u0002J\u0012\u0010&\u001a\u00020$2\b\u0010\'\u001a\u0004\u0018\u00010(H\u0014J\b\u0010)\u001a\u00020$H\u0014J\b\u0010*\u001a\u00020$H\u0014J\b\u0010+\u001a\u00020$H\u0014J\b\u0010,\u001a\u00020$H\u0014J\b\u0010-\u001a\u00020$H\u0014J\b\u0010.\u001a\u00020$H\u0002J\b\u0010/\u001a\u00020$H\u0002J\b\u00100\u001a\u00020$H\u0002J\b\u00101\u001a\u00020$H\u0002J\b\u00102\u001a\u00020$H\u0002J\f\u00103\u001a\u00020$*\u00020\u0004H\u0002J\f\u00104\u001a\u00020$*\u00020\u0004H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u001c\u0010\u000b\u001a\u0010\u0012\f\u0012\n \u000e*\u0004\u0018\u00010\r0\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\u00120\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0013\u001a\u00020\u00148\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0015\u0010\u0016\"\u0004\b\u0017\u0010\u0018R\u0014\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\r0\u001aX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u001b\u001a\u00020\u001c8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001f\u0010 \u001a\u0004\b\u001d\u0010\u001eR\u000e\u0010!\u001a\u00020\"X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00066"}, d2 = {"Lcom/singularitycoder/connectme/MainActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "Lcom/singularitycoder/connectme/databinding/ActivityMainBinding;", "lastUpdatedLocation", "Landroid/location/Location;", "getLastUpdatedLocation", "()Landroid/location/Location;", "setLastUpdatedLocation", "(Landroid/location/Location;)V", "locationPermissionResult", "Landroidx/activity/result/ActivityResultLauncher;", "", "kotlin.jvm.PlatformType", "locationToggleStatusReceiver", "Landroid/content/BroadcastReceiver;", "permissionsResult", "", "playServicesAvailabilityChecker", "Lcom/singularitycoder/connectme/helpers/locationData/PlayServicesAvailabilityChecker;", "getPlayServicesAvailabilityChecker", "()Lcom/singularitycoder/connectme/helpers/locationData/PlayServicesAvailabilityChecker;", "setPlayServicesAvailabilityChecker", "(Lcom/singularitycoder/connectme/helpers/locationData/PlayServicesAvailabilityChecker;)V", "tabNamesList", "", "viewModel", "Lcom/singularitycoder/treasurehunt/MainViewModel;", "getViewModel", "()Lcom/singularitycoder/treasurehunt/MainViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "viewPager2PageChangeListener", "Landroidx/viewpager2/widget/ViewPager2$OnPageChangeCallback;", "grantLocationPermissions", "", "observeForData", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onPause", "onResume", "onStart", "onStop", "registerReceiver", "setLocationToggleListener", "showLocationPermissionDialog", "showLocationToggleDialog", "unregisterReceiver", "setUpViewPager", "setupUI", "HomeViewPagerAdapter", "app_debug"})
@dagger.hilt.android.AndroidEntryPoint()
public final class MainActivity extends androidx.appcompat.app.AppCompatActivity {
    @javax.inject.Inject()
    public com.singularitycoder.connectme.helpers.locationData.PlayServicesAvailabilityChecker playServicesAvailabilityChecker;
    private com.singularitycoder.connectme.databinding.ActivityMainBinding binding;
    private final kotlin.Lazy viewModel$delegate = null;
    private final java.util.List<java.lang.String> tabNamesList = null;
    @org.jetbrains.annotations.Nullable()
    private android.location.Location lastUpdatedLocation;
    private final android.content.BroadcastReceiver locationToggleStatusReceiver = null;
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String[]> permissionsResult = null;
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> locationPermissionResult = null;
    private final androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback viewPager2PageChangeListener = null;
    
    public MainActivity() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.singularitycoder.connectme.helpers.locationData.PlayServicesAvailabilityChecker getPlayServicesAvailabilityChecker() {
        return null;
    }
    
    public final void setPlayServicesAvailabilityChecker(@org.jetbrains.annotations.NotNull()
    com.singularitycoder.connectme.helpers.locationData.PlayServicesAvailabilityChecker p0) {
    }
    
    private final com.singularitycoder.treasurehunt.MainViewModel getViewModel() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.location.Location getLastUpdatedLocation() {
        return null;
    }
    
    public final void setLastUpdatedLocation(@org.jetbrains.annotations.Nullable()
    android.location.Location p0) {
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    protected void onStart() {
    }
    
    @java.lang.Override()
    protected void onResume() {
    }
    
    @java.lang.Override()
    protected void onPause() {
    }
    
    @java.lang.Override()
    protected void onStop() {
    }
    
    @java.lang.Override()
    protected void onDestroy() {
    }
    
    private final void setupUI(com.singularitycoder.connectme.databinding.ActivityMainBinding $this$setupUI) {
    }
    
    private final void setUpViewPager(com.singularitycoder.connectme.databinding.ActivityMainBinding $this$setUpViewPager) {
    }
    
    private final void setLocationToggleListener() {
    }
    
    private final void observeForData() {
    }
    
    private final void grantLocationPermissions() {
    }
    
    private final void registerReceiver() {
    }
    
    private final void unregisterReceiver() {
    }
    
    private final void showLocationToggleDialog() {
    }
    
    private final void showLocationPermissionDialog() {
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0004\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0016J\b\u0010\u000b\u001a\u00020\nH\u0016\u00a8\u0006\f"}, d2 = {"Lcom/singularitycoder/connectme/MainActivity$HomeViewPagerAdapter;", "Landroidx/viewpager2/adapter/FragmentStateAdapter;", "fragmentManager", "Landroidx/fragment/app/FragmentManager;", "lifecycle", "Landroidx/lifecycle/Lifecycle;", "(Lcom/singularitycoder/connectme/MainActivity;Landroidx/fragment/app/FragmentManager;Landroidx/lifecycle/Lifecycle;)V", "createFragment", "Landroidx/fragment/app/Fragment;", "position", "", "getItemCount", "app_debug"})
    public final class HomeViewPagerAdapter extends androidx.viewpager2.adapter.FragmentStateAdapter {
        
        public HomeViewPagerAdapter(@org.jetbrains.annotations.NotNull()
        androidx.fragment.app.FragmentManager fragmentManager, @org.jetbrains.annotations.NotNull()
        androidx.lifecycle.Lifecycle lifecycle) {
            super(null);
        }
        
        @java.lang.Override()
        public int getItemCount() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        @java.lang.Override()
        public androidx.fragment.app.Fragment createFragment(int position) {
            return null;
        }
    }
}