package com.singularitycoder.connectme.search.view

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.AnimationDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemInsightBinding
import com.singularitycoder.connectme.helpers.color
import com.singularitycoder.connectme.helpers.constants.ChatRole
import com.singularitycoder.connectme.helpers.deviceWidth
import com.singularitycoder.connectme.helpers.dpToPx
import com.singularitycoder.connectme.helpers.onSafeClick
import com.singularitycoder.connectme.search.model.Insight

class InsightsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var insightsList = mutableListOf<Insight?>()

    private var itemClickListener: (insight: Insight?, position: Int) -> Unit = { _, _ ->}
    private var itemLongClickListener: (insight: Insight?, view: View?, position: Int) -> Unit = { _, _, _ -> }
    private var fullScreenClickListener: (insight: Insight?, currentImagePosition: Int) -> Unit = { _, _ -> }
    private var animationDrawable: AnimationDrawable? = null
    private var currentImagePosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemInsightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThisViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ThisViewHolder).setData(insightsList[position])
    }

    override fun getItemCount(): Int = insightsList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnItemClickListener(listener: (insight: Insight?, position: Int) -> Unit) {
        itemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: (insight: Insight?, view: View?, position: Int) -> Unit) {
        itemLongClickListener = listener
    }

    fun setOnFullScreenClickListener(listener: (insight: Insight?, currentImagePosition: Int) -> Unit) {
        fullScreenClickListener = listener
    }

    // https://c1ctech.com/android-highlight-a-word-in-texttospeech/
    // https://medium.com/androiddevelopers/spantastic-text-styling-with-spans-17b0c16b4568
    fun setTtsTextHighlighting(
        utteranceId: String?,
        start: Int,
        end: Int,
        recyclerView: RecyclerView,
        adapterPosition: Int,
        insight: Insight?
    ) {
        val textWithHighlights: Spannable = SpannableString(utteranceId).apply {
            setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            setSpan(BackgroundColorSpan(Color.YELLOW), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
//        setSpan(ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
//        setSpan(QuoteSpan(itemBinding.root.context.color(R.color.purple_500)), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
//        setSpan(RelativeSizeSpan(1.5f), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        }
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(adapterPosition) as ThisViewHolder
        if (insight?.userType == ChatRole.USER.ordinal) {
            viewHolder.getView().tvTextRequest.text = textWithHighlights
        } else {
            viewHolder.getView().tvTextResponse.text = textWithHighlights
        }
    }

    fun removeTextViewSpan(
        recyclerView: RecyclerView,
        adapterPosition: Int,
        insight: Insight?
    ) {
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(adapterPosition) as ThisViewHolder
        if (insight?.userType == ChatRole.USER.ordinal) {
            viewHolder.getView().tvTextRequest.text = insight.insight
        } else {
            viewHolder.getView().tvTextResponse.text = insight?.insight
        }
    }

    inner class ThisViewHolder(
        private val itemBinding: ListItemInsightBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun getView() = itemBinding
        fun setData(insight: Insight?) {
            itemBinding.apply {
                if (insight?.userType == ChatRole.USER.ordinal) {
                    cardRequest.isVisible = true
                    cardResponse.isVisible = false
                    tvTextRequest.text = insight.insight
                } else {
                    cardRequest.isVisible = false
                    cardResponse.isVisible = true
                    tvTextResponse.text = insight?.insight
                    if (
                        insight?.insight?.contains(other = "thinking...", ignoreCase = true) == true ||
                        insight?.insight?.contains(other = "painting...", ignoreCase = true) == true
                    ) {
                        cardResponse.setBackgroundResource(
                            if (insight.insight.contains(other = "thinking...", ignoreCase = true)) {
                                R.drawable.animated_chat_gradient
                            } else {
                                tvTextResponse.setTextColor(root.context.color(R.color.white))
                                R.drawable.animated_chat_image_gradient
                            }
                        )
                        animationDrawable = (cardResponse.background as? AnimationDrawable)?.apply {
                            setEnterFadeDuration(500)
                            setExitFadeDuration(500)
                        }
                        if (animationDrawable?.isRunning?.not() == true) animationDrawable?.start()
                    } else {
                        if (animationDrawable?.isRunning == true) animationDrawable?.stop()
                    }

                    if (insight?.imageList.isNullOrEmpty().not()) {
                        clChatImage.isVisible = true
                        tvTextResponse.isVisible = false
                        ivChatImage.layoutParams.width = deviceWidth() - (deviceWidth() / 3) + 32.dpToPx().toInt()
                        ivChatImage.layoutParams.height = deviceWidth() - (deviceWidth() / 3) + 32.dpToPx().toInt()
                        sliderChatImage.max = insight?.imageList?.lastIndex ?: 0
                        setImageDetails(insight)
//                        val factor = if ((insight?.imageList?.lastIndex ?: 0) < 100) {
//                            ceil(100F / (insight?.imageList?.lastIndex?.toFloat() ?: 0F)).toInt()
//                        } else {
//                            1
//                        }
                        sliderChatImage.isClickable = false
                        cardResponse.setOnClickListener {
                            if (currentImagePosition == insight?.imageList?.lastIndex) {
                                currentImagePosition = 0
                            } else {
                                currentImagePosition++
                            }
                            setImageDetails(insight)
                        }
                        btnFullScreen.onSafeClick {
                            fullScreenClickListener.invoke(insight, currentImagePosition)
                        }
                    } else {
                        clChatImage.isVisible = false
                        tvTextResponse.isVisible = true
                    }
                }
                cardResponse.apply {
                    setOnLongClickListener {
                        itemLongClickListener.invoke(insight, it, bindingAdapterPosition)
                        false
                    }
                    onSafeClick {
                        itemClickListener.invoke(insight, bindingAdapterPosition)
                    }
                }
                cardRequest.apply {
                    setOnLongClickListener {
                        itemLongClickListener.invoke(insight, it, bindingAdapterPosition)
                        false
                    }
                    onSafeClick {
                        itemClickListener.invoke(insight, bindingAdapterPosition)
                    }
                }
            }
        }

        private fun ListItemInsightBinding.setImageDetails(insight: Insight?) {
            tvImageCount.text = "${currentImagePosition + 1}/${insight?.imageList?.size}"
            ivChatImage.load(insight?.imageList?.getOrNull(currentImagePosition)) {
                placeholder(R.color.black)
                error(R.color.md_red_dark)
            }
            sliderChatImage.progress = currentImagePosition
        }
    }
}