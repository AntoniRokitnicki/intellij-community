package com.yourorg.branchlights

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.StreamUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository
import java.security.MessageDigest

internal object BranchComparator {

  fun compareAgainstBranches(project: Project, repo: GitRepository, vf: VirtualFile): List<LightState> {
    val workingBytes = ReadAction.compute<byteArray> {
      vf.inputStream.use { it.readBytes() }
    }
    val workingHash = sha256(workingBytes)
    val rel = VfsUtilCore.getRelativePath(vf, repo.root) ?: return List(BranchConfig.branches.size) { LightState.GRAY }

    return BranchConfig.branches.map { info ->
      val blobHash = tryRevParseBlob(project, repo, info.name, rel)
      if (blobHash == null) {
        LightState.RED // file missing on branch
      } else {
        // Quick path: compare blob id by content if available would be ideal
        // Here we fetch content when needed
        val content = getContentFromBranch(project, repo, info.name, vf)
        if (content == null) LightState.RED
        else {
          val h = sha256(content.toByteArray())
          if (h.contentEquals(workingHash)) LightState.GREEN else LightState.RED
        }
      }
    }
  }

  fun getContentFromBranch(project: Project, repo: GitRepository, branch: String, vf: VirtualFile): String? {
    val rel = VfsUtilCore.getRelativePath(vf, repo.root) ?: return null
    val handler = GitLineHandler(project, repo.root, GitCommand.SHOW).apply {
      addParameters("$branch:$rel")
      setSilent(true)
    }
    val res = Git.getInstance().runCommand(handler)
    if (!res.success()) return null
    return res.outputAsJoinedString
  }

  private fun tryRevParseBlob(project: Project, repo: GitRepository, branch: String, relPath: String): String? {
    val handler = GitLineHandler(project, repo.root, GitCommand.REV_PARSE).apply {
      addParameters("$branch:$relPath")
      setSilent(true)
    }
    val res = Git.getInstance().runCommand(handler)
    return if (res.success()) res.outputAsJoinedString.trim() else null
  }

  private fun sha256(bytes: ByteArray): ByteArray {
    val md = MessageDigest.getInstance("SHA-256")
    return md.digest(bytes)
  }
}
