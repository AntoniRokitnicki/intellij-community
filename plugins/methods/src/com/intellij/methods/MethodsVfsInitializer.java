package com.intellij.methods;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * Ensures that {@link MethodsVirtualFileSystem} is loaded during startup.
 */
public class MethodsVfsInitializer implements StartupActivity, DumbAware {
  @Override
  public void runActivity(@NotNull Project project) {
    MethodsVirtualFileSystem.getInstance();
  }
}
