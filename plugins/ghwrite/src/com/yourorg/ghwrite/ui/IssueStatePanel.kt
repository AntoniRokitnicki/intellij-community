package com.yourorg.ghwrite.ui

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.yourorg.ghwrite.core.GhClient
import com.yourorg.ghwrite.ui.common.FormPanel
import com.yourorg.ghwrite.ui.common.textField
import java.awt.BorderLayout
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JToolBar
import javax.swing.JTextArea
import javax.swing.SwingUtilities

internal class IssueStatePanel(project: Project) : JPanel(BorderLayout()) {
  private val gh = GhClient(project)

  private val owner = textField(12)
  private val repo = textField(16)
  private val number = textField(8)
  private val close = JRadioButton("Close")
  private val reopen = JRadioButton("Reopen")

  private val submit = JButton("Update State")
  private val dryRun = JBCheckBox("Dry Run").apply { isSelected = true }
  private val open = JBCheckBox("Open in browser").apply { isSelected = true }
  private val output = JTextArea().apply { isEditable = false }

  init {
    val bg = ButtonGroup(); bg.add(close); bg.add(reopen); close.isSelected = true

    val bar = JToolBar().apply { isFloatable = false }
    bar.add(submit); bar.add(dryRun); bar.add(open)
    add(bar, BorderLayout.NORTH)

    val statePanel = JPanel().apply { add(close); add(reopen) }
    val form = FormPanel().apply {
      addRow("Owner", owner)
      addRow("Repo", repo)
      addRow("Issue #", number)
      addRow("State", statePanel)
    }
    add(form, BorderLayout.CENTER)
    add(output, BorderLayout.SOUTH)

    submit.addActionListener { doUpdate() }
  }

  private fun doUpdate() {
    val o = owner.text.trim()
    val r = repo.text.trim()
    val n = number.text.trim()
    if (o.isEmpty() || r.isEmpty() || n.isEmpty()) {
      output.text = "Owner, Repo, Issue # required."
      return
    }
    val state = if (close.isSelected) "closed" else "open"
    val json = "{\"state\":${q(state)}}"
    if (dryRun.isSelected) {
      output.text = "PATCH /repos/$o/$r/issues/$n\n$json"
      return
    }
    output.text = "Updating…"
    val res = gh.runApiJson("PATCH", "/repos/$o/$r/issues/$n", json)
    SwingUtilities.invokeLater {
      output.text = if (res.ok) res.out.ifBlank { "Updated." } else "ERROR: ${res.err}\n${res.out}"
      if (res.ok && open.isSelected) {
        val key = "\"html_url\":\""
        val idx = res.out.indexOf(key)
        if (idx >= 0) {
          val end = res.out.indexOf('\"', idx + key.length)
          if (end > idx) BrowserUtil.browse(res.out.substring(idx + key.length, end))
        }
      }
    }
  }

  private fun q(s: String) = "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}
