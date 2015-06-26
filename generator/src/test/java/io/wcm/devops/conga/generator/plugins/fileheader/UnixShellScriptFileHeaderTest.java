/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.devops.conga.generator.plugins.fileheader;

import static org.junit.Assert.assertTrue;
import io.wcm.devops.conga.generator.spi.FileHeaderPlugin;
import io.wcm.devops.conga.generator.spi.context.FileContext;
import io.wcm.devops.conga.generator.spi.context.FileHeaderContext;
import io.wcm.devops.conga.generator.util.PluginManager;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class UnixShellScriptFileHeaderTest {

  private FileHeaderPlugin underTest;

  @Before
  public void setUp() {
    underTest = new PluginManager().get(UnixShellScriptFileHeader.NAME, FileHeaderPlugin.class);
  }

  @Test
  public void testApply() throws Exception {
    File file = new File("target/generation-test/fileHeader.sh");
    FileUtils.write(file, "#!/bin/bash\n"
        + "myscript");

    FileHeaderContext context = new FileHeaderContext().commentLines(ImmutableList.of("a", "b", "c"));
    FileContext fileContext = new FileContext().file(file);
    assertTrue(underTest.accepts(fileContext, context));
    underTest.apply(fileContext, context);

    String content = FileUtils.readFileToString(file);
    assertTrue(StringUtils.contains(content, "# a\n# b\n# c\n"));
    assertTrue(StringUtils.endsWith(content, "\nmyscript"));
    assertTrue(StringUtils.startsWith(content, "#!/bin/bash\n"));

    file.delete();
  }

}