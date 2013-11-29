package org.jetbrains.postfixCompletion.templates;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.postfixCompletion.infrastructure.PostfixTemplateContext;
import org.jetbrains.postfixCompletion.infrastructure.PrefixExpressionContext;
import org.jetbrains.postfixCompletion.infrastructure.TemplateProvider;
import org.jetbrains.postfixCompletion.lookupItems.ExpressionPostfixLookupElementBase;
import org.jetbrains.postfixCompletion.util.CommonUtils;
import org.jetbrains.postfixCompletion.util.JavaSurroundersProxy;

import java.util.List;

@TemplateProvider(
  templateName = "cast",
  description = "Surrounds expression with cast",
  example = "(SomeType) expr",
  worksInsideFragments = true)
public final class CastExpressionPostfixTemplateProvider extends PostfixTemplateProvider {
  @Override
  public void createItems(@NotNull PostfixTemplateContext context, @NotNull List<LookupElement> consumer) {
    if (!context.executionContext.isForceMode) return;

    PrefixExpressionContext bestContext = context.outerExpression();
    List<PrefixExpressionContext> expressions = context.expressions();

    for (int index = expressions.size() - 1; index >= 0; index--) {
      PrefixExpressionContext expressionContext = expressions.get(index);
      if (CommonUtils.isNiceExpression(expressionContext.expression)) {
        bestContext = expressionContext;
        break;
      }
    }

    consumer.add(new CastLookupElement(bestContext));
  }

  static final class CastLookupElement extends ExpressionPostfixLookupElementBase<PsiExpression> {
    public CastLookupElement(@NotNull PrefixExpressionContext context) {
      super("cast", context);
    }

    @NotNull
    @Override
    protected PsiExpression createNewExpression(@NotNull PsiElementFactory factory,
                                                @NotNull PsiElement expression,
                                                @NotNull PsiElement context) {
      return (PsiExpression)expression;
    }

    @Override
    protected void postProcess(@NotNull final InsertionContext context, @NotNull PsiExpression expression) {
      JavaSurroundersProxy.cast(context.getProject(), context.getEditor(), expression);
    }
  }
}