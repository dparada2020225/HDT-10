# Sistema de Optimización Logística 🚚

## Descripción del Proyecto

Sistema de optimización de rutas logísticas que utiliza el **algoritmo de Floyd-Warshall** para encontrar las rutas más cortas entre ciudades, considerando diferentes condiciones climáticas que afectan los tiempos de viaje.

## 👥 Autores

- **Denil Parada** - Carné: 24761
- **Harry Méndez** - Carné: 24089

**Universidad del Valle de Guatemala**  
Facultad de Ingeniería  
Departamento de Ciencias de la Computación  
CC2016 – Algoritmos y Estructura de Datos  
Semestre I – 2025

## 🎯 Objetivos Cumplidos

- ✅ Implementación de grafo dirigido en Java usando matriz de adyacencia
- ✅ Implementación del algoritmo de Floyd-Warshall para rutas más cortas
- ✅ Cálculo del centro del grafo usando excentricidades
- ✅ Sistema interactivo con menú de opciones
- ✅ Manejo de múltiples condiciones climáticas
- ✅ Pruebas unitarias comprehensivas con JUnit
- ✅ Control de versiones con Git
- ✅ Documentación completa con diagrama UML

## 🚀 Funcionalidades

### Funciones Principales
1. **Consulta de Rutas**: Encuentra la ruta más corta entre cualquier par de ciudades
2. **Centro del Grafo**: Calcula la ciudad que minimiza la distancia máxima a todas las demás
3. **Modificación Dinámica**: 
   - Agregar/eliminar conexiones entre ciudades
   - Simular interrupciones de tráfico
   - Cambiar condiciones climáticas en tiempo real
4. **Visualización**: Muestra matriz de adyacencia y rutas completas

### Condiciones Climáticas Soportadas
- 🌤️ **Clima Normal**: Condiciones óptimas de viaje
- 🌧️ **Lluvia**: Incremento moderado en tiempos de viaje
- ❄️ **Nieve**: Mayor impacto en los tiempos
- ⛈️ **Tormenta**: Condiciones más adversas

## 📁 Estructura del Proyecto

```
HDT10/
├── GrafoFloyd.java          # Clase principal del grafo
├── GrafoFloydTest.java      # Pruebas unitarias
├── guategrafo.txt          # Datos principales de ciudades guatemaltecas
├── test_logistica.txt      # Datos de prueba
├── UML.png                 # Diagrama UML de clases
└── README.md               # Documentación del proyecto
```

## 🏗️ Arquitectura

### Clases Principales

1. **`GrafoFloyd`**
   - Implementa grafo dirigido con matriz de adyacencia 3D
   - Contiene algoritmo de Floyd-Warshall optimizado
   - Maneja 4 tipos de condiciones climáticas simultáneamente

2. **`ResultadoFloyd`** (Clase interna)
   - Encapsula matrices de distancias y rutas
   - Facilita el retorno de resultados del algoritmo

3. **`ProgramaPrincipal`**
   - Interfaz de usuario interactiva
   - Coordina todas las operaciones del sistema
   - Maneja entrada/salida y validaciones

4. **`GrafoFloydTest`**
   - Suite completa de pruebas unitarias
   - Cobertura de todos los métodos críticos
   - Incluye casos edge y validaciones

## 🛠️ Tecnologías Utilizadas

- **Java 11+**: Lenguaje principal
- **JUnit 5**: Framework de pruebas unitarias
- **Git**: Control de versiones

## 📋 Prerrequisitos

- Java Development Kit (JDK) 11 o superior
- IDE compatible con Java (IntelliJ IDEA, Eclipse, VS Code)
- JUnit 5 para ejecutar las pruebas


## 📊 Formato del Archivo de Datos

El archivo `guategrafo.txt` debe tener el siguiente formato:

```
Ciudad1 Ciudad2 tiempoNormal tiempoLluvia tiempoNieve tiempoTormenta
GuatemalaCity Antigua 1.5 2.0 2.5 4.0
GuatemalaCity Escuintla 2.0 2.8 3.5 5.5
...
```

**Reglas importantes:**
- No usar espacios en nombres de ciudades (ej: `GuatemalaCity`)
- Los tiempos deben ser progresivos: Normal ≤ Lluvia ≤ Nieve ≤ Tormenta
- Valores numéricos con punto decimal

## 🎮 Uso del Sistema

### Menú Principal
```
=== MENÚ PRINCIPAL ===
1. Consultar ruta más corta entre ciudades
2. Mostrar centro del grafo
3. Modificar grafo
4. Salir
```

### Ejemplo de Consulta
```
Ingrese ciudad origen: GuatemalaCity
Ingrese ciudad destino: Quetzaltenango

Ruta más corta de GuatemalaCity a Quetzaltenango:
Distancia total: 5.3 horas
Camino: GuatemalaCity -> Chimaltenango -> Quetzaltenango
```

## 🧪 Pruebas

El proyecto incluye una suite comprehensiva de pruebas unitarias que valida:

- ✅ Carga correcta de datos desde archivo
- ✅ Funcionamiento del algoritmo de Floyd-Warshall
- ✅ Cálculo preciso de rutas más cortas
- ✅ Determinación correcta del centro del grafo
- ✅ Operaciones de modificación del grafo
- ✅ Manejo de diferentes condiciones climáticas
- ✅ Casos edge y validación de errores

**Cobertura**: >90% de los métodos críticos

## 📈 Rendimiento

- **Complejidad Temporal**: O(V³) para el algoritmo de Floyd-Warshall
- **Complejidad Espacial**: O(V²) para almacenamiento de matrices
- **Ciudades Soportadas**: Hasta 1000+ nodos (limitado por memoria)

## 🗺️ Datos Incluidos

El proyecto incluye datos reales de **17 ciudades guatemaltecas**:

- Guatemala City, Antigua, Escuintla, Chimaltenango
- Quetzaltenango, Huehuetenango, San Marcos
- Mixco, Villa Nueva, Amatitlán, San Lucas
- Puerto San José, Mazatenango, Retalhuleu
- Sololá, Panajachel, Coatepeque

## 📚 Referencias

1. Cálculo del centro de un grafo - Documento adjunto de la hoja de trabajo
2. Oracle Java Documentation - https://docs.oracle.com/en/java/
3. JUnit 5 User Guide - https://junit.org/junit5/docs/current/user-guide/
4. Claude AI Assistant - Anthropic (Asistencia en desarrollo y documentación)

---

**© 2025 - Universidad del Valle de Guatemala**  
*Proyecto desarrollado para el curso CC2016 - Algoritmos y Estructura de Datos*