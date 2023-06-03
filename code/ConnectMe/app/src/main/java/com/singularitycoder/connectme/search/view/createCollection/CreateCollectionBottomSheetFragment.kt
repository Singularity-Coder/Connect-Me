package com.singularitycoder.connectme.search.view.createCollection

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.connectme.databinding.FragmentCreateCollectionBottomSheetBinding
import com.singularitycoder.connectme.helpers.dpToPx
import com.singularitycoder.connectme.helpers.onSafeClick
import com.singularitycoder.connectme.helpers.setTransparentBackground
import com.singularitycoder.connectme.helpers.showKeyboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateCollectionBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = CreateCollectionBottomSheetFragment()
    }

    private lateinit var binding: FragmentCreateCollectionBottomSheetBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreateCollectionBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        binding.setupUserActionListeners()
    }

    private fun setupUI() {
        /** https://stackoverflow.com/questions/48002290/show-entire-bottom-sheet-with-edittext-above-keyboard
         * This is for adjusting the input field properly when keyboard visible */
        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        setTransparentBackground()

        binding.etEnterCollectionName.editText?.showKeyboard()
    }

    private fun FragmentCreateCollectionBottomSheetBinding.setupUserActionListeners() {
        etEnterCollectionName.editText?.doAfterTextChanged { it: Editable? ->
            if (etEnterCollectionName.editText?.text.isNullOrBlank()) {
                etEnterCollectionName.error = "This is required!"
            } else {
                etEnterCollectionName.error = null
            }
        }

        etEnterCollectionName.editText?.setOnFocusChangeListener { view, isFocused ->
            etEnterCollectionName.boxStrokeWidth = if (etEnterCollectionName.editText?.text.isNullOrBlank().not()) 2.dpToPx().toInt() else 0
        }

        btnCreate.onSafeClick {
            if (etEnterCollectionName.editText?.text.isNullOrBlank()) {
                etEnterCollectionName.boxStrokeWidth = 2.dpToPx().toInt()
                etEnterCollectionName.error = "This is required!"
                return@onSafeClick
            }
            if ((etEnterCollectionName.editText?.text?.length ?: 0) > 50) {
                etEnterCollectionName.boxStrokeWidth = 2.dpToPx().toInt()
                etEnterCollectionName.error = "Max 50 characters"
                return@onSafeClick
            }
            // TODO send this to search frag or create new coll here
            etEnterCollectionName.editText?.text.toString().trim()
            dismiss()
        }
    }
}