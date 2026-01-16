package com.example.embeddedbrowser.ui

import com.intellij.openapi.project.Project
import org.cef.browser.CefBrowser
import org.cef.handler.CefPermissionHandler

class PermissionHandler(private val project: Project) : CefPermissionHandler {
  override fun onRequestGeolocationPermission(
    browser: CefBrowser?,
    requestingUrl: String?,
    requestId: Int,
    callback: CefPermissionHandler.CefGeolocationCallback?
  ): Boolean {
    callback?.Continue(true)
    return true
  }

  override fun onShowPermissionPrompt(
    browser: CefBrowser?,
    requestingUrl: String?,
    types: Int,
    callback: CefPermissionHandler.CefPermissionPromptCallback?
  ): Boolean {
    callback?.Continue(true)
    return true
  }

  override fun onDismissPermissionPrompt(
    browser: CefBrowser?,
    callback: CefPermissionHandler.CefPermissionPromptCallback?
  ) {}
}
