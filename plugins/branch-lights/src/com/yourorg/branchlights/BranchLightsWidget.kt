package com.yourorg.branchlights

import com.intellij.diff.DiffManager
import com.intellij.diff.DiffContentFactory
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.impl.status.WidgetPresentationData
import com.intellij.ui.ClickListener
import com.intellij.ui.JBColor
import com.intellij.util.Consumer
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import java.awt.Color
import java.awt.Cursor
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ToolTipManager

private enum class LightState { GREEN, RED, GRAY }

class BranchLightsWidget(private val project: Project) :
  StatusBarWidget, StatusBarWidget.Multiframe, Disposable {

  private var statusBar: StatusBar? = null
  private val panel = LightsComponent(project)

  override fun ID(): String = "com.yourorg.branchlights.widget"
  override fun getPresentation(): StatusBarWidget.WidgetPresentation? = null
  override fun install(statusBar: StatusBar) {
    this.statusBar = statusBar
    panel.refresh() // initial
    ToolTipManager.sharedInstance().registerComponent(panel)
    val fdm = FileDocumentManager.getInstance()
    val bus = project.messageBus.connect(this)

    // Refresh on file save
    bus.subscribe(FileDocumentManager.FILE_DOCUMENT_SYNC, object : FileDocumentManager.Listener {
      override fun beforeDocumentSaving(document: com.intellij.openapi.editor.Document) {
        ApplicationManager.getApplication().invokeLater { panel.refresh() }
      }
    })

    // Refresh when selection changes
    val fem = FileEditorManager.getInstance(project)
    fem.addFileEditorManagerListener({ panel.refresh() }, this)

    // Also refresh on editor create dispose
    EditorFactory.getInstance().addEditorFactoryListener(object : EditorFactoryListener {
      override fun editorCreated(event: EditorFactoryEvent) { panel.refresh() }
      override fun editorReleased(event: EditorFactoryEvent) { panel.refresh() }
    }, this)
  }

  override fun dispose() {
    Disposer.dispose(panel)
  }

  override fun getComponent(): JComponent = panel
  override fun copy(): StatusBarWidget = BranchLightsWidget(project)
}
