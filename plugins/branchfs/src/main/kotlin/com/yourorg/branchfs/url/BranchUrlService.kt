package com.yourorg.branchfs.url

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import git4idea.repo.GitRepositoryManager
import com.yourorg.branchfs.vfs.BranchVirtualFileSystem

/**
 * Utilities for building and parsing branchfs URLs.
 */
object BranchUrlService {
    fun buildByFileUrl(project: Project, file: VirtualFile, branch: String): String? {
        val repo = GitRepositoryManager.getInstance(project).getRepositoryForFileQuick(file) ?: return null
        val root = repo.root.path
        val relPath = file.path.removePrefix("$root/")
        return "${BranchVirtualFileSystem.PROTOCOL}://$branch/$relPath"
    }

    fun resolve(url: String) = VirtualFileManager.getInstance().findFileByUrl(url)

    data class Parsed(val branch: String, val relPath: String)

    fun parse(pathInScheme: String): Parsed? {
        val idx = pathInScheme.indexOf('/')
        if (idx == -1) return null
        val branch = pathInScheme.substring(0, idx)
        val relPath = pathInScheme.substring(idx + 1)
        return Parsed(branch, relPath)
    }
}
