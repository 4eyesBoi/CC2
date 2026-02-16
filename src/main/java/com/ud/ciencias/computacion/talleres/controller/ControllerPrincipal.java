
package com.ud.ciencias.computacion.talleres.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ud.ciencias.computacion.talleres.services.CrearService;



@Controller
public class ControllerPrincipal {

    private CrearService crearService;

    public ControllerPrincipal(CrearService crearService) {
        this.crearService = crearService;
    }

    @GetMapping("/")
    public String home() {
        return "index"; // templates/index.html
    }
    
    @GetMapping("/arreglo")
    public String serviciosActivos() {


        System.out.println("hola arreglo");
        return "prueba"; // templates/index.html
    }

    @PostMapping("/crearArreglo")
    public String crearArreglo(@RequestParam("tamArr") int tamArr, @RequestParam("tamClav") int tamClav) {
        
        this.crearService.crearArreglo(tamArr, tamClav);
        return "prueba"; // templates/resultado.html
    }
}

