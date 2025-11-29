package com.yourorg.branchfs.vfs

import java.util.concurrent.ConcurrentHashMap

/**
 * Registry of in-memory virtual files keyed by branchfs path.
 */
object BranchFileRegistry {
    private val files = ConcurrentHashMap<String, BranchVirtualFile>()

    fun getOrCreate(pathInScheme: String): BranchVirtualFile =
        files.computeIfAbsent(pathInScheme) { BranchVirtualFile(it) }

    fun update(pathInScheme: String, bytes: ByteArray, timestamp: Long) {
        getOrCreate(pathInScheme).update(bytes, timestamp)
    }
}
