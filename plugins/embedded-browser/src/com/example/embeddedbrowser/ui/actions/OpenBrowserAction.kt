package com.example.embeddedbrowser.ui.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager

class OpenBrowserAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val tw = ToolWindowManager.getInstance(e.project!!).getToolWindow("Browser")
    tw?.activate(null, true)
  }
}
