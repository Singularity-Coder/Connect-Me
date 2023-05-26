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
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemInsightBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.ChatRole
import com.singularitycoder.connectme.search.model.Insight

class InsightsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var insightsList = mutableListOf<Insight?>()

    private var itemClickListener: (insight: Insight?, position: Int) -> Unit = { _, _ -> }
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
        start: Int = 0,
        end: Int = 0,
        query: String,
        utteranceId: String?,
        recyclerView: RecyclerView,
        adapterPosition: Int,
        insight: Insight?
    ) {
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(adapterPosition) as ThisViewHolder
        val textWithHighlights: Spannable = SpannableString(utteranceId).apply {
            setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            setSpan(BackgroundColorSpan(Color.YELLOW), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        }
        if (insight?.userType == ChatRole.USER.ordinal) {
            viewHolder.getView().tvTextRequest.apply {
                text = textWithHighlights
//                text = utteranceId
//                highlightText(query = query, result = text.toString())
            }
        } else {
            viewHolder.getView().tvTextResponse.apply {
                text = textWithHighlights
//                text = utteranceId
//                highlightText(query = query, result = text.toString())
            }
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
                setupUserActionListeners(insight)
                if (insight?.userType == ChatRole.USER.ordinal) {
                    cardRequest.isVisible = true
                    cardResponse.isVisible = false
                    tvTextRequest.text = if (insight.insight.isNullOrBlank()) {
                        "Something went wrong. Try again!"
                    } else insight.insight
                } else {
                    cardRequest.isVisible = false
                    cardResponse.isVisible = true
                    tvTextResponse.text = if (insight?.insight.isNullOrBlank()) {
                        "Something went wrong. Try again!"
                    } else insight?.insight
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
                        currentImagePosition = 0
                        clChatImage.isVisible = true
                        tvTextResponse.isVisible = false
                        ivChatImage.layoutParams.width = deviceWidth() - (deviceWidth() / 3) + 32.dpToPx().toInt()
                        ivChatImage.layoutParams.height = deviceWidth() - (deviceWidth() / 3) + 32.dpToPx().toInt()
                        sliderChatImage.layoutParams.width = deviceWidth() - (deviceWidth() / 3) + 32.dpToPx().toInt()
                        sliderChatImage.max = insight?.imageList?.lastIndex ?: 0
                        setImageDetails(insight)
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
                        sliderChatImage.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                                println("seekbar progress: $progress")
                                tvImageCount.text = "${progress + 1}/${insight?.imageList?.size}"
                                ivChatImage.load(insight?.imageList?.getOrNull(progress)) {
                                    placeholder(R.color.black)
                                    error(R.color.md_red_dark)
                                }
                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
                            override fun onStopTrackingTouch(seekBar: SeekBar) {
                                currentImagePosition = seekBar.progress
                            }
                        })
                    } else {
                        clChatImage.isVisible = false
                        tvTextResponse.isVisible = true
                    }
                }
            }
        }

        private fun ListItemInsightBinding.setupUserActionListeners(insight: Insight?) {
            tvTextResponse.apply {
                onCustomLongClick {
                    cardResponse.performLongClick()
                }
                onSafeClick {
                    cardResponse.performClick()
                }
            }
            tvTextRequest.apply {
                onCustomLongClick {
                    cardRequest.performLongClick()
                }
                onSafeClick {
                    cardRequest.performClick()
                }
            }
            cardResponse.apply {
                onCustomLongClick {
                    itemLongClickListener.invoke(insight, it, bindingAdapterPosition)
                }
                onSafeClick {
                    itemClickListener.invoke(insight, bindingAdapterPosition)
                }
            }
            cardRequest.apply {
                onCustomLongClick {
                    itemLongClickListener.invoke(insight, it, bindingAdapterPosition)
                }
                onSafeClick {
                    itemClickListener.invoke(insight, bindingAdapterPosition)
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