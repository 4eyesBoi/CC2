package com.ud.ciencias.computacion.talleres.services;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import com.ud.ciencias.computacion.talleres.models.EstructuraLineal;
import com.ud.ciencias.computacion.talleres.models.BusquedaResultado;

@Service
public class EstructuraService {
    private EstructuraLineal estructuraActual;
    // hash-specific internal storage
    private String[] tablaHash;
    private String hashAlg;
    private int hashModulo;

    private String tipoEstructura;
    private String tipoBusqueda; // "lineal" o "binaria" or other descriptor
    private int tamanoMaximo;
    private int tamanoClaves;
    // para búsquedas por bloques
    private int numBloques;
    
    // pequeño log en memoria para enviar al frontend y también imprimiendo en consola
    private java.util.List<String> logs = new java.util.ArrayList<>();

    private void log(String mensaje) {
        // imprime en consola del servidor y acumula en la lista
        System.out.println(mensaje);
        logs.add(mensaje);
    }

    /** Devuelve los logs acumulados y limpia el buffer */
    public java.util.List<String> obtenerYLimpiarLogs() {
        java.util.List<String> copia = new java.util.ArrayList<>(logs);
        logs.clear();
        return copia;
    }

    /**
     * Sobrecarga para compatibilidad con versiones anteriores / tests.
     */
    public boolean crearEstructura(String tipo, int tamano, int tamanoClaves, String tipeLimite) {
        return crearEstructura(tipo, tamano, tamanoClaves, tipeLimite, null, null);
    }

    /**
     * Crea una estructura en el servicio.
     * - para lineal/binaria normales utiliza estructuraActual
     * - para tipos que terminan en "-bloques" también usa estructuraActual, pero
     *   registra numBloques y conserva el tipo para búsquedas especiales
     * - no crea tablaHash a menos que el tipo sea exactamente "hash"
     *
     * @param tipo nombre completo del método (ej. "lineal","binaria","lineal-bloques","hash-mod-bloques" etc)
     * @param tamano cantidad de posiciones totales
     * @param tamanoClaves longitud fija de las claves
     * @param tipeLimite "estatico" o "dinamico" (aplicable a lineal/binaria)
     * @param numBloques número de bloques cuando aplique (puede ser null)
     * @param modulo módulo para funciones hash externas e internas (puede ser null)
     */
    public boolean crearEstructura(String tipo, int tamano, int tamanoClaves, String tipeLimite,
                                   Integer numBloques, Integer modulo) {
        log("[Servicio] crearEstructura -> tipo=" + tipo + " tamano=" + tamano + " tamanoClaves=" + tamanoClaves + " limite=" + tipeLimite + " numBloques=" + numBloques + " modulo=" + modulo);
        try {
            this.tipoEstructura = tipo;
            this.tamanoMaximo = tamano;
            this.tamanoClaves = tamanoClaves;
            boolean estatico = "estatico".equalsIgnoreCase(tipeLimite);
            this.numBloques = (numBloques != null && numBloques > 0) ? numBloques : 1;

            // limpiar cualquier estado previo de hash
            this.tablaHash = null;
            this.hashAlg = null;
            this.hashModulo = 0;

            if (tipo.equalsIgnoreCase("hash")) {
                // internal hash table creation
                this.tablaHash = new String[tamano];
                for (int i = 0; i < tamano; i++) tablaHash[i] = "";
                this.tipoBusqueda = null; // no búsqueda lineal/binaria
                if (modulo != null && modulo > 0) this.hashModulo = modulo;
            } else if (tipo.startsWith("hash-") && tipo.endsWith("-bloques")) {
                // external hash por bloques: use linea structure for datos, keep algoritmo
                this.estructuraActual = new EstructuraLineal(tamano, estatico);
                this.hashAlg = tipo.substring(5, tipo.length() - 8);
                this.hashModulo = (modulo != null && modulo > 0) ? modulo : tamano;
                this.tipoBusqueda = tipo;
            } else if (tipo.equalsIgnoreCase("lineal") || tipo.equalsIgnoreCase("binaria")
                    || tipo.equalsIgnoreCase("lineal-bloques") || tipo.equalsIgnoreCase("binaria-bloques")) {
                this.estructuraActual = new EstructuraLineal(tamano, estatico);
                this.tipoBusqueda = tipo;
            } else {
                log("[Servicio] tipo de estructura desconocido: " + tipo);
                return false;
            }

            return true;
        } catch (Exception e) {
            log("[Servicio] error al crear estructura: " + e.getMessage());
            return false;
        }
    }
    
