package com.yourorg.branchfs.ui

import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.ProjectViewNode
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.projectView.AbstractTreeNode
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.yourorg.branchfs.url.BranchUrlService

/**
 * Injects branch snapshot nodes next to real files in the project view.
 */
class BranchSiblingsProvider : TreeStructureProvider, DumbAware {

    private val branches = listOf("v1", "v2")

    override fun modify(parent: AbstractTreeNode<*>, children: MutableCollection<AbstractTreeNode<*>>, settings: ViewSettings?): MutableCollection<AbstractTreeNode<*>> {
        val project = parent.project ?: return children
        val additions = mutableListOf<AbstractTreeNode<*>>()
        children.forEach { child ->
            if (child is PsiFileNode) {
                val vf = child.virtualFile ?: return@forEach
                branches.forEach { branch ->
                    val url = BranchUrlService.buildByFileUrl(project, vf, branch) ?: return@forEach
                    val branchFile = VirtualFileManager.getInstance().findFileByUrl(url) ?: return@forEach
                    additions += BranchSnapshotNode(project, branchFile, child, branch)
                }
            }
        }
        children.addAll(additions)
        return children
    }

    override fun getData(selected: MutableCollection<out AbstractTreeNode<*>>?, dataId: String): Any? = null
}
