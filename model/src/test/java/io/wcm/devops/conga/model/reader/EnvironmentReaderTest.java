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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import io.wcm.devops.conga.model.environment.Environment;
import io.wcm.devops.conga.model.environment.Node;
import io.wcm.devops.conga.model.environment.NodeRole;
import io.wcm.devops.conga.model.environment.Tenant;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class EnvironmentReaderTest {

  private Environment environment;

  @Before
  public void setUp() throws IOException {
    EnvironmentReader reader = new EnvironmentReader();
    try (InputStream is = getClass().getResourceAsStream("/environment.yaml")) {
      environment = reader.read(is);
    }
    assertNotNull(environment);
  }

  @Test
  public void testEnvironment() {
    assertEquals(3, environment.getNodes().size());
    assertEquals(2, environment.getTenants().size());

    assertEquals(ImmutableMap.of(
        "jvm", ImmutableMap.of("heapspace", ImmutableMap.of("max", "4096m")),
        "topologyConnectors", ImmutableList.of("http://host1/connector", "http://host2/connector")
        ), environment.getConfig());

    assertEquals(ImmutableMap.of("var1", "value1"), environment.getVariables());
  }

  @Test
  public void testNode() {
    Node node = environment.getNodes().get(0);

    assertEquals("importer", node.getNode());

    assertEquals(ImmutableMap.of("jvm", ImmutableMap.of("heapspace", ImmutableMap.of("max", "2048m"))), node.getConfig());

    assertEquals(2, node.getRoles().size());
  }

  @Test
  public void testNodeRole() {
    NodeRole role = environment.getNodes().get(0).getRoles().get(0);

    assertEquals("tomcat-services", role.getRole());
    assertEquals("importer", role.getVariant());
    assertEquals(ImmutableMap.of("topologyConnectors", ImmutableList.of("http://host3/connector")), role.getConfig());
  }

  @Test
  public void testTenant() {
    Tenant tenant = environment.getTenants().get(0);

    assertEquals("tenant1", tenant.getTenant());
    assertEquals(ImmutableList.of("website", "application"), tenant.getRoles());
  }

}