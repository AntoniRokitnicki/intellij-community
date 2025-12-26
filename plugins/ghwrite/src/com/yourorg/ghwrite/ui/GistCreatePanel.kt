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

internal class GistCreatePanel(project: Project) : JPanel(BorderLayout()) {
  private val gh = GhClient(project)

  private val description = textField(32)
  private val publicCheck = JBCheckBox("Public").apply { isSelected = true }
  private val filename = textField(24)
  private val content = textArea(10, 60)

  private val submit = JButton("Create Gist")
  private val dryRun = JBCheckBox("Dry Run").apply { isSelected = true }
  private val open = JBCheckBox("Open in browser").apply { isSelected = true }
  private val output = JTextArea().apply { isEditable = false }

  init {
    val bar = JToolBar().apply { isFloatable = false }
    bar.add(submit); bar.add(dryRun); bar.add(open)
    add(bar, BorderLayout.NORTH)

    val form = FormPanel().apply {
      addRow("Description", description)
      addRow("", publicCheck)
      addRow("Filename", filename)
      addGrow(content)
    }
    add(form, BorderLayout.CENTER)
    add(output, BorderLayout.SOUTH)

    submit.addActionListener { doCreate() }
  }

  private fun doCreate() {
    val desc = description.text.trim()
    val file = filename.text.trim()
    val cont = content.text
    if (file.isEmpty() || cont.isEmpty()) {
      output.text = "Filename and Content required."
      return
    }
    val json = buildString {
      append("{")
      append("\"description\":").append(q(desc)).append(',')
      append("\"public\":").append(if (publicCheck.isSelected) "true" else "false").append(',')
      append("\"files\":{")
      append(q(file)).append(':')
      append("{\"content\":").append(q(cont)).append("}")
      append("}}");
    }
    if (dryRun.isSelected) {
      output.text = "POST /gists\n$json"
      return
    }
    output.text = "Creating…"
    val res = gh.runApiJson("POST", "/gists", json)
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
