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

import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.squidbridge.checks.CheckMessagesVerifierRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.source.Symbolizable;
import org.sonar.api.source.Symbolizable.SymbolTableBuilder;
import org.sonar.java.JavaAstScanner;
import org.sonar.java.SonarComponents;
import org.sonar.java.model.VisitorsBridge;
import org.sonar.squidbridge.api.SourceFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UnusedPrivateFieldCheckTest {

  @Rule
  public CheckMessagesVerifierRule checkMessagesVerifier = new CheckMessagesVerifierRule();

  @Test
  public void test() {
    SourceFile file = JavaAstScanner.scanSingleFile(new File("src/test/files/checks/UnusedPrivateFieldCheck.java"), new VisitorsBridge(new UnusedPrivateFieldCheck()));
    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(3).withMessage("Remove this unused \"unusedField\" private field.")
      .next().atLine(6).withMessage("Remove this unused \"foo\" private field.");
  }

  @Test
  public void testLombok() {
    UnusedPrivateFieldCheck check = new UnusedPrivateFieldCheck();
    List<File> classpath = new ArrayList<File>();
    for (String path : System.getProperty("java.class.path").split(File.pathSeparator)) {
      File file = new File(path);
      if (file.exists() && path.endsWith(".jar") && file.getName().startsWith("lombok")) {
        classpath.add(file);
      }
    }
    for (File file : new File("src/test/files/checks/lombok/").listFiles()) {
      SourceFile sourceFile = JavaAstScanner.scanSingleFile(file, getVisitors(check, classpath));
      checkMessagesVerifier.verify(sourceFile.getCheckMessages()).noMore();
    }
  }

  public static VisitorsBridge getVisitors(JavaFileScanner scanner, List<File> classpath) {
    if (classpath == null || classpath.isEmpty()) {
      return new VisitorsBridge(scanner);
    }
    SonarComponents sonarComponents = Mockito.mock(SonarComponents.class);
    Mockito.when(sonarComponents.getProjectClasspath()).thenReturn(classpath);
    Symbolizable symbolizable = Mockito.mock(Symbolizable.class);
    Mockito.when(symbolizable.newSymbolTableBuilder()).thenReturn(Mockito.mock(SymbolTableBuilder.class));
    Mockito.when(sonarComponents.symbolizableFor(Mockito.any(File.class))).thenReturn(symbolizable);
    return new VisitorsBridge(Arrays.asList(scanner), sonarComponents);
  }

}
