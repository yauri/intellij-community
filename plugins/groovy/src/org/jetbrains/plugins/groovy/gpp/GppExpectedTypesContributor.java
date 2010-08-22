package org.jetbrains.plugins.groovy.gpp;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.api.GroovyResolveResult;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.GrListOrMap;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrNamedArgument;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.expectedTypes.GroovyExpectedTypesContributor;
import org.jetbrains.plugins.groovy.lang.psi.expectedTypes.GroovyExpectedTypesProvider;
import org.jetbrains.plugins.groovy.lang.psi.expectedTypes.SubtypeConstraint;
import org.jetbrains.plugins.groovy.lang.psi.expectedTypes.TypeConstraint;
import org.jetbrains.plugins.groovy.lang.psi.impl.GrTupleType;
import org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author peter
 */
public class GppExpectedTypesContributor extends GroovyExpectedTypesContributor {
  @Override
  public List<TypeConstraint> calculateTypeConstraints(@NotNull GrExpression expression) {
    final PsiElement parent = expression.getParent();
    if (parent instanceof GrListOrMap) {
      final GrListOrMap list = (GrListOrMap)parent;
      if (!list.isMap()) {
        return addExpectedConstructorParameters(expression, list);
      }
    }
    if (parent instanceof GrNamedArgument) {
      final PsiElement map = parent.getParent();
      if (map instanceof GrListOrMap && "super".equals(((GrNamedArgument)parent).getLabelName())) {
        //todo expected property types
        return addExpectedConstructorParameters((GrListOrMap)map, new PsiType[]{expression.getType()}, 0);
      }
    }
    return Collections.emptyList();
  }

  private static List<TypeConstraint> addExpectedConstructorParameters(GrExpression expression, GrListOrMap list) {
    final PsiType listType = list.getType();
    if (!(listType instanceof GrTupleType)) {
      return Collections.emptyList();
    }

    final PsiType[] argTypes = ((GrTupleType)listType).getComponentTypes();
    final int argIndex = Arrays.asList(list.getInitializers()).indexOf(expression);
    assert argIndex >= 0;

    return addExpectedConstructorParameters(list, argTypes, argIndex);
  }

  private static List<TypeConstraint> addExpectedConstructorParameters(GrListOrMap list, PsiType[] argTypes, int argIndex) {
    final ArrayList<TypeConstraint> result = new ArrayList<TypeConstraint>();
    for (PsiType type : GroovyExpectedTypesProvider.getDefaultExpectedTypes(list)) {
      if (type instanceof PsiClassType) {
        for (GroovyResolveResult resolveResult : PsiUtil.getConstructorCandidates((PsiClassType)type, argTypes, list)) {
          final PsiElement method = resolveResult.getElement();
          if (method instanceof PsiMethod && ((PsiMethod)method).isConstructor()) {
            final PsiParameter[] constructorParameters = ((PsiMethod)method).getParameterList().getParameters();
            if (constructorParameters.length > argIndex) {
              final PsiType toCastTo = resolveResult.getSubstitutor().substitute(constructorParameters[argIndex].getType());
              result.add(SubtypeConstraint.create(toCastTo));
            }
          }
        }
      }
    }
    return result;
  }
}
