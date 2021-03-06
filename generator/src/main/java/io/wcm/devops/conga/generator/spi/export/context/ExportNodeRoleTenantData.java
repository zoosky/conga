/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2016 wcm.io
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
package io.wcm.devops.conga.generator.spi.export.context;

import java.util.List;
import java.util.Map;

/**
 * Export tenant data for node.
 */
public final class ExportNodeRoleTenantData {

  private String tenant;
  private List<String> roles;
  private Map<String, Object> config;

  /**
   * @return Tenant
   */
  public String getTenant() {
    return this.tenant;
  }

  /**
   * @param value Tenant
   * @return this
   */
  public ExportNodeRoleTenantData tenant(String value) {
    this.tenant = value;
    return this;
  }

  /**
   * @return Roles
   */
  public List<String> getRoles() {
    return this.roles;
  }

  /**
   * @param value Roles
   * @return this
   */
  public ExportNodeRoleTenantData roles(List<String> value) {
    this.roles = value;
    return this;
  }

  /**
   * @return Configuration
   */
  public Map<String, Object> getConfig() {
    return this.config;
  }

  /**
   * @param value Configuration
   * @return this
   */
  public ExportNodeRoleTenantData config(Map<String, Object> value) {
    this.config = value;
    return this;
  }

}
