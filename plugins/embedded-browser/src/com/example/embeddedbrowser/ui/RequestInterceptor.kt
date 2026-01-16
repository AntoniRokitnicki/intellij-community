package com.example.embeddedbrowser.ui

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.cef.browser.CefBrowser
import org.cef.handler.CefRequestHandler
import org.cef.network.CefRequest
import org.cef.network.CefResponse

class RequestInterceptor(private val project: Project) : CefRequestHandler {
  private val log = Logger.getInstance(RequestInterceptor::class.java)

  override fun onBeforeBrowse(
    browser: CefBrowser?,
    frame: org.cef.browser.CefFrame?,
    request: CefRequest?,
    userGesture: Boolean,
    isRedirect: Boolean
  ): Boolean {
    request?.setHeaderByName("X-IDE", "IntelliJ-Embedded", true)
    log.debug("Browse: ${'$'}{request?.url}")
    return false
  }

  override fun onResourceResponse(
    browser: CefBrowser?,
    frame: org.cef.browser.CefFrame?,
    request: CefRequest?,
    response: CefResponse?
  ): Boolean {
    return false
  }

  override fun getResourceRequestHandler(
    browser: CefBrowser?,
    frame: org.cef.browser.CefFrame?,
    request: CefRequest?,
    isNavigation: Boolean,
    isDownload: Boolean,
    requestInitiator: String?,
    disableDefaultHandling: BooleanArray?
  ): org.cef.handler.CefResourceRequestHandler? = null
}
