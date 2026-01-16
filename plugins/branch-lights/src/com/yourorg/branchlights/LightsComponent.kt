package com.yourorg.branchlights

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.ClickListener
import com.intellij.ui.JBColor
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import java.awt.*
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.ToolTipManager
import kotlin.math.min

internal class LightsComponent(private val project: Project) : JComponent(), Disposable {

  private data class Cell(val bounds: Rectangle, val branchIndex: Int)

  private var cells: List<Cell> = emptyList()
  private var states: List<LightState> = emptyList()
  private var tooltips: List<String> = emptyList()
  private var currentFile: VirtualFile? = null
  private var repo: GitRepository? = null

  init {
    preferredSize = Dimension(160, 18)
    background = null
    isOpaque = false

    object : ClickListener() {
      override fun onClick(e: MouseEvent, clickCount: Int): Boolean {
        val p = e.point
        val hit = cells.firstOrNull { it.bounds.contains(p) } ?: return false
        openDiff(hit.branchIndex)
        return true
      }
    }.installOn(this)

    ToolTipManager.sharedInstance().registerComponent(this)
  }

  fun refresh() {
    val fem = FileEditorManager.getInstance(project)
    val vf = fem.selectedFiles.firstOrNull()
    currentFile = vf
    repo = vf?.let { GitRepositoryManager.getInstance(project).getRepositoryForFileQuick(it) }
    if (vf == null || repo == null) {
      states = List(BranchConfig.branches.size) { LightState.GRAY }
      tooltips = List(BranchConfig.branches.size) { "No file or outside Git repo" }
      repaint()
      return
    }
    ApplicationManager.getApplication().executeOnPooledThread {
      val newStates = BranchComparator.compareAgainstBranches(project, repo!!, vf)
      val newTips = BranchConfig.branches.map { "${it.name}: ${it.miniDescription}" }
      ApplicationManager.getApplication().invokeLater {
        states = newStates
        tooltips = newTips
        repaint()
      }
    }
  }

  override fun paintComponent(g: Graphics) {
    super.paintComponent(g)
    val g2 = g as Graphics2D
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

    val n = BranchConfig.branches.size
    val w = width
    val h = height
    val pad = 2
    val cellW = min(24, w / n)
    val startX = (w - cellW * n) / 2
    val radius = min(cellW - pad * 2, h - pad * 2)

    val newCells = ArrayList<Cell>(n)
    for (i in 0 until n) {
      val x = startX + i * cellW + pad
      val y = (h - radius) / 2
      val state = states.getOrNull(i) ?: LightState.GRAY
      val color = when (state) {
        LightState.GREEN -> Color(0, 170, 0)
        LightState.RED -> Color(200, 40, 40)
        LightState.GRAY -> JBColor.GRAY
      }
      g2.color = color
      g2.fillOval(x, y, radius, radius)
      g2.color = JBColor.border()
      g2.drawOval(x, y, radius, radius)

      // tiny label under the light
      g2.font = g2.font.deriveFont(10f)
      val label = BranchConfig.branches[i].shortLabel
      val fm = g2.fontMetrics
      val lw = fm.stringWidth(label)
      g2.color = JBColor.foreground()
      g2.drawString(label, x + (radius - lw) / 2, y + radius + fm.ascent - 2)

      newCells += Cell(Rectangle(x, y, radius, radius), i)
    }
    cells = newCells
  }

  override fun getToolTipText(event: java.awt.event.MouseEvent): String? {
    val hit = cells.firstOrNull { it.bounds.contains(event.point) } ?: return null
    return tooltips.getOrNull(hit.branchIndex)
  }

  private fun openDiff(branchIndex: Int) {
    val vf = currentFile ?: return
    val repo = repo ?: return
    val branch = BranchConfig.branches[branchIndex].name
    ApplicationManager.getApplication().executeOnPooledThread {
      val content = BranchComparator.getContentFromBranch(project, repo, branch, vf) ?: return@executeOnPooledThread
      val df = com.intellij.diff.DiffContentFactory.getInstance()
      val left = df.create(project, content, com.intellij.openapi.fileTypes.FileTypeRegistry.getInstance().getFileTypeByFileName(vf.name))
      val right = df.create(project, vf)
      val title = "${vf.name}  vs  $branch"
      val req = SimpleDiffRequest(title, left, right, branch, "Working Tree")
      ApplicationManager.getApplication().invokeLater {
        DiffManager.getInstance().showDiff(project, req)
      }
    }
  }

  override fun dispose() {}
}