    public boolean insertarClave(String clave) {
        // if no estructura at all, bail out
        if (!tieneEstructura()) {
            log("[Servicio] intento de insertar sin estructura");
            return false;
        }
        // only internal hash uses tablaHash as a separate table
        if ("hash".equalsIgnoreCase(tipoEstructura)) {
            return insertarClaveHash(clave);
        }
        if (estructuraActual == null) {
            // this should not happen because tieneEstructura() already covered
            log("[Servicio] intento de insertar sin estructura lineal/base");
            return false;
        }
        if (!validarClave(clave)) {
            log("[Servicio] clave " + clave + " no cumple tamaño requerido de " + tamanoClaves + " caracteres");
            return false;
        }
        log("[Servicio] insertando clave " + clave + " en " + tipoEstructura);
        boolean exito = estructuraActual.insertar(clave);
        log("[Servicio] resultado inserción: " + exito);
        return exito;
    }
    
    /**
     * Búsqueda genérica que sabe distinguir entre los diferentes tipos de
     * estructuras, incluyendo comportamientos por bloques y hash por bloques.
     *
     * @param clave clave a buscar
     * @param numBloques opcional, override del número de bloques si se proporciona
     * @param usarBinario indica si dentro de cada bloque se debe usar búsqueda
     *                    binaria en lugar de lineal
     * @param modulo opcional para funciones hash externas (sobrepasa hashModulo)
     */
    public BusquedaResultado buscarClave(String clave, Integer numBloques,
                                         Boolean usarBinario, Integer modulo) {
        if (!tieneEstructura()) {
            log("[Servicio] intento de buscar sin estructura");
            return new BusquedaResultado();
        }

        log("[Servicio] buscarClave-> tipo=" + tipoEstructura + " clave=" + clave
            + " numBloques=" + numBloques + " usarBinario=" + usarBinario + " modulo=" + modulo);

        // internal hash (no bloques)
        if ("hash".equalsIgnoreCase(tipoEstructura)) {
            return buscarHashInterno(clave);
        }

        // external hash por bloques
        if (tipoEstructura.startsWith("hash-") && tipoEstructura.endsWith("-bloques")) {
            String alg = tipoEstructura.substring(5, tipoEstructura.length() - 8);
            int bloques = (numBloques != null && numBloques > 0) ? numBloques : this.numBloques;
            boolean bin = (usarBinario != null) ? usarBinario : false;
            int mod = (modulo != null && modulo > 0) ? modulo : this.hashModulo;
            return buscarHashPorBloques(alg, clave, bloques, bin, mod);
        }

        // búsqueda por bloques simple
        if (tipoEstructura.endsWith("-bloques")) {
            int bloques = (numBloques != null && numBloques > 0) ? numBloques : this.numBloques;
            boolean bin = (usarBinario != null) ? usarBinario : false;
            return buscarPorBloques(clave, bloques, bin);
        }

        // lineal/binaria normal
        log("[Servicio] buscando clave " + clave + " mediante " + tipoBusqueda);
        BusquedaResultado resultado;
        if ("binaria".equalsIgnoreCase(tipoBusqueda)) {
            resultado = estructuraActual.buscarBinario(clave);
        } else {
            resultado = estructuraActual.buscarLineal(clave);
        }
        log("[Servicio] búsqueda encontró? " + resultado.isEncontrado() + " posición " + resultado.getPosicion());
        return resultado;
    }
    
