/*
 * SonarQube Java
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.java.checks;

import com.google.common.collect.ImmutableList;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.java.checks.methods.AbstractMethodDetection;
import org.sonar.java.checks.methods.MethodInvocationMatcher;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.List;

@Rule(
  key = "S1872",
  name = "Classes should not be compared by name",
  tags = {"bug", "cwe"},
  priority = Priority.CRITICAL)
@ActivatedByDefault
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.INSTRUCTION_RELIABILITY)
@SqaleConstantRemediation("5min")
public class ClassComparedByNameCheck extends AbstractMethodDetection {

  private ClassGetNameDetector classGetNameDetector = new ClassGetNameDetector();

  @Override
  protected List<MethodInvocationMatcher> getMethodInvocationMatchers() {
    return ImmutableList.of(MethodInvocationMatcher.create().typeDefinition("java.lang.String").name("equals").withNoParameterConstraint());
  }

  @Override
  protected void onMethodFound(MethodInvocationTree mit) {
    if(mit.methodSelect().is(Tree.Kind.MEMBER_SELECT)) {
      ((MemberSelectExpressionTree) mit.methodSelect()).expression().accept(classGetNameDetector);
    }
    mit.arguments().get(0).accept(classGetNameDetector);
  }

  private class ClassGetNameDetector extends BaseTreeVisitor {

    private final List<MethodInvocationMatcher> methodMatchers =  ImmutableList.of(
          MethodInvocationMatcher.create().typeDefinition("java.lang.Class").name("getName"),
          MethodInvocationMatcher.create().typeDefinition("java.lang.Class").name("getSimpleName")
      );


    @Override
    public void visitMethodInvocation(MethodInvocationTree tree) {
      for (MethodInvocationMatcher methodMatcher : methodMatchers) {
        if(methodMatcher.matches(tree, getSemanticModel())) {
          addIssue(tree, "Use an \"instanceof\" comparison instead.");
        }
      }
      scan(tree.methodSelect());
    }
  }
}
