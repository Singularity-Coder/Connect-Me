package com.singularitycoder.connectme.explore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemExploreBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.exploreItemColorsList
import com.singularitycoder.connectme.helpers.constants.typefaceList

class ExploreAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var exploreList = emptyList<Explore>()
    private var colorPosition: Int = -1
    private var isListReversible: Boolean = false
    private var typefacePosition = 0
    private var itemClickListener: (explore: Explore?) -> Unit = {}
    private var itemLongClickListener: (explore: Explore?, view: View?) -> Unit = { _, _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemExploreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as NewsViewHolder).setData(exploreList[position])
    }

    override fun getItemCount(): Int = exploreList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnItemClickListener(listener: (explore: Explore?) -> Unit) {
        itemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: (explore: Explore?, view: View?) -> Unit) {
        itemLongClickListener = listener
    }

    fun setTypefacePosition(typefacePosition: Int) {
        this.typefacePosition = typefacePosition
    }

    inner class NewsViewHolder(
        private val itemBinding: ListItemExploreBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(explore: Explore) {
            itemBinding.apply {
                val source = if (explore.source.isNullOrBlank()) {
                    explore.link?.substringAfter("//")?.substringBefore("/")?.replace("www.", "")
                } else {
                    explore.source
                }
                if (colorPosition == exploreItemColorsList.lastIndex) {
                    isListReversible = true
                }
                if (colorPosition <= 0) {
                    isListReversible = false
                }
                if (isListReversible) {
                    colorPosition--
                } else {
                    colorPosition++
                }
                val color = exploreItemColorsList.getOrNull(colorPosition)
                tvSource.apply {
                    text = "$source  \u2022  ${explore.time}"
                    setTextColor(root.context.color(color?.textColor ?: R.color.purple_500))
                }
                tvTitle.apply {
                    text = explore.title
                    setTextColor(root.context.color(color?.textColor ?: R.color.purple_500))
                    setTypeface(root.context, typefaceList.getOrNull(typefacePosition) ?: R.font.milkshake)
                }
                clExplore.background = root.context.drawable(color?.gradientColor ?: R.drawable.gradient_default_light)
                root.onSafeClick {
                    itemClickListener.invoke(explore)
                }
                viewDummyCenter.setOnClickListener {
                    itemLongClickListener.invoke(explore, it)
                }
                root.onCustomLongClick {
                    viewDummyCenter.performClick()
                }
            }
        }
    }
}
