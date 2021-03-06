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
package io.wcm.devops.conga.generator;

import static io.wcm.devops.conga.generator.TestUtils.setupGenerator;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class GeneratorInvalidEnvironmentTest {

  private Generator underTest;
  private File destDir;

  @Before
  public void setUp() {
    destDir = new File("target/generation-test/" + getClass().getSimpleName());
    underTest = setupGenerator(destDir);
  }

  @Test(expected = GeneratorException.class)
  public void testInvalidEnvironments() {
    underTest.generate("unknown");
  }

}
