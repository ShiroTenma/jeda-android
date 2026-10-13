package com.jeda.android.monitoring.visual

data class VisualSamplingConfig(
    val normalIntervalMillis: Long = 3_000L,
    val elevatedIntervalMillis: Long = 2_000L,
    val targetWidth: Int = 224,
    val targetHeight: Int = 224,
    val jpegQuality: Int = 80
)
