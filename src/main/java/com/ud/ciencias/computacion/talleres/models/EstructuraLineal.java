package com.ud.ciencias.computacion.talleres.models;

import java.util.ArrayList;
import java.util.List;

public class EstructuraLineal {
    private List<Integer> claves;
    private int tamanoMaximo;
    private boolean estatico;
    
    public EstructuraLineal(int tamano, boolean estatico) {
        this.claves = new ArrayList<>();
        this.tamanoMaximo = tamano;
        this.estatico = estatico;
    }
    
    public boolean insertar(int clave) {
        // Verificar si ya existe
        if (claves.contains(clave)) {
            return false;
        }
        
        // Verificar si está lleno y es estático
        if (estatico && claves.size() >= tamanoMaximo) {
            return false;
        }
        
        // Insertar en posición ordenada
        int posicion = 0;
        while (posicion < claves.size() && claves.get(posicion) < clave) {
            posicion++;
        }
        
        claves.add(posicion, clave);
        return true;
    }
    
    public BusquedaResultado buscarLineal(int clave) {
        BusquedaResultado resultado = new BusquedaResultado();
        
        for (int i = 0; i < claves.size(); i++) {
            resultado.agregarPaso(i, claves.get(i), claves.get(i) == clave);
            
            if (claves.get(i) == clave) {
                resultado.setEncontrado(true);
                resultado.setPosicion(i);
                return resultado;
            }
        }
        
        resultado.setEncontrado(false);
        return resultado;
    }
    
    public BusquedaResultado buscarBinario(int clave) {
        BusquedaResultado resultado = new BusquedaResultado();
        int izquierda = 0;
        int derecha = claves.size() - 1;
        
        while (izquierda <= derecha) {
            int medio = izquierda + (derecha - izquierda) / 2;
            int valorMedio = claves.get(medio);
            
            // Registrar paso
            resultado.agregarPasoBinario(izquierda, derecha, medio, valorMedio, 
                                        clave == valorMedio, clave > valorMedio);
            
            if (valorMedio == clave) {
                resultado.setEncontrado(true);
                resultado.setPosicion(medio);
                return resultado;
            } else if (clave > valorMedio) {
                izquierda = medio + 1;
            } else {
                derecha = medio - 1;
            }
        }
        
        resultado.setEncontrado(false);
        return resultado;
    }
    
    public boolean eliminar(int clave) {
        for (int i = 0; i < claves.size(); i++) {
            if (claves.get(i) == clave) {
                claves.remove(i);
                return true;
            }
        }
        return false;
    }
    
    public void limpiar() {
        claves.clear();
    }
    
    public List<Integer> getClaves() {
        return claves;
    }
    
    public int getTamano() {
        return claves.size();
    }
    
    public boolean estaLleno() {
        return estatico && claves.size() >= tamanoMaximo;
    }
}
