package io.cloudops.security;

import org.springframework.security.oauth2.jwt.Jwt;
import java.util.List;
import java.util.Map;
import java.util.Optional;
public class KeycloakJwtConverter {
    public static Optional<String> getUserId(Jwt jwt) {
        return Optional.ofNullable(jwt.getClaimAsString("sub")); // 'sub'

    }
    public static List<String> getRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            return (List<String>) realmAccess.get("roles");
        }
        return List.of();
    }
}
