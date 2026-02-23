
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

    @GetMapping("/busquedas/lineal")
    public String busquedasLineal() {
        System.out.println("Accediendo a Busquedas - Lineal");
        return "busquedas-lineal"; // templates/busquedas-lineal.html
    }

    @GetMapping("/busquedas/binaria")
    public String busquedasBinaria() {
        System.out.println("Accediendo a Busquedas - Binaria");
        return "busquedas-binaria"; // templates/busquedas-binaria.html
    }

    @GetMapping("/busquedas/hash")
    public String busquedasHash() {
        System.out.println("Accediendo a Busquedas - Funciones Hash");
        return "busquedas-hash"; // templates/busquedas-hash.html
    }
}

