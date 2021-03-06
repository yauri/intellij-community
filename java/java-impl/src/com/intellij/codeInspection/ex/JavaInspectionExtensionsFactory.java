/*
 * Copyright 2000-2009 JetBrains s.r.o.
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

/*
 * User: anna
 * Date: 20-Dec-2007
 */
package com.intellij.codeInspection.ex;

import com.intellij.codeInspection.HTMLComposer;
import com.intellij.codeInspection.SuppressManager;
import com.intellij.codeInspection.lang.GlobalInspectionContextExtension;
import com.intellij.codeInspection.lang.HTMLComposerExtension;
import com.intellij.codeInspection.lang.InspectionExtensionsFactory;
import com.intellij.codeInspection.lang.RefManagerExtension;
import com.intellij.codeInspection.reference.RefJavaManagerImpl;
import com.intellij.codeInspection.reference.RefManager;
import com.intellij.codeInspection.reference.RefManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public class JavaInspectionExtensionsFactory extends InspectionExtensionsFactory {

  public GlobalInspectionContextExtension createGlobalInspectionContextExtension() {
    return new GlobalJavaInspectionContextImpl();
  }

  public RefManagerExtension createRefManagerExtension(final RefManager refManager) {
    return new RefJavaManagerImpl((RefManagerImpl)refManager);
  }

  public HTMLComposerExtension createHTMLComposerExtension(final HTMLComposer composer) {
    return new HTMLJavaHTMLComposerImpl((HTMLComposerImpl)composer);
  }

  public boolean isToCheckMember(final PsiElement element, final String id) {
    return SuppressManager.getInstance().getElementToolSuppressedIn(element, id) == null;
  }

  @Nullable
  public String getSuppressedInspectionIdsIn(final PsiElement element) {
    return SuppressManager.getInstance().getSuppressedInspectionIdsIn(element);
  }

  public boolean isProjectConfiguredToRunInspections(final Project project, final boolean online) {
    return GlobalJavaInspectionContextImpl.isInspectionsEnabled(online, project);
  }
}