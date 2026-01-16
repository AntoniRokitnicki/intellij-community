package org.intellij.samples.embeddedbrowser

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowser
import org.cef.browser.CefBrowser
import java.awt.BorderLayout
import javax.swing.JPanel

class EmbeddedBrowserToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val browser = JBCefBrowser("https://www.jetbrains.com")
        val panel = JPanel(BorderLayout())
        panel.add(createToolbar(browser).component, BorderLayout.NORTH)
        panel.add(browser.component, BorderLayout.CENTER)

        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    private fun createToolbar(browser: JBCefBrowser): ActionToolbar {
        val actions = DefaultActionGroup(
            BackAction(browser.cefBrowser),
            ForwardAction(browser.cefBrowser),
            ReloadAction(browser)
        )
        return ActionManager.getInstance().createActionToolbar("EmbeddedBrowser", actions, true)
    }

    private class BackAction(private val browser: CefBrowser) : AnAction("Back") {
        override fun actionPerformed(e: AnActionEvent) = browser.goBack()
        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = browser.canGoBack()
        }
    }

    private class ForwardAction(private val browser: CefBrowser) : AnAction("Forward") {
        override fun actionPerformed(e: AnActionEvent) = browser.goForward()
        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = browser.canGoForward()
        }
    }

    private class ReloadAction(private val browser: JBCefBrowser) : AnAction("Reload") {
        override fun actionPerformed(e: AnActionEvent) {
            val url = browser.cefBrowser.url
            if (url != null) {
                browser.loadURL(url)
            }
        }
    }
}
