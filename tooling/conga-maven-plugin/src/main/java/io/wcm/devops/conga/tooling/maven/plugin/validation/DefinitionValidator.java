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
package io.wcm.devops.conga.tooling.maven.plugin.validation;

import org.apache.maven.plugin.MojoFailureException;

import io.wcm.devops.conga.resource.Resource;

/**
 * Resource definition validator
 */
public interface DefinitionValidator {

  /**
   * Validates the given resource
   * @param resource Resource
   * @param pathForLog Path for log message
   * @throws MojoFailureException Mojo failure exception
   */
  void validate(Resource resource, String pathForLog) throws MojoFailureException;

}
