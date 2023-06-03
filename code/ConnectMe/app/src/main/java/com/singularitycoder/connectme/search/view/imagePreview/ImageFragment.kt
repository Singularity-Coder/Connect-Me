package com.singularitycoder.connectme.search.view.imagePreview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentImageBinding
import dagger.hilt.android.AndroidEntryPoint

private const val IMAGE_URL = "IMAGE_URL"

@AndroidEntryPoint
class ImageFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(imageUrl: String) = ImageFragment().apply {
            arguments = Bundle().apply { putString(IMAGE_URL, imageUrl) }
        }
    }

    private var imageUrl: String? = null

    private lateinit var binding: FragmentImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageUrl = arguments?.getString(IMAGE_URL)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentImageBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
    }

    private fun FragmentImageBinding.setupUI() {
        ivImage.load(imageUrl) {
            placeholder(R.color.black)
            error(R.color.md_red_dark)
        }
    }
}