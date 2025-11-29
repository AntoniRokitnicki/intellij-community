package com.yourorg.branchfs.git

import com.intellij.openapi.project.Project
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository
import com.yourorg.branchfs.cache.BranchContentCache
import com.yourorg.branchfs.cache.BranchContentCache.Entry
import java.nio.charset.StandardCharsets

/**
 * Reads file content from git using git show and rev-parse.
 */
class BranchGitReader(private val project: Project) {

    fun read(repository: GitRepository, branch: String, relPath: String): Entry {
        val root = repository.root
        val git = Git.getInstance()
        val revision = "$branch:$relPath"

        val revParse = GitLineHandler(project, root, GitCommand.REV_PARSE).apply {
            addParameters(revision)
            isSilent = true
        }
        val revResult = git.runCommand(revParse)
        if (revResult.success()) {
            val blobHash = revResult.outputAsJoinedString.trim()
            val show = GitLineHandler(project, root, GitCommand.SHOW).apply {
                addParameters(revision)
                isSilent = true
            }
            val showResult = git.runCommand(show)
            val text = showResult.outputAsJoinedString
            val bytes = text.toByteArray(StandardCharsets.UTF_8)
            return Entry(bytes, blobHash, System.currentTimeMillis(), false)
        }
        val placeholder = "[missing on $branch]".toByteArray(StandardCharsets.UTF_8)
        return Entry(placeholder, null, System.currentTimeMillis(), true)
    }
}
