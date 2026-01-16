package com.example.embeddedbrowser.ui

import com.intellij.openapi.project.Project
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JPanel

class BrowserTabsPanel(private val project: Project) : JPanel(BorderLayout()) {
  private val tabs = JBTabbedPane()
  private val devtoolsSplitter = JBSplitter(false, 0.8f, 0.2f, 0.95f)
  private var devToolsAttached = false

  init {
    border = JBUI.Borders.empty()
    devtoolsSplitter.firstComponent = tabs
    add(devtoolsSplitter, BorderLayout.CENTER)
  }

  fun openHome() = openUrl("https://www.example.com")

  fun openUrl(url: String) {
    val tab = SingleBrowserTab(project, url, onOpenDevTools = { attachDevTools(it) })
    tabs.addTab(tab.title(), tab.icon(), tab.component, tab.tooltip())
    tabs.selectedIndex = tabs.tabCount - 1
  }

  private fun attachDevTools(devToolsComponent: java.awt.Component?) {
    if (devToolsComponent == null) return
    devtoolsSplitter.secondComponent = devToolsComponent
    devToolsAttached = true
  }

  fun detachDevTools() {
    devtoolsSplitter.secondComponent = null
    devToolsAttached = false
  }
}
