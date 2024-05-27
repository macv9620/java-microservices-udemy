package com.formacionbdi.springboot.app.gateway.security;

import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationManagerJwt implements ReactiveAuthenticationManager{

	@Value("${config.security.oauth.jwt.key}")
	private String llaveJwt;
	
	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {
		//Extrae el token de la autenticación
		return Mono.just(authentication.getCredentials().toString())
				//Parsea el token para acceder al payload que está en los Claims
				.map(token -> {
					SecretKey llave =
							Keys
									.hmacShaKeyFor(Base64.getEncoder()
											.encode(llaveJwt.getBytes()));
					return Jwts
							.parserBuilder()
							//Se valida autenticidad del token con la firma
							.setSigningKey(llave).build()
							.parseClaimsJws(token)
							.getBody();
				})
				.map(claims -> {
					//Obtiene el nombre de usuario del payload o claims y los roles
					String username = claims.get("user_name", String.class);
					List<String> roles = claims.get("authorities", List.class);

					//Genera roles (grantedAuthority) para Spring Security
					Collection<GrantedAuthority> authorities = roles
							.stream()
							.map(SimpleGrantedAuthority::new)
							.collect(Collectors.toList());
					return new UsernamePasswordAuthenticationToken(username, null, authorities);
					
				});
	}

}
