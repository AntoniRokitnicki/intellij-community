package com.yourorg.branchlights

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBarWidgetFactory

class BranchLightsFactory : StatusBarWidgetFactory {
  override fun getId(): String = "com.yourorg.branchlights.widget"
  override fun getDisplayName(): String = "Branch Lights"
  override fun isAvailable(project: Project): Boolean = true
  override fun createWidget(project: Project) = BranchLightsWidget(project)
  override fun disposeWidget(widget: com.intellij.openapi.wm.StatusBarWidget) {}
  override fun canBeEnabledOn(statusBar: com.intellij.openapi.wm.StatusBar): Boolean = true
}
