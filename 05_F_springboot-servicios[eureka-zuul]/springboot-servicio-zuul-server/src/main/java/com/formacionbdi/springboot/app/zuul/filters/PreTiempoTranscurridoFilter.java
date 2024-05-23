package com.formacionbdi.springboot.app.zuul.filters;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

@Component
public class PreTiempoTranscurridoFilter extends ZuulFilter{
	
	private static Logger log = LoggerFactory.getLogger(PreTiempoTranscurridoFilter.class);

	@Override
	public boolean shouldFilter() {
		//Determina si se ejecuta o no el método run
		//Se puede definir lógica para determinar si se ejecuta o no
		//En este caso se setea true para que siempre se ejecute
		return true;
	}

	@Override
	public Object run() throws ZuulException {

		//En este método se resuelve la lógica del filtro

		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();
		
		log.info(String.format("%s request enrutado a %s", request.getMethod(), request.getRequestURL().toString()));
		
		Long tiempoInicio = System.currentTimeMillis();
		request.setAttribute("tiempoInicio", tiempoInicio);
		
		return null;
	}

	@Override
	public String filterType() {
		//Tipo de filtro a implementar en este caso pre
		//Estos valores ya vienen establecidos
		return "pre";
	}

	@Override
	public int filterOrder() {
		//Orden de ejecución en caso se tener varios pre
		return 1;
	}

}
