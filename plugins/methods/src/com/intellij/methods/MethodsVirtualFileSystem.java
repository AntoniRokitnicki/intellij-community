package com.intellij.methods;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Virtual file system providing read-only access to generated method listings.
 */
public class MethodsVirtualFileSystem extends VirtualFileSystem {
  public static final String PROTOCOL = "methods";
  private static final MethodsVirtualFileSystem INSTANCE = new MethodsVirtualFileSystem();

  private static volatile boolean registered;

  private MethodsVirtualFileSystem() {
  }

  public static MethodsVirtualFileSystem getInstance() {
    if (!registered) {
      register();
    }
    return INSTANCE;
  }

  private static void register() {
    registered = true;
    VirtualFileManager manager = VirtualFileManager.getInstance();
    try {
      Field field = manager.getClass().getDeclaredField("myCollector");
      field.setAccessible(true);
      Object collector = field.get(manager);
      collector.getClass().getMethod("addExplicitExtension", String.class, Object.class)
        .invoke(collector, PROTOCOL, INSTANCE);
    } catch (Exception ignored) {
      // best effort; if registration fails the VFS will not function
    }
  }

  @Override
  public @NotNull String getProtocol() {
    return PROTOCOL;
  }

  @Override
  public @Nullable VirtualFile findFileByPath(@NotNull String path) {
    return MethodsFileRegistry.getOrCreate(path);
  }

  @Override
  public void refresh(boolean asynchronous) {
  }

  @Override
  public @Nullable VirtualFile refreshAndFindFileByPath(@NotNull String path) {
    return findFileByPath(path);
  }

  @Override
  public void addVirtualFileListener(@NotNull com.intellij.openapi.vfs.VirtualFileListener listener) {
  }

  @Override
  public void removeVirtualFileListener(@NotNull com.intellij.openapi.vfs.VirtualFileListener listener) {
  }

  @Override
  protected void deleteFile(Object requestor, @NotNull VirtualFile vFile) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void moveFile(Object requestor, @NotNull VirtualFile vFile, @NotNull VirtualFile newParent) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void renameFile(Object requestor, @NotNull VirtualFile vFile, @NotNull String newName) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected @NotNull VirtualFile createChildFile(Object requestor, @NotNull VirtualFile parent, @NotNull String file) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected @NotNull VirtualFile createChildDirectory(Object requestor, @NotNull VirtualFile parent, @NotNull String dir) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected @NotNull VirtualFile copyFile(Object requestor, @NotNull VirtualFile vFile, @NotNull VirtualFile newParent, @NotNull String copyName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }
}
