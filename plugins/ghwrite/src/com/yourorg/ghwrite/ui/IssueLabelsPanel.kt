package com.yourorg.ghwrite.ui

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.yourorg.ghwrite.core.GhClient
import com.yourorg.ghwrite.ui.common.FormPanel
import com.yourorg.ghwrite.ui.common.textField
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JToolBar
import javax.swing.JTextArea
import javax.swing.SwingUtilities

internal class IssueLabelsPanel(project: Project) : JPanel(BorderLayout()) {
  private val gh = GhClient(project)

  private val owner = textField(12)
  private val repo = textField(16)
  private val number = textField(8)
  private val labels = textField(24).apply { toolTipText = "comma separated" }

  private val submit = JButton("Set Labels")
  private val dryRun = JBCheckBox("Dry Run").apply { isSelected = true }
  private val output = JTextArea().apply { isEditable = false }

  init {
    val bar = JToolBar().apply { isFloatable = false }
    bar.add(submit); bar.add(dryRun)
    add(bar, BorderLayout.NORTH)

    val form = FormPanel().apply {
      addRow("Owner", owner)
      addRow("Repo", repo)
      addRow("Issue #", number)
      addRow("Labels", labels)
    }
    add(form, BorderLayout.CENTER)
    add(output, BorderLayout.SOUTH)

    submit.addActionListener { doSet() }
  }

  private fun doSet() {
    val o = owner.text.trim()
    val r = repo.text.trim()
    val n = number.text.trim()
    if (o.isEmpty() || r.isEmpty() || n.isEmpty()) {
      output.text = "Owner, Repo, Issue # required."
      return
    }
    val labelList = labels.text.split(",").mapNotNull { it.trim().ifEmpty { null } }
    val json = "{\"labels\":${labelList.joinToString(prefix = "[", postfix = "]") { q(it) }}}"
    if (dryRun.isSelected) {
      output.text = "PUT /repos/$o/$r/issues/$n/labels\n$json"
      return
    }
    output.text = "Updating…"
    val res = gh.runApiJson("PUT", "/repos/$o/$r/issues/$n/labels", json)
    SwingUtilities.invokeLater {
      output.text = if (res.ok) res.out.ifBlank { "Updated." } else "ERROR: ${res.err}\n${res.out}"
    }
  }

  private fun q(s: String) = "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}
