import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import java.util.List;

public class GrafoFloydTest {
    
    private GrafoFloyd grafo;
    private final String archivoTest = "test_logistica.txt";
    
    @BeforeEach
    void setUp() throws IOException {
        grafo = new GrafoFloyd();
        crearArchivoTest();
        grafo.cargarDesdeArchivo(archivoTest);
    }
    
    private void crearArchivoTest() throws IOException {
        FileWriter writer = new FileWriter(archivoTest);
        writer.write("CiudadA CiudadB 10 15 20 50\n");
        writer.write("CiudadB CiudadC 5 8 12 25\n");
        writer.write("CiudadA CiudadC 20 25 35 80\n");
        writer.write("CiudadC CiudadA 18 22 30 75\n");
        writer.close();
    }
    
    @Test
    @DisplayName("Test cargar grafo desde archivo")
    void testCargarDesdeArchivo() {
        assertNotNull(grafo);
        assertEquals(3, grafo.getNumCiudades());
        assertTrue(grafo.getCiudades().contains("CiudadA"));
        assertTrue(grafo.getCiudades().contains("CiudadB"));
        assertTrue(grafo.getCiudades().contains("CiudadC"));
    }
    
    @Test
    @DisplayName("Test algoritmo de Floyd")
    void testAlgoritmoFloyd() {
        GrafoFloyd.ResultadoFloyd resultado = grafo.aplicarFloyd(0); // clima normal
        
        assertNotNull(resultado);
        assertNotNull(resultado.distancias);
        assertNotNull(resultado.siguiente);
        
        // Verificar que las distancias directas coinciden
        int indiceA = grafo.getCiudades().indexOf("CiudadA");
        int indiceB = grafo.getCiudades().indexOf("CiudadB");
        int indiceC = grafo.getCiudades().indexOf("CiudadC");
        
        assertEquals(10.0, resultado.distancias[indiceA][indiceB], 0.01);
        assertEquals(5.0, resultado.distancias[indiceB][indiceC], 0.01);
        
        // Verificar ruta más corta A->C (debería ser A->B->C = 15, no directa = 20)
        assertEquals(15.0, resultado.distancias[indiceA][indiceC], 0.01);
    }
    
    @Test
    @DisplayName("Test obtener camino")
    void testObtenerCamino() {
        GrafoFloyd.ResultadoFloyd resultado = grafo.aplicarFloyd(0);
        
        List<String> camino = grafo.obtenerCamino("CiudadA", "CiudadC", resultado.siguiente);
        
        assertNotNull(camino);
        assertEquals(3, camino.size());
        assertEquals("CiudadA", camino.get(0));
        assertEquals("CiudadB", camino.get(1));
        assertEquals("CiudadC", camino.get(2));
    }
    
    @Test
    @DisplayName("Test obtener camino inexistente")
    void testObtenerCaminoInexistente() {
        GrafoFloyd.ResultadoFloyd resultado = grafo.aplicarFloyd(0);
        
        List<String> camino = grafo.obtenerCamino("CiudadX", "CiudadY", resultado.siguiente);
        
        assertNull(camino);
    }
    
    @Test
    @DisplayName("Test calcular centro del grafo")
    void testCalcularCentro() {
        GrafoFloyd.ResultadoFloyd resultado = grafo.aplicarFloyd(0);
        String centro = grafo.calcularCentro(resultado.distancias);
        
        assertNotNull(centro);
        assertTrue(grafo.getCiudades().contains(centro));
    }
    
    @Test
    @DisplayName("Test agregar conexión")
    void testAgregarConexion() {
        // Crear un nuevo grafo más simple para esta prueba
        try {
            FileWriter writer = new FileWriter("test_simple.txt");
            writer.write("A B 10 12 15 20\n");
            writer.close();
            
            GrafoFloyd grafoSimple = new GrafoFloyd();
            grafoSimple.cargarDesdeArchivo("test_simple.txt");
            
            // Agregar nueva conexión
            grafoSimple.agregarConexion("B", "A", 8, 10, 13, 18);
            
            GrafoFloyd.ResultadoFloyd resultado = grafoSimple.aplicarFloyd(0);
            
            int indiceA = grafoSimple.getCiudades().indexOf("A");
            int indiceB = grafoSimple.getCiudades().indexOf("B");
            
            assertEquals(8.0, resultado.distancias[indiceB][indiceA], 0.01);
            
            // Limpiar archivo de prueba
            new File("test_simple.txt").delete();
            
        } catch (IOException e) {
            fail("Error en test de agregar conexión: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Test eliminar conexión")
    void testEliminarConexion() {
        grafo.eliminarConexion("CiudadA", "CiudadB");
        
        GrafoFloyd.ResultadoFloyd resultado = grafo.aplicarFloyd(0);
        
        int indiceA = grafo.getCiudades().indexOf("CiudadA");
        int indiceC = grafo.getCiudades().indexOf("CiudadC");
        
        // Ahora la ruta A->C debería ser directa (20) ya que eliminamos A->B
        assertEquals(20.0, resultado.distancias[indiceA][indiceC], 0.01);
    }
    
    @Test
    @DisplayName("Test diferentes condiciones climáticas")
    void testDiferentesClimas() {
        GrafoFloyd.ResultadoFloyd resultadoNormal = grafo.aplicarFloyd(0);
        GrafoFloyd.ResultadoFloyd resultadoLluvia = grafo.aplicarFloyd(1);
        GrafoFloyd.ResultadoFloyd resultadoNieve = grafo.aplicarFloyd(2);
        GrafoFloyd.ResultadoFloyd resultadoTormenta = grafo.aplicarFloyd(3);
        
        int indiceA = grafo.getCiudades().indexOf("CiudadA");
        int indiceB = grafo.getCiudades().indexOf("CiudadB");
        
        // Los tiempos deben incrementar con peores condiciones climáticas
        assertTrue(resultadoNormal.distancias[indiceA][indiceB] < 
                  resultadoLluvia.distancias[indiceA][indiceB]);
        assertTrue(resultadoLluvia.distancias[indiceA][indiceB] < 
                  resultadoNieve.distancias[indiceA][indiceB]);
        assertTrue(resultadoNieve.distancias[indiceA][indiceB] < 
                  resultadoTormenta.distancias[indiceA][indiceB]);
    }
    
    @Test
    @DisplayName("Test matriz de adyacencia")
    void testMatrizAdyacencia() {
        // Este test verifica que el método mostrarMatriz no lance excepciones
        assertDoesNotThrow(() -> {
            grafo.mostrarMatriz(0);
            grafo.mostrarMatriz(1);
            grafo.mostrarMatriz(2);
            grafo.mostrarMatriz(3);
        });
    }
    
    @Test
    @DisplayName("Test ciudades válidas")
    void testCiudadesValidas() {
        List<String> ciudades = grafo.getCiudades();
        
        assertNotNull(ciudades);
        assertFalse(ciudades.isEmpty());
        assertEquals(3, ciudades.size());
        
        // Verificar que no hay duplicados
        assertEquals(ciudades.size(), ciudades.stream().distinct().count());
    }
    
    // Método para limpiar después de las pruebas
    @Test
    @DisplayName("Test de limpieza")
    void testLimpieza() {
        // Eliminar archivo de prueba
        File archivo = new File(archivoTest);
        if (archivo.exists()) {
            archivo.delete();
        }
        assertTrue(true); // Test de limpieza exitoso
    }
}