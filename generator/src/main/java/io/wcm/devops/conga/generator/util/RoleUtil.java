/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
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
package io.wcm.devops.conga.generator.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;
import com.rits.cloning.Cloner;

import io.wcm.devops.conga.generator.GeneratorException;
import io.wcm.devops.conga.model.role.Role;
import io.wcm.devops.conga.model.role.RoleFile;
import io.wcm.devops.conga.model.role.RoleInherit;
import io.wcm.devops.conga.model.role.RoleVariant;
import io.wcm.devops.conga.model.util.MapMerger;

/**
 * Helper methods for managing roles.
 */
public final class RoleUtil {

  private static final int INHERIT_MAX_LEVEL = 20;

  private RoleUtil() {
    // static methods only
  }

  /**
   * Resolve role inheritance.
   * As a result one or multiple roles are returned that reflect the inheritanceship.
   * File that are generated by both the sub and the super role are eliminated in the super role.
   * @param roleName Role name - role may contain inheritance relations
   * @param environmentContext Context information.
   * @param roles All roles for the system
   * @return Resolved role(s) without pending inheritance relations.
   * @throws GeneratorException when role or any of it's inheritance relations is not found
   */
  public static Map<String, Role> resolveRole(String roleName, String environmentContext, Map<String, Role> roles)
      throws GeneratorException {
    return resolveRole(roleName, environmentContext, roles, 0);
  }

  private static Map<String, Role> resolveRole(String roleName, String environmentContext, Map<String, Role> roles, int inheritLevel)
      throws GeneratorException {
    if (inheritLevel > INHERIT_MAX_LEVEL) {
      throw new GeneratorException("Cyclic inheritance dependency for role '" + roleName + "'.");
    }

    Map<String, Role> resolvedRoles = new LinkedHashMap<>();

    // get role that was directly referenced
    Role role = getRole(roleName, environmentContext, roles);

    // add inherits of role (super roles before sub roles)
    if (!role.getInherits().isEmpty()) {
      for (RoleInherit inherit : role.getInherits()) {
        String superRoleName = inherit.getRole();
        Map<String, Role> superRoles = resolveRole(superRoleName, environmentContext + " > " + roleName, roles, inheritLevel + 1);
        for (Role superRole : superRoles.values()) {
          validateRole(roleName, role, superRoleName, superRole);
          mergeRoleConfig(role, superRole);
        }
        resolvedRoles.putAll(superRoles);
      }
      role.setInherits(null);
    }

    // add role that was directly referenced last
    resolvedRoles.put(roleName, role);

    // remove duplicate file generation definitions from super roles
    removeFileDuplicates(resolvedRoles);

    return resolvedRoles;
  }

  private static Role getRole(String roleName, String context, Map<String, Role> roles) {
    Role role = roles.get(roleName);
    if (role == null) {
      throw new GeneratorException("Role '" + roleName + "' "
          + "referenced in " + context + " does not exist.");
    }
    // clone role object because it may be changed when resolving inheritance
    return Cloner.standard().deepClone(role);
  }

  private static void validateRole(String roleName, Role role, String superRoleName, Role superRole) {
    // if the super role define variants: ensure the role inheriting defines the same variants (or more)
    if (!superRole.getVariants().isEmpty()) {
      Set<String> variants = new HashSet<>();
      for (RoleVariant variant : role.getVariants()) {
        variants.add(variant.getVariant());
      }
      for (RoleVariant superVariant : superRole.getVariants()) {
        if (!variants.contains(superVariant.getVariant())) {
          throw new GeneratorException("Role '" + roleName + "' has to define the same variants as the super role '" + superRoleName + "'.");
        }
      }
    }
  }

  /**
   * Merged configuration of a role with it's super role (parameters from the role have higher precedence).
   * As a result, configuration of both role and super role is identical.
   * @param role Role
   * @param superRole Super role
   */
  private static void mergeRoleConfig(Role role, Role superRole) {
    // merge config
    Map<String, Object> merged = MapMerger.merge(role.getConfig(), superRole.getConfig());
    role.setConfig(merged);
    superRole.setConfig(merged);

    // merge variant configs
    for (RoleVariant variant : role.getVariants()) {
      boolean found = false;
      for (RoleVariant superVariant : superRole.getVariants()) {
        if (StringUtils.equals(superVariant.getVariant(), variant.getVariant())) {
          Map<String, Object> variantMerged = MapMerger.merge(variant.getConfig(), superVariant.getConfig());
          variant.setConfig(variantMerged);
          superVariant.setConfig(variantMerged);
          found = true;
          break;
        }
      }
      if (!found) {
        // if super role does not have the variant from the sub role, add it to super role as well
        List<RoleVariant> mergedVariants = new ArrayList<>(superRole.getVariants());
        mergedVariants.add(Cloner.standard().deepClone(variant));
        superRole.setVariants(mergedVariants);
      }
    }
  }

  /**
   * Removes file definitions resulting in a file with the same name from super roles.
   * That means duplicated file definitions in roles have higher precedence than files with the same name from super
   * roles.
   * @param resolvedRoles Resolved roles
   */
  private static void removeFileDuplicates(Map<String, Role> resolvedRoles) {
    List<Role> roles = ImmutableList.copyOf(resolvedRoles.values());

    // super roles are first, sub roles last.
    // got from last to first, collect all generated file definitions and remove duplicates
    Set<String> fileNames = new HashSet<>();
    for (int i = roles.size() - 1; i >= 0; i--) {
      Role role = roles.get(i);
      List<RoleFile> filteredFiles = new ArrayList<>();
      for (RoleFile file : role.getFiles()) {
        String fileKey = getFileNameKey(file);
        if (!fileNames.contains(fileKey)) {
          filteredFiles.add(file);
          fileNames.add(fileKey);
        }
      }
      role.setFiles(filteredFiles);
    }
  }

  private static String getFileNameKey(RoleFile file) {
    if (StringUtils.isNotBlank(file.getFile())) {
      return file.getFile();
    }
    else if (StringUtils.isNotBlank(file.getUrl())) {
      return file.getUrl();
    }
    else {
      return file.toString();
    }
  }

  /**
   * Checks if the given role file should be generated for the given set of variants.
   * @param roleFile Role file
   * @param variants Variants select for a node
   * @return true if file should be rendered
   */
  public static boolean matchesRoleFile(RoleFile roleFile, List<String> variants) {
    Set<String> assignedVariants = new HashSet<>(variants);
    for (String requiredVariant : roleFile.getVariants()) {
      if (!assignedVariants.contains(requiredVariant)) {
        return false;
      }
    }
    return true;
  }

}
