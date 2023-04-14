package com.singularitycoder.connectme.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemExploreBinding
import com.singularitycoder.connectme.helpers.color
import com.singularitycoder.connectme.helpers.constants.exploreItemColorsList
import com.singularitycoder.connectme.helpers.constants.typefaceList
import com.singularitycoder.connectme.helpers.drawable
import com.singularitycoder.connectme.helpers.setTypeface
import kotlin.random.Random

class ExploreAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var feedList = emptyList<Explore>()
    private var newsClickListener: (explore: Explore) -> Unit = {}
    private var colorPosition: Int = -1
    private var isListReversible: Boolean = false
    private var typefacePosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemExploreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as NewsViewHolder).setData(feedList[position])
    }

    override fun getItemCount(): Int = feedList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnNewsClickListener(listener: (explore: Explore) -> Unit) {
        newsClickListener = listener
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
                root.setOnClickListener {
                    newsClickListener.invoke(explore)
                }
            }
        }
    }
}
