package com.singularitycoder.connectme.search.view.createCollection

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.connectme.collections.CollectionWebPage
import com.singularitycoder.connectme.collections.CollectionsViewModel
import com.singularitycoder.connectme.databinding.FragmentCreateCollectionBottomSheetBinding
import com.singularitycoder.connectme.helpers.*
import dagger.hilt.android.AndroidEntryPoint

private const val ARG_PARAM_COLLECTION_WEB_PAGE = "ARG_PARAM_COLLECTION_WEB_PAGE"

@AndroidEntryPoint
class CreateCollectionBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance(collectionWebPage: CollectionWebPage?) = CreateCollectionBottomSheetFragment().apply {
            arguments = Bundle().apply { putParcelable(ARG_PARAM_COLLECTION_WEB_PAGE, collectionWebPage) }
        }
    }

    private lateinit var binding: FragmentCreateCollectionBottomSheetBinding

    private var collectionWebPage: CollectionWebPage? = null

    private val collectionsViewModel by viewModels<CollectionsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectionWebPage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_PARAM_COLLECTION_WEB_PAGE, CollectionWebPage::class.java)
        } else {
            arguments?.getParcelable(ARG_PARAM_COLLECTION_WEB_PAGE)
        }
    }

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
        enableSoftInput()

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
            val collectionTitle = etEnterCollectionName.editText?.text.toString().trim()
            collectionsViewModel.addToCollections(
                collectionWebPage?.copy(collectionTitle = collectionTitle)
            )
            etEnterCollectionName.editText?.hideKeyboard()
            requireContext().showToast("Added to $collectionTitle")
            dismiss()
        }
    }
}