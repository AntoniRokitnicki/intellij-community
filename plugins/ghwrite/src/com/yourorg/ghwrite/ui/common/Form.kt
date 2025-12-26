package com.yourorg.ghwrite.ui.common

import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

internal class LabeledField(label: String, val field: JComponent) : JPanel(BorderLayout()) {
  init {
    val l = JLabel(label)
    l.border = JBUI.Borders.empty(2, 0, 2, 8)
    add(l, BorderLayout.WEST)
    add(field, BorderLayout.CENTER)
  }
}

internal fun textField(columns: Int = 24) = JBTextField().apply { this.columns = columns }
internal fun textArea(rows: Int = 8, cols: Int = 40) = JBTextArea(rows, cols).apply {
  lineWrap = true
  wrapStyleWord = true
}

internal class FormPanel : JPanel(GridBagLayout()) {
  private var row = 0
  private fun gbc(y: Int, weightY: Double = 0.0): GridBagConstraints =
    GridBagConstraints().apply {
      gridx = 0
      gridy = y
      weightx = 1.0
      this.weighty = weightY
      fill = GridBagConstraints.HORIZONTAL
      anchor = GridBagConstraints.NORTH
      insets = JBUI.insets(4, 8, 4, 8)
    }
  fun addRow(label: String, comp: JComponent) {
    add(LabeledField(label, comp), gbc(row++))
  }
  fun addGrow(comp: JComponent) {
    add(comp, gbc(row++, weightY = 1.0).apply { fill = GridBagConstraints.BOTH })
  }
}
