package org.intellij.examples.browser

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBuilder
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JToolBar

class BrowserToolWindow(@Suppress("unused") private val project: Project) {
  private val browser: JBCefBrowser = JBCefBrowserBuilder().build()
  val component: JPanel = JPanel(BorderLayout())

  init {
    val toolbar = JToolBar()
    val urlField = JBTextField("https://www.jetbrains.com")
    val back = JButton("<")
    val forward = JButton(">")
    val reload = JButton("Reload")
    val home = JButton("Home")
    val devTools = JButton("DevTools")
    val runJs = JButton("Run JS")

    back.addActionListener { browser.cefBrowser.goBack() }
    forward.addActionListener { browser.cefBrowser.goForward() }
    reload.addActionListener { browser.cefBrowser.reload() }
    home.addActionListener { browser.loadURL("https://www.jetbrains.com") }
    devTools.addActionListener { browser.openDevtools() }
    runJs.addActionListener {
      browser.cefBrowser.executeJavaScript(
        "alert('Hello from the IDE!')",
        browser.cefBrowser.url,
        0
      )
    }

    urlField.addActionListener {
      var url = urlField.text.trim()
      if (!url.startsWith("http://") && !url.startsWith("https://")) {
        url = "https://$url"
      }
      browser.loadURL(url)
    }

    toolbar.add(back)
    toolbar.add(forward)
    toolbar.add(reload)
    toolbar.add(home)
    toolbar.add(urlField)
    toolbar.add(devTools)
    toolbar.add(runJs)

    component.add(toolbar, BorderLayout.NORTH)
    component.add(browser.component, BorderLayout.CENTER)
    component.border = JBUI.Borders.empty()

    component.addComponentListener(object : ComponentAdapter() {
      override fun componentResized(e: ComponentEvent) {
        browser.component.preferredSize = component.size
      }
    })
  }
}
