
package com.ud.ciencias.computacion.talleres.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;



@Controller
public class ControllerPrincipal {

    

    @GetMapping("/")
    public String home() {
        return "index"; // templates/index.html
    }
    
    @GetMapping("/servicios/activos")
    public String serviciosActivos() {


        System.out.println("Ingrese servicios activos");
        return "prueba"; // templates/prueba.html
    }
}

