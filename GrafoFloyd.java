// Clase principal del grafo dirigido
import java.util.*;
import java.io.*;

public class GrafoFloyd {
    private Map<String, Integer> ciudadIndice;
    private List<String> ciudades;
    private double[][][] matriz; // [tipo_clima][origen][destino]
    private int numCiudades;
    private static final int CLIMA_NORMAL = 0;
    private static final int CLIMA_LLUVIA = 1;
    private static final int CLIMA_NIEVE = 2;
    private static final int CLIMA_TORMENTA = 3;
    private static final double INFINITO = Double.MAX_VALUE;

    public GrafoFloyd() {
        ciudadIndice = new HashMap<>();
        ciudades = new ArrayList<>();
        numCiudades = 0;
    }

    // Método para cargar el grafo desde archivo
    public void cargarDesdeArchivo(String nombreArchivo) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(nombreArchivo));
        String linea;
        Set<String> ciudadesUnicas = new HashSet<>();
        List<String[]> datos = new ArrayList<>();

        // Primera pasada: identificar todas las ciudades
        while ((linea = br.readLine()) != null) {
            String[] partes = linea.trim().split("\\s+");
            if (partes.length >= 6) {
                ciudadesUnicas.add(partes[0]);
                ciudadesUnicas.add(partes[1]);
                datos.add(partes);
            }
        }
        br.close();

        // Inicializar estructuras
        ciudades = new ArrayList<>(ciudadesUnicas);
        Collections.sort(ciudades);
        numCiudades = ciudades.size();
        
        for (int i = 0; i < numCiudades; i++) {
            ciudadIndice.put(ciudades.get(i), i);
        }

        // Inicializar matriz de adyacencia
        matriz = new double[4][numCiudades][numCiudades];
        for (int clima = 0; clima < 4; clima++) {
            for (int i = 0; i < numCiudades; i++) {
                for (int j = 0; j < numCiudades; j++) {
                    if (i == j) {
                        matriz[clima][i][j] = 0;
                    } else {
                        matriz[clima][i][j] = INFINITO;
                    }
                }
            }
        }

        // Segunda pasada: llenar la matriz
        for (String[] partes : datos) {
            String ciudad1 = partes[0];
            String ciudad2 = partes[1];
            int indice1 = ciudadIndice.get(ciudad1);
            int indice2 = ciudadIndice.get(ciudad2);

            matriz[CLIMA_NORMAL][indice1][indice2] = Double.parseDouble(partes[2]);
            matriz[CLIMA_LLUVIA][indice1][indice2] = Double.parseDouble(partes[3]);
            matriz[CLIMA_NIEVE][indice1][indice2] = Double.parseDouble(partes[4]);
            matriz[CLIMA_TORMENTA][indice1][indice2] = Double.parseDouble(partes[5]);
        }
    }

    // Algoritmo de Floyd-Warshall
    public ResultadoFloyd aplicarFloyd(int tipoClima) {
        double[][] distancias = new double[numCiudades][numCiudades];
        int[][] siguiente = new int[numCiudades][numCiudades];

        // Inicializar matrices
        for (int i = 0; i < numCiudades; i++) {
            for (int j = 0; j < numCiudades; j++) {
                distancias[i][j] = matriz[tipoClima][i][j];
                if (i != j && matriz[tipoClima][i][j] != INFINITO) {
                    siguiente[i][j] = j;
                } else {
                    siguiente[i][j] = -1;
                }
            }
        }

        // Algoritmo de Floyd
        for (int k = 0; k < numCiudades; k++) {
            for (int i = 0; i < numCiudades; i++) {
                for (int j = 0; j < numCiudades; j++) {
                    if (distancias[i][k] != INFINITO && 
                        distancias[k][j] != INFINITO &&
                        distancias[i][k] + distancias[k][j] < distancias[i][j]) {
                        distancias[i][j] = distancias[i][k] + distancias[k][j];
                        siguiente[i][j] = siguiente[i][k];
                    }
                }
            }
        }

        return new ResultadoFloyd(distancias, siguiente);
    }

    // Método para obtener el camino entre dos ciudades
    public List<String> obtenerCamino(String origen, String destino, int[][] siguiente) {
        if (!ciudadIndice.containsKey(origen) || !ciudadIndice.containsKey(destino)) {
            return null;
        }

        int i = ciudadIndice.get(origen);
        int j = ciudadIndice.get(destino);
        
        if (siguiente[i][j] == -1) {
            return null; // No hay camino
        }

        List<String> camino = new ArrayList<>();
        camino.add(origen);
        
        while (i != j) {
            i = siguiente[i][j];
            camino.add(ciudades.get(i));
        }
        
        return camino;
    }

    // Calcular el centro del grafo
    public String calcularCentro(double[][] distancias) {
        double[] excentricidades = new double[numCiudades];
        
        for (int i = 0; i < numCiudades; i++) {
            double maxDistancia = 0;
            for (int j = 0; j < numCiudades; j++) {
                if (i != j && distancias[i][j] != INFINITO) {
                    maxDistancia = Math.max(maxDistancia, distancias[i][j]);
                }
            }
            excentricidades[i] = maxDistancia;
        }

        // Encontrar el vértice con mínima excentricidad
        double minExcentricidad = excentricidades[0];
        int indiceCentro = 0;
        
        for (int i = 1; i < numCiudades; i++) {
            if (excentricidades[i] < minExcentricidad) {
                minExcentricidad = excentricidades[i];
                indiceCentro = i;
            }
        }

        return ciudades.get(indiceCentro);
    }

    // Agregar nueva conexión
    public void agregarConexion(String ciudad1, String ciudad2, 
                               double tiempoNormal, double tiempoLluvia, 
                               double tiempoNieve, double tiempoTormenta) {
        if (!ciudadIndice.containsKey(ciudad1) || !ciudadIndice.containsKey(ciudad2)) {
            System.out.println("Una o ambas ciudades no existen en el grafo.");
            return;
        }

        int indice1 = ciudadIndice.get(ciudad1);
        int indice2 = ciudadIndice.get(ciudad2);

        matriz[CLIMA_NORMAL][indice1][indice2] = tiempoNormal;
        matriz[CLIMA_LLUVIA][indice1][indice2] = tiempoLluvia;
        matriz[CLIMA_NIEVE][indice1][indice2] = tiempoNieve;
        matriz[CLIMA_TORMENTA][indice1][indice2] = tiempoTormenta;
    }

    // Eliminar conexión
    public void eliminarConexion(String ciudad1, String ciudad2) {
        if (!ciudadIndice.containsKey(ciudad1) || !ciudadIndice.containsKey(ciudad2)) {
            System.out.println("Una o ambas ciudades no existen en el grafo.");
            return;
        }

        int indice1 = ciudadIndice.get(ciudad1);
        int indice2 = ciudadIndice.get(ciudad2);

        for (int clima = 0; clima < 4; clima++) {
            matriz[clima][indice1][indice2] = INFINITO;
        }
    }

    // Mostrar matriz de adyacencia
    public void mostrarMatriz(int tipoClima) {
        String[] tiposClima = {"Normal", "Lluvia", "Nieve", "Tormenta"};
        System.out.println("\nMatriz de Adyacencia - Clima " + tiposClima[tipoClima] + ":");
        
        // Encabezados de columna
        System.out.print(String.format("%15s", ""));
        for (String ciudad : ciudades) {
            System.out.print(String.format("%15s", ciudad));
        }
        System.out.println();

        // Filas
        for (int i = 0; i < numCiudades; i++) {
            System.out.print(String.format("%15s", ciudades.get(i)));
            for (int j = 0; j < numCiudades; j++) {
                if (matriz[tipoClima][i][j] == INFINITO) {
                    System.out.print(String.format("%15s", "∞"));
                } else {
                    System.out.print(String.format("%15.1f", matriz[tipoClima][i][j]));
                }
            }
            System.out.println();
        }
    }

    // Getters
    public List<String> getCiudades() {
        return new ArrayList<>(ciudades);
    }

    public int getNumCiudades() {
        return numCiudades;
    }

    // Clase interna para resultado de Floyd
    public static class ResultadoFloyd {
        public final double[][] distancias;
        public final int[][] siguiente;

        public ResultadoFloyd(double[][] distancias, int[][] siguiente) {
            this.distancias = distancias;
            this.siguiente = siguiente;
        }
    }
}

