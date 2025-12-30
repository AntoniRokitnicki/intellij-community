package com.intellij.methods;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Utility building URLs and providing generated content for {@link MethodsVirtualFileSystem}.
 */
public final class MethodsUrlService {
  private static final int MAX_ENTRIES = 500;

  private MethodsUrlService() {
  }

  public static @NotNull String buildByFileUrl(@NotNull Project project, @NotNull VirtualFile file) {
    String path;
    VirtualFile root = ProjectLevelVcsManager.getInstance(project).getVcsRootFor(file);
    if (root != null) {
      String rel = com.intellij.openapi.vfs.VfsUtilCore.getRelativePath(file, root, '/');
      path = "by-file/" + (rel == null ? file.getName() : rel);
    } else {
      path = "by-file/abs/" + file.getPath();
    }
    updateByFile(project, file, path);
    return MethodsVirtualFileSystem.PROTOCOL + "://" + path;
  }

  public static @NotNull String buildByScopeUrl(@NotNull Project project, @NotNull String scopeId) {
    String path = "by-scope/" + scopeId;
    updateByScope(project, scopeId, path);
    return MethodsVirtualFileSystem.PROTOCOL + "://" + path;
  }

  public static VirtualFile resolve(@NotNull String url) {
    return VirtualFileManager.getInstance().findFileByUrl(url);
  }

  private static void updateByFile(Project project, VirtualFile file, String path) {
    DumbService dumb = DumbService.getInstance(project);
    if (dumb.isDumb()) {
      String content = "Indexing in progress";
      MethodsFileRegistry.update(path, content.getBytes(StandardCharsets.UTF_8), System.currentTimeMillis());
      dumb.runWhenSmart(() -> updateByFile(project, file, path));
      return;
    }
    List<String> names = ReadAction.compute(() -> collectMethodNames(project, file));
    String content = String.join("\n", names);
    MethodsFileRegistry.update(path, content.getBytes(StandardCharsets.UTF_8), System.currentTimeMillis());
  }

  private static void updateByScope(Project project, String scopeId, String path) {
    DumbService dumb = DumbService.getInstance(project);
    if (dumb.isDumb()) {
      String content = "Indexing in progress";
      MethodsFileRegistry.update(path, content.getBytes(StandardCharsets.UTF_8), System.currentTimeMillis());
      dumb.runWhenSmart(() -> updateByScope(project, scopeId, path));
      return;
    }
    List<String> lines = ReadAction.compute(() -> collectMethodsInScope(project));
    MethodsFileRegistry.update(path, String.join("\n", lines).getBytes(StandardCharsets.UTF_8), System.currentTimeMillis());
  }

  private static List<String> collectMethodNames(Project project, VirtualFile file) {
    List<String> names = new ArrayList<>();
    PsiFile psi = PsiManager.getInstance(project).findFile(file);
    if (psi == null) return names;
    psi.accept(new JavaRecursiveElementVisitor() {
      @Override
      public void visitMethod(PsiMethod method) {
        names.add(method.getName());
      }
    });
    try {
      Class<?> ktFunction = Class.forName("org.jetbrains.kotlin.psi.KtNamedFunction");
      Class<?> ktVisitor = Class.forName("org.jetbrains.kotlin.psi.KtTreeVisitorVoid");
      Object visitor = java.lang.reflect.Proxy.newProxyInstance(
        ktVisitor.getClassLoader(), new Class[]{ktVisitor}, (proxy, m, args) -> {
          if ("visitNamedFunction".equals(m.getName()) && args != null && args.length > 1 && ktFunction.isInstance(args[0])) {
            Object fn = args[0];
            Object name = fn.getClass().getMethod("getName").invoke(fn);
            if (name instanceof String) names.add((String) name);
          }
          if (args != null && args.length > 0 && args[0] instanceof PsiElement) {
            ((PsiElement)args[0]).acceptChildren((PsiElementVisitor)proxy);
          }
          return null;
        });
      psi.accept((PsiElementVisitor)visitor);
    } catch (ClassNotFoundException ignored) {
      // Kotlin plugin absent
    } catch (Exception ignored) {
    }
    return names;
  }

  private static List<String> collectMethodsInScope(Project project) {
    PsiShortNamesCache cache = PsiShortNamesCache.getInstance(project);
    GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
    List<String> lines = new ArrayList<>();
    AtomicInteger count = new AtomicInteger();
    for (String name : cache.getAllMethodNames()) {
      for (PsiMethod method : cache.getMethodsByName(name, scope)) {
        PsiClass cls = method.getContainingClass();
        String cname = cls != null ? cls.getQualifiedName() : "";
        lines.add(name + " " + cname);
        if (count.incrementAndGet() >= MAX_ENTRIES) {
          return lines;
        }
      }
    }
    return lines;
  }
}
