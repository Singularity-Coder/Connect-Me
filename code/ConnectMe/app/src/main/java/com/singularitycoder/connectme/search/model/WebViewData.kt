package com.singularitycoder.connectme.search.model

import android.graphics.Bitmap
import android.net.http.SslCertificate

data class WebViewData(
    val url: String? = null,
    val title: String? = null,
    val favIcon: Bitmap? = null,
    val certificate: SslCertificate? = null
)
