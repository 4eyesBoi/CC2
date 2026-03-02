package com.ud.ciencias.computacion.talleres.models;

import java.util.ArrayList;
import java.util.List;

public class EstructuraLineal {
    private List<String> claves;
    private int tamanoMaximo;
    private boolean estatico;
    
    public EstructuraLineal(int tamano, boolean estatico) {
        this.claves = new ArrayList<>();
        this.tamanoMaximo = tamano;
        this.estatico = estatico;
    }
    
    public boolean insertar(String clave) {
        // Verificar si ya existe
        if (claves.contains(clave)) {
            return false;
        }
        
        // Verificar si está lleno y es estático
        if (estatico && claves.size() >= tamanoMaximo) {
            return false;
        }
        
        // Insertar en posición ordenada utilizando comparación lexicográfica
        int posicion = 0;
        while (posicion < claves.size() && claves.get(posicion).compareTo(clave) < 0) {
            posicion++;
        }
        
        claves.add(posicion, clave);
        return true;
    }
    
    public BusquedaResultado buscarLineal(String clave) {
        BusquedaResultado resultado = new BusquedaResultado();
        
        for (int i = 0; i < claves.size(); i++) {
            String actual = claves.get(i);
            resultado.agregarPaso(i, actual, actual.equals(clave));
            
            if (actual.equals(clave)) {
                resultado.setEncontrado(true);
                resultado.setPosicion(i);
                return resultado;
            }
        }
        
        resultado.setEncontrado(false);
        return resultado;
    }
    
    public BusquedaResultado buscarBinario(String clave) {
        BusquedaResultado resultado = new BusquedaResultado();
        int izquierda = 0;
        int derecha = claves.size() - 1;
        
        while (izquierda <= derecha) {
            int medio = izquierda + (derecha - izquierda) / 2;
            String valorMedio = claves.get(medio);
            
            // Registrar paso
            resultado.agregarPasoBinario(izquierda, derecha, medio, valorMedio, 
                                        valorMedio.equals(clave), valorMedio.compareTo(clave) < 0);
            
            if (valorMedio.equals(clave)) {
                resultado.setEncontrado(true);
                resultado.setPosicion(medio);
                return resultado;
            } else if (valorMedio.compareTo(clave) < 0) {
                izquierda = medio + 1;
            } else {
                derecha = medio - 1;
            }
        }
        
        resultado.setEncontrado(false);
        return resultado;
    }
    
    public boolean eliminar(String clave) {
        for (int i = 0; i < claves.size(); i++) {
            if (claves.get(i).equals(clave)) {
                claves.remove(i);
                return true;
            }
        }
        return false;
    }
    
    public void limpiar() {
        claves.clear();
    }
    
    public List<String> getClaves() {
        return claves;
    }
    
    public int getTamano() {
        return claves.size();
    }
    
    public boolean estaLleno() {
        return estatico && claves.size() >= tamanoMaximo;
    }
}
