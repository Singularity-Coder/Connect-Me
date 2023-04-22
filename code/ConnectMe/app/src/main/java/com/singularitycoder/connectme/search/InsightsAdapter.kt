package com.singularitycoder.connectme.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemInsightBinding
import com.singularitycoder.connectme.helpers.color

class InsightsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var insightsList = mutableListOf<InsightObject.Insight?>()
    private var itemClickListener: (insight: InsightObject.Insight?) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemInsightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThisViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ThisViewHolder).setData(insightsList[position])
    }

    override fun getItemCount(): Int = insightsList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnItemClickListener(listener: (insight: InsightObject.Insight?) -> Unit) {
        itemClickListener = listener
    }

//    fun setInsightsList(insightsList: List<InsightObject.Insight?>) {
//        this.insightsList = insightsList
//    }

    inner class ThisViewHolder(
        private val itemBinding: ListItemInsightBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(insight: InsightObject.Insight?) {
            itemBinding.apply {
                if (insight?.userType == 0) {
                    root.setCardBackgroundColor(root.context.color(R.color.purple_50))
                    tvText.setTextColor(root.context.color(R.color.purple_500))
                } else {
                    root.setCardBackgroundColor(root.context.color(R.color.black_50))
                    tvText.setTextColor(root.context.color(R.color.title_color))
                }
                tvText.text = insight?.insight
                root.setOnClickListener {
                    itemClickListener.invoke(insight)
                }
            }
        }
    }
}
