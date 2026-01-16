package com.example.embeddedbrowser.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBTextField
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefJSQuery
import com.intellij.util.ui.JBUI
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefLifeSpanHandlerAdapter
import java.awt.BorderLayout
import java.awt.Toolkit
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*

class SingleBrowserTab(
  private val project: Project,
  startUrl: String,
  private val onOpenDevTools: (java.awt.Component?) -> Unit
) : Disposable {

  private val client = JBCefApp.getInstance().createClient()
  private val browser = JBCefBrowser(client, startUrl)
  private val address = JBTextField(startUrl)
  private val panel = JPanel(BorderLayout())
  private val toolbar = JToolBar().apply { isFloatable = false }
  private val jsBridge = JBCefJSQuery.create(browser)

  val component: JComponent get() = panel

  init {
    val back = JButton(AllIcons.Actions.Back).apply { addActionListener { browser.cefBrowser.goBack() } }
    val fwd = JButton(AllIcons.Actions.Forward).apply { addActionListener { browser.cefBrowser.goForward() } }
    val reload = JButton(AllIcons.Actions.Refresh).apply { addActionListener { browser.cefBrowser.reloadIgnoreCache() } }
    val stop = JButton(AllIcons.Actions.Suspend).apply { addActionListener { browser.cefBrowser.stopLoad() } }
    val home = JButton(AllIcons.Actions.Home).apply { addActionListener { load("https://www.example.com") } }
    val devt = JButton(AllIcons.Debugger.ShowCurrentFrame).apply { addActionListener { openDevTools() } }
    val zoomIn = JButton("+").apply { addActionListener { changeZoom(+0.1) } }
    val zoomOut = JButton("−").apply { addActionListener { changeZoom(-0.1) } }
    val find = JButton(AllIcons.Actions.Find).apply { addActionListener { showFindInPage() } }

    address.border = JBUI.Borders.empty(2)
    address.addActionListener { load(address.text) }
    address.addKeyListener(object : KeyAdapter() {
      override fun keyReleased(e: KeyEvent) {
        if (e.keyCode == KeyEvent.VK_ESCAPE) address.text = browser.cefBrowser.url
      }
    })

    listOf(back, fwd, reload, stop, home, devt, zoomOut, zoomIn, find).forEach { toolbar.add(it) }
    toolbar.add(address)

    client.addDownloadHandler(DownloadAndOpenHandler(project), browser.cefBrowser)
    client.addDialogHandler(DialogAndFileHandler(project), browser.cefBrowser)
    client.addPermissionHandler(PermissionHandler(project), browser.cefBrowser)
    client.addContextMenuHandler(ContextMenuHandler(project, ::openDevToolsQuick, ::copyUrl), browser.cefBrowser)
    client.addRequestHandler(RequestInterceptor(project), browser.cefBrowser)

    client.addLifeSpanHandler(object : CefLifeSpanHandlerAdapter() {
      override fun onAfterCreated(browser: CefBrowser?) {}
    }, browser.cefBrowser)

    jsBridge.addHandler { query, _: CefQueryCallback ->
      SwingUtilities.invokeLater { Messages.showInfoMessage(project, "JS says: $query", "Bridge") }
      true
    }
    injectBridge()

    panel.add(toolbar, BorderLayout.NORTH)
    panel.add(browser.component, BorderLayout.CENTER)
  }

  fun title(): String = "Tab"
  fun icon(): Icon = AllIcons.General.Web
  fun tooltip(): String = "Embedded Browser"

  private fun load(url: String) {
    val finalUrl = if (url.matches(Regex("^[a-zA-Z]+://.*"))) url else "https://$url"
    browser.loadURL(finalUrl)
  }

  private fun changeZoom(delta: Double) {
    val host = browser.cefBrowser.host
    val current = host.zoomLevel
    host.zoomLevel = (current + delta).coerceIn(-2.0, 3.0)
  }

  private fun openDevTools() {
    val dev = browser.createDevtoolsBrowser()
    onOpenDevTools(dev.component)
  }

  private fun openDevToolsQuick() = openDevTools()

  private fun copyUrl() {
    Toolkit.getDefaultToolkit().systemClipboard
      .setContents(java.awt.datatransfer.StringSelection(browser.cefBrowser.url), null)
  }

  private fun showFindInPage() {
    val text = JOptionPane.showInputDialog(panel, "Find:")
    if (!text.isNullOrEmpty()) {
      browser.cefBrowser.host.find(0, text, true, false, false)
    }
  }

  private fun injectBridge() {
    browser.cefBrowser.executeJavaScript(
      """
        (function(){
          if (window.ideEcho) return;
          window.ideEcho = function(msg){ ${'$'}{jsBridge.createRequestJS("msg")} };
        })();
      """.trimIndent(),
      browser.cefBrowser.url,
      0
    )
  }

  override fun dispose() {
    jsBridge.dispose()
    browser.dispose()
  }
}
