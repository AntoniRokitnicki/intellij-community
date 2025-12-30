package com.intellij.methods;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.scope.packageSet.CustomScopesProviderEx;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.psi.search.scope.packageSet.PackageSetBase;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Provides scopes based on presence of method declarations in files.
 */
public class MethodsScopeProvider extends CustomScopesProviderEx implements DumbAware {
  private static final String ANY_NAME = "FilesWithAnyMethod";
  private static final String REGEX_NAME = "FilesWithMethodNameRegex";
  private static final Pattern REGEX = Pattern.compile(".*");

  @Override
  public @NotNull List<NamedScope> getCustomScopes() {
    NamedScope any = new NamedScope(ANY_NAME, new AnyMethodPackageSet());
    NamedScope regex = new NamedScope(REGEX_NAME, new RegexMethodPackageSet(REGEX));
    return Arrays.asList(any, regex);
  }

  private static List<String> getMethodNames(@NotNull PsiFile file) {
    return CachedValuesManager.getCachedValue(file, () -> {
      List<String> names = new ArrayList<>();
      file.accept(new JavaRecursiveElementVisitor() {
        @Override
        public void visitMethod(PsiMethod method) {
          names.add(method.getName());
        }
      });
      try {
        Class<?> ktFunction = Class.forName("org.jetbrains.kotlin.psi.KtNamedFunction");
        file.accept(new PsiRecursiveElementVisitor() {
          @Override
          public void visitElement(@NotNull PsiElement element) {
            if (ktFunction.isInstance(element)) {
              try {
                Object name = element.getClass().getMethod("getName").invoke(element);
                if (name instanceof String) names.add((String)name);
              } catch (Exception ignored) {}
            }
            super.visitElement(element);
          }
        });
      } catch (ClassNotFoundException ignored) {
      }
      return CachedValueProvider.Result.create(names, file, PsiModificationTracker.MODIFICATION_COUNT);
    });
  }

  private static class AnyMethodPackageSet extends PackageSetBase {
    AnyMethodPackageSet() {
      super("any-method");
    }

    @Override
    public boolean contains(@NotNull VirtualFile file, @NotNull Project project, @NotNull FileIndexFacade facade) {
      if (DumbService.isDumb(project)) return false;
      PsiFile psi = PsiManager.getInstance(project).findFile(file);
      return psi != null && !getMethodNames(psi).isEmpty();
    }

    @Override
    public @Nullable PackageSetBase createCopy() {
      return new AnyMethodPackageSet();
    }
  }

  private static class RegexMethodPackageSet extends PackageSetBase {
    private final Pattern pattern;

    RegexMethodPackageSet(Pattern pattern) {
      super("regex-method");
      this.pattern = pattern;
    }

    @Override
    public boolean contains(@NotNull VirtualFile file, @NotNull Project project, @NotNull FileIndexFacade facade) {
      if (DumbService.isDumb(project)) return false;
      PsiFile psi = PsiManager.getInstance(project).findFile(file);
      if (psi == null) return false;
      for (String name : getMethodNames(psi)) {
        if (pattern.matcher(name).matches()) return true;
      }
      return false;
    }

    @Override
    public @Nullable PackageSetBase createCopy() {
      return new RegexMethodPackageSet(pattern);
    }
  }
}
