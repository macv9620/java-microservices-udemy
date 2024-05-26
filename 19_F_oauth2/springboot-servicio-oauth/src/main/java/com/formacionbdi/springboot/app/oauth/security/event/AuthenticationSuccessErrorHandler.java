package com.formacionbdi.springboot.app.oauth.security.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import com.formacionbdi.springboot.app.oauth.services.IUsuarioService;
import com.formacionbdi.springboot.app.commons.usuarios.models.entity.Usuario;

import feign.FeignException;

@Component
public class AuthenticationSuccessErrorHandler implements AuthenticationEventPublisher {

	private Logger log = LoggerFactory.getLogger(AuthenticationSuccessErrorHandler.class);

	@Autowired
	private IUsuarioService usuarioService;

	//Método para extender funcionalidad de autenticación en caso de éxito
	@Override
	public void publishAuthenticationSuccess(Authentication authentication) {

		//El login del cliente es otro evento que pasa por este proceso de autenticación
		//En este caso se ignora para centrarse solamente en el login del usuario
		//Si se requiere control sobre el login del cliente acá se puede controlar
		// if(authentication.getName().equalsIgnoreCase("frontendapp")) {
		if(authentication.getDetails() instanceof WebAuthenticationDetails) {
			return;
		}

		UserDetails user = (UserDetails) authentication.getPrincipal();
		String mensaje = "Success Login: " + user.getUsername();
		System.out.println(mensaje);
		log.info(mensaje);

		Usuario usuario = usuarioService.findByUsername(authentication.getName());

		//Si el usuario tiene intentos fallidos cuando se autentique correctamente
		//Reiniciar el conteo
		if(usuario.getIntentos() != null && usuario.getIntentos() > 0) {
			usuario.setIntentos(0);
			usuarioService.update(usuario, usuario.getId());
		}
	}

	//Método para extender funcionalidad de autenticación en caso de error
	@Override
	public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
		String mensaje = "Error en el Login: " + exception.getMessage();
		log.error(mensaje);
		System.out.println(mensaje);

		try {

			StringBuilder errors = new StringBuilder();
			errors.append(mensaje);

			Usuario usuario = usuarioService.findByUsername(authentication.getName());

			if (usuario.getIntentos() == null) {
				usuario.setIntentos(0);
			}

			log.info("Intentos actual es de: " + usuario.getIntentos());

			//Si el usuario falla se suma un intento fallido
			usuario.setIntentos(usuario.getIntentos()+1);

			log.info("Intentos después es de: " + usuario.getIntentos());

			errors.append(" - Intentos del login: " + usuario.getIntentos());

			//Si el usuario falla 3 veces se inactiva la cuenta
			if(usuario.getIntentos() >= 3) {
				String errorMaxIntentos = String.format("El usuario %s des-habilitado por máximos intentos.", usuario.getUsername());
				log.error(errorMaxIntentos);
				errors.append(" - " + errorMaxIntentos);

				//Este parámetro se pasa en el UserDetailService y Spring no permite el acceso
				//a usuarios deshabilitados
				usuario.setEnabled(false);
			}

			//Se actualiza estado de usuario
			usuarioService.update(usuario, usuario.getId());

		} catch (FeignException e) {
			log.error(String.format("El usuario %s no existe en el sistema", authentication.getName()));
		}

	}

}
