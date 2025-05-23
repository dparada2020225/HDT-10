/**
 * SISTEMA DE OPTIMIZACIÓN LOGÍSTICA - ALGORITMO DE FLOYD-WARSHALL
 * Autores: Denil Parada , Harry Méndez 
 * Universidad del Valle de Guatemala
 * Facultad de Ingeniería - Departamento de Ciencias de la Computación
 * Hoja de Trabajo No. 10
 */

//==========================================================================
// CLASE: GrafoFloyd.java
// DESCRIPCIÓN: Implementa grafo dirigido con matriz de adyacencia para 
//              optimización de rutas logísticas usando Floyd-Warshall
//==========================================================================
import java.util.*;
import java.io.*;

public class GrafoFloyd {
    //----------------------------------------------------------------------
    // ATRIBUTOS DE LA CLASE GRAFOFLOYD
    //----------------------------------------------------------------------
    private Map<String, Integer> ciudadIndice;    // Mapea nombres de ciudades a índices numéricos
    private List<String> ciudades;                // Lista ordenada de todas las ciudades del grafo
    private double[][][] matriz;                  
    private int numCiudades;                      
    
    //----------------------------------------------------------------------
    // CONSTANTES PARA TIPOS DE CLIMA
    // Define los índices para acceder a las diferentes condiciones climáticas
    //----------------------------------------------------------------------
    private static final int CLIMA_NORMAL = 0;     // Condiciones normales de viaje
    private static final int CLIMA_LLUVIA = 1;     // Condiciones con lluvia (tiempos incrementados)
    private static final int CLIMA_NIEVE = 2;      // Condiciones con nieve (mayor impacto)
    private static final int CLIMA_TORMENTA = 3;   // Condiciones de tormenta (máximo impacto)
    private static final double INFINITO = Double.MAX_VALUE;  // Representa rutas inexistentes
    
    //----------------------------------------------------------------------
    // CONSTRUCTOR DE LA CLASE GRAFOFLOYD
    // Inicializa las estructuras de datos del grafo
    //----------------------------------------------------------------------
    public GrafoFloyd() {
        ciudadIndice = new HashMap<>();
        ciudades = new ArrayList<>();
        numCiudades = 0;
    }
    
