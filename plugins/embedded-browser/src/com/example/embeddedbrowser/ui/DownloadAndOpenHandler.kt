package com.example.embeddedbrowser.ui

import com.intellij.openapi.project.Project
import org.cef.callback.CefDownloadItem
import org.cef.callback.CefDownloadItemCallback
import org.cef.handler.CefDownloadHandler

class DownloadAndOpenHandler(private val project: Project) : CefDownloadHandler {
  override fun onBeforeDownload(
    browser: org.cef.browser.CefBrowser?,
    item: CefDownloadItem?,
    suggestedName: String?,
    callback: CefDownloadItemCallback?
  ): Boolean {
    val target = java.nio.file.Files.createTempFile("jcef-", suggestedName ?: "download.bin")
    callback?.Continue(target.toString(), true)
    return true
  }

  override fun onDownloadUpdated(
    browser: org.cef.browser.CefBrowser?,
    item: CefDownloadItem?,
    callback: CefDownloadItemCallback?
  ) {
    if (item != null && item.isComplete) {
      java.awt.Desktop.getDesktop().open(java.io.File(item.fullPath))
    }
  }
}
