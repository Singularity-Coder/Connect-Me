package com.singularitycoder.connectme.explore.addTimings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentAddBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.flowlauncher.helper.*
import kotlinx.coroutines.launch
import java.util.*

private const val KEY_ADD_LIST_TYPE = "KEY_ADD_LIST_TYPE"

// https://proandroiddev.com/enter-animation-using-recyclerview-and-layoutanimation-part-1-list-75a874a5d213
class AddFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(listType: String) = AddFragment().apply {
            arguments = Bundle().apply {
                putString(KEY_ADD_LIST_TYPE, listType)
            }
        }
    }

    private lateinit var binding: FragmentAddBinding

    private val addItemAdapter = AddItemAdapter()
    private var addItemList = mutableListOf<AddItem>()
    private var listType: String? = null

//    private val sharedViewModel by activityViewModels<SharedViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            listType = it.getString(KEY_ADD_LIST_TYPE, "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.observeForData()
        binding.setupUserActionListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().setNavigationBarColor(R.color.white)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentAddBinding.observeForData() {

    }

    private fun FragmentAddBinding.setupUI() {
        requireActivity().setNavigationBarColor(R.color.black)
        rvRoutineSteps.apply {
            layoutAnimation = rvRoutineSteps.context.layoutAnimationController(R.anim.layout_animation_fall_down)
            layoutManager = LinearLayoutManager(context)
            adapter = addItemAdapter
            addItemAdapter.setListType(listType)
        }
    }

    // https://stackoverflow.com/questions/3467205/android-key-dispatching-timed-out
    private fun FragmentAddBinding.setupUserActionListeners() {

        ibAddItem.onSafeClick {
            if (etAddItem.text.isNullOrBlank()) return@onSafeClick
        }

        ibBack.onSafeClick {
            requireActivity().supportFragmentManager.popBackStackImmediate()
        }

        addItemAdapter.setItemClickListener { item: AddItem ->
            root.context.clipboard()?.text = item.link
            root.showSnackBar(message = "Copied", anchorView = cardAddItemParent)
        }

        addItemAdapter.setItemLongClickListener { it: AddItem ->
            requireContext().showAlertDialog(
                title = "Delete Item",
                message = "This action cannot be undone. Are you sure?",
                positiveBtnText = "Delete",
                negativeBtnText = "Cancel",
                positiveAction = {
                    lifecycleScope.launch {
                    }
                }
            )
        }

        etAddItem.onImeClick {
            ibAddItem.performClick()
        }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            /* Drag Directions */ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
            /* Swipe Directions */0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                // FIXME drag is not smooth and gets attached to its immediate next position
//                val fromPosition = viewHolder.bindingAdapterPosition
//                val toPosition = target.bindingAdapterPosition
//                val fromPositionItem = addItemList[fromPosition]
//                addItemList[fromPosition] = addItemList[toPosition]
//                addItemList[toPosition] = fromPositionItem
//                addItemAdapter.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvRoutineSteps)
    }
}