    //----------------------------------------------------------------------
    // MÉTODO PARA CARGAR EL GRAFO DESDE ARCHIVO
    // Lee un archivo de texto con formato: Ciudad1 Ciudad2 tiempoNormal tiempoLluvia tiempoNieve tiempoTormenta
    // Construye la matriz de adyacencia con todas las condiciones climáticas
    //----------------------------------------------------------------------
    public void cargarDesdeArchivo(String nombreArchivo) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(nombreArchivo));
        String linea;
        Set<String> ciudadesUnicas = new HashSet<>();
        List<String[]> datos = new ArrayList<>();

        // PRIMERA PASADA: Identificar todas las ciudades únicas del archivo
        while ((linea = br.readLine()) != null) {
            String[] partes = linea.trim().split("\\s+");
            if (partes.length >= 6) {  
                ciudadesUnicas.add(partes[0]);
                ciudadesUnicas.add(partes[1]);
                datos.add(partes);
            }
        }
        br.close();

        // INICIALIZAR ESTRUCTURAS: Crear lista ordenada y mapeo de índices
        ciudades = new ArrayList<>(ciudadesUnicas);
        Collections.sort(ciudades);  // Ordenar alfabéticamente para consistencia
        numCiudades = ciudades.size();
        
        for (int i = 0; i < numCiudades; i++) {
            ciudadIndice.put(ciudades.get(i), i);
        }

        matriz = new double[4][numCiudades][numCiudades];
        for (int clima = 0; clima < 4; clima++) {
            for (int i = 0; i < numCiudades; i++) {
                for (int j = 0; j < numCiudades; j++) {
                    if (i == j) {
                        matriz[clima][i][j] = 0;  // Distancia de una ciudad a sí misma es 0
                    } else {
                        matriz[clima][i][j] = INFINITO;  // Inicialmente no hay conexiones
                    }
                }
            }
        }

        // SEGUNDA PASADA: Llenar la matriz con los datos del archivo
        for (String[] partes : datos) {
            String ciudad1 = partes[0];
            String ciudad2 = partes[1];
            int indice1 = ciudadIndice.get(ciudad1);
            int indice2 = ciudadIndice.get(ciudad2);

            // Asignar tiempos para cada condición climática
            matriz[CLIMA_NORMAL][indice1][indice2] = Double.parseDouble(partes[2]);
            matriz[CLIMA_LLUVIA][indice1][indice2] = Double.parseDouble(partes[3]);
            matriz[CLIMA_NIEVE][indice1][indice2] = Double.parseDouble(partes[4]);
            matriz[CLIMA_TORMENTA][indice1][indice2] = Double.parseDouble(partes[5]);
        }
    }
    
    //----------------------------------------------------------------------
    // ALGORITMO DE FLOYD-WARSHALL
    // Calcula las rutas más cortas entre todos los pares de vértices
    // Complejidad: O(V³) donde V es el número de ciudades
    // Retorna: ResultadoFloyd con matrices de distancias y rutas
    //----------------------------------------------------------------------
    public ResultadoFloyd aplicarFloyd(int tipoClima) {
        double[][] distancias = new double[numCiudades][numCiudades];
        int[][] siguiente = new int[numCiudades][numCiudades];

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

        // ALGORITMO DE FLOYD-WARSHALL: Buscar rutas más cortas a través de nodos intermedios
        for (int k = 0; k < numCiudades; k++) {           // k = nodo intermedio
            for (int i = 0; i < numCiudades; i++) {       // i = nodo origen
                for (int j = 0; j < numCiudades; j++) {   // j = nodo destino
                    // Si existe ruta i->k y k->j, y es más corta que la ruta directa i->j
                    if (distancias[i][k] != INFINITO && 
                        distancias[k][j] != INFINITO &&
                        distancias[i][k] + distancias[k][j] < distancias[i][j]) {
                        distancias[i][j] = distancias[i][k] + distancias[k][j];
                        siguiente[i][j] = siguiente[i][k];  // Actualizar ruta
                    }
                }
            }
        }

        return new ResultadoFloyd(distancias, siguiente);
    }
    
    //----------------------------------------------------------------------
    // MÉTODO PARA OBTENER EL CAMINO COMPLETO ENTRE DOS CIUDADES
    // Utiliza la matriz de rutas generada por Floyd-Warshall
    // Retorna: Lista con los nombres de las ciudades en el camino, null si no existe ruta
    //----------------------------------------------------------------------
    public List<String> obtenerCamino(String origen, String destino, int[][] siguiente) {
        // Verificar que ambas ciudades existen en el grafo
        if (!ciudadIndice.containsKey(origen) || !ciudadIndice.containsKey(destino)) {
            return null;
        }

        int i = ciudadIndice.get(origen);
        int j = ciudadIndice.get(destino);
        
        // Verificar que existe una ruta
        if (siguiente[i][j] == -1) {
            return null; // No hay camino
        }

        // Reconstruir el camino siguiendo la matriz de rutas
        List<String> camino = new ArrayList<>();
        camino.add(origen);
        
        while (i != j) {
            i = siguiente[i][j];
            camino.add(ciudades.get(i));
        }
        
        return camino;
    }
    
    //----------------------------------------------------------------------
    // MÉTODO PARA CALCULAR EL CENTRO DEL GRAFO
    //----------------------------------------------------------------------
    public String calcularCentro(double[][] distancias) {
        double[] excentricidades = new double[numCiudades];
        
        // Calcular la excentricidad de cada ciudad
        for (int i = 0; i < numCiudades; i++) {
            double maxDistancia = 0;
            for (int j = 0; j < numCiudades; j++) {
                if (i != j && distancias[i][j] != INFINITO) {
                    maxDistancia = Math.max(maxDistancia, distancias[i][j]);
                }
            }
            excentricidades[i] = maxDistancia;
        }

        // Encontrar el vértice con mínima excentricidad (centro del grafo)
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
    
    //----------------------------------------------------------------------
    // MÉTODO PARA AGREGAR NUEVA CONEXIÓN ENTRE CIUDADES
    // Permite modificar dinámicamente el grafo agregando rutas
    // Requiere tiempos para todas las condiciones climáticas
    //----------------------------------------------------------------------
    public void agregarConexion(String ciudad1, String ciudad2, 
                               double tiempoNormal, double tiempoLluvia, 
                               double tiempoNieve, double tiempoTormenta) {
        // Verificar que ambas ciudades existen en el grafo
        if (!ciudadIndice.containsKey(ciudad1) || !ciudadIndice.containsKey(ciudad2)) {
            System.out.println("Una o ambas ciudades no existen en el grafo.");
            return;
        }

        int indice1 = ciudadIndice.get(ciudad1);
        int indice2 = ciudadIndice.get(ciudad2);

        // Agregar conexión para todas las condiciones climáticas
        matriz[CLIMA_NORMAL][indice1][indice2] = tiempoNormal;
        matriz[CLIMA_LLUVIA][indice1][indice2] = tiempoLluvia;
        matriz[CLIMA_NIEVE][indice1][indice2] = tiempoNieve;
        matriz[CLIMA_TORMENTA][indice1][indice2] = tiempoTormenta;
    }
    
    //----------------------------------------------------------------------
    // MÉTODO PARA ELIMINAR CONEXIÓN ENTRE CIUDADES
    // Simula interrupciones de tráfico estableciendo distancia infinita
    // Elimina la conexión para todas las condiciones climáticas
    //----------------------------------------------------------------------
    public void eliminarConexion(String ciudad1, String ciudad2) {
        // Verificar que ambas ciudades existen en el grafo
        if (!ciudadIndice.containsKey(ciudad1) || !ciudadIndice.containsKey(ciudad2)) {
            System.out.println("Una o ambas ciudades no existen en el grafo.");
            return;
        }

        int indice1 = ciudadIndice.get(ciudad1);
        int indice2 = ciudadIndice.get(ciudad2);

        // Eliminar conexión para todas las condiciones climáticas
        for (int clima = 0; clima < 4; clima++) {
            matriz[clima][indice1][indice2] = INFINITO;
        }
    }
    
    //----------------------------------------------------------------------
    // MÉTODO PARA MOSTRAR MATRIZ DE ADYACENCIA
    // Visualiza la matriz de adyacencia para una condición climática específica
    //----------------------------------------------------------------------
    public void mostrarMatriz(int tipoClima) {
        String[] tiposClima = {"Normal", "Lluvia", "Nieve", "Tormenta"};
        System.out.println("\nMatriz de Adyacencia - Clima " + tiposClima[tipoClima] + ":");
        
        // Encabezados de columna: nombres de ciudades
        System.out.print(String.format("%15s", ""));
        for (String ciudad : ciudades) {
            System.out.print(String.format("%15s", ciudad));
        }
        System.out.println();

        // Filas: cada fila representa una ciudad origen
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
    
    //----------------------------------------------------------------------
    // MÉTODOS GETTER PARA ACCESO A DATOS DEL GRAFO
    //----------------------------------------------------------------------
    public List<String> getCiudades() {
        return new ArrayList<>(ciudades);  // Retorna copia para evitar modificaciones externas
    }

    public int getNumCiudades() {
        return numCiudades;
    }
    
    //----------------------------------------------------------------------
    // CLASE INTERNA: ResultadoFloyd
    // Encapsula los resultados del algoritmo de Floyd-Warshall
    // Contiene matriz de distancias mínimas y matriz de rutas
    //----------------------------------------------------------------------
    public static class ResultadoFloyd {
        public final double[][] distancias; 
        public final int[][] siguiente;      

        public ResultadoFloyd(double[][] distancias, int[][] siguiente) {
            this.distancias = distancias;
            this.siguiente = siguiente;
        }
    }
}

//==========================================================================
//Interfaz de usuario para interactuar con el sistema de optimización logística
//==========================================================================
class ProgramaPrincipal {
    //----------------------------------------------------------------------
    // ATRIBUTOS ESTÁTICOS DE LA CLASE PRINCIPAL
    //----------------------------------------------------------------------
    private static Scanner scanner = new Scanner(System.in);           
    private static GrafoFloyd grafo = new GrafoFloyd();              
    private static GrafoFloyd.ResultadoFloyd resultado;              

    public static void main(String[] args) {
        try {
            System.out.println("=== Sistema de Optimización Logística ===");
            System.out.println("Cargando grafo desde archivo guategrafo.txt...");
            
            // Cargar datos del archivo y aplicar algoritmo inicial
            grafo.cargarDesdeArchivo("guategrafo.txt");
            System.out.println("Grafo cargado exitosamente.");
            
            // Aplicar algoritmo de Floyd con clima normal por defecto
            resultado = grafo.aplicarFloyd(0); // 0 = clima normal
            
            // Mostrar información inicial
            grafo.mostrarMatriz(0);
            String centro = grafo.calcularCentro(resultado.distancias);
            System.out.println("\nCentro del grafo: " + centro);
            
            // Iniciar bucle principal del menú
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
            System.err.println("Asegúrese de que el archivo 'guategrafo.txt' existe.");
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
            return -1;  // Retorna -1 para opción inválida
        }
    }
    
    private static void consultarRutaMasCorta() {
        System.out.print("Ingrese ciudad origen: ");
        String origen = scanner.nextLine();
        System.out.print("Ingrese ciudad destino: ");
        String destino = scanner.nextLine();

        // Obtener el camino usando la matriz de rutas
        List<String> camino = grafo.obtenerCamino(origen, destino, resultado.siguiente);
        
        if (camino == null) {
            System.out.println("No existe ruta entre " + origen + " y " + destino);
            return;
        }

        // Calcular y mostrar la distancia total
        int indiceOrigen = grafo.getCiudades().indexOf(origen);
        int indiceDestino = grafo.getCiudades().indexOf(destino);
        
        if (indiceOrigen == -1 || indiceDestino == -1) {
            System.out.println("Una o ambas ciudades no existen.");
            return;
        }

        double distancia = resultado.distancias[indiceOrigen][indiceDestino];
        
        // Mostrar resultado completo
        System.out.println("\nRuta más corta de " + origen + " a " + destino + ":");
        System.out.println("Distancia total: " + distancia + " horas");
        System.out.print("Camino: ");
        for (int i = 0; i < camino.size(); i++) {
            System.out.print(camino.get(i));
            if (i < camino.size() - 1) {
                System.out.print(" -> ");
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
        
        // Recalcular rutas y centro después de modificaciones
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