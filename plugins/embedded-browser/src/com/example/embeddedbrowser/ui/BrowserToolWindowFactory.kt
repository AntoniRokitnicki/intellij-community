package com.example.embeddedbrowser.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefApp
import javax.swing.JLabel

class BrowserToolWindowFactory : ToolWindowFactory {
  override fun isApplicable(project: Project): Boolean = true

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val contentFactory = ContentFactory.getInstance()
    if (!JBCefApp.isSupported()) {
      val fallback = JLabel("JCEF not supported on this platform. Using external browser fallback.")
      toolWindow.contentManager.addContent(contentFactory.createContent(fallback, "Unavailable", false))
      return
    }
    val panel = BrowserTabsPanel(project)
    toolWindow.contentManager.addContent(contentFactory.createContent(panel, "", false))
    panel.openHome()
  }
}
