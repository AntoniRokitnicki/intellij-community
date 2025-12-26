package com.yourorg.ghwrite.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JPanel

internal class GhWriteRootPanel(project: Project) : JPanel(BorderLayout()) {
  private val tabs = JBTabbedPane()
  init {
    border = JBUI.Borders.empty()
    tabs.addTab("Issue: Create", AllIcons.Actions.CreateFromUsage, IssueCreatePanel(project))
    tabs.addTab("PR: Create", AllIcons.Actions.Diff, PrCreatePanel(project))
    tabs.addTab("Issue: Comment", AllIcons.Actions.Edit, IssueCommentPanel(project))
    tabs.addTab("Issue: Labels", AllIcons.Actions.Properties, IssueLabelsPanel(project))
    tabs.addTab("Issue: Close/Reopen", AllIcons.Actions.Suspend, IssueStatePanel(project))
    tabs.addTab("PR: Merge", AllIcons.Actions.Commit, PrMergePanel(project))
    tabs.addTab("Release: Create", AllIcons.Nodes.PpLib, ReleaseCreatePanel(project))
    tabs.addTab("Gist: Create", AllIcons.Nodes.Folder, GistCreatePanel(project))
    add(tabs, BorderLayout.CENTER)
  }
}