// Programa principal
class ProgramaPrincipal {
    private static Scanner scanner = new Scanner(System.in);
    private static GrafoFloyd grafo = new GrafoFloyd();
    private static GrafoFloyd.ResultadoFloyd resultado;

    public static void main(String[] args) {
        try {
            System.out.println("=== Sistema de Optimización Logística ===");
            System.out.println("Cargando grafo desde archivo logistica.txt...");
            
            grafo.cargarDesdeArchivo("logistica.txt");
            System.out.println("Grafo cargado exitosamente.");
            
            // Aplicar algoritmo de Floyd con clima normal
            resultado = grafo.aplicarFloyd(0); // 0 = clima normal
            
            // Mostrar matriz inicial
            grafo.mostrarMatriz(0);
            
            // Mostrar centro del grafo
            String centro = grafo.calcularCentro(resultado.distancias);
            System.out.println("\nCentro del grafo: " + centro);
            
            // Menú principal
            boolean continuar = true;
            while (continuar) {
                mostrarMenu();
                int opcion = leerOpcion();
                
                switch (opcion) {
                    case 1:
                        consultarRutaMasCorta();
                        break;
                    case 2:
                        mostrarCentroGrafo();
                        break;
                    case 3:
                        modificarGrafo();
                        break;
                    case 4:
                        continuar = false;
                        System.out.println("¡Gracias por usar el sistema!");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error al cargar el archivo: " + e.getMessage());
            System.err.println("Asegúrese de que el archivo 'logistica.txt' existe.");
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n=== MENÚ PRINCIPAL ===");
        System.out.println("1. Consultar ruta más corta entre ciudades");
        System.out.println("2. Mostrar centro del grafo");
        System.out.println("3. Modificar grafo");
        System.out.println("4. Salir");
        System.out.print("Seleccione una opción: ");
    }

    private static int leerOpcion() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void consultarRutaMasCorta() {
        System.out.print("Ingrese ciudad origen: ");
        String origen = scanner.nextLine();
        System.out.print("Ingrese ciudad destino: ");
        String destino = scanner.nextLine();

        List<String> camino = grafo.obtenerCamino(origen, destino, resultado.siguiente);
        
        if (camino == null) {
            System.out.println("No existe ruta entre " + origen + " y " + destino);
            return;
        }

        int indiceOrigen = grafo.getCiudades().indexOf(origen);
        int indiceDestino = grafo.getCiudades().indexOf(destino);
        
        if (indiceOrigen == -1 || indiceDestino == -1) {
            System.out.println("Una o ambas ciudades no existen.");
            return;
        }

        double distancia = resultado.distancias[indiceOrigen][indiceDestino];
        
        System.out.println("\nRuta más corta de " + origen + " a " + destino + ":");
        System.out.println("Distancia total: " + distancia + " horas");
        System.out.print("Camino: ");
        for (int i = 0; i < camino.size(); i++) {
            System.out.print(camino.get(i));
            if (i < camino.size() - 1) {
                System.out.print(" → ");
            }
        }
        System.out.println();
    }

    private static void mostrarCentroGrafo() {
        String centro = grafo.calcularCentro(resultado.distancias);
        System.out.println("\nEl centro del grafo es: " + centro);
    }

    private static void modificarGrafo() {
        System.out.println("\n=== MODIFICAR GRAFO ===");
        System.out.println("1. Interrumpir tráfico entre ciudades");
        System.out.println("2. Establecer nueva conexión");
        System.out.println("3. Cambiar condición climática");
        System.out.print("Seleccione una opción: ");
        
        int opcion = leerOpcion();
        
        switch (opcion) {
            case 1:
                interrumpirTrafico();
                break;
            case 2:
                establecerConexion();
                break;
            case 3:
                cambiarClima();
                break;
            default:
                System.out.println("Opción no válida.");
                return;
        }
        
        // Recalcular Floyd y centro
        resultado = grafo.aplicarFloyd(0);
        String nuevoCentro = grafo.calcularCentro(resultado.distancias);
        System.out.println("Nuevo centro del grafo: " + nuevoCentro);
    }

    private static void interrumpirTrafico() {
        System.out.print("Ciudad 1: ");
        String ciudad1 = scanner.nextLine();
        System.out.print("Ciudad 2: ");
        String ciudad2 = scanner.nextLine();
        
        grafo.eliminarConexion(ciudad1, ciudad2);
        System.out.println("Conexión eliminada entre " + ciudad1 + " y " + ciudad2);
    }

    private static void establecerConexion() {
        System.out.print("Ciudad origen: ");
        String ciudad1 = scanner.nextLine();
        System.out.print("Ciudad destino: ");
        String ciudad2 = scanner.nextLine();
        
        try {
            System.out.print("Tiempo con clima normal: ");
            double normal = Double.parseDouble(scanner.nextLine());
            System.out.print("Tiempo con lluvia: ");
            double lluvia = Double.parseDouble(scanner.nextLine());
            System.out.print("Tiempo con nieve: ");
            double nieve = Double.parseDouble(scanner.nextLine());
            System.out.print("Tiempo con tormenta: ");
            double tormenta = Double.parseDouble(scanner.nextLine());
            
            grafo.agregarConexion(ciudad1, ciudad2, normal, lluvia, nieve, tormenta);
            System.out.println("Nueva conexión establecida.");
            
        } catch (NumberFormatException e) {
            System.out.println("Error: Ingrese valores numéricos válidos.");
        }
    }

    private static void cambiarClima() {
        System.out.println("Tipos de clima:");
        System.out.println("0 - Normal, 1 - Lluvia, 2 - Nieve, 3 - Tormenta");
        System.out.print("Seleccione tipo de clima: ");
        
        try {
            int tipoClima = Integer.parseInt(scanner.nextLine());
            if (tipoClima >= 0 && tipoClima <= 3) {
                resultado = grafo.aplicarFloyd(tipoClima);
                grafo.mostrarMatriz(tipoClima);
                String[] nombres = {"Normal", "Lluvia", "Nieve", "Tormenta"};
                System.out.println("Algoritmo aplicado con clima: " + nombres[tipoClima]);
            } else {
                System.out.println("Tipo de clima no válido.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Ingrese un número válido.");
        }
    }
}