package com.singularitycoder.connectme.downloads

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.view.*
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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

    private val downloadsViewModel by viewModels<DownloadsViewModel>()

    private val downloadsAdapter = DownloadsAdapter()
    private val downloadsList = mutableListOf<Download>()
    private val fileNavigationStack = Stack<File?>()

    private var topicParam: String? = null
    private var isSelfProfile: Boolean = false
    private var filesList = mutableListOf<File>()

    private val fileFilterOptionsList = listOf(
        Pair("All Files", R.drawable.outline_all_inclusive_24),
        Pair("Photos", R.drawable.outline_image_24),
        Pair("Videos", R.drawable.outline_videocam_24),
        Pair("Audio", R.drawable.outline_audiotrack_24),
        Pair("Documents", R.drawable.outline_article_24),
        Pair("Archives", R.drawable.outline_folder_zip_24),
        Pair("APKs", R.drawable.outline_android_24),
        Pair("Folders", R.drawable.outline_folder_24),
        Pair("Other Files", R.drawable.outline_insert_drive_file_24),
    )
    private var selectedFileFilter: String = fileFilterOptionsList.first().first

    private val sortByOptionsList = listOf(
        Pair("Most recent", R.drawable.round_check_24),
        Pair("File name (A to Z)", R.drawable.round_check_24),
        Pair("File name (Z to A)", R.drawable.round_check_24),
        Pair("Modified (newest first)", R.drawable.round_check_24),
        Pair("Modified (oldest first)", R.drawable.round_check_24),
        Pair("Type (A to Z)", R.drawable.round_check_24),
        Pair("Type (Z to A)", R.drawable.round_check_24),
        Pair("Size (largest first)", R.drawable.round_check_24),
        Pair("Size (smallest first)", R.drawable.round_check_24),
    )
    private var selectedFileSorting: String = sortByOptionsList.first().first

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
        if (filesList.isNotEmpty()) return
        loadRootFolderFiles()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentDownloadsBinding.setupUI() {
        layoutSearch.btnMore.icon = requireContext().drawable(R.drawable.ic_round_more_horiz_24)
        layoutSearch.cardSearch.isVisible = isSelfProfile
        layoutSearch.btnMore.isVisible = isSelfProfile
        rvDownloads.apply {
            layoutManager = GridLayoutManager(/* context = */ context, /* spanCount = */ 2)
            adapter = downloadsAdapter
        }
        ivShield.setMargins(top = (deviceHeight() / 2) - 200.dpToPx().toInt())
        layoutSearch.etSearch.hint = "Search in ${getDownloadDirectory().name}"
        fileNavigationStack.push(getDownloadDirectory())
    }

    @SuppressLint("NotifyDataSetChanged")
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
                        setupFilterFilesPopupMenu(view = pair.first)
                    }
                    optionsList[1].first -> {
                        setupSortFilesPopupMenu(view = pair.first)
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

        downloadsAdapter.setOnItemClickListener { download: Download?, position: Int ->
            val selectedItem = filesList.getOrNull(position) ?: return@setOnItemClickListener
            if (selectedItem.isDirectory) {
                selectedFileFilter = fileFilterOptionsList.first().first
                selectedFileSorting = sortByOptionsList.first().first
                fileNavigationStack.push(selectedItem)
                updateFileNavigation()
                filesList = getFilesListFrom(selectedItem).toMutableList()
            }
            openIfFileElseShowFilesListIfDirectory(selectedItem)
        }

        downloadsAdapter.setOnItemLongClickListener { download: Download?, view: View?, position: Int? ->
            val optionsList = listOf(
                Pair("Open source", R.drawable.ic_round_link_24),
                Pair("Open with...", R.drawable.outline_open_in_new_24),
                Pair("Organize", R.drawable.outline_drive_file_move_24),
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
                        setupOpenSourcePopupMenu(view)
                    }
                    optionsList[1].first -> {
                    }
                    optionsList[2].first -> {
                        setupOrganizePopupMenu(view)
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
                        val file = File("${download?.path}")
                        if (file.exists()) {
                            file.delete()
                            filesList.removeAt(position ?: 0)
                            downloadsList.removeAt(position ?: 0)
                            downloadsViewModel.deleteItem(download)
                            downloadsAdapter.notifyItemRemoved(position ?: 0)
                        }
                    }
                }
            }
        }

        layoutSearch.etSearch.onImeClick {
            layoutSearch.etSearch.hideKeyboard()
        }

        layoutSearch.ibClearSearch.onSafeClick {
            layoutSearch.etSearch.setText("")
        }

        layoutSearch.etSearch.doAfterTextChanged { query: Editable? ->
            layoutSearch.ibClearSearch.isVisible = query.isNullOrBlank().not()
            if (query.isNullOrBlank()) {
                val folderToNavigate = fileNavigationStack.peek() ?: getDownloadDirectory()
                openIfFileElseShowFilesListIfDirectory(folderToNavigate)
                return@doAfterTextChanged
            }
            downloadsAdapter.downloadsList = downloadsList.filter { it.title.contains(other = query, ignoreCase = true) }
            downloadsAdapter.notifyDataSetChanged()
        }

        btnGivePermission.onSafeClick {
            requireActivity().requestStoragePermission()
        }

        layoutSearch.ivNavigateBack.onSafeClick {
            selectedFileFilter = fileFilterOptionsList.first().first
            selectedFileSorting = sortByOptionsList.first().first
            fileNavigationStack.pop()
            val previousFolder = fileNavigationStack.peek() ?: getDownloadDirectory()
            updateFileNavigation()
            filesList = getFilesListFrom(previousFolder).toMutableList()
            openIfFileElseShowFilesListIfDirectory(previousFolder)
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
            File("${fileNavigationStack.peek()?.absolutePath}/$newFolderNameText").also {
                if (it.exists().not()) it.mkdirs()
            }
        }
    }

    private fun observeForData() {
    }

    private fun updateFileNavigation() {
        binding.layoutSearch.ivNavigateBack.isVisible = fileNavigationStack.size > 1
        binding.layoutSearch.etSearch.hint = "Search in ${fileNavigationStack.peek()?.name}"
    }

    private fun loadRootFolderFiles() {
        //        if (
        //            selectedFileFilter != fileFilterOptionsList.first().first ||
        //            selectedFileSorting != sortByOptionsList.first().first
        //        ) return

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

            //            filesList = if (getFilesListFrom(getDownloadDirectory()).size > 1) {
            //                getFilesListFrom(getDownloadDirectory()).subList(fromIndex = 1, toIndex = getFilesListFrom(getDownloadDirectory()).lastIndex).toMutableList()
            //            } else mutableListOf()
            filesList = getFilesListFrom(getDownloadDirectory()).toMutableList()
            openIfFileElseShowFilesListIfDirectory(getDownloadDirectory())
        } else {
            binding.llStoragePermissionRationaleView.isVisible = true
            binding.rvDownloads.isVisible = false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun openIfFileElseShowFilesListIfDirectory(currentDirectory: File) {
        println("Parent file path: ${currentDirectory.parentFile.path}") // /storage/emulated

        if (currentDirectory.isFile) {
            requireActivity().openFile(currentDirectory)
            return
        }

        downloadsList.clear() // TODO Give a refresh option instead of loading all the time
        filesList.forEach { file: File ->
            val downloadItem = Download(
                path = file.absolutePath,
                title = file.nameWithoutExtension,
                time = if (file.isDirectory) {
                    "${getFilesListFrom(currentDirectory).size} items"
                } else "${file.extension.toUpCase()}  •  ${file.getAppropriateSize()}",
                link = "",
                extension = file.extension,
                isDirectory = file.isDirectory
            )
//            if (file.absolutePath == currentDirectory.absolutePath) return@forEach
            downloadsList.add(downloadItem)
        }
        downloadsAdapter.downloadsList = downloadsList
        downloadsAdapter.notifyDataSetChanged()
    }

    private fun setupOrganizePopupMenu(view: View?) {
        val optionsList = listOf(
            Pair("Copy to...", R.drawable.baseline_content_copy_24),
            Pair("Move to...", R.drawable.outline_control_camera_24),
            Pair("Zip", R.drawable.outline_folder_zip_24),
            Pair("Rename", R.drawable.outline_drive_file_rename_outline_24),
        )
        requireContext().showPopupMenuWithIcons(
            view = view,
            menuList = optionsList,
            title = "Organize"
        ) { it: MenuItem? ->
            when (it?.title?.toString()?.trim()) {
                optionsList[0].first -> {
                }
                optionsList[1].first -> {
                }
                optionsList[2].first -> {
                }
            }
        }
    }

    private fun setupOpenSourcePopupMenu(view: View?) {
        val optionsList = listOf(
            Pair("Peek", R.drawable.outline_remove_red_eye_24),
            Pair("New tab", R.drawable.round_add_circle_outline_24),
            Pair("New private tab", R.drawable.outline_policy_24),
        )
        requireContext().showPopupMenuWithIcons(
            view = view,
            menuList = optionsList,
            title = "Open source in"
        ) { it: MenuItem? ->
            when (it?.title?.toString()?.trim()) {
                optionsList[0].first -> {
                }
                optionsList[1].first -> {
                }
                optionsList[2].first -> {
                }
            }
        }
    }

    private fun setupFilterFilesPopupMenu(view: View?) {
        requireContext().showSingleSelectionPopupMenu(
            view = view,
            title = "Filter",
            disabledColor = R.color.light_gray_2,
            selectedOption = selectedFileFilter,
            menuList = fileFilterOptionsList,
        ) { menuItem: MenuItem? ->
            selectedFileSorting = sortByOptionsList.first().first
            selectedFileFilter = menuItem?.title?.toString()?.trim() ?: ""
            applyFileFilters()
        }
    }

    private fun applyFileFilters() {
        /** The first dir is the downloads dir itself. So avoiding that. */
//        val allFilesList = if (getFilesListFrom(getDownloadDirectory()).size > 1) {
//            getFilesListFrom(getDownloadDirectory()).subList(fromIndex = 1, toIndex = getFilesListFrom(getDownloadDirectory()).lastIndex).toMutableList()
//        } else mutableListOf()
        val allFilesList = getFilesListFrom(currentDirectory = fileNavigationStack.peek() ?: return).toMutableList()
        when (selectedFileFilter) {
            fileFilterOptionsList[0].first -> {
                filesList = allFilesList
            }
            fileFilterOptionsList[1].first -> {
                filesList = allFilesList.filter { file: File -> ImageFormat.values().map { it.value }.contains(file.extension) }.toMutableList()
            }
            fileFilterOptionsList[2].first -> {
                filesList = allFilesList.filter { file: File -> VideoFormat.values().map { it.value }.contains(file.extension) }.toMutableList()
            }
            fileFilterOptionsList[3].first -> {
                filesList = allFilesList.filter { file: File -> AudioFormat.values().map { it.value }.contains(file.extension) }.toMutableList()
            }
            fileFilterOptionsList[4].first -> {
                filesList = allFilesList.filter { file: File -> DocumentFormat.values().map { it.value }.contains(file.extension) }.toMutableList()
            }
            fileFilterOptionsList[5].first -> {
                filesList = allFilesList.filter { file: File -> ArchiveFormat.values().map { it.value }.contains(file.extension) }.toMutableList()
            }
            fileFilterOptionsList[6].first -> {
                filesList = allFilesList.filter { file: File -> AndroidFormat.values().map { it.value }.contains(file.extension) }.toMutableList()
            }
            fileFilterOptionsList[7].first -> {
                filesList = allFilesList.filter { file: File -> file.isDirectory }.toMutableList()
            }
            fileFilterOptionsList[8].first -> {
                filesList = allFilesList.filter { file: File ->
                    ImageFormat.values().map { it.value }.contains(file.extension).not() &&
                            VideoFormat.values().map { it.value }.contains(file.extension).not() &&
                            AudioFormat.values().map { it.value }.contains(file.extension).not() &&
                            DocumentFormat.values().map { it.value }.contains(file.extension).not() &&
                            ArchiveFormat.values().map { it.value }.contains(file.extension).not() &&
                            AndroidFormat.values().map { it.value }.contains(file.extension).not() &&
                            file.isDirectory.not()
                }.toMutableList()
            }
        }
        openIfFileElseShowFilesListIfDirectory(currentDirectory = fileNavigationStack.peek() ?: return)
    }

    private fun setupSortFilesPopupMenu(view: View?) {
        requireContext().showSingleSelectionPopupMenu(
            view = view,
            title = "Sort by",
            selectedOption = selectedFileSorting,
            menuList = sortByOptionsList,
        ) { menuItem: MenuItem? ->
            selectedFileSorting = menuItem?.title?.toString()?.trim() ?: ""
            applyFileSorting()
        }
    }

    private fun applyFileSorting() {
        when (selectedFileSorting) {
            sortByOptionsList[0].first -> {
                filesList = filesList.sortedBy { it.lastModified() }.toMutableList()
            }
            sortByOptionsList[1].first -> {
                filesList = filesList.sortedBy { it.name.toLowCase() }.toMutableList()
            }
            sortByOptionsList[2].first -> {
                filesList = filesList.sortedByDescending { it.name.toLowCase() }.toMutableList()
            }
            sortByOptionsList[3].first -> {
                filesList = filesList.sortedBy { it.lastModified() }.toMutableList()
            }
            sortByOptionsList[4].first -> {
                filesList = filesList.sortedByDescending { it.lastModified() }.toMutableList()
            }
            sortByOptionsList[5].first -> {
                filesList = filesList.sortedBy { it.extension.toLowCase() }.toMutableList()
            }
            sortByOptionsList[6].first -> {
                filesList = filesList.sortedByDescending { it.extension.toLowCase() }.toMutableList()
            }
            sortByOptionsList[7].first -> {
                filesList = filesList.sortedByDescending { it.sizeInBytes() }.toMutableList()
            }
            sortByOptionsList[8].first -> {
                filesList = filesList.sortedBy { it.sizeInBytes() }.toMutableList()
            }
        }
        openIfFileElseShowFilesListIfDirectory(currentDirectory = fileNavigationStack.peek() ?: return)
    }
}