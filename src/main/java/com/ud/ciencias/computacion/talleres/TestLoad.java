package com.ud.ciencias.computacion.talleres;

public class TestLoad {
    public static void main(String[] args) throws Exception {
        System.out.println("loading class...");
        Class<?> c = Class.forName("com.ud.ciencias.computacion.talleres.models.BusquedaResultado");
        System.out.println("loaded " + c);
    }
}