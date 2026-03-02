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
            @RequestParam String tipeLimite,
            @RequestParam(required = false) Integer numBloques,
            @RequestParam(required = false) String alg,
            @RequestParam(required = false) Integer modulo) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean exito;
            if (tipo.startsWith("hash") && alg != null && !tipo.endsWith("-bloques")) {
                // internal hash, still use older helper
                exito = estructuraService.crearEstructuraHash(tamano, tamanoClaves, alg, modulo);
            } else {
                exito = estructuraService.crearEstructura(tipo, tamano, tamanoClaves, tipeLimite, numBloques, modulo);
            }
            
            if (exito) {
                response.put("exito", true);
                response.put("mensaje", "Estructura " + tipo + " creada correctamente");
                response.put("claves", estructuraService.obtenerClaves());
                response.put("tamano", estructuraService.getTamanoMaximo());
                response.put("tamanoClaves", estructuraService.getTamanoClaves());
                response.put("numBloques", estructuraService.getNumBloques());
                response.put("logs", estructuraService.obtenerYLimpiarLogs());
                return ResponseEntity.ok(response);
            } else {
                response.put("exito", false);
                response.put("mensaje", "Error al crear la estructura");
                response.put("logs", estructuraService.obtenerYLimpiarLogs());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error: " + e.getMessage());
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/insertar")
    public ResponseEntity<Map<String, Object>> insertarClave(@RequestParam String clave) {
        Map<String, Object> response = new HashMap<>();
        
        if (!estructuraService.tieneEstructura()) {
            response.put("exito", false);
            response.put("mensaje", "Debe crear una estructura antes de insertar");
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // validar tamaño de caracteres antes de intentar insertar
            if (!estructuraService.validarClave(clave)) {
                response.put("exito", false);
                response.put("mensaje", "La clave debe tener exactamente " + estructuraService.getTamanoClaves() + " caracteres");
                response.put("claves", estructuraService.obtenerClaves());
                response.put("logs", estructuraService.obtenerYLimpiarLogs());
                return ResponseEntity.ok(response);
            }

            // precompute pasos de comparación para la animación
            BusquedaResultado pasosAnim;
            java.util.List<String> antes = new java.util.ArrayList<>(estructuraService.obtenerClaves());
            if (estructuraService.getTipoEstructura() != null && estructuraService.getTipoEstructura().startsWith("hash")) {
                pasosAnim = estructuraService.generarPasosInsertarHash(clave);
            } else {
                pasosAnim = new BusquedaResultado();
                for (int i = 0; i < antes.size(); i++) {
                    String actual = antes.get(i);
                    pasosAnim.agregarPaso(i, actual, false);
                    if (actual.compareTo(clave) >= 0) {
                        break;
                    }
                }
            }

            boolean exito = estructuraService.insertarClave(clave);
            
            if (exito) {
                response.put("exito", true);
                response.put("mensaje", "Clave " + clave + " insertada correctamente");
                response.put("claves", estructuraService.obtenerClaves());
            } else {
                response.put("exito", false);
                response.put("mensaje", "No se pudo insertar la clave. Puede estar duplicada o la estructura estar llena");
                response.put("claves", estructuraService.obtenerClaves());
            }
            response.put("pasos", pasosAnim.getPasos());
            response.put("clavesAntes", antes);
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error: " + e.getMessage());
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/buscar")
    public ResponseEntity<Map<String, Object>> buscarClave(
            @RequestParam String clave,
            @RequestParam(required = false) Integer numBloques,
            @RequestParam(defaultValue = "false") boolean binarioEnBloque,
            @RequestParam(required = false) Integer modulo) {
        Map<String, Object> response = new HashMap<>();
        
        if (!estructuraService.tieneEstructura()) {
            response.put("exito", false);
            response.put("mensaje", "Debe crear una estructura primero");
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // check length of clave
            if (!estructuraService.validarClave(clave)) {
                response.put("exito", false);
                response.put("mensaje", "La clave debe tener exactamente " + estructuraService.getTamanoClaves() + " caracteres");
                response.put("logs", estructuraService.obtenerYLimpiarLogs());
                return ResponseEntity.ok(response);
            }

            BusquedaResultado resultado = estructuraService.buscarClave(clave, numBloques, binarioEnBloque, modulo);
            
            response.put("exito", true);
            response.put("encontrado", resultado.isEncontrado());
            response.put("posicion", resultado.getPosicion());
            response.put("pasos", resultado.getPasos());
            response.put("claves", estructuraService.getEstructuraActual().getClaves());
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
            
            if (resultado.isEncontrado()) {
                response.put("mensaje", "Clave " + clave + " encontrada en posición " + resultado.getPosicion());
            } else {
                response.put("mensaje", "Clave " + clave + " no encontrada");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error: " + e.getMessage());
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/eliminar")
    public ResponseEntity<Map<String, Object>> eliminarClave(
            @RequestParam String clave,
            @RequestParam(required = false) Integer numBloques,
            @RequestParam(defaultValue = "false") boolean binarioEnBloque,
            @RequestParam(required = false) Integer modulo) {
        Map<String, Object> response = new HashMap<>();
        
        if (!estructuraService.tieneEstructura()) {
            response.put("exito", false);
            response.put("mensaje", "Debe crear una estructura primero");
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // validar longitud primero
            if (!estructuraService.validarClave(clave)) {
                response.put("exito", false);
                response.put("mensaje", "La clave debe tener exactamente " + estructuraService.getTamanoClaves() + " caracteres");
                response.put("logs", estructuraService.obtenerYLimpiarLogs());
                return ResponseEntity.ok(response);
            }
            // snapshot antes de eliminar
            java.util.List<String> clavesAntes = new java.util.ArrayList<>(
                    estructuraService.obtenerClaves());

            // Primero buscar para obtener los pasos (usa el estado actual)
            BusquedaResultado resultado = estructuraService.buscarClave(clave, numBloques, binarioEnBloque, modulo);
            
            // then remove if found
            boolean eliminado = false;
            if (resultado.isEncontrado()) {
                eliminado = estructuraService.eliminarClave(clave);
            }
            
            java.util.List<String> clavesDespues = new java.util.ArrayList<>(
                    estructuraService.obtenerClaves());
            
            response.put("exito", eliminado);
            response.put("encontrado", resultado.isEncontrado());
            response.put("pasos", resultado.getPasos());
            response.put("clavesAntes", clavesAntes);
            response.put("clavesDespues", clavesDespues);
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
            
            if (eliminado) {
                response.put("mensaje", "Clave " + clave + " eliminada correctamente");
            } else {
                response.put("mensaje", "No se pudo eliminar la clave");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error: " + e.getMessage());
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
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
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error: " + e.getMessage());
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
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
            response.put("tamano", estructuraService.getTamanoMaximo());
            response.put("tamanoClaves", estructuraService.getTamanoClaves());
            response.put("numBloques", estructuraService.getNumBloques());
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
        } else {
            response.put("tieneEstructura", false);
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/hash")
    public ResponseEntity<Map<String, Object>> aplicarHash(
            @RequestParam String alg,
            @RequestParam String clave,
            @RequestParam(required = false) Integer modulo) {
        Map<String, Object> response = new HashMap<>();
        if (!estructuraService.tieneEstructura()) {
            response.put("exito", false);
            response.put("mensaje", "Debe crear una estructura primero");
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
            return ResponseEntity.badRequest().body(response);
        }

        try {
            if (!estructuraService.validarClave(clave)) {
                response.put("exito", false);
                response.put("mensaje", "La clave debe tener exactamente " + estructuraService.getTamanoClaves() + " caracteres");
                response.put("logs", estructuraService.obtenerYLimpiarLogs());
                return ResponseEntity.ok(response);
            }

            BusquedaResultado resultado = estructuraService.buscarHash(alg, clave, modulo);

            response.put("exito", true);
            response.put("encontrado", resultado.isEncontrado());
            response.put("posicion", resultado.getPosicion());
            response.put("pasos", resultado.getPasos());
            response.put("claves", estructuraService.getEstructuraActual().getClaves());
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
            response.put("mensaje", resultado.isEncontrado() ? "Clave encontrada en posición " + resultado.getPosicion() : "Clave no encontrada");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error: " + e.getMessage());
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/hash-bloques")
    public ResponseEntity<Map<String, Object>> aplicarHashBloques(
            @RequestParam String alg,
            @RequestParam String clave,
            @RequestParam int numBloques,
            @RequestParam(defaultValue = "false") boolean binarioEnBloque,
            @RequestParam(required = false) Integer modulo) {
        Map<String, Object> response = new HashMap<>();
        if (!estructuraService.tieneEstructura()) {
            response.put("exito", false);
            response.put("mensaje", "Debe crear una estructura primero");
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
            return ResponseEntity.badRequest().body(response);
        }

        try {
            if (!estructuraService.validarClave(clave)) {
                response.put("exito", false);
                response.put("mensaje", "La clave debe tener exactamente " + estructuraService.getTamanoClaves() + " caracteres");
                response.put("logs", estructuraService.obtenerYLimpiarLogs());
                return ResponseEntity.ok(response);
            }

            BusquedaResultado resultado = estructuraService.buscarHashPorBloques(alg, clave, numBloques, binarioEnBloque, modulo);

            response.put("exito", true);
            response.put("encontrado", resultado.isEncontrado());
            response.put("posicion", resultado.getPosicion());
            response.put("pasos", resultado.getPasos());
            response.put("claves", estructuraService.getEstructuraActual().getClaves());
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
            response.put("mensaje", resultado.isEncontrado() ? "Clave encontrada en posición " + resultado.getPosicion() : "Clave no encontrada");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error: " + e.getMessage());
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/buscar-bloques")
    public ResponseEntity<Map<String, Object>> buscarPorBloquesEndpoint(
            @RequestParam String clave,
            @RequestParam int numBloques,
            @RequestParam(defaultValue = "false") boolean binarioEnBloque) {
        Map<String, Object> response = new HashMap<>();
        if (!estructuraService.tieneEstructura()) {
            response.put("exito", false);
            response.put("mensaje", "Debe crear una estructura primero");
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
            return ResponseEntity.badRequest().body(response);
        }

        try {
            BusquedaResultado resultado = estructuraService.buscarPorBloques(clave, numBloques, binarioEnBloque);
            response.put("exito", true);
            response.put("encontrado", resultado.isEncontrado());
            response.put("posicion", resultado.getPosicion());
            response.put("pasos", resultado.getPasos());
            response.put("claves", estructuraService.getEstructuraActual().getClaves());
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
            if (resultado.isEncontrado()) {
                response.put("mensaje", "Clave encontrada en posición " + resultado.getPosicion());
            } else {
                response.put("mensaje", "Clave no encontrada");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error: " + e.getMessage());
            response.put("logs", estructuraService.obtenerYLimpiarLogs());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
