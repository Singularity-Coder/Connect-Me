package com.singularitycoder.connectme.downloads

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentDownloadsBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.*
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.*
import javax.inject.Inject

private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"
private const val ARG_PARAM_IS_SELF_PROFILE = "ARG_PARAM_IS_SELF_PROFILE"

@AndroidEntryPoint
class DownloadsFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String, isSelfProfile: Boolean) = DownloadsFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM_SCREEN_TYPE, screenType)
                putBoolean(ARG_PARAM_IS_SELF_PROFILE, isSelfProfile)
            }
        }
    }

    @Inject
    lateinit var preferences: SharedPreferences

    private lateinit var binding: FragmentDownloadsBinding

    private val feedAdapter = DownloadsAdapter()
    private val feedList = mutableListOf<Download>()
    private val sortByOptionsList = listOf(
        "Most recent",
        "File name (A to Z)",
        "File name (Z to A)",
        "Modified (newest first)",
        "Modified (oldest first)",
        "Type (A to Z)",
        "Type (Z to A)",
        "Size (largest first)",
        "Size (smallest first)",
    )

    private var topicParam: String? = null
    private var isSelfProfile: Boolean = false

    private lateinit var filesList: List<File>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topicParam = arguments?.getString(ARG_PARAM_SCREEN_TYPE)
        isSelfProfile = arguments?.getBoolean(ARG_PARAM_IS_SELF_PROFILE) ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDownloadsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    override fun onResume() {
        super.onResume()
        val hasPermission = requireActivity().checkStoragePermission()
        if (hasPermission) {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                if (Environment.isExternalStorageLegacy().not()) {
                    binding.llStoragePermissionRationaleView.isVisible = true
                    return
                }
            }

            binding.llStoragePermissionRationaleView.isVisible = false
            binding.rvDownloads.isVisible = true

            // TODO: Use getStorageDirectory instead https://developer.android.com/reference/android/os/Environment.html#getStorageDirectory()

            val allDownloadFilesList = getFilesListFrom(getDownloadDirectory())
            filesList = allDownloadFilesList.subList(1, allDownloadFilesList.lastIndex)
            openIfFileElseShowFilesListIfDirectory(getDownloadDirectory())
        } else {
            binding.llStoragePermissionRationaleView.isVisible = true
            binding.rvDownloads.isVisible = false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentDownloadsBinding.setupUI() {
        layoutSearch.btnMore.icon = requireContext().drawable(R.drawable.ic_round_more_horiz_24)
        layoutSearch.cardSearch.isVisible = isSelfProfile
        layoutSearch.btnMore.isVisible = isSelfProfile
        rvDownloads.apply {
            layoutManager = GridLayoutManager(/* context = */ context, /* spanCount = */ 2)
            adapter = feedAdapter
        }
        preferences.edit().putString(Preferences.KEY_DOWNLOAD_SORT_BY, sortByOptionsList[0]).apply()
        ivShield.setMargins(top = (deviceHeight() / 2) - 200.dpToPx().toInt())
    }

    private fun FragmentDownloadsBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        layoutSearch.btnMore.onSafeClick { pair: Pair<View?, Boolean> ->
            val optionsList = listOf(
                Pair("Filter", R.drawable.round_filter_list_24),
                Pair("Sort by", R.drawable.round_sort_24),
                Pair("Add New Folder", R.drawable.outline_create_new_folder_24)
            )
            requireContext().showPopupMenuWithIcons(
                view = pair.first,
                menuList = optionsList
            ) { it: MenuItem? ->
                when (it?.title?.toString()?.trim()) {
                    optionsList[0].first -> {
                        // Filter by image, video, audio, file - dynamic filter based on the file types provided
                    }
                    optionsList[1].first -> {
                        setupSortOptionsMenu(view = pair.first)
                    }
                    optionsList[2].first -> {
                        // TODO another bottom sheet for adding single named items
                        EditBottomSheetFragment.newInstance(
                            eventType = EditEvent.CREATE_NEW_DOWNLOAD_FOLDER
                        ).show(parentFragmentManager, BottomSheetTag.TAG_EDIT)
                    }
                }
            }
        }

        feedAdapter.setOnItemClickListener { download: Download?, position: Int ->
            val selectedItem = filesList.getOrNull(position) ?: return@setOnItemClickListener
            openIfFileElseShowFilesListIfDirectory(selectedItem)
        }

        feedAdapter.setOnItemLongClickListener { download: Download?, view: View? ->
            val optionsList = listOf(
                Pair("Open source", R.drawable.round_add_circle_outline_24),
                Pair("Open with...", R.drawable.outline_open_in_new_24),
                Pair("Copy to...", R.drawable.baseline_content_copy_24),
                Pair("Move to...", R.drawable.outline_control_camera_24),
                Pair("Zip", R.drawable.outline_folder_zip_24),
                Pair("Rename", R.drawable.outline_drive_file_rename_outline_24),
                Pair("Get info", R.drawable.outline_info_24),
                Pair("Share", R.drawable.outline_share_24),
                Pair("Copy link", R.drawable.baseline_content_copy_24),
                Pair("Delete", R.drawable.outline_delete_24),
            )
            requireContext().showPopupMenuWithIcons(
                view = view,
                menuList = optionsList,
                customColor = R.color.md_red_700,
                customColorItem = optionsList.last().first
            ) { it: MenuItem? ->
                when (it?.title?.toString()?.trim()) {
                    optionsList[0].first -> {
                    }
                    optionsList[1].first -> {
                    }
                    optionsList[2].first -> {
                    }
                    optionsList[3].first -> {
                    }
                    optionsList[4].first -> {
                    }
                    optionsList[5].first -> {
                        EditBottomSheetFragment.newInstance(
                            eventType = EditEvent.RENAME_DOWNLOAD_FILE
                        ).show(parentFragmentManager, BottomSheetTag.TAG_EDIT)
                    }
                    optionsList[6].first -> {
                    }
                    optionsList[7].first -> {
                    }
                    optionsList[8].first -> {
                    }
                }
            }
        }

        layoutSearch.etSearch.onImeClick {
            layoutSearch.etSearch.hideKeyboard()
        }

        btnGivePermission.onSafeClick {
            requireActivity().requestStoragePermission()
        }

        parentFragmentManager.setFragmentResultListener(
            /* requestKey = */ FragmentResultKey.RENAME_DOWNLOAD_FILE,
            /* lifecycleOwner = */ viewLifecycleOwner
        ) { _, bundle: Bundle ->
            val renamedFileText = bundle.getString(FragmentResultBundleKey.RENAME_DOWNLOAD_FILE)
        }

        parentFragmentManager.setFragmentResultListener(
            /* requestKey = */ FragmentResultKey.CREATE_NEW_DOWNLOAD_FOLDER,
            /* lifecycleOwner = */ viewLifecycleOwner
        ) { _, bundle: Bundle ->
            val newFolderNameText = bundle.getString(FragmentResultBundleKey.CREATE_NEW_DOWNLOAD_FOLDER)?.trim()
            File("${getDownloadDirectory().absolutePath}/$newFolderNameText").also {
                if (it.exists().not()) it.mkdirs()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun openIfFileElseShowFilesListIfDirectory(currentDirectory: File) {
        println("Parent file path: ${currentDirectory.parentFile.path}") // /storage/emulated

        if (currentDirectory.isFile) return requireActivity().openFile(currentDirectory)

        feedList.clear() // TODO Give a refresh option instead of loading all the time
        filesList.forEach { file: File ->
            val downloadItem = Download(
                imageUrl = file.absolutePath,
                title = file.name,
                time = if (isSelfProfile) {
                    "${file.getAppropriateSize()}  •  ${file.lastModified().toIntuitiveDateTime()}"
                } else file.getAppropriateSize(),
                link = "",
                isDirectory = file.isDirectory
            )
            feedList.add(downloadItem)
        }
        feedAdapter.feedList = feedList
        feedAdapter.notifyDataSetChanged()
    }

    private fun setupSortOptionsMenu(view: View?) {
        val selectedOpenAiModel = preferences.getString(Preferences.KEY_DOWNLOAD_SORT_BY, "")
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menu.add(Menu.NONE, -1, 0, "Sort by").apply {
            isEnabled = false
        }
        sortByOptionsList.forEach {
            popupMenu.menu.add(
                0, 1, 1, menuIconWithText(
                    icon = requireContext().drawable(R.drawable.round_check_24)?.changeColor(
                        context = requireContext(),
                        color = if (selectedOpenAiModel == it) R.color.purple_500 else android.R.color.transparent
                    ),
                    title = it
                )
            )
        }
        popupMenu.setOnMenuItemClickListener { aiModelMenuItem: MenuItem? ->
            view?.setHapticFeedback()
            when (aiModelMenuItem?.title?.toString()?.trim()) {
                sortByOptionsList[0] -> {
                    preferences.edit().putString(Preferences.KEY_DOWNLOAD_SORT_BY, sortByOptionsList[0]).apply()
                }
                sortByOptionsList[1] -> {
                    preferences.edit().putString(Preferences.KEY_DOWNLOAD_SORT_BY, sortByOptionsList[1]).apply()
                }
                sortByOptionsList[2] -> {
                    preferences.edit().putString(Preferences.KEY_DOWNLOAD_SORT_BY, sortByOptionsList[2]).apply()
                }
                sortByOptionsList[3] -> {
                    preferences.edit().putString(Preferences.KEY_DOWNLOAD_SORT_BY, sortByOptionsList[3]).apply()
                }
                sortByOptionsList[4] -> {
                    preferences.edit().putString(Preferences.KEY_DOWNLOAD_SORT_BY, sortByOptionsList[4]).apply()
                }
                sortByOptionsList[5] -> {
                    preferences.edit().putString(Preferences.KEY_DOWNLOAD_SORT_BY, sortByOptionsList[5]).apply()
                }
                sortByOptionsList[6] -> {
                    preferences.edit().putString(Preferences.KEY_DOWNLOAD_SORT_BY, sortByOptionsList[6]).apply()
                }
                sortByOptionsList[7] -> {
                    preferences.edit().putString(Preferences.KEY_DOWNLOAD_SORT_BY, sortByOptionsList[7]).apply()
                }
                sortByOptionsList[8] -> {
                    preferences.edit().putString(Preferences.KEY_DOWNLOAD_SORT_BY, sortByOptionsList[8]).apply()
                }
            }
            false
        }
        popupMenu.show()
    }

    private fun observeForData() {

    }
}