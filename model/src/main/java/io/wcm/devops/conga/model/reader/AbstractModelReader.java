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
package io.wcm.devops.conga.model.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.CharEncoding;
import org.yaml.snakeyaml.Yaml;

import com.google.common.collect.ImmutableSet;

/**
 * Shared functionality for model readers.
 */
public abstract class AbstractModelReader<T> implements ModelReader<T> {

  private static final Set<String> SUPPORTED_EXTENSIONS = ImmutableSet.of("yaml");

  private final Yaml yaml;

  /**
   * @param yaml YAML
   */
  public AbstractModelReader(Yaml yaml) {
    this.yaml = yaml;
  }

  @Override
  public boolean accepts(File file) {
    return (file.isFile() && SUPPORTED_EXTENSIONS.contains(FilenameUtils.getExtension(file.getName())));
  }

  @Override
  public final T read(File file) throws IOException {
    try (InputStream is = new FileInputStream(file)) {
      return read(is);
    }
  }

  @Override
  public final T read(InputStream is) throws IOException {
    return read(new InputStreamReader(is, CharEncoding.UTF_8));
  }

  @Override
  @SuppressWarnings("unchecked")
  public final T read(Reader reader) {
    return (T)yaml.load(reader);
  }

}