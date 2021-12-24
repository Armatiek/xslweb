/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.armatiek.xslweb.shiro;

import java.util.Map;
import java.util.Optional;

import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.oidc.profile.OidcProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public class OidcAuthorizationGenerator implements AuthorizationGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(OidcAuthorizationGenerator.class);

  private String clientId;

  public OidcAuthorizationGenerator() {
  }

  public OidcAuthorizationGenerator(final String clientId) {
    this.clientId = clientId;
  }

  @Override
  public Optional<UserProfile> generate(final WebContext context, final SessionStore sessionStore, final UserProfile profile) {
    if (profile instanceof OidcProfile) {
      try {
        final JWT jwt = SignedJWT.parse(((OidcProfile) profile).getAccessToken().getValue());
        final JWTClaimsSet jwtClaimsSet = jwt.getJWTClaimsSet();

        final Map<String, Object> realmRolesJsonObject = jwtClaimsSet.getJSONObjectClaim("realm_access");
        if (realmRolesJsonObject != null) {
          final JSONArray realmRolesJsonArray = (JSONArray) realmRolesJsonObject.get("roles");
          if (realmRolesJsonArray != null) {
            realmRolesJsonArray.forEach(role -> profile.addRole((String) role));
          }
        }

        if (clientId != null) {
          final Map<String, Object> resourceAccess = jwtClaimsSet.getJSONObjectClaim("resource_access");
          if (resourceAccess != null) {
            final JSONObject clientRolesJsonObject = (JSONObject) resourceAccess.get(clientId);
            if (clientRolesJsonObject != null) {
              final JSONArray clientRolesJsonArray = (JSONArray) clientRolesJsonObject.get("roles");
              if (clientRolesJsonArray != null) {
                clientRolesJsonArray.forEach(role -> profile.addRole((String) role));
              }
            }
          }
        }

      } catch (final Exception e) {
        LOGGER.warn("Cannot parse Oidc roles", e);
      }
    }

    return Optional.of(profile);
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(final String clientId) {
    this.clientId = clientId;
  }

}