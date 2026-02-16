package com.ud.ciencias.computacion.talleres.services;

import org.springframework.stereotype.Service;

@Service
public class CrearService {
    

    public void crearArreglo(int tamArr, int tamClav) {
        System.out.println("creando arreglo con tamaño: " + tamArr + " y clave de tamaño: " + tamClav);
        int[] arreglo = new int[tamArr]; // Crear un arreglo de enteros con tamaño tamArr
        System.out.println("Arreglo creado: " + java.util.Arrays.toString(arreglo));
    }
}
