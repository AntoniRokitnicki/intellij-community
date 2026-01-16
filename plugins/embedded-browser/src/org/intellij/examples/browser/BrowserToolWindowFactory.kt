package org.intellij.examples.browser

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class BrowserToolWindowFactory : ToolWindowFactory, DumbAware {
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val browserWindow = BrowserToolWindow(project)
    val content = ContentFactory.getInstance().createContent(browserWindow.component, "", false)
    toolWindow.contentManager.addContent(content)
  }
}