    public boolean eliminarClave(String clave) {
        if (!tieneEstructura()) {
            log("[Servicio] intento de eliminar sin estructura");
            return false;
        }
        // only internal hash uses tablaHash
        if ("hash".equalsIgnoreCase(tipoEstructura)) {
            return eliminarClaveHash(clave);
        }
        if (estructuraActual == null) {
            log("[Servicio] intento de eliminar sin estructura base");
            return false;
        }
        log("[Servicio] eliminando clave " + clave + " en " + tipoEstructura);
        boolean eliminado = estructuraActual.eliminar(clave);
        log("[Servicio] resultado eliminación: " + eliminado);
        return eliminado;
    }
    
    public void limpiarEstructura() {
        if (estructuraActual != null) {
            log("[Servicio] limpiando estructura");
            estructuraActual.limpiar();
        }
    }
    
    public void eliminarEstructura() {
        this.estructuraActual = null;
        this.tipoEstructura = null;
        this.tipoBusqueda = null;
        this.tablaHash = null;
        this.hashAlg = null;
        this.hashModulo = 0;
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

    public int getTamanoMaximo() {
        return tamanoMaximo;
    }

    public int getTamanoClaves() {
        return tamanoClaves;
    }

    public int getNumBloques() {
        return numBloques;
    }
    
    public boolean tieneEstructura() {
        if (tipoEstructura != null) {
            if (tipoEstructura.equalsIgnoreCase("hash")) {
                return tablaHash != null;
            }
            if (tipoEstructura.startsWith("hash-") && tipoEstructura.endsWith("-bloques")) {
                // external hash-bloques uses estructuraActual
                return estructuraActual != null;
            }
            if (tipoEstructura.endsWith("-bloques")) {
                return estructuraActual != null;
            }
        }
        return estructuraActual != null;
    }

    /** Verifica que la clave tenga el número exacto de caracteres especificado al crearla */
    public boolean validarClave(String clave) {
        if (clave == null) return false;
        return clave.length() == tamanoClaves;
    }

    // --- Hash helpers (operate on current estructura snapshot)
    /**
     * Calcula el hash con FÓRMULA DETALLADA para animación paso-a-paso.
     * Retorna un objeto que contiene el índice y una descripción de la fórmula.
     */
    public static class HashResultado {
        public int indiceCalculado;
        public String formula; // "4 mod 10 = 4" o similar
        
        public HashResultado(int indice, String formula) {
            this.indiceCalculado = indice;
            this.formula = formula;
        }
    }
    
    /**
     * Calcula índice hash con fórmula detallada para visualización.
     * @return objeto con índice calculado y descripción de la fórmula aplicada
     */
    public HashResultado computeHashConFormula(String alg, String clave) {
        if (tamanoMaximo <= 0) {
            log("[Servicio] computeHashConFormula sin tabla inicializada");
            throw new IllegalStateException("Tabla no inicializada");
        }
        long k;
        try {
            k = Long.parseLong(clave);
        } catch (NumberFormatException e) {
            k = 0;
            for (char c : clave.toCharArray()) {
                k = k * 10 + (c & 0xF);
            }
        }
        int m = tamanoMaximo;
        long result;
        String formula = "";
        
        switch (alg) {
            case "mod":
                // h(k) = k mod m
                result = Math.floorMod(k, m);
                formula = k + " mod " + m + " = " + result;
                break;
            case "cuadrado":
                long sq = k * k;
                String s = Long.toString(sq);
                int len = Integer.toString(m).length();
                int start = Math.max(0, (s.length() - len) / 2);
                String mid = s.substring(start, Math.min(start + len, s.length()));
                long val = Long.parseLong(mid);
                result = val % m;
                formula = k + "² = " + sq + " → dígitos [" + start + ":" + (start + len) + "] = " + mid + " mod " + m + " = " + result;
                break;
            case "plegamiento":
                String str = Long.toString(Math.abs(k));
                int partSize = Integer.toString(m).length();
                long sum = 0;
                StringBuilder parts = new StringBuilder();
                for (int i = 0; i < str.length(); i += partSize) {
                    int end = Math.min(i + partSize, str.length());
                    long part = Long.parseLong(str.substring(i, end));
                    if (i > 0) parts.append(" + ");
                    parts.append(part);
                    sum += part;
                }
                result = Math.floorMod(sum, m);
                formula = "(" + parts + ") mod " + m + " = " + sum + " mod " + m + " = " + result;
                break;
            case "truncamiento":
                String t = Long.toString(Math.abs(k));
                int take = Integer.toString(m).length();
                if (t.length() > take) {
                    t = t.substring(t.length() - take);
                }
                long num = t.isEmpty() ? 0 : Long.parseLong(t);
                result = Math.floorMod(num, m);
                formula = "últimos " + take + " dígitos de " + k + " = " + t + " mod " + m + " = " + result;
                break;
            default:
                result = Math.floorMod(k, m);
                formula = k + " mod " + m + " = " + result;
        }
        
        log("[Servicio] computeHashConFormula alg=" + alg + " clave=" + clave + " → " + (int)result + " (fórmula: " + formula + ")");
        return new HashResultado((int) result, formula);
    }
    
    public int computeHash(String alg, String clave) {
        return computeHashConFormula(alg, clave).indiceCalculado;
    }
    // create hash-specific structure with algorithm and optional modulo
    public boolean crearEstructuraHash(int tamano, int tamanoClaves, String alg, Integer modulo) {
        this.tipoEstructura = "hash-" + alg;
        this.tamanoMaximo = tamano;
        this.tamanoClaves = tamanoClaves;
        this.hashAlg = alg;
        this.hashModulo = (modulo != null && modulo > 0) ? modulo : tamano;
        this.tablaHash = new String[tamano];
        for (int i = 0; i < tamano; i++) tablaHash[i] = "";
        log("[Servicio] creado hash estructura alg=" + alg + " tamano=" + tamano + " modulo=" + this.hashModulo);
        return true;
    }

    private boolean insertarClaveHash(String clave) {
        if (tablaHash == null) {
            log("[Servicio] intento de insertar en hash sin estructura");
            return false;
        }
        if (!validarClave(clave)) {
            log("[Servicio] clave " + clave + " no cumple tamaño requerido");
            return false;
        }
        int index;
        try {
            index = computeHash(hashAlg, clave);
        } catch (IllegalStateException e) {
            log("[Servicio] no se puede calcular hash sin Tabla: " + e.getMessage());
            return false;
        }
        log("[Servicio] hash insertar iterando desde " + index);
        for (int i = 0; i < tamanoMaximo; i++) {
            int pos = (index + i) % tamanoMaximo;
            String existente = tablaHash[pos];
            if (existente == null || existente.isEmpty() || existente.equals(clave)) {
                if (existente != null && existente.equals(clave)) {
                    log("[Servicio] hash insertar clave ya existe en " + pos);
                    return false;
                }
                tablaHash[pos] = clave;
                log("[Servicio] hash inserted at " + pos);
                return true;
            }
        }
        log("[Servicio] hash tabla llena");
        return false;
    }

    private BusquedaResultado buscarHashInterno(String clave) {
        BusquedaResultado resultado = new BusquedaResultado();
        if (tablaHash == null) {
            log("[Servicio] buscarHashInterno sin estructura");
            resultado.setEncontrado(false);
            return resultado;
        }
        if (!validarClave(clave)) {
            log("[Servicio] buscarHashInterno clave inválida");
            resultado.setEncontrado(false);
            return resultado;
        }
        int index;
        try {
            index = computeHash(hashAlg, clave);
        } catch (IllegalStateException e) {
            log("[Servicio] buscarHashInterno fallo hash: " + e.getMessage());
            resultado.setEncontrado(false);
            return resultado;
        }
        for (int i = 0; i < tamanoMaximo; i++) {
            int pos = (index + i) % tamanoMaximo;
            String val = tablaHash[pos];
            resultado.agregarPaso(pos, val, val != null && val.equals(clave));
            if (val != null && val.equals(clave)) {
                resultado.setEncontrado(true);
                resultado.setPosicion(pos);
                log("[Servicio] buscarHashInterno encontrado en " + pos);
                return resultado;
            }
            if (val == null || val.isEmpty()) break;
        }
        resultado.setEncontrado(false);
        log("[Servicio] buscarHashInterno no encontrado");
        return resultado;
    }

    private boolean eliminarClaveHash(String clave) {
        if (tablaHash == null) {
            log("[Servicio] eliminarClaveHash sin estructura");
            return false;
        }
        if (!validarClave(clave)) {
            log("[Servicio] eliminarClaveHash clave inválida");
            return false;
        }
        int index = computeHash(hashAlg, clave);
        for (int i = 0; i < tamanoMaximo; i++) {
            int pos = (index + i) % tamanoMaximo;
            String val = tablaHash[pos];
            if (val != null && val.equals(clave)) {
                tablaHash[pos] = "";
                log("[Servicio] eliminado hash en " + pos);
                return true;
            }
            if (val == null || val.isEmpty()) break;
        }
        log("[Servicio] clave no encontrada para eliminar");
        return false;
    }

    public java.util.List<String> obtenerClaves() {
        if ("hash".equalsIgnoreCase(tipoEstructura)) {
            if (tablaHash == null) return new java.util.ArrayList<>();
            java.util.List<String> copia = new java.util.ArrayList<>(tamanoMaximo);
            for (int i = 0; i < tamanoMaximo; i++) {
                String v = tablaHash[i];
                copia.add(v == null ? "" : v);
            }
            return copia;
        } else if (estructuraActual != null) {
            return estructuraActual.getClaves();
        } else {
            return new java.util.ArrayList<>();
        }
    }
    /** Genera los pasos que tomaría insertar una clave en la tabla hash (sin modificarla). */
    public BusquedaResultado generarPasosInsertarHash(String clave) {
        BusquedaResultado resultado = new BusquedaResultado();
        if (tablaHash == null) {
            return resultado;
        }
        if (!validarClave(clave)) {
            return resultado;
        }
        int index = computeHash(hashAlg, clave);
        for (int i = 0; i < tamanoMaximo; i++) {
            int pos = (index + i) % tamanoMaximo;
            String val = tablaHash[pos];
            resultado.agregarPaso(pos, val, false);
            if (val == null || val.isEmpty() || val.equals(clave)) {
                break;
            }
        }
        return resultado;
    }
    /** Realiza una búsqueda usando una función hash: calcula índice y compara en esa posición. */
    public BusquedaResultado buscarHash(String alg, String clave, Integer modulo) {
        BusquedaResultado resultado = new BusquedaResultado();
        if (estructuraActual == null) {
            log("[Servicio] buscarHash sin estructura");
            resultado.setEncontrado(false);
            return resultado;
        }

        if (!validarClave(clave)) {
            log("[Servicio] buscarHash: clave inválida length=" + (clave == null ? 0 : clave.length()));
            resultado.setEncontrado(false);
            return resultado;
        }

        // modulo parameter ignored; always use tabla size
        int index = computeHash(alg, clave);

        java.util.List<String> claves = estructuraActual.getClaves();
        String valorEnIndice = (index >= 0 && index < claves.size()) ? claves.get(index) : null;

        // marcar el índice calculado como paso
        resultado.agregarPaso(index, valorEnIndice, valorEnIndice != null && valorEnIndice.equals(clave));
        if (valorEnIndice != null && valorEnIndice.equals(clave)) {
            resultado.setEncontrado(true);
            resultado.setPosicion(index);
            log("[Servicio] buscarHash: encontrado en index=" + index);
        } else {
            resultado.setEncontrado(false);
            resultado.setPosicion(-1);
            log("[Servicio] buscarHash: no encontrado en index=" + index);
        }

        return resultado;
    }

    /** Busca usando función hash y luego explora el bloque determinado por el índice hash */
    public BusquedaResultado buscarHashPorBloques(String alg, String clave, int numBloques, boolean usarBinarioEnBloque, Integer modulo) {
        BusquedaResultado resultado = new BusquedaResultado();
        if (estructuraActual == null) {
            log("[Servicio] buscarHashPorBloques sin estructura");
            resultado.setEncontrado(false);
            return resultado;
        }

        if (!validarClave(clave)) {
            log("[Servicio] buscarHashPorBloques: clave inválida length=" + (clave == null ? 0 : clave.length()));
            resultado.setEncontrado(false);
            return resultado;
        }

        java.util.List<String> claves = estructuraActual.getClaves();
        int n = claves.size();
        if (numBloques <= 0) numBloques = 1;

        // FASE 1: Calcular hash con fórmula detallada
        HashResultado hashRes = computeHashConFormula(alg, clave);
        int hashIndex = hashRes.indiceCalculado;
        String formulaHash = hashRes.formula;
        
        resultado.agregarPasoHashCalculo(alg, clave, 0, tamanoMaximo, hashIndex, formulaHash);
        log("[Servicio] buscarHashPorBloques: hash calculado=" + hashIndex + " fórmula=" + formulaHash);

        // FASE 2: Determinar bloque basado en índice hash
        int blockIndex = Math.floorMod(hashIndex, numBloques);
        int blockSize = (int) Math.ceil((double) Math.max(1, n) / numBloques);
        int start = blockIndex * blockSize;
        int end = Math.min(start + blockSize - 1, n - 1);
        
        if (start > end) {
            resultado.setEncontrado(false);
            log("[Servicio] buscarHashPorBloques: bloque vacío para blockIndex=" + blockIndex);
            return resultado;
        }

        // PASO: Mostrar bloque seleccionado por hash
        int medioBloque = start + (end - start) / 2;
        String valorMedio = claves.get(medioBloque);
        resultado.agregarPasoBloqueScan(blockIndex, start, end, valorMedio, false);
        log("[Servicio] buscarHashPorBloques: hash dirigió a bloque " + blockIndex + " rango[" + start + "," + end + "]");

        // FASE 3: Buscar dentro del bloque asignado
        if (usarBinarioEnBloque) {
            int iz = start, de = end;
            while (iz <= de) {
                int m = iz + (de - iz) / 2;
                String val = claves.get(m);
                String comparacion = val.compareTo(clave) < 0 ? "menor" : (val.equals(clave) ? "igual" : "mayor");
                resultado.agregarPasoBusquedaInternaBinaria(blockIndex, iz, de, m, val, val.equals(clave), comparacion);
                log("[Servicio] búsqueda interna (binaria) en bloque " + blockIndex + ": iz=" + iz + " de=" + de + " m=" + m);
                
                if (val.equals(clave)) {
                    resultado.setEncontrado(true);
                    resultado.setPosicion(m);
                    log("[Servicio] ¡ENCONTRADO en bloque " + blockIndex + " posición=" + m);
                    return resultado;
                } else if (val.compareTo(clave) < 0) {
                    iz = m + 1;
                } else {
                    de = m - 1;
                }
            }
        } else {
            for (int i = start; i <= end; i++) {
                String val = claves.get(i);
                resultado.agregarPasoBusquedaInternaLineal(blockIndex, i, val, val.equals(clave));
                log("[Servicio] búsqueda interna (lineal) en bloque " + blockIndex + ": i=" + i + " val=" + val);
                
                if (val.equals(clave)) {
                    resultado.setEncontrado(true);
                    resultado.setPosicion(i);
                    log("[Servicio] ¡ENCONTRADO en bloque " + blockIndex + " posición=" + i);
                    return resultado;
                }
            }
        }

        resultado.setEncontrado(false);
        log("[Servicio] buscarHashPorBloques: clave no encontrada en bloque " + blockIndex);
        return resultado;
    }

    // Buscar por bloques: divide la lista actual en bloques y realiza búsqueda por bloques
    public BusquedaResultado buscarPorBloques(String clave, int numBloques, boolean usarBinarioEnBloque) {
        BusquedaResultado resultado = new BusquedaResultado();
        if (estructuraActual == null) {
            log("[Servicio] buscarPorBloques sin estructura");
            resultado.setEncontrado(false);
            return resultado;
        }

        java.util.List<String> claves = estructuraActual.getClaves();
        int n = claves.size();
        if (numBloques <= 0) numBloques = 1;
        int blockSize = (int) Math.ceil((double) Math.max(1, n) / numBloques);

        log("[Servicio] buscarPorBloques: clave=" + clave + " numBloques=" + numBloques + " blockSize=" + blockSize + " usarBinario=" + usarBinarioEnBloque);

        // FASE 1: Recorrer bloques secuencialmente (LINEAL POR BLOQUES) o binariamente (BINARIA POR BLOQUES)
        if ("lineal-bloques".equalsIgnoreCase(tipoEstructura)) {
            // Búsqueda LINEAL por bloques: examina cada bloque secuencialmente
            for (int b = 0; b < numBloques; b++) {
                int start = b * blockSize;
                int end = Math.min(start + blockSize - 1, n - 1);
                if (start > end) break;

                // PASO: Mostrar bloque siendo examinado (índice visual = medio del bloque)
                int medioBloque = start + (end - start) / 2;
                String valorMedio = claves.get(medioBloque);
                resultado.agregarPasoBloqueScan(b, start, end, valorMedio, false);
                log("[Servicio] examinando bloque " + b + " rango[" + start + "," + end + "] valor_medio=" + valorMedio);

                // Verificar si la clave pudiera estar en este bloque
                String primero = claves.get(start);
                String ultimo = claves.get(end);
                if (clave.compareTo(primero) >= 0 && clave.compareTo(ultimo) <= 0) {
                    // FASE 2: Búsqueda dentro del bloque encontrado
                    log("[Servicio] clave está en rango de bloque " + b + ", buscando internamente...");
                    
                    if (usarBinarioEnBloque) {
                        // Búsqueda binaria dentro del bloque
                        int iz = start, de = end;
                        while (iz <= de) {
                            int m = iz + (de - iz) / 2;
                            String val = claves.get(m);
                            String comparacion = val.compareTo(clave) < 0 ? "menor" : (val.equals(clave) ? "igual" : "mayor");
                            resultado.agregarPasoBusquedaInternaBinaria(b, iz, de, m, val, val.equals(clave), comparacion);
                            log("[Servicio] búsqueda interna binaria en bloque " + b + ": iz=" + iz + " de=" + de + " m=" + m + " val=" + val + " comparacion=" + comparacion);
                            
                            if (val.equals(clave)) {
                                resultado.setEncontrado(true);
                                resultado.setPosicion(m);
                                log("[Servicio] ¡ENCONTRADO en bloque " + b + " posición=" + m);
                                return resultado;
                            } else if (val.compareTo(clave) < 0) {
                                iz = m + 1;
                            } else {
                                de = m - 1;
                            }
                        }
                    } else {
                        // Búsqueda lineal dentro del bloque
                        for (int i = start; i <= end; i++) {
                            String val = claves.get(i);
                            resultado.agregarPasoBusquedaInternaLineal(b, i, val, val.equals(clave));
                            log("[Servicio] búsqueda interna lineal en bloque " + b + ": i=" + i + " val=" + val);
                            
                            if (val.equals(clave)) {
                                resultado.setEncontrado(true);
                                resultado.setPosicion(i);
                                log("[Servicio] ¡ENCONTRADO en bloque " + b + " posición=" + i);
                                return resultado;
                            }
                        }
                    }
                }
            }

            resultado.setEncontrado(false);
            log("[Servicio] buscarPorBloques (lineal): no encontrado en ningún bloque");
            return resultado;
            
        } else if ("binaria-bloques".equalsIgnoreCase(tipoEstructura)) {
            // Búsqueda BINARIA por bloques: estrategia de divide-y-conquista en bloques
            int bloqueIz = 0, bloqueDer = numBloques - 1;
            
            while (bloqueIz <= bloqueDer) {
                int bloqueMid = bloqueIz + (bloqueDer - bloqueIz) / 2;
                int start = bloqueMid * blockSize;
                int end = Math.min(start + blockSize - 1, n - 1);
                if (start > end) {
                    bloqueDer = bloqueMid - 1;
                    continue;
                }

                // PASO: Mostrar bloque medio siendo examinado
                int medioBloque = start + (end - start) / 2;
                String valorMedio = claves.get(medioBloque);
                resultado.agregarPasoBloqueScan(bloqueMid, start, end, valorMedio, false);
                log("[Servicio] examinando bloque medio " + bloqueMid + " rango[" + start + "," + end + "]");

                String primero = claves.get(start);
                String ultimo = claves.get(end);
                
                // Decidir si buscar en este bloque o descartar mitad
                if (clave.compareTo(primero) >= 0 && clave.compareTo(ultimo) <= 0) {
                    // Clave puede estar en este bloque
                    log("[Servicio] clave en rango de bloque " + bloqueMid + ", búsqueda interna...");
                    
                    if (usarBinarioEnBloque) {
                        int iz = start, de = end;
                        while (iz <= de) {
                            int m = iz + (de - iz) / 2;
                            String val = claves.get(m);
                            String comparacion = val.compareTo(clave) < 0 ? "menor" : (val.equals(clave) ? "igual" : "mayor");
                            resultado.agregarPasoBusquedaInternaBinaria(bloqueMid, iz, de, m, val, val.equals(clave), comparacion);
                            
                            if (val.equals(clave)) {
                                resultado.setEncontrado(true);
                                resultado.setPosicion(m);
                                log("[Servicio] ¡ENCONTRADO en bloque " + bloqueMid + " posición=" + m);
                                return resultado;
                            } else if (val.compareTo(clave) < 0) {
                                iz = m + 1;
                            } else {
                                de = m - 1;
                            }
                        }
                    } else {
                        for (int i = start; i <= end; i++) {
                            String val = claves.get(i);
                            resultado.agregarPasoBusquedaInternaLineal(bloqueMid, i, val, val.equals(clave));
                            
                            if (val.equals(clave)) {
                                resultado.setEncontrado(true);
                                resultado.setPosicion(i);
                                log("[Servicio] ¡ENCONTRADO en bloque " + bloqueMid + " posición=" + i);
                                return resultado;
                            }
                        }
                    }
                    
                    // No encontrado en este bloque
                    resultado.setEncontrado(false);
                    log("[Servicio] no encontrado en rango de bloque " + bloqueMid);
                    return resultado;
                } else if (clave.compareTo(primero) < 0) {
                    // Descartar mitad derecha
                    bloqueDer = bloqueMid - 1;
                    log("[Servicio] clave < primero, descartando bloques derechos, bloqueDer=" + bloqueDer);
                } else {
                    // Descartar mitad izquierda
                    bloqueIz = bloqueMid + 1;
                    log("[Servicio] clave > ultimo, descartando bloques izquierdos, bloqueIz=" + bloqueIz);
                }
            }

            resultado.setEncontrado(false);
            log("[Servicio] buscarPorBloques (binaria): no encontrado");
            return resultado;
        }

        resultado.setEncontrado(false);
        return resultado;
    }
}
