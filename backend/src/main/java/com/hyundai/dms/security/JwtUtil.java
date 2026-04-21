package com.hyundai.dms.security;

import com.hyundai.dms.entity.User;
import com.hyundai.dms.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    private final UserRepository userRepository;

    /**
     * Generates a JWT token embedding userId, roles, and dealerId as custom claims.
     * This satisfies the requirement: JWT contains userId, role, dealerId.
     */
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        // Resolve user entity to get userId and dealerId
        User user = userRepository.findByUsername(userPrincipal.getUsername()).orElse(null);
        Long userId   = user != null ? user.getId()       : null;
        Long dealerId = user != null ? user.getDealerId() : null;

        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                // Custom claims: userId, dealerId, roles
                .claim("userId",   userId)
                .claim("dealerId", dealerId)
                .claim("roles",    roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Extract userId directly from token claims — avoids extra DB lookup. */
    public Long extractUserId(String token) {
        Object val = extractAllClaims(token).get("userId");
        if (val instanceof Number) return ((Number) val).longValue();
        return null;
    }

    /** Extract dealerId directly from token claims. */
    public Long extractDealerId(String token) {
        Object val = extractAllClaims(token).get("dealerId");
        if (val instanceof Number) return ((Number) val).longValue();
        return null;
    }

    /** Extract roles list from token claims. */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object val = extractAllClaims(token).get("roles");
        if (val instanceof List) return (List<String>) val;
        return List.of();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}
