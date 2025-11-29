package com.yourorg.branchfs.vfs

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem

/**
 * Read only virtual file system exposing branch snapshots.
 */
object BranchVirtualFileSystem : VirtualFileSystem() {
    const val PROTOCOL: String = "branchfs"

    override fun getProtocol(): String = PROTOCOL

    override fun findFileByPath(path: String): VirtualFile? = BranchFileRegistry.getOrCreate(path)

    override fun refresh(asynchronous: Boolean) {
        // read-only system, nothing to refresh
    }

    override fun refreshAndFindFileByPath(path: String): VirtualFile? = findFileByPath(path)

    override fun isReadOnly(): Boolean = true

    private fun unsupported(): Nothing = throw UnsupportedOperationException("branchfs is read-only")

    override fun deleteFile(requestor: Any?, vFile: VirtualFile) = unsupported()

    override fun moveFile(requestor: Any?, vFile: VirtualFile, newParent: VirtualFile) = unsupported()

    override fun renameFile(requestor: Any?, vFile: VirtualFile, newName: String) = unsupported()

    override fun createChildFile(requestor: Any?, parent: VirtualFile, fileName: String): VirtualFile = unsupported()

    override fun createChildDirectory(requestor: Any?, parent: VirtualFile, dirName: String): VirtualFile = unsupported()

    override fun copyFile(requestor: Any?, vFile: VirtualFile, newParent: VirtualFile, newName: String): VirtualFile = unsupported()
}
