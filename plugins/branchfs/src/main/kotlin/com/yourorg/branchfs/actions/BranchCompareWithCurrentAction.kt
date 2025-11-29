package com.yourorg.branchfs.actions

import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAware
import git4idea.repo.GitRepositoryManager
import com.yourorg.branchfs.url.BranchUrlService
import com.yourorg.branchfs.vfs.BranchVirtualFileSystem

/**
 * Invokes diff between branch snapshot and working tree file.
 */
class BranchCompareWithCurrentAction : AnAction(), DumbAware {

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = file?.fileSystem?.protocol == BranchVirtualFileSystem.PROTOCOL
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val branchFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val pathInScheme = branchFile.path.removePrefix("${BranchVirtualFileSystem.PROTOCOL}://")
        val parsed = BranchUrlService.parse(pathInScheme) ?: return
        val repo = GitRepositoryManager.getInstance(project).repositories.firstOrNull() ?: return
        val currentFile = repo.root.findFileByRelativePath(parsed.relPath) ?: return

        val request = SimpleDiffRequest("${parsed.branch} vs Current", branchFile, currentFile, parsed.branch, "Current")
        DiffManager.getInstance().showDiff(project, request)
    }
}
