package com.yourorg.branchfs.vfs

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.vfs.VirtualFile
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicLong

/**
 * In-memory virtual file containing branch snapshot content.
 */
class BranchVirtualFile(private val pathInScheme: String) : VirtualFile() {

    @Volatile
    private var content: ByteArray = ByteArray(0)

    private val stamp = AtomicLong(-1)

    fun update(bytes: ByteArray, timestamp: Long) {
        content = bytes
        stamp.set(timestamp)
    }

    override fun getFileSystem() = BranchVirtualFileSystem

    override fun getName(): String = pathInScheme.substringAfterLast('/')

    override fun getFileType(): FileType = FileTypeManager.getInstance().getFileTypeByFileName(name)

    override fun getPath(): String = "${BranchVirtualFileSystem.PROTOCOL}://$pathInScheme"

    override fun isWritable(): Boolean = false

    override fun isDirectory(): Boolean = false

    override fun isValid(): Boolean = true

    override fun getParent(): VirtualFile? = null

    override fun getChildren(): Array<VirtualFile> = emptyArray()

    override fun getOutputStream(requestor: Any?, newModificationStamp: Long, newTimeStamp: Long): OutputStream =
        throw UnsupportedOperationException("branchfs is read-only")

    override fun contentsToByteArray(): ByteArray = content

    override fun getInputStream(): InputStream = ByteArrayInputStream(content)

    override fun getTimeStamp(): Long = stamp.get()

    override fun getLength(): Long = content.size.toLong()

    override fun refresh(asynchronous: Boolean, recursive: Boolean, postRunnable: Runnable?) {}

    override fun getModificationStamp(): Long = stamp.get()
}
