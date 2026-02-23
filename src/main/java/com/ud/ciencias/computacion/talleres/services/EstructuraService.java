package com.ud.ciencias.computacion.talleres.services;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import com.ud.ciencias.computacion.talleres.models.EstructuraLineal;
import com.ud.ciencias.computacion.talleres.models.BusquedaResultado;

@Service
@SessionScope
public class EstructuraService {
    private EstructuraLineal estructuraActual;
    private String tipoEstructura;
    private String tipoBusqueda; // "lineal" o "binaria"
    
    public boolean crearEstructura(String tipo, int tamano, int tamanoClaves, String tipeLimite) {
        try {
            this.tipoEstructura = tipo;
            boolean estatico = "estatico".equalsIgnoreCase(tipeLimite);
            
            if ("lineal".equalsIgnoreCase(tipo)) {
                this.estructuraActual = new EstructuraLineal(tamano, estatico);
                this.tipoBusqueda = "lineal";
            } else if ("binaria".equalsIgnoreCase(tipo)) {
                this.estructuraActual = new EstructuraLineal(tamano, estatico);
                this.tipoBusqueda = "binaria";
            } else {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean insertarClave(int clave) {
        if (estructuraActual == null) {
            return false;
        }
        return estructuraActual.insertar(clave);
    }
    
    public BusquedaResultado buscarClave(int clave) {
        if (estructuraActual == null) {
            return null;
        }
        
        if ("binaria".equalsIgnoreCase(tipoBusqueda)) {
            return estructuraActual.buscarBinario(clave);
        } else {
            return estructuraActual.buscarLineal(clave);
        }
    }
    
    public boolean eliminarClave(int clave) {
        if (estructuraActual == null) {
            return false;
        }
        return estructuraActual.eliminar(clave);
    }
    
    public void limpiarEstructura() {
        if (estructuraActual != null) {
            estructuraActual.limpiar();
        }
    }
    
    public void eliminarEstructura() {
        this.estructuraActual = null;
        this.tipoEstructura = null;
        this.tipoBusqueda = null;
    }
    
    public EstructuraLineal getEstructuraActual() {
        return estructuraActual;
    }
    
    public String getTipoEstructura() {
        return tipoEstructura;
    }
    
    public String getTipoBusqueda() {
        return tipoBusqueda;
    }
    
    public boolean tieneEstructura() {
        return estructuraActual != null;
    }
}
