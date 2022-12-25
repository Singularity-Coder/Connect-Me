package com.singularitycoder.connectme

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.databinding.ListItemNewsBinding

class HomeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var newsList = emptyList<News>()
    private var newsClickListener: (news: News) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as NewsViewHolder).setData(newsList[position])
    }

    override fun getItemCount(): Int = newsList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnNewsClickListener(listener: (news: News) -> Unit) {
        newsClickListener = listener
    }

    inner class NewsViewHolder(
        private val itemBinding: ListItemNewsBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(news: News) {
            itemBinding.apply {
                ivNewsImage.load(news.imageUrl) {
                    placeholder(R.color.black)
                }
                val source = if (news.source.isNullOrBlank()) {
                    news.link?.substringAfter("//")?.substringBefore("/")?.replace("www.", "")
                } else {
                    news.source
                }
                tvSource.text = "$source  \u2022  ${news.time}"
                tvTitle.text = news.title
                root.setOnClickListener {
                    newsClickListener.invoke(news)
                }
            }
        }
    }
}
