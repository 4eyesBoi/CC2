package com.ud.ciencias.computacion.talleres.models;

import java.util.ArrayList;
import java.util.List;

public class BusquedaResultado {
    private boolean encontrado;
    private int posicion;
    private List<PasoBusqueda> pasos;
    
    public BusquedaResultado() {
        this.pasos = new ArrayList<>();
        this.encontrado = false;
        this.posicion = -1;
    }
    
    public void agregarPaso(int indice, int valor, boolean esCoincidencia) {
        PasoBusqueda paso = new PasoBusqueda();
        paso.setTipo("lineal");
        paso.setIndiceActual(indice);
        paso.setValorActual(valor);
        paso.setEsCoincidencia(esCoincidencia);
        pasos.add(paso);
    }
    
    public void agregarPasoBinario(int izq, int der, int medio, int valor, 
                                   boolean esCoincidencia, boolean irDerecha) {
        PasoBusqueda paso = new PasoBusqueda();
        paso.setTipo("binaria");
        paso.setIzquierda(izq);
        paso.setDerecha(der);
        paso.setMedio(medio);
        paso.setValorActual(valor);
        paso.setEsCoincidencia(esCoincidencia);
        paso.setIrDerecha(irDerecha);
        pasos.add(paso);
    }
    
    public boolean isEncontrado() {
        return encontrado;
    }
    
    public void setEncontrado(boolean encontrado) {
        this.encontrado = encontrado;
    }
    
    public int getPosicion() {
        return posicion;
    }
    
    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }
    
    public List<PasoBusqueda> getPasos() {
        return pasos;
    }
    
    public static class PasoBusqueda {
        private String tipo; // "lineal" o "binaria"
        private int indiceActual;
        private int valorActual;
        private boolean esCoincidencia;
        private int izquierda;
        private int derecha;
        private int medio;
        private boolean irDerecha;
        
        // Getters y Setters
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        
        public int getIndiceActual() { return indiceActual; }
        public void setIndiceActual(int indiceActual) { this.indiceActual = indiceActual; }
        
        public int getValorActual() { return valorActual; }
        public void setValorActual(int valorActual) { this.valorActual = valorActual; }
        
        public boolean isEsCoincidencia() { return esCoincidencia; }
        public void setEsCoincidencia(boolean esCoincidencia) { this.esCoincidencia = esCoincidencia; }
        
        public int getIzquierda() { return izquierda; }
        public void setIzquierda(int izquierda) { this.izquierda = izquierda; }
        
        public int getDerecha() { return derecha; }
        public void setDerecha(int derecha) { this.derecha = derecha; }
        
        public int getMedio() { return medio; }
        public void setMedio(int medio) { this.medio = medio; }
        
        public boolean isIrDerecha() { return irDerecha; }
        public void setIrDerecha(boolean irDerecha) { this.irDerecha = irDerecha; }
    }
}
