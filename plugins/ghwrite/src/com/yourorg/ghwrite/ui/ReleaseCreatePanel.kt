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

internal class ReleaseCreatePanel(project: Project) : JPanel(BorderLayout()) {
  private val gh = GhClient(project)

  private val owner = textField(12)
  private val repo = textField(16)
  private val tag = textField(16)
  private val target = textField(16)
  private val name = textField(32)
  private val body = textArea(8, 60)
  private val draft = JBCheckBox("Draft")
  private val prerelease = JBCheckBox("Pre-release")

  private val submit = JButton("Create Release")
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
      addRow("Tag", tag)
      addRow("Target", target)
      addRow("Name", name)
      addGrow(body)
      addRow("", draft)
      addRow("", prerelease)
    }
    add(form, BorderLayout.CENTER)
    add(output, BorderLayout.SOUTH)

    submit.addActionListener { doCreate() }
  }

  private fun doCreate() {
    val o = owner.text.trim()
    val r = repo.text.trim()
    val tg = tag.text.trim()
    if (o.isEmpty() || r.isEmpty() || tg.isEmpty()) {
      output.text = "Owner, Repo, Tag required."
      return
    }
    val targetCommit = target.text.trim()
    val n = name.text.trim()
    val b = body.text
    val json = buildString {
      append("{")
      append("\"tag_name\":").append(q(tg)).append(',')
      if (targetCommit.isNotEmpty()) append("\"target_commitish\":").append(q(targetCommit)).append(',')
      if (n.isNotEmpty()) append("\"name\":").append(q(n)).append(',')
      if (b.isNotEmpty()) append("\"body\":").append(q(b)).append(',')
      append("\"draft\":").append(if (draft.isSelected) "true" else "false").append(',')
      append("\"prerelease\":").append(if (prerelease.isSelected) "true" else "false")
      append("}")
    }
    if (dryRun.isSelected) {
      output.text = "POST /repos/$o/$r/releases\n$json"
      return
    }
    output.text = "Creating…"
    val res = gh.runApiJson("POST", "/repos/$o/$r/releases", json)
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
