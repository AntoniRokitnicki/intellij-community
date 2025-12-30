package com.intellij.methods;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Lightweight in-memory {@link VirtualFile} used by {@link MethodsVirtualFileSystem}.
 * Content is supplied externally via {@link #update(byte[], long)}.
 */
public class MethodsVirtualFile extends VirtualFile {
  private final String path;
  private volatile byte[] content = new byte[0];
  private volatile long timestamp;

  MethodsVirtualFile(@NotNull String path) {
    this.path = path;
    this.timestamp = System.currentTimeMillis();
  }

  void update(@NotNull byte[] data, long ts) {
    content = data;
    timestamp = ts;
  }

  @Override
  public @NotNull String getName() {
    int idx = path.lastIndexOf('/');
    return idx >= 0 ? path.substring(idx + 1) : path;
  }

  @Override
  public @NotNull VirtualFileSystem getFileSystem() {
    return MethodsVirtualFileSystem.getInstance();
  }

  @Override
  public @NotNull String getPath() {
    return MethodsVirtualFileSystem.PROTOCOL + "://" + path;
  }

  @Override
  public boolean isWritable() {
    return false;
  }

  @Override
  public boolean isDirectory() {
    return false;
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public @Nullable VirtualFile getParent() {
    return null;
  }

  @Override
  public VirtualFile[] getChildren() {
    return EMPTY_ARRAY;
  }

  @Override
  public @NotNull InputStream getInputStream() throws IOException {
    return new ByteArrayInputStream(content);
  }

  @Override
  public @NotNull byte[] contentsToByteArray() throws IOException {
    return content;
  }

  @Override
  public long getTimeStamp() {
    return timestamp;
  }

  @Override
  public long getLength() {
    return content.length;
  }

  @Override
  public void refresh(boolean asynchronous, boolean recursive, @Nullable Runnable postRunnable) {
  }

  @Override
  public OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp) {
    throw new UnsupportedOperationException("Read-only");
  }

  @Override
  public long getModificationStamp() {
    return timestamp;
  }
}
