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

import static io.wcm.devops.conga.generator.TestUtils.assertDirectory;
import static io.wcm.devops.conga.generator.TestUtils.assertFile;
import static io.wcm.devops.conga.generator.TestUtils.setupGenerator;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import com.google.common.collect.ImmutableList;

import io.wcm.devops.conga.model.util.MapExpander;

@SuppressWarnings("unchecked")
public class GeneratorTestNodeModelExportTest {

  private Generator underTest;
  private File destDir;

  @Before
  public void setUp() {
    destDir = new File("target/generation-test/" + getClass().getSimpleName());
    underTest = setupGenerator(destDir);
    underTest.generate();
  }

  @Test
  public void testAllEnvironments() throws IOException {
    File node1Dir = assertDirectory(destDir, "env1/node1");
    File model1File = assertFile(node1Dir, "model.yaml");
    Map<String, Object> model1 = readYaml(model1File);

    Map<String, Object> role1 = getRole(model1, "role1");
    assertFiles(role1,
        "text/test-role1.variant11.env1.node1.txt",
        "xml/test.tenant1.tenantRole1,tenantRole2.env1.xml",
        "xml/test.tenant2.tenantRole1.env1.xml",
        "text/test-conditional-tenant1.txt",
        "files/sample.txt",
        "files/sample-filesystem.txt");
    assertEquals("globalFromRole1", getConfig(role1, "globalString"));
    assertEquals(ImmutableList.of("tenantRole1", "tenantRole2"), getTenantRoles(role1, "tenant1"));
    assertEquals("\"value1\" äöüß€", getTenantConfig(role1, "tenant1", "defaultString"));
    assertEquals(ImmutableList.of("tenantRole1"), getTenantRoles(role1, "tenant2"));
    assertEquals("defaultFromTenant2", getTenantConfig(role1, "tenant2", "defaultString"));
    assertEquals(ImmutableList.of(), getTenantRoles(role1, "tenant3"));
    assertEquals("\"value1\" äöüß€", getTenantConfig(role1, "tenant3", "defaultString"));

    Map<String, Object> role2 = getRole(model1, "role2");
    assertFiles(role2,
        "json/test.json");
    assertEquals("globalValue äöüß€", getConfig(role2, "globalString"));
    assertEquals("globalValue äöüß€", getConfig(role2, "globalString"));
    assertEquals(ImmutableList.of("tenantRole1", "tenantRole2"), getTenantRoles(role2, "tenant1"));
    assertEquals("value2", getTenantConfig(role2, "tenant1", "defaultString"));
    assertEquals(ImmutableList.of("tenantRole1"), getTenantRoles(role2, "tenant2"));
    assertEquals("defaultFromTenant2", getTenantConfig(role2, "tenant2", "defaultString"));
    assertEquals(ImmutableList.of(), getTenantRoles(role2, "tenant3"));
    assertEquals("value2", getTenantConfig(role2, "tenant3", "defaultString"));
  }

  private Map<String, Object> readYaml(File file) throws IOException {
    try (FileInputStream is = new FileInputStream(file);
        Reader reader = new InputStreamReader(is, CharEncoding.UTF_8)) {
      return (Map<String, Object>)new Yaml().load(reader);
    }
  }

  private Map<String, Object> getRole(Map<String, Object> model, String role) {
    List<Map<String, Object>> roles = (List<Map<String, Object>>)model.get("roles");
    if (roles == null) {
      return null;
    }
    for (Map<String, Object> item : roles) {
      if (StringUtils.equals(role, (String)item.get("role"))) {
        return item;
      }
    }
    return null;
  }

  private void assertFiles(Map<String, Object> role, String... fileNamesExpected) {
    List<String> fileNamesFound = new ArrayList<>();
    List<Map<String, Object>> files = (List<Map<String, Object>>)role.get("files");
    if (files != null) {
      files.forEach(item -> fileNamesFound.add((String)item.get("path")));
    }
    assertEquals(ImmutableList.copyOf(fileNamesExpected), fileNamesFound);
  }

  private Object getConfig(Map<String, Object> configurable, String key) {
    Map<String, Object> config = (Map<String, Object>)configurable.get("config");
    if (config == null) {
      return null;
    }
    return MapExpander.getDeep(config, key);
  }

  private Map<String, Object> getTenant(Map<String, Object> role, String tenant) {
    List<Map<String, Object>> tenants = (List<Map<String, Object>>)role.get("tenants");
    if (tenants == null) {
      return null;
    }
    for (Map<String, Object> item : tenants) {
      if (StringUtils.equals(tenant, (String)item.get("tenant"))) {
        return item;
      }
    }
    return null;
  }

  private Object getTenantConfig(Map<String, Object> role, String tenant, String key) {
    Map<String, Object> tenantObject = getTenant(role, tenant);
    if (tenantObject == null) {
      return null;
    }
    return getConfig(tenantObject, key);
  }

  private List<String> getTenantRoles(Map<String, Object> role, String tenant) {
    Map<String, Object> tenantObject = getTenant(role, tenant);
    if (tenantObject == null) {
      return ImmutableList.of();
    }
    return (List<String>)ObjectUtils.defaultIfNull(tenantObject.get("roles"), ImmutableList.of());
  }

}