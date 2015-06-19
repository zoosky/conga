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
package io.wcm.devops.conga.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.google.common.collect.ImmutableList;

class ClasspathResourceCollectionImpl extends AbstractClasspathResourceImpl implements ResourceCollection {

  private final List<URL> fileUrls = new ArrayList<>();
  private final List<String> folderPaths = new ArrayList<>();

  public ClasspathResourceCollectionImpl(String path) {
    super(path);

    try {
      PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(this.classLoader);
      org.springframework.core.io.Resource[] classpathResources = resolver.getResources(convertPath(path) + "/*");
      for (org.springframework.core.io.Resource resource : classpathResources) {
        if (isFolder(resource)) {
          folderPaths.add(path + "/" + resource.getFilename());
        }
        else {
          fileUrls.add(resource.getURL());
        }
      }
    }
    catch (FileNotFoundException ex) {
      // empty folder
    }
    catch (IOException ex) {
      throw new ResourceException("Unable to enumerate classpath resources at " + path, ex);
    }
  }

  private static boolean isFolder(org.springframework.core.io.Resource classpathResource) throws IOException {
    return StringUtils.endsWith(classpathResource.getURL().toString(), "/");
  }

  @Override
  public boolean exists() {
    return !fileUrls.isEmpty();
  }

  @Override
  public List<Resource> getResources() {
    return ImmutableList.copyOf(fileUrls.stream()
        .map(url -> new ClasspathResourceImpl(url))
        .collect(Collectors.toList()));
  }

  @Override
  public List<ResourceCollection> getResourceCollections() {
    return ImmutableList.copyOf(folderPaths.stream()
        .map(folderPath -> new ClasspathResourceCollectionImpl(folderPath))
        .collect(Collectors.toList()));
  }

}