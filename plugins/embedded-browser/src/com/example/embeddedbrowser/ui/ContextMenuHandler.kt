package com.example.embeddedbrowser.ui

import com.intellij.openapi.project.Project
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefContextMenuParams
import org.cef.handler.CefContextMenuHandler
import org.cef.handler.CefMenuModel

class ContextMenuHandler(
  private val project: Project,
  private val openDevTools: () -> Unit,
  private val copyUrl: () -> Unit
) : CefContextMenuHandler {
  override fun onBeforeContextMenu(
    browser: CefBrowser?,
    frame: CefFrame?,
    params: CefContextMenuParams?,
    model: CefMenuModel?
  ) {
    model?.addSeparator()
    model?.addItem(10001, "Open DevTools")
    model?.addItem(10002, "Copy URL")
  }

  override fun onContextMenuCommand(
    browser: CefBrowser?,
    frame: CefFrame?,
    params: CefContextMenuParams?,
    commandId: Int,
    eventFlags: Int
  ): Boolean {
    when (commandId) {
      10001 -> openDevTools()
      10002 -> copyUrl()
    }
    return true
  }

  override fun onContextMenuDismissed(browser: CefBrowser?, frame: CefFrame?) {}
}
