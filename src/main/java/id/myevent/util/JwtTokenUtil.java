package id.myevent.util;

import id.myevent.model.dto.UserAuthDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/** Utility Class for JWT. */
@Component
public class JwtTokenUtil {
  @Value("${jwt.secret}")
  private String secretKey;

  @Value("${jwt.token-validity}")
  private long tokenValidity;

  private Claims getAllClaimsFromToken(String token) {
    return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
  }

  private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  private Boolean isTokenExpired(String token) {
    final Date expiration = getExpirationDateFromToken(token);
    return expiration.before(new Date());
  }

  private String doGenerateToken(Map<String, Object> claims, String subject) {
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + tokenValidity * 1000))
        .signWith(SignatureAlgorithm.HS512, secretKey)
        .compact();
  }

  public String getUsernameFromToken(String token) {
    return getAllClaimsFromToken(token).get("username", String.class);
  }

  public String getSubjectFromToken(String token){
    return getClaimFromToken(token, Claims::getSubject);
  }

  public Date getExpirationDateFromToken(String token) {
    return getClaimFromToken(token, Claims::getExpiration);
  }

  /** generate token. */
  public String generateToken(UserAuthDto userAuthDto) {
    Map<String, Object> claims =
        new HashMap<String, Object>() {
          {
            put("username", userAuthDto.getUsername());
          }
        };
    return doGenerateToken(claims, userAuthDto.getId().toString());
  }

  public Boolean validateToken(String token, UserDetails userDetails) {
    final String username = getUsernameFromToken(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }
}
