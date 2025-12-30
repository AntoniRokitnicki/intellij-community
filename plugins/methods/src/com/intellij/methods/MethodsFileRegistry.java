package com.intellij.methods;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory registry of {@link MethodsVirtualFile} instances.
 */
public final class MethodsFileRegistry {
  private static final ConcurrentHashMap<String, MethodsVirtualFile> files = new ConcurrentHashMap<>();

  private MethodsFileRegistry() {}

  public static @NotNull MethodsVirtualFile getOrCreate(@NotNull String path) {
    return files.computeIfAbsent(path, MethodsVirtualFile::new);
  }

  public static void update(@NotNull String path, byte[] data, long stamp) {
    MethodsVirtualFile file = getOrCreate(path);
    file.update(data, stamp);
  }
}
