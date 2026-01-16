package com.example.embeddedbrowser.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import org.cef.callback.CefDialogHandler
import org.cef.handler.CefDialogHandler.FileDialogMode
import java.io.File

class DialogAndFileHandler(private val project: Project) : CefDialogHandler {
  override fun onFileDialog(
    browser: org.cef.browser.CefBrowser?,
    mode: FileDialogMode?,
    title: String?,
    defaultFilePath: String?,
    acceptFilters: MutableList<String>?,
    selectedAcceptFilter: Int,
    callback: CefDialogHandler.FileDialogCallback?
  ): Boolean {
    val chooser = javax.swing.JFileChooser().apply {
      fileSelectionMode = javax.swing.JFileChooser.FILES_ONLY
      isMultiSelectionEnabled = mode == FileDialogMode.FILE_DIALOG_OPEN_MULTIPLE
    }
    val res = chooser.showOpenDialog(null)
    if (res == javax.swing.JFileChooser.APPROVE_OPTION) {
      val files = if (chooser.isMultiSelectionEnabled) chooser.selectedFiles else arrayOf(chooser.selectedFile)
      callback?.Continue(if (files.isEmpty()) emptyList() else files.map(File::getAbsolutePath), selectedAcceptFilter)
    } else {
      callback?.Cancel()
    }
    return true
  }

  override fun onJSDialog(
    browser: org.cef.browser.CefBrowser?,
    originUrl: String?,
    dialogType: org.cef.handler.CefJSDialogHandler.JSDialogType?,
    messageText: String?,
    defaultPromptText: String?,
    callback: org.cef.callback.CefJSDialogCallback?,
    suppressMessage: BooleanArray?
  ): Boolean {
    val result = when (dialogType) {
      org.cef.handler.CefJSDialogHandler.JSDialogType.JSDIALOGTYPE_ALERT -> {
        Messages.showInfoMessage(project, messageText ?: "", "Alert"); true
      }
      org.cef.handler.CefJSDialogHandler.JSDialogType.JSDIALOGTYPE_CONFIRM ->
        Messages.showOkCancelDialog(project, messageText ?: "", "Confirm", null) == Messages.OK
      org.cef.handler.CefJSDialogHandler.JSDialogType.JSDIALOGTYPE_PROMPT -> {
        val input = Messages.showInputDialog(project, messageText ?: "", "Prompt", null, defaultPromptText ?: "", null)
        if (input != null) {
          callback?.Continue(true, input)
          return true
        } else false
      }
      else -> true
    }
    callback?.Continue(result, "")
    return true
  }
}
