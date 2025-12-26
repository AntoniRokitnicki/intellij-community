package com.yourorg.ghwrite.ui

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.yourorg.ghwrite.core.GhClient
import com.yourorg.ghwrite.ui.common.FormPanel
import com.yourorg.ghwrite.ui.common.textArea
import com.yourorg.ghwrite.ui.common.textField
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JPanel
import javax.swing.JToolBar
import javax.swing.JTextArea
import javax.swing.SwingUtilities

internal class PrMergePanel(project: Project) : JPanel(BorderLayout()) {
  private val gh = GhClient(project)

  private val owner = textField(12)
  private val repo = textField(16)
  private val number = textField(8)
  private val method = JComboBox(arrayOf("merge", "squash", "rebase"))
  private val title = textField(32)
  private val message = textArea(6, 60)

  private val submit = JButton("Merge PR")
  private val dryRun = JBCheckBox("Dry Run").apply { isSelected = true }
  private val output = JTextArea().apply { isEditable = false }

  init {
    val bar = JToolBar().apply { isFloatable = false }
    bar.add(submit); bar.add(dryRun)
    add(bar, BorderLayout.NORTH)

    val form = FormPanel().apply {
      addRow("Owner", owner)
      addRow("Repo", repo)
      addRow("PR #", number)
      addRow("Method", method)
      addRow("Title", title)
      addGrow(message)
    }
    add(form, BorderLayout.CENTER)
    add(output, BorderLayout.SOUTH)

    submit.addActionListener { doMerge() }
  }

  private fun doMerge() {
    val o = owner.text.trim()
    val r = repo.text.trim()
    val n = number.text.trim()
    if (o.isEmpty() || r.isEmpty() || n.isEmpty()) {
      output.text = "Owner, Repo, PR # required."
      return
    }
    val m = method.selectedItem as String
    val t = title.text.trim()
    val msg = message.text
    val json = buildString {
      append("{")
      append("\"merge_method\":").append(q(m)).append(',')
      if (t.isNotEmpty()) append("\"commit_title\":").append(q(t)).append(',')
      if (msg.isNotEmpty()) append("\"commit_message\":").append(q(msg)).append(',')
      deleteCharAt(length - 1)
      append("}")
    }
    if (dryRun.isSelected) {
      output.text = "PUT /repos/$o/$r/pulls/$n/merge\n$json"
      return
    }
    output.text = "Merging…"
    val res = gh.runApiJson("PUT", "/repos/$o/$r/pulls/$n/merge", json)
    SwingUtilities.invokeLater {
      output.text = if (res.ok) res.out.ifBlank { "Merged." } else "ERROR: ${res.err}\n${res.out}"
    }
  }

  private fun q(s: String) = "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}
