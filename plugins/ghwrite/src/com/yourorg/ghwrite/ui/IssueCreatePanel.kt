package com.yourorg.ghwrite.ui

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextArea
import com.yourorg.ghwrite.core.GhClient
import com.yourorg.ghwrite.ui.common.FormPanel
import com.yourorg.ghwrite.ui.common.textArea
import com.yourorg.ghwrite.ui.common.textField
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JToolBar
import javax.swing.SwingUtilities

internal class IssueCreatePanel(private val project: Project) : JPanel(BorderLayout()) {
  private val gh = GhClient(project)

  private val owner = textField(12).apply { toolTipText = "owner" }
  private val repo = textField(16).apply { toolTipText = "repo" }
  private val title = textField(32)
  private val body: JBTextArea = textArea(10, 60)
  private val labels = textField(24).apply { toolTipText = "comma separated (optional)" }
  private val assignees = textField(24).apply { toolTipText = "comma separated (optional)" }
  private val draft = JBCheckBox("Create as draft issue? (projects legacy)").apply { isEnabled = false }

  private val submit = JButton("Create Issue")
  private val dryRun = JBCheckBox("Dry Run (show JSON only)").apply { isSelected = true }
  private val openInBrowser = JBCheckBox("Open in browser after create").apply { isSelected = true }
  private val output = JBTextArea().apply { isEditable = false }

  init {
    val bar = JToolBar().apply { isFloatable = false }
    bar.add(submit); bar.add(dryRun); bar.add(openInBrowser)
    add(bar, BorderLayout.NORTH)

    val form = FormPanel().apply {
      addRow("Owner", owner)
      addRow("Repo", repo)
      addRow("Title", title)
      addGrow(body)
      addRow("Labels", labels)
      addRow("Assignees", assignees)
    }
    add(form, BorderLayout.CENTER)
    add(output, BorderLayout.SOUTH)

    submit.addActionListener { doCreate() }
  }

  private fun doCreate() {
    val o = owner.text.trim()
    val r = repo.text.trim()
    val t = title.text.trim()
    val b = body.text
    if (o.isEmpty() || r.isEmpty() || t.isEmpty()) {
      output.text = "Owner, Repo, Title are required."
      return
    }
    val labelList = labels.text.split(",").mapNotNull { it.trim().ifEmpty { null } }
    val assignList = assignees.text.split(",").mapNotNull { it.trim().ifEmpty { null } }

    val json = buildString {
      append("{")
      append("\"title\":").append(quote(t)).append(',')
      append("\"body\":").append(quote(b)).append(',')
      if (labelList.isNotEmpty()) append("\"labels\":").append(toJsonArray(labelList)).append(',')
      if (assignList.isNotEmpty()) append("\"assignees\":").append(toJsonArray(assignList)).append(',')
      append("\"state\":\"open\"")
      append("}")
    }

    if (dryRun.isSelected) {
      output.text = "POST /repos/$o/$r/issues\n$json"
      return
    }

    output.text = "Creating…"
    val res = gh.runApiJson("POST", "/repos/$o/$r/issues", json)
    SwingUtilities.invokeLater {
      output.text = if (res.ok) res.out.ifBlank { "Created." } else "ERROR: ${res.err}\n${res.out}"
      if (res.ok && openInBrowser.isSelected) {
        val url = "\"html_url\":\""
        val idx = res.out.indexOf(url)
        if (idx >= 0) {
          val end = res.out.indexOf('\"', idx + url.length)
          if (end > idx) BrowserUtil.browse(res.out.substring(idx + url.length, end))
        }
      }
    }
  }

  private fun quote(s: String) = "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
  private fun toJsonArray(list: List<String>) = list.joinToString(prefix = "[", postfix = "]") { quote(it) }
}
