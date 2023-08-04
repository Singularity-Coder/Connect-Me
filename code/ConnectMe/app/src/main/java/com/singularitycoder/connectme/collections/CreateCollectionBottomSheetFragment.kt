package com.singularitycoder.connectme.collections

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.connectme.databinding.FragmentCreateCollectionBottomSheetBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.CollectionScreenEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ARG_PARAM_COLLECTION_WEB_PAGE = "ARG_PARAM_COLLECTION_WEB_PAGE"
private const val ARG_PARAM_EVENT_TYPE = "ARG_PARAM_EVENT_TYPE"

@AndroidEntryPoint
class CreateCollectionBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance(
            collectionWebPage: CollectionWebPage? = null,
            eventType: CollectionScreenEvent
        ) = CreateCollectionBottomSheetFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_PARAM_COLLECTION_WEB_PAGE, collectionWebPage)
                putParcelable(ARG_PARAM_EVENT_TYPE, eventType)
            }
        }
    }

    private lateinit var binding: FragmentCreateCollectionBottomSheetBinding

    private var collectionWebPage: CollectionWebPage? = null
    private var eventType: CollectionScreenEvent? = null

    private val collectionsViewModel by viewModels<CollectionsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_PARAM_EVENT_TYPE, CollectionScreenEvent::class.java)
        } else {
            arguments?.getParcelable(ARG_PARAM_EVENT_TYPE)
        }
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
        binding.setupUI()
        binding.setupUserActionListeners()
    }

    private fun FragmentCreateCollectionBottomSheetBinding.setupUI() {
        enableSoftInput()

        setTransparentBackground()

        when (eventType) {
            CollectionScreenEvent.ADD_TO_COLLECTION -> {
                etEnterLink.isVisible = false
                etChooseCollection.isVisible = false
                etEnterCollectionName.isVisible = true
                etEnterCollectionName.editText?.showKeyboard()
                tvHeader.text = "Create collection"
                btnDone.text = "Done"
            }
            CollectionScreenEvent.CREATE_NEW_COLLECTION -> {
                etEnterLink.isVisible = true
                etChooseCollection.isVisible = true
                etEnterCollectionName.isVisible = false
                etEnterLink.editText?.showKeyboard()
                tvHeader.text = "Create collection"
                btnDone.text = "Done"
                lifecycleScope.launch {
                    // TODO getting collections can be a separate table. When adding coll we add to that tabel and all dups will be ignore. Minor opt
                    val collectionTitlesList = collectionsViewModel.getAllUniqueCollectionTitles().toArrayList().apply {
                        add("Create new")
                    }
                    withContext(Dispatchers.Main) {
                        val collectionListAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, collectionTitlesList)
                        (etChooseCollection.editText as? AutoCompleteTextView)?.setAdapter(collectionListAdapter)
                    }
                }
            }
            CollectionScreenEvent.RENAME_COLLECTION -> {
                etEnterLink.isVisible = false
                etChooseCollection.isVisible = false
                etEnterCollectionName.isVisible = true
                etEnterCollectionName.editText?.showKeyboard()
                etEnterCollectionName.editText?.setText(collectionWebPage?.collectionTitle)
                etEnterCollectionName.editText?.setSelection(etEnterCollectionName.editText?.text?.length ?: 0)
                tvHeader.text = "Rename collection"
                btnDone.text = "Done"
            }
            else -> Unit
        }
    }

    private fun FragmentCreateCollectionBottomSheetBinding.setupUserActionListeners() {
        etEnterLink.editText?.doAfterTextChanged { it: Editable? ->
            if (etEnterLink.editText?.text.isNullOrBlank()) {
                etEnterLink.error = "This is required!"
            } else {
                etEnterLink.error = null
            }
        }

        etChooseCollection.editText?.doAfterTextChanged { it: Editable? ->
            etChooseCollection.error = null
            etEnterCollectionName.isVisible = it.toString() == "Create new"
            if (it.toString() == "Create new") {
                etEnterCollectionName.error = null
                etEnterCollectionName.requestFocus()
            }
        }

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

        btnDone.onSafeClick {
            etEnterLink.error = null
            etEnterCollectionName.error = null
            etChooseCollection.error = null
            if (etEnterCollectionName.isVisible) {
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
            }
            val collectionName = if (etEnterCollectionName.isVisible) {
                etEnterCollectionName.editText?.text.toString().trim()
            } else {
                etChooseCollection.editText?.text.toString().trim()
            }
            when (eventType) {
                CollectionScreenEvent.CREATE_NEW_COLLECTION -> {
                    if (etEnterLink.editText?.text.toString().toLowCase().isValidURL().not()) {
                        etEnterLink.boxStrokeWidth = 2.dpToPx().toInt()
                        etEnterLink.error = "Invalid link"
                        return@onSafeClick
                    }
                    if (etEnterLink.editText?.text.isNullOrBlank()) {
                        etEnterLink.boxStrokeWidth = 2.dpToPx().toInt()
                        etEnterLink.error = "This is required!"
                        return@onSafeClick
                    }
                    if (etChooseCollection.editText?.text.isNullOrBlank()) {
                        etChooseCollection.boxStrokeWidth = 2.dpToPx().toInt()
                        etChooseCollection.error = "This is required!"
                        return@onSafeClick
                    }
                    btnDone.isVisible = false
                    progressCircular.isVisible = true
                    requireContext().onWebPageLoaded(url = etEnterLink.editText?.text.toString().trim()) { webView: WebView?, favicon: Bitmap? ->
                        val collectionWebPage = CollectionWebPage(
                            collectionTitle = collectionName,
                            title = webView?.title ?: getHostFrom(url = etEnterLink.editText?.text.toString().trim()),
                            favicon = encodeBitmapToBase64String(bitmap = favicon),
                            time = timeNow,
                            link = webView?.url ?: etEnterLink.editText?.text.toString().trim()
                        )
                        collectionsViewModel.addToCollections(collectionWebPage)
                        requireContext().showToast("Added to ${etEnterCollectionName.editText?.text}")
                        btnDone.isVisible = true
                        progressCircular.isVisible = false
                        dismiss()
                    }
                }
                CollectionScreenEvent.ADD_TO_COLLECTION -> {
                    collectionsViewModel.addToCollections(
                        collectionWebPage?.copy(collectionTitle = collectionName)
                    )
                    requireContext().showToast("Added to ${etEnterCollectionName.editText?.text}")
                    dismiss()
                }
                CollectionScreenEvent.RENAME_COLLECTION -> {
                    tvHeader.text = collectionName
                    collectionsViewModel.renameCollection(
                        newCollectionTitle = collectionName,
                        oldCollectionTitle = collectionWebPage?.collectionTitle
                    )
                    dismiss()
                }
                else -> Unit
            }
        }
    }
}