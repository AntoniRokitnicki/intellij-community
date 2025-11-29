package com.yourorg.branchfs.cache

import java.util.concurrent.ConcurrentHashMap

/**
 * Simple in-memory cache for branch file contents.
 */
object BranchContentCache {
    data class Key(val repoRoot: String, val relPath: String, val branch: String)
    data class Entry(val bytes: ByteArray, val blobHash: String?, val timestamp: Long, val missing: Boolean)

    private val cache = ConcurrentHashMap<Key, Entry>()

    fun get(key: Key): Entry? = cache[key]

    fun put(key: Key, entry: Entry) {
        cache[key] = entry
    }

    fun invalidate(key: Key) {
        cache.remove(key)
    }
}
