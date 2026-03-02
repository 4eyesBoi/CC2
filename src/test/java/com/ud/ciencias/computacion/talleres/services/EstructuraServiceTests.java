package com.ud.ciencias.computacion.talleres.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EstructuraServiceTests {

    private EstructuraService service;

    @BeforeEach
    public void setup() {
        service = new EstructuraService();
    }

    @Test
    void validarClaveExacta() {
        service.crearEstructura("lineal", 5, 3, "dinamico");
        assertTrue(service.validarClave("123"));
        assertFalse(service.validarClave("12"));
        assertFalse(service.validarClave("1234"));
    }

    @Test
    void insertarConTamanoIncorrectoNoAñade() {
        service.crearEstructura("lineal", 5, 2, "dinamico");
        boolean ok1 = service.insertarClave("10");
        assertTrue(ok1);
        boolean ok2 = service.insertarClave("123");
        assertFalse(ok2);
    }

    @Test
    void logsSeAcumulanYSeLimpiar() {
        service.crearEstructura("lineal", 3, 1, "estatico");
        service.insertarClave("1");
        java.util.List<String> logs = service.obtenerYLimpiarLogs();
        assertFalse(logs.isEmpty());
        // afterwards logs should be empty
        assertTrue(service.obtenerYLimpiarLogs().isEmpty());
    }

    @Test
    void aceptaAlfanumericoYConCeroInicial() {
        service.crearEstructura("lineal", 5, 2, "dinamico");
        assertTrue(service.validarClave("0A"));
        assertTrue(service.insertarClave("0A"));
        boolean ok = service.insertarClave("A0");
        assertTrue(ok);
        // ensure ordering lexicográfico: "0A" antes que "A0"
        java.util.List<String> lista = service.getEstructuraActual().getClaves();
        assertEquals("0A", lista.get(0));
        assertEquals("A0", lista.get(1));
    }

    @Test
    void computeHashFormulasAreCorrect() {
        service.crearEstructura("lineal", 10, 1, "estatico");
        assertEquals(123 % 10, service.computeHash("mod", "123"));
        assertEquals(25 % 10, service.computeHash("mod", "25"));
        service.crearEstructura("lineal", 100, 1, "estatico");
        // cuadrado
        assertEquals(16, service.computeHash("cuadrado", "4"));
        assertEquals(44, service.computeHash("cuadrado", "12"));
        // plegamiento: parts length == digits of m (3)
        assertEquals((123 + 456) % 100, service.computeHash("plegamiento", "123456"));
        // truncamiento: take last digits
        service.crearEstructura("lineal", 50, 1, "estatico");
        assertEquals(34, service.computeHash("truncamiento", "1234"));
    }

    @Test
    void externalHashStructureDetectsAsEstructura() {
        service.crearEstructura("hash-mod-bloques", 10, 1, "estatico", 2, null);
        assertTrue(service.tieneEstructura());
        service.eliminarEstructura();
        assertFalse(service.tieneEstructura());
    }}
