package com.jeda.android.monitoring.intervention

import android.content.Context

/** Persistent list of soft-intervention timestamps. Swapped for an in-memory one in tests. */
interface TimestampStore {
    fun read(): List<Long>
    fun write(timestamps: List<Long>)
}

class InMemoryTimestampStore(initial: List<Long> = emptyList()) : TimestampStore {
    private var values = initial.toList()

    override fun read(): List<Long> = values.toList()

    override fun write(timestamps: List<Long>) {
        values = timestamps.toList()
    }
}

class PrefsTimestampStore(context: Context, private val key: String) : TimestampStore {
    private val prefs = context.applicationContext
        .getSharedPreferences("jeda_interventions", Context.MODE_PRIVATE)

    override fun read(): List<Long> = prefs.getStringSet(key, emptySet())
        .orEmpty()
        .mapNotNull(String::toLongOrNull)

    override fun write(timestamps: List<Long>) {
        prefs.edit().putStringSet(key, timestamps.map(Long::toString).toSet()).apply()
    }
}
