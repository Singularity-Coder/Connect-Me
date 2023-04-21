package com.singularitycoder.connectme

import android.Manifest
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.singularitycoder.connectme.databinding.ActivityMainBinding
import com.singularitycoder.connectme.helpers.constants.FragmentsTag
import com.singularitycoder.connectme.helpers.hasPermission
import com.singularitycoder.connectme.helpers.isLocationToggleEnabled
import com.singularitycoder.connectme.helpers.shouldShowRationaleFor
import com.singularitycoder.connectme.helpers.showScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
            }
            return@registerForActivityResult
        }
        val accessFineLocationGranted = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val isLocationToggleEnabled = isLocationToggleEnabled()
        println("isLocationToggleEnabled: $isLocationToggleEnabled")
        if (accessFineLocationGranted && isLocationToggleEnabled) {

        } else {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showScreen(
            fragment = MainFragment.newInstance(""),
            tag = FragmentsTag.MAIN,
            isAdd = true,
            isAddToBackStack = false
        )
    }

    private fun grantLocationPermissions() {
        locationPermissionResult.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}