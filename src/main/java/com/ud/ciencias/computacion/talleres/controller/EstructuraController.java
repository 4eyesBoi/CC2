package com.ud.ciencias.computacion.talleres.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ud.ciencias.computacion.talleres.services.EstructuraService;
import com.ud.ciencias.computacion.talleres.models.BusquedaResultado;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/estructura")
@CrossOrigin(origins = "*")
public class EstructuraController {
    
    @Autowired
    private EstructuraService estructuraService;
    
    @PostMapping("/crear")
    public ResponseEntity<Map<String, Object>> crearEstructura(
            @RequestParam String tipo,
            @RequestParam int tamano,
            @RequestParam int tamanoClaves,
            @RequestParam String tipeLimite) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean exito = estructuraService.crearEstructura(tipo, tamano, tamanoClaves, tipeLimite);
            
            if (exito) {
                response.put("exito", true);
                response.put("mensaje", "Estructura " + tipo + " creada correctamente");
                response.put("claves", estructuraService.getEstructuraActual().getClaves());
                return ResponseEntity.ok(response);
            } else {
                response.put("exito", false);
                response.put("mensaje", "Error al crear la estructura");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/insertar")
    public ResponseEntity<Map<String, Object>> insertarClave(@RequestParam int clave) {
        Map<String, Object> response = new HashMap<>();
        
        if (!estructuraService.tieneEstructura()) {
            response.put("exito", false);
            response.put("mensaje", "Debe crear una estructura primero");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            boolean exito = estructuraService.insertarClave(clave);
            
            if (exito) {
                response.put("exito", true);
                response.put("mensaje", "Clave " + clave + " insertada correctamente");
                response.put("claves", estructuraService.getEstructuraActual().getClaves());
            } else {
                response.put("exito", false);
                response.put("mensaje", "No se pudo insertar la clave. Puede estar duplicada o la estructura estar llena");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/buscar")
    public ResponseEntity<Map<String, Object>> buscarClave(@RequestParam int clave) {
        Map<String, Object> response = new HashMap<>();
        
        if (!estructuraService.tieneEstructura()) {
            response.put("exito", false);
            response.put("mensaje", "Debe crear una estructura primero");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            BusquedaResultado resultado = estructuraService.buscarClave(clave);
            
            response.put("exito", true);
            response.put("encontrado", resultado.isEncontrado());
            response.put("posicion", resultado.getPosicion());
            response.put("pasos", resultado.getPasos());
            response.put("claves", estructuraService.getEstructuraActual().getClaves());
            
            if (resultado.isEncontrado()) {
                response.put("mensaje", "Clave " + clave + " encontrada en posición " + resultado.getPosicion());
            } else {
                response.put("mensaje", "Clave " + clave + " no encontrada");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/eliminar")
    public ResponseEntity<Map<String, Object>> eliminarClave(@RequestParam int clave) {
        Map<String, Object> response = new HashMap<>();
        
        if (!estructuraService.tieneEstructura()) {
            response.put("exito", false);
            response.put("mensaje", "Debe crear una estructura primero");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // Primero buscar para obtener los pasos
            BusquedaResultado resultado = estructuraService.buscarClave(clave);
            
            // Luego eliminar
            boolean eliminado = estructuraService.eliminarClave(clave);
            
            response.put("exito", eliminado);
            response.put("encontrado", resultado.isEncontrado());
            response.put("pasos", resultado.getPasos());
            response.put("claves", estructuraService.getEstructuraActual().getClaves());
            
            if (eliminado) {
                response.put("mensaje", "Clave " + clave + " eliminada correctamente");
            } else {
                response.put("mensaje", "No se pudo eliminar la clave");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/eliminar-estructura")
    public ResponseEntity<Map<String, Object>> eliminarEstructura() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            estructuraService.eliminarEstructura();
            response.put("exito", true);
            response.put("mensaje", "Estructura eliminada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/estado")
    public ResponseEntity<Map<String, Object>> obtenerEstado() {
        Map<String, Object> response = new HashMap<>();
        
        if (estructuraService.tieneEstructura()) {
            response.put("tieneEstructura", true);
            response.put("tipo", estructuraService.getTipoEstructura());
            response.put("claves", estructuraService.getEstructuraActual().getClaves());
            response.put("tamano", estructuraService.getEstructuraActual().getTamano());
        } else {
            response.put("tieneEstructura", false);
        }
        
        return ResponseEntity.ok(response);
    }
}
