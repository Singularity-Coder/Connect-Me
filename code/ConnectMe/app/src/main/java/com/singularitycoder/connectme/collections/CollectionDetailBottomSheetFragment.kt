package com.singularitycoder.connectme.collections

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.connectme.databinding.FragmentCollectionDetailBottomSheetBinding
import com.singularitycoder.connectme.helpers.enableSoftInput
import com.singularitycoder.connectme.helpers.setTransparentBackground
import dagger.hilt.android.AndroidEntryPoint

private const val ARG_PARAM_COLLECTION_WEB_PAGE = "ARG_PARAM_COLLECTION_WEB_PAGE"

@AndroidEntryPoint
class CollectionDetailBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance(collectionWebPage: CollectionWebPage?) = CollectionDetailBottomSheetFragment().apply {
            arguments = Bundle().apply { putParcelable(ARG_PARAM_COLLECTION_WEB_PAGE, collectionWebPage) }
        }
    }

    private lateinit var binding: FragmentCollectionDetailBottomSheetBinding

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
        binding = FragmentCollectionDetailBottomSheetBinding.inflate(inflater, container, false)
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
    }

    private fun FragmentCollectionDetailBottomSheetBinding.setupUserActionListeners() {

    }
}