package com.yourorg.branchfs.init

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.yourorg.branchfs.vfs.BranchVirtualFileSystem

/**
 * Touches branchfs so VirtualFileManager recognizes the protocol.
 */
class BranchFsInitializer : StartupActivity.DumbAware {
    override fun runActivity(project: Project) {
        // access object to force class initialization
        BranchVirtualFileSystem.refresh(false)
    }
}
