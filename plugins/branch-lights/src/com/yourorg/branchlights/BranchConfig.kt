package com.yourorg.branchlights

data class BranchInfo(
  val name: String,
  val shortLabel: String,
  val miniDescription: String
)

object BranchConfig {
  // Hard coded five branches with short labels and mini descriptions
  val branches: List<BranchInfo> = listOf(
    BranchInfo("branchA", "A", "Main integration"),
    BranchInfo("branchB", "B", "Release 25.1"),
    BranchInfo("branchC", "C", "Release 25.2"),
    BranchInfo("branchD", "D", "Hotfix line"),
    BranchInfo("branchE", "E", "Experiment")
  )
}
