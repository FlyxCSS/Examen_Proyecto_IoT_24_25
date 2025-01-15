package com.example.alumno.examen;

import java.util.HashMap;
import java.util.Map;

// 2. JAVA a)
public class Eleccion {
    private int anyo;
    private String partido;
    private String presidente;

    public Eleccion(int anyo, String partido, String presidente) {
        this.anyo = anyo;
        this.partido = partido;
        this.presidente = presidente;
    }

    public Eleccion() {
    // Para el Recycler
    }

    public int getAnyo() {
        return anyo;
    }

    public String getPartido() {
        return partido;
    }

    public String getPresidente() {
        return presidente;
    }

    public void setAnyo(int anyo) {
        this.anyo = anyo;
    }

    public void setPartido(String partido) {
        this.partido = partido;
    }

    public void setPresidente(String presidente) {
        this.presidente = presidente;
    }

    @Override
    public String toString() {
        return "POJO{" +
                "anyo=" + anyo +
                ", partido='" + partido + '\'' +
                ", presidente='" + presidente + '\'' +
                '}';
    }

    // 2. JAVA b)
    public static Map<String, Eleccion> convertirListasAMap(int[] anyo, String[] partido, String[] presidente) {
        Map<String, Eleccion> mapOut = new HashMap<>();
        for (int i = 0; i < partido.length-1; i++) {
            // Crear un POJO para cada conjunto de datos
            Eleccion objeto = new Eleccion(anyo[i], partido[i], presidente[i]);
            // Insertar en el mapa usando nombre como clave
            mapOut.put(String.valueOf(anyo[i]), objeto);
        }
        return mapOut;
    }


}

