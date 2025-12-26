package com.yourorg.ghwrite.ui.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager

internal class OpenGhWriteToolWindowAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val window = ToolWindowManager.getInstance(project).getToolWindow("GitHub Write") ?: return
    window.show()
  }
}
