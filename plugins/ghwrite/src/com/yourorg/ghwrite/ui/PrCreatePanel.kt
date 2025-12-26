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
import javax.swing.SwingUtilities

internal class PrCreatePanel(private val project: Project) : JPanel(BorderLayout()) {
  private val gh = GhClient(project)

  private val owner = textField(12)
  private val repo = textField(16)
  private val head = textField(16).apply { toolTipText = "branch or user:branch" }
  private val base = textField(16).apply { toolTipText = "target branch, e.g. main" }
  private val title = textField(32)
  private val body = textArea(10, 60)
  private val draft = JBCheckBox("Draft PR")
  private val maintainer = JBCheckBox("Allow edits by maintainers").apply { isSelected = true }

  private val submit = JButton("Create PR")
  private val dryRun = JBCheckBox("Dry Run").apply { isSelected = true }
  private val open = JBCheckBox("Open in browser").apply { isSelected = true }
  private val output = javax.swing.JTextArea().apply { isEditable = false }

  init {
    val bar = JToolBar().apply { isFloatable = false }
    bar.add(submit); bar.add(dryRun); bar.add(open)
    add(bar, BorderLayout.NORTH)

    val form = FormPanel().apply {
      addRow("Owner", owner)
      addRow("Repo", repo)
      addRow("Head", head)
      addRow("Base", base)
      addRow("Title", title)
      addGrow(body)
      addRow("", draft)
      addRow("", maintainer)
    }
    add(form, BorderLayout.CENTER)
    add(output, BorderLayout.SOUTH)

    submit.addActionListener { doCreate() }
  }

  private fun doCreate() {
    val o = owner.text.trim()
    val r = repo.text.trim()
    val h = head.text.trim()
    val b = base.text.trim()
    val t = title.text.trim()
    val desc = body.text
    if (o.isEmpty() || r.isEmpty() || h.isEmpty() || b.isEmpty() || t.isEmpty()) {
      output.text = "Owner, Repo, Head, Base, Title required."
      return
    }
    val json = buildString {
      append("{")
      append("\"title\":").append(q(t)).append(',')
      append("\"head\":").append(q(h)).append(',')
      append("\"base\":").append(q(b)).append(',')
      append("\"body\":").append(q(desc)).append(',')
      append("\"draft\":").append(if (draft.isSelected) "true" else "false").append(',')
      append("\"maintainer_can_modify\":").append(if (maintainer.isSelected) "true" else "false")
      append("}")
    }

    if (dryRun.isSelected) {
      output.text = "POST /repos/$o/$r/pulls\n$json"
      return
    }

    output.text = "Creating…"
    val res = gh.runApiJson("POST", "/repos/$o/$r/pulls", json)
    SwingUtilities.invokeLater {
      output.text = if (res.ok) res.out.ifBlank { "Created." } else "ERROR: ${res.err}\n${res.out}"
      if (res.ok && open.isSelected) {
        val urlKey = "\"html_url\":\""
        val start = res.out.indexOf(urlKey)
        if (start >= 0) {
          val end = res.out.indexOf('\"', start + urlKey.length)
          if (end > start) BrowserUtil.browse(res.out.substring(start + urlKey.length, end))
        }
      }
    }
  }

  private fun q(s: String) = "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}
