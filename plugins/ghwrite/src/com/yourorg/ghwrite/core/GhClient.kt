package com.yourorg.ghwrite.core

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

internal class GhClient(private val project: Project) {
  private val log = Logger.getInstance(GhClient::class.java)

  data class CmdResult(val ok: Boolean, val out: String, val err: String, val exit: Int)
  private val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  fun available(): Boolean = runCmd(listOf("gh", "--version"), 5_000).ok

  fun run(args: List<String>, timeoutMs: Long = 60_000): CmdResult = runCmd(args, timeoutMs)

  fun runApiJson(method: String, endpoint: String, json: String, extraHeaders: List<String> = emptyList(), timeoutMs: Long = 60_000): CmdResult {
    val cmd = mutableListOf("gh", "api", endpoint, "--method", method, "-H", "Accept: application/vnd.github+json")
    extraHeaders.forEach { cmd += listOf("-H", it) }
    cmd += listOf("-F", "data=@-")
    return runCmd(cmd, timeoutMs, stdin = json)
  }

  private fun runCmd(args: List<String>, timeoutMs: Long, stdin: String? = null): CmdResult {
    val indicator = ProgressManager.getInstance().progressIndicator
    return try {
      val pb = ProcessBuilder(args)
      val proc = pb.start()
      if (stdin != null) {
        proc.outputStream.use { it.write(stdin.toByteArray(StandardCharsets.UTF_8)) }
      }
      var waited = 0L
      while (!proc.waitFor(50, TimeUnit.MILLISECONDS)) {
        if (indicator?.isCanceled == true) {
          proc.destroyForcibly()
          return CmdResult(false, "", "Canceled", -1)
        }
        waited += 50
        if (waited > timeoutMs) {
          proc.destroyForcibly()
          return CmdResult(false, "", "Timeout", -1)
        }
      }
      val out = proc.inputStream.readBytes().toString(StandardCharsets.UTF_8)
      val err = proc.errorStream.readBytes().toString(StandardCharsets.UTF_8)
      CmdResult(proc.exitValue() == 0, out, err, proc.exitValue())
    } catch (t: Throwable) {
      log.warn("gh failed: ${t.message}", t)
      CmdResult(false, "", t.message ?: "error", -1)
    }
  }
}
