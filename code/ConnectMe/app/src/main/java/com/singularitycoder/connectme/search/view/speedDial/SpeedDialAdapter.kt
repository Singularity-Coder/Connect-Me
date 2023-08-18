package com.singularitycoder.connectme.search.view.speedDial

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemHistoryBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.SpeedDialFeatures

class SpeedDialAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var speedDialList = emptyList<SpeedDial?>()
    private var itemClickListener: (speedDial: SpeedDial?) -> Unit = {}
    private var itemLongClickListener: (speedDial: SpeedDial?) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThisViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ThisViewHolder).setData(speedDialList[position])
    }

    override fun getItemCount(): Int = speedDialList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnItemClickListener(listener: (speedDial: SpeedDial?) -> Unit) {
        itemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: (speedDial: SpeedDial?) -> Unit) {
        itemLongClickListener = listener
    }

    inner class ThisViewHolder(
        private val itemBinding: ListItemHistoryBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(speedDial: SpeedDial?) {
            itemBinding.apply {
                tvDate.isVisible = speedDial?.isDateShown ?: false
                tvDate.text = speedDial?.timeInString ?: speedDial?.time.toShortDate()
                val icon = if (speedDial?.type == SpeedDialFeatures.DOWNLOADS.value) {
                    ivWebappIcon.setBackgroundColor(root.context.color(R.color.purple_50))
                    ivWebappIcon.scaleType = ImageView.ScaleType.CENTER
                    speedDial.bitmap
                } else {
                    ivWebappIcon.setBackgroundColor(root.context.color(R.color.white))
                    ivWebappIcon.scaleType = ImageView.ScaleType.CENTER_CROP
                    decodeBase64StringToBitmap(speedDial?.favicon)
                }
                ivWebappIcon.load(icon) {
                    placeholder(R.drawable.ic_placeholder_rectangle)
                    error(R.drawable.ic_placeholder_rectangle)
                }
                tvTitle.text = if (speedDial?.title.isNullOrBlank()) {
                    getHostFrom(url = speedDial?.link)
                } else {
                    speedDial?.title
                }
                tvSubtitle.text = getHostFrom(url = speedDial?.link).replace("www.", "")
                tvTime.text = speedDial?.time?.toShortTime()
                root.onSafeClick {
                    itemClickListener.invoke(speedDial)
                }
                root.onCustomLongClick {
                    itemLongClickListener.invoke(speedDial)
                }
            }
        }
    }
}
