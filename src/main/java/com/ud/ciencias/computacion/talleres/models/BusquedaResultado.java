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
    
    public void agregarPaso(int indice, String valor, boolean esCoincidencia) {
        PasoBusqueda paso = new PasoBusqueda();
        paso.setTipo("lineal");
        paso.setIndiceActual(indice);
        paso.setValorActual(valor);
        paso.setEsCoincidencia(esCoincidencia);
        pasos.add(paso);
    }
    
    public void agregarPasoBinario(int izq, int der, int medio, String valor, 
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
    
    // Métodos para pasos de bloques
    public void agregarPasoBloqueScan(int numBloque, int bloqueInicio, int bloqueFinal, 
                                      String valorMedio, boolean esCoincidencia) {
        PasoBusqueda paso = new PasoBusqueda();
        paso.setTipo("bloque-scan");
        paso.setEtapaAlgoritmo("bloque_seleccion");
        paso.setNumBloque(numBloque);
        paso.setBloqueInicio(bloqueInicio);
        paso.setBloqueFinal(bloqueFinal);
        paso.setIndiceActual(bloqueInicio + (bloqueFinal - bloqueInicio) / 2); // índice visual (medio)
        paso.setValorActual(valorMedio);
        paso.setEsCoincidencia(esCoincidencia);
        pasos.add(paso);
    }
    
    public void agregarPasoBusquedaInternaLineal(int numBloque, int indice, String valor, boolean esCoincidencia) {
        PasoBusqueda paso = new PasoBusqueda();
        paso.setTipo("interno-bloque");
        paso.setEtapaAlgoritmo("busqueda_interna");
        paso.setNumBloque(numBloque);
        paso.setIndiceActual(indice);
        paso.setValorActual(valor);
        paso.setEsCoincidencia(esCoincidencia);
        pasos.add(paso);
    }
    
    public void agregarPasoBusquedaInternaBinaria(int numBloque, int izq, int der, int medio, 
                                                   String valor, boolean esCoincidencia, 
                                                   String comparacion) {
        PasoBusqueda paso = new PasoBusqueda();
        paso.setTipo("interno-bloque-binaria");
        paso.setEtapaAlgoritmo("busqueda_interna");
        paso.setNumBloque(numBloque);
        paso.setIzquierda(izq);
        paso.setDerecha(der);
        paso.setMedio(medio);
        paso.setValorActual(valor);
        paso.setEsCoincidencia(esCoincidencia);
        paso.setComparacionResultado(comparacion);
        pasos.add(paso);
    }
    
    public void agregarPasoHashCalculo(String alg, String clave, long claveNumero, 
                                       int modulo, int indiceCalculado, String formula) {
        PasoBusqueda paso = new PasoBusqueda();
        paso.setTipo("hash");
        paso.setEtapaAlgoritmo("hash_calculo");
        paso.setValorActual(clave);
        paso.setFormulaHash(formula);
        paso.setIndiceHashCalculado(indiceCalculado);
        paso.setIndiceActual(indiceCalculado); // para visualización
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
        private String tipo; // "lineal", "binaria", "hash", "bloque-scan", "interno-bloque"
        private int indiceActual;
        private String valorActual;
        private boolean esCoincidencia;
        private int izquierda;
        private int derecha;
        private int medio;
        private boolean irDerecha;
        
        // Información de bloques
        private Integer numBloque; // Número del bloque siendo procesado
        private Integer bloqueInicio; // Índice de inicio del bloque
        private Integer bloqueFinal;  // Índice de final del bloque
        private String etapaAlgoritmo; // "hash_calculo", "bloque_seleccion", "busqueda_interna", etc.
        private String formulaHash; // Descripción de la fórmula aplicada: "k mod m = X mod Y = Z"
        private Integer indiceHashCalculado; // El índice resultante del hash
        private String comparacionResultado; // "menor", "mayor", "igual" para binaria
        
        // Getters y Setters
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        
        public int getIndiceActual() { return indiceActual; }
        public void setIndiceActual(int indiceActual) { this.indiceActual = indiceActual; }
        
        public String getValorActual() { return valorActual; }
        public void setValorActual(String valorActual) { this.valorActual = valorActual; }
        
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
        
        public Integer getNumBloque() { return numBloque; }
        public void setNumBloque(Integer numBloque) { this.numBloque = numBloque; }
        
        public Integer getBloqueInicio() { return bloqueInicio; }
        public void setBloqueInicio(Integer bloqueInicio) { this.bloqueInicio = bloqueInicio; }
        
        public Integer getBloqueFinal() { return bloqueFinal; }
        public void setBloqueFinal(Integer bloqueFinal) { this.bloqueFinal = bloqueFinal; }
        
        public String getEtapaAlgoritmo() { return etapaAlgoritmo; }
        public void setEtapaAlgoritmo(String etapaAlgoritmo) { this.etapaAlgoritmo = etapaAlgoritmo; }
        
        public String getFormulaHash() { return formulaHash; }
        public void setFormulaHash(String formulaHash) { this.formulaHash = formulaHash; }
        
        public Integer getIndiceHashCalculado() { return indiceHashCalculado; }
        public void setIndiceHashCalculado(Integer indiceHashCalculado) { this.indiceHashCalculado = indiceHashCalculado; }
        
        public String getComparacionResultado() { return comparacionResultado; }
        public void setComparacionResultado(String comparacionResultado) { this.comparacionResultado = comparacionResultado; }
    }
}
