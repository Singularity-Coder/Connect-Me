package com.singularitycoder.connectme.search.view

import android.graphics.drawable.AnimationDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemInsightBinding
import com.singularitycoder.connectme.helpers.constants.ChatPeople
import com.singularitycoder.connectme.helpers.deviceWidth
import com.singularitycoder.connectme.helpers.dpToPx
import com.singularitycoder.connectme.helpers.onSafeClick
import com.singularitycoder.connectme.search.model.Insight
import kotlin.math.ceil

class InsightsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var insightsList = mutableListOf<Insight?>()
    private var itemClickListener: (insight: Insight?) -> Unit = {}
    private var itemLongClickListener: (insight: Insight?, view: View?) -> Unit = { _, _ -> }
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

    fun setOnItemClickListener(listener: (insight: Insight?) -> Unit) {
        itemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: (insight: Insight?, view: View?) -> Unit) {
        itemLongClickListener = listener
    }

    inner class ThisViewHolder(
        private val itemBinding: ListItemInsightBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(insight: Insight?) {
            itemBinding.apply {
                if (insight?.userType == ChatPeople.USER.ordinal) {
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
                            } else R.drawable.animated_chat_image_gradient
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

                        }
                    } else {
                        clChatImage.isVisible = false
                        tvTextResponse.isVisible = true
                    }
                }
                cardResponse.setOnLongClickListener {
                    itemLongClickListener.invoke(insight, it)
                    false
                }
                cardRequest.setOnLongClickListener {
                    itemLongClickListener.invoke(insight, it)
                    false
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