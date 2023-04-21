package com.singularitycoder.connectme.search

import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.connectme.databinding.FragmentAddApiKeyBottomSheetBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.BottomSheetTag
import com.singularitycoder.connectme.helpers.constants.Preferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddApiKeyBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = AddApiKeyBottomSheetFragment()
    }

    @Inject
    lateinit var preferences: SharedPreferences

    private lateinit var binding: FragmentAddApiKeyBottomSheetBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddApiKeyBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTransparentBackground()
        binding.setupUserActionListeners()
    }

    private fun FragmentAddApiKeyBottomSheetBinding.setupUserActionListeners() {
        etApiKey.editText?.doAfterTextChanged { it: Editable? ->
            if (etApiKey.editText?.text.isNullOrBlank()) {
                etApiKey.error = "This is required!"
            } else {
                etApiKey.error = null
            }
        }

        etApiKey.editText?.setOnFocusChangeListener { view, isFocused ->
            etApiKey.boxStrokeWidth = if (etApiKey.editText?.text.isNullOrBlank().not()) 2.dpToPx().toInt() else 0
        }

        btnDone.onSafeClick {
            if (etApiKey.editText?.text.isNullOrBlank()) {
                etApiKey.boxStrokeWidth = 2.dpToPx().toInt()
                etApiKey.error = "This is required!"
                return@onSafeClick
            }
            if ((etApiKey.editText?.text?.length ?: 0) < 10 || (etApiKey.editText?.text?.length ?: 0) > 100) {
                etApiKey.boxStrokeWidth = 2.dpToPx().toInt()
                etApiKey.error = "Invalid API Key"
                return@onSafeClick
            }
            addApiKey(apiKey = etApiKey.editText?.text.toString().trim())
            dismiss()
        }

        ibPasteApiKey.onSafeClick {
            if (context.clipboard()?.text.isNullOrBlank().not()) {
                etApiKey.boxStrokeWidth = 2.dpToPx().toInt()
                binding.etApiKey.editText?.setText(context.clipboard()?.text?.trim())
            }
        }
    }

    private fun addApiKey(apiKey: String) {
        preferences.edit().putString(Preferences.KEY_OPEN_AI_API_SECRET, AesEncryption.encrypt(apiKey)).apply()
        GetInsightsBottomSheetFragment.newInstance().show(requireActivity().supportFragmentManager, BottomSheetTag.TAG_GET_INSIGHTS)
    }
}