package com.formacionbdi.springboot.app.item.clientes;

import java.util.List;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.formacionbdi.springboot.app.item.models.Producto;

@FeignClient(name = "servicio-productos")
public interface ProductoClienteRest {
	
	@GetMapping("/listar")
	@LoadBalanced
	public List<Producto> listar();
	
	@GetMapping("/ver/{id}")
	@LoadBalanced
	public Producto detalle(@PathVariable Long id);

}
