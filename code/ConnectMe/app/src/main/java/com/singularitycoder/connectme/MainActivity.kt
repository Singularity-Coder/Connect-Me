package com.singularitycoder.connectme

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.singularitycoder.connectme.databinding.ActivityMainBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.locationData.PlayServicesAvailabilityChecker
import com.singularitycoder.connectme.search.SearchFragment
import com.singularitycoder.treasurehunt.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var playServicesAvailabilityChecker: PlayServicesAvailabilityChecker

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

//    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
//        override fun handleOnBackPressed() {
//            val searchFragment = supportFragmentManager.fragments.firstOrNull {
//                it.javaClass.simpleName == SearchFragment.newInstance("").javaClass.simpleName
//            } as? SearchFragment
//            if (searchFragment?.isKeyboardShown() == true) {
//                searchFragment.doWhenSearchIsNotFocused()
//            } else {
//                this@MainActivity.finish()
//            }
//        }
//    }

    // FIXME This is not working
    private val locationToggleStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != BroadcastKey.LOCATION_TOGGLE_STATUS) return
            val isLocationToggleOn = intent.getBooleanExtra(IntentKey.LOCATION_TOGGLE_STATUS, false)
            if (isLocationToggleOn.not()) {
                showLocationToggleDialog()
            }
        }
    }

    private val permissionsResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions: Map<String, @JvmSuppressWildcards Boolean>? ->
        permissions?.entries?.forEach { it: Map.Entry<String, @JvmSuppressWildcards Boolean> ->
            val permission = it.key
            val isGranted = it.value
            when {
                isGranted -> Unit
                ActivityCompat.shouldShowRequestPermissionRationale(this, permission) -> {
                    // Permission denied but not permanently, tell user why you need it. Ideally provide a button to request it again and another to dismiss
                }
                else -> {
                    // permission permanently denied. Show settings dialog
                }
            }
        }
    }

    private val locationPermissionResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isPermissionGranted: Boolean? ->
        isPermissionGranted ?: return@registerForActivityResult
        if (isPermissionGranted.not()) {
            val accessFineLocationNeedsRationale = shouldShowRationaleFor(Manifest.permission.ACCESS_FINE_LOCATION)
            if (accessFineLocationNeedsRationale) {
                showLocationPermissionDialog()
            }
            return@registerForActivityResult
        }
        val accessFineLocationGranted = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val isLocationToggleEnabled = isLocationToggleEnabled()
        println("isLocationToggleEnabled: $isLocationToggleEnabled")
        if (accessFineLocationGranted && isLocationToggleEnabled) {
            viewModel.toggleLocationUpdates()
        } else {
            showLocationToggleDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showScreen(
            fragment = MainFragment.newInstance(""),
            tag = FragmentsTag.MAIN,
            isAdd = true,
            isAddToBackStack = false
        )
        setLocationToggleListener()
    }

    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, ForegroundLocationService::class.java)
        bindService(serviceIntent, viewModel, BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver()
        grantLocationPermissions()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver()
    }

    override fun onStop() {
        super.onStop()
        unbindService(viewModel)
    }

    // https://developer.android.com/reference/android/location/LocationManager#registerGnssStatusCallback(android.location.GnssStatus.Callback,%20android.os.Handler)
    private fun setLocationToggleListener() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            locationManager.registerGnssStatusCallback(object : GnssStatus.Callback() {
                override fun onStopped() {
                    super.onStopped()
                    showLocationToggleDialog()
                    println("location toggle: stopped")
                }

                override fun onStarted() {
                    super.onStarted()
                    println("location toggle: started")
                }

                override fun onSatelliteStatusChanged(status: GnssStatus) {
                    super.onSatelliteStatusChanged(status)
                    println("location toggle: ${status.satelliteCount}")
                }
            }, null)
        }
    }

    private fun grantLocationPermissions() {
        locationPermissionResult.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun registerReceiver() {
        registerReceiver(locationToggleStatusReceiver, IntentFilter(BroadcastKey.LOCATION_TOGGLE_STATUS))
    }

    private fun unregisterReceiver() {
        unregisterReceiver(locationToggleStatusReceiver)
    }

    private fun showLocationToggleDialog() {
        MaterialAlertDialogBuilder(
            this@MainActivity,
            com.google.android.material.R.style.ThemeOverlay_MaterialComponents_Dialog
        ).apply {
            setCancelable(false)
            setTitle("Location toggle disabled")
            setMessage("Turn on the location toggle to hunt treasures. Swipe down notifications drawer -> Location")
            background = ContextCompat.getDrawable(this@MainActivity, R.drawable.alert_dialog_bg)
            setPositiveButton("Ok") { dialog, int ->
                grantLocationPermissions()
            }
            create()
            show()
        }
    }

    private fun showLocationPermissionDialog() {
        MaterialAlertDialogBuilder(
            this@MainActivity,
            com.google.android.material.R.style.ThemeOverlay_MaterialComponents_Dialog
        ).apply {
            setCancelable(false)
            setTitle(R.string.permission_rationale_dialog_title)
            setMessage(R.string.permission_rationale_dialog_message)
            background = ContextCompat.getDrawable(this@MainActivity, R.drawable.alert_dialog_bg)
            setPositiveButton("Ok") { dialog, int ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
            }
            setNegativeButton("Cancel") { dialog, int ->
            }
            create()
            show()
        }
    }
}