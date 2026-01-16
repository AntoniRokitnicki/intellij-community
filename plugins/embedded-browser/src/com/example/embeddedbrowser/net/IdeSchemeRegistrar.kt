package com.example.embeddedbrowser.net

import com.intellij.openapi.components.Service
import com.intellij.ui.jcef.JBCefApp
import org.cef.callback.CefCallback
import org.cef.callback.CefResourceHandler
import org.cef.handler.CefSchemeHandlerFactory
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import java.nio.charset.StandardCharsets

@Service
class IdeSchemeRegistrar {
  init {
    if (JBCefApp.isSupported()) {
      val app = JBCefApp.getInstance().cefApp
      app.registerSchemeHandlerFactory("ide", "local", CefSchemeHandlerFactory { _, _, url ->
        object : CefResourceHandler {
          private val data: ByteArray = """
            <html><body style="font-family:sans-serif">
              <h3>Hello from IDE</h3>
              <p>This is served by <code>ide://local/hello</code></p>
              <script>if (window.ideEcho) ideEcho("from ide:// page")</script>
            </body></html>
          """.trimIndent().toByteArray(StandardCharsets.UTF_8)

          override fun processRequest(request: CefRequest?, callback: CefCallback?): Boolean {
            callback?.Continue()
            return true
          }

          override fun getResponseHeaders(
            response: CefResponse?,
            responseLength: IntArray?,
            redirectUrl: StringBuffer?
          ) {
            response?.mimeType = "text/html"
            response?.status = 200
            responseLength?.set(0, data.size)
          }

          override fun readResponse(
            dataOut: java.io.OutputStream?,
            bytesToRead: Int,
            bytesRead: IntArray?,
            callback: CefCallback?
          ): Boolean {
            dataOut?.write(data)
            bytesRead?.set(0, data.size)
            return false
          }

          override fun cancel() {}
        }
      })
    }
  }
}
