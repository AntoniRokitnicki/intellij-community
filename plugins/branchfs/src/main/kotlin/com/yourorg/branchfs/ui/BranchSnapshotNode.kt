package com.yourorg.branchfs.ui

import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.ProjectViewNode
import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * Node representing branch snapshot in the project view.
 */
class BranchSnapshotNode(
    project: Project,
    private val branchFile: VirtualFile,
    private val source: ProjectViewNode<*>,
    private val branch: String
) : ProjectViewNode<VirtualFile>(project, branchFile, source.settings) {

    override fun getChildren(): Collection<com.intellij.ide.projectView.AbstractTreeNode<*>> = emptyList()

    override fun update(presentation: PresentationData) {
        val baseName = source.virtualFile?.name ?: branchFile.name
        presentation.presentableText = "$baseName [$branch]"
        presentation.setIcon(branchFile.fileType.icon)
    }

    override fun contains(file: VirtualFile): Boolean = false

    override fun getVirtualFile(): VirtualFile = branchFile
}
