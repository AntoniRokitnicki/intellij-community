/*
 * Copyright 2010-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.search;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.searches.AnnotatedMembersSearch;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.plugin.PluginTestCaseBase;
import org.jetbrains.jet.testing.InTextDirectivesUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JUnitMembersSearcherTest extends AbstractSearcherTest {
    private static final LightProjectDescriptor junitProjectDescriptor = new LightProjectDescriptor() {
        @Override
        public ModuleType getModuleType() {
            return StdModuleTypes.JAVA;
        }

        @Override
        public Sdk getSdk() {
            return PluginTestCaseBase.jdkFromIdeaHome();
        }

        @Override
        public void configureModule(Module module, ModifiableRootModel model, ContentEntry contentEntry) {
            Library library = model.getModuleLibraryTable().createLibrary("junit");
            Library.ModifiableModel modifiableModel = library.getModifiableModel();
            modifiableModel.addRoot(VfsUtil.getUrlForLibraryRoot(
                    new File(PathManager.getHomePath().replace(File.separatorChar, '/') + "/lib/junit-4.10.jar")),
                    OrderRootType.CLASSES);
            modifiableModel.commit();
        }
    };

    public void testJunit3() throws IOException {
        myFixture.configureByFile(getFileName());
        List<String> directives = InTextDirectivesUtils.findListWithPrefix("// CLASS: ", FileUtil.loadFile(new File(getPathToFile())));
        assertFalse("Specify CLASS directive in test file", directives.isEmpty());
        String superClassName = directives.get(0);
        PsiClass psiClass = getPsiClass(superClassName);
        checkResult(ClassInheritorsSearch.search(psiClass, getProjectScope(), false));
    }

    public void testJunit4() throws IOException {
        myFixture.configureByFile(getFileName());
        List<String> directives = InTextDirectivesUtils.findListWithPrefix("// ANNOTATION: ", FileUtil.loadFile(new File(getPathToFile())));
        assertFalse("Specify ANNOTATION directive in test file", directives.isEmpty());
        String annotationClassName = directives.get(0);
        PsiClass psiClass = getPsiClass(annotationClassName);
        checkResult(AnnotatedMembersSearch.search(psiClass, getProjectScope()));
    }

    @Override
    protected String getTestDataPath() {
        return new File(PluginTestCaseBase.getTestDataPathBase(), "/search/junit").getPath() + File.separator;
    }

    @NotNull
    @Override
    protected LightProjectDescriptor getProjectDescriptor() {

        return junitProjectDescriptor;
    }
}
