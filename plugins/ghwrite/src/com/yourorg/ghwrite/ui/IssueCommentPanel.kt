package com.yourorg.ghwrite.ui

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.yourorg.ghwrite.core.GhClient
import com.yourorg.ghwrite.ui.common.FormPanel
import com.yourorg.ghwrite.ui.common.textArea
import com.yourorg.ghwrite.ui.common.textField
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JToolBar
import javax.swing.JTextArea
import javax.swing.SwingUtilities

internal class IssueCommentPanel(project: Project) : JPanel(BorderLayout()) {
  private val gh = GhClient(project)

  private val owner = textField(12)
  private val repo = textField(16)
  private val number = textField(8)
  private val body = textArea(8, 60)

  private val submit = JButton("Add Comment")
  private val dryRun = JBCheckBox("Dry Run").apply { isSelected = true }
  private val open = JBCheckBox("Open in browser").apply { isSelected = true }
  private val output = JTextArea().apply { isEditable = false }

  init {
    val bar = JToolBar().apply { isFloatable = false }
    bar.add(submit); bar.add(dryRun); bar.add(open)
    add(bar, BorderLayout.NORTH)

    val form = FormPanel().apply {
      addRow("Owner", owner)
      addRow("Repo", repo)
      addRow("Issue #", number)
      addGrow(body)
    }
    add(form, BorderLayout.CENTER)
    add(output, BorderLayout.SOUTH)

    submit.addActionListener { doComment() }
  }

  private fun doComment() {
    val o = owner.text.trim()
    val r = repo.text.trim()
    val n = number.text.trim()
    val b = body.text
    if (o.isEmpty() || r.isEmpty() || n.isEmpty() || b.isEmpty()) {
      output.text = "Owner, Repo, Issue #, Comment required."
      return
    }
    val json = "{\"body\":${q(b)}}"
    if (dryRun.isSelected) {
      output.text = "POST /repos/$o/$r/issues/$n/comments\n$json"
      return
    }
    output.text = "Creating…"
    val res = gh.runApiJson("POST", "/repos/$o/$r/issues/$n/comments", json)
    SwingUtilities.invokeLater {
      output.text = if (res.ok) res.out.ifBlank { "Created." } else "ERROR: ${res.err}\n${res.out}"
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
