package com.singularitycoder.connectme.search.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.connectme.databinding.ListItem3TextsRoundBinding
import com.singularitycoder.connectme.helpers.deviceWidth
import com.singularitycoder.connectme.helpers.dpToPx
import com.singularitycoder.connectme.helpers.onSafeClick
import com.singularitycoder.connectme.helpers.setMargins

class PromptsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var promptList = mutableListOf<
            Triple<
                    Pair<String, String>,
                    Pair<String, String>,
                    Pair<String, String>
                    >
            >()
    private var itemClickListener: (promptPair: Pair<String, String>) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItem3TextsRoundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThisViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ThisViewHolder).setData(promptList[position])
    }

    override fun getItemCount(): Int = promptList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnItemClickListener(listener: (promptPair: Pair<String, String>) -> Unit) {
        itemClickListener = listener
    }

    inner class ThisViewHolder(
        private val itemBinding: ListItem3TextsRoundBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(
            promptPair: Triple<
                    Pair<String, String>,
                    Pair<String, String>,
                    Pair<String, String>
                    >
        ) {
            itemBinding.apply {
                root.layoutParams.width = deviceWidth() - (deviceWidth() / 3)
                when (bindingAdapterPosition) {
                    0 -> {
                        root.setPadding(16.dpToPx().toInt(), 0, 0, 0)
                    }
                    promptList.lastIndex -> {
                        root.setPadding(8.dpToPx().toInt(), 0, 16.dpToPx().toInt(), 0)
                    }
                    else -> {
                        root.setPadding(8.dpToPx().toInt(), 0, 0, 0)
                    }
                }

                cardText1.isVisible = promptPair.first.first.isBlank().not()
                tvText1.text = promptPair.first.first
                cardText1.onSafeClick {
                    itemClickListener.invoke(Pair(first = promptPair.first.first, second = promptPair.first.second))
                }

                cardText2.isVisible = promptPair.second.first.isBlank().not()
                tvText2.text = promptPair.second.first
                cardText2.onSafeClick {
                    itemClickListener.invoke(Pair(first = promptPair.second.first, second = promptPair.second.second))
                }

                cardText3.isVisible = promptPair.third.first.isBlank().not()
                tvText3.text = promptPair.third.first
                cardText3.onSafeClick {
                    itemClickListener.invoke(Pair(first = promptPair.third.first, second = promptPair.third.second))
                }
            }
        }
    }
}