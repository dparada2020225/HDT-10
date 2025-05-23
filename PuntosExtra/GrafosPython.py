#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import networkx as nx
import pandas as pd
import numpy as np
from typing import Dict, List, Tuple, Optional
import os

class GrafoFloydPython:

    def __init__(self):

        # Crear grafos separados para cada condición climática
        self.grafos = {
            'normal': nx.DiGraph(),
            'lluvia': nx.DiGraph(),
            'nieve': nx.DiGraph(),
            'tormenta': nx.DiGraph()
        }
        
        # Mapeo de índices a nombres de clima
        self.climas = ['normal', 'lluvia', 'nieve', 'tormenta']
        
        # Variables para almacenar resultados
        self.ciudades = []
        self.matrices_distancia = {}
        self.matrices_ruta = {}
        
    def cargar_desde_archivo(self, nombre_archivo: str) -> bool:
        try:
            # Verificar que el archivo existe
            if not os.path.exists(nombre_archivo):
                print(f"Error: El archivo {nombre_archivo} no existe.")
                return False
            
            # Leer datos del archivo
            with open(nombre_archivo, 'r', encoding='utf-8') as archivo:
                lineas = archivo.readlines()
            
            # Procesar cada línea
            for linea in lineas:
                partes = linea.strip().split()
                
                # Verificar formato correcto
                if len(partes) >= 6:
                    ciudad1, ciudad2 = partes[0], partes[1]
                    tiempos = [float(partes[i]) for i in range(2, 6)]
                    
                    # Agregar nodos y aristas a cada grafo según el clima
                    for i, clima in enumerate(self.climas):
                        self.grafos[clima].add_edge(ciudad1, ciudad2, weight=tiempos[i])
            
            # Obtener lista de ciudades (nodos) únicos
            self.ciudades = list(self.grafos['normal'].nodes())
            self.ciudades.sort()  # Ordenar alfabéticamente
            
            print(f"Grafo cargado exitosamente con {len(self.ciudades)} ciudades.")
            return True
            
        except Exception as e:
            print(f"Error al cargar el archivo: {e}")
            return False
    
    def aplicar_floyd_warshall(self, tipo_clima: str = 'normal') -> Tuple[np.ndarray, np.ndarray]:

        if tipo_clima not in self.climas:
            print(f"Error: Tipo de clima invalido: {tipo_clima}")
            return np.array([]), np.array([])
        
        grafo = self.grafos[tipo_clima]
        n = len(self.ciudades)
        
        if n == 0:
            return np.array([]), np.array([])
        
        # Crear matrices iniciales
        matriz_distancias = np.full((n, n), np.inf)
        matriz_rutas = np.full((n, n), -1, dtype=int)
        
        # Mapear ciudades a índices
        ciudad_a_indice = {ciudad: i for i, ciudad in enumerate(self.ciudades)}
        
        # Inicializar matriz de distancias
        for i in range(n):
            matriz_distancias[i][i] = 0
            matriz_rutas[i][i] = i
        
        # Llenar con las aristas existentes
        for u, v, data in grafo.edges(data=True):
            i = ciudad_a_indice[u]
            j = ciudad_a_indice[v]
            peso = data['weight']
            matriz_distancias[i][j] = peso
            matriz_rutas[i][j] = j
        
        # Algoritmo de Floyd-Warshall manual
        for k in range(n):
            for i in range(n):
                for j in range(n):
                    if (matriz_distancias[i][k] != np.inf and 
                        matriz_distancias[k][j] != np.inf and
                        matriz_distancias[i][k] + matriz_distancias[k][j] < matriz_distancias[i][j]):
                        matriz_distancias[i][j] = matriz_distancias[i][k] + matriz_distancias[k][j]
                        matriz_rutas[i][j] = matriz_rutas[i][k]
        
        # Almacenar resultados
        self.matrices_distancia[tipo_clima] = matriz_distancias
        self.matrices_ruta[tipo_clima] = matriz_rutas
        
        return matriz_distancias, matriz_rutas
    
    def obtener_camino(self, origen: str, destino: str, tipo_clima: str = 'normal') -> Optional[List[str]]:
        if origen not in self.ciudades or destino not in self.ciudades:
            return None
        
        # Asegurar que tenemos las matrices calculadas
        matriz_distancias, matriz_rutas = self.aplicar_floyd_warshall(tipo_clima)
        
        if matriz_distancias.size == 0:
            return None
        
        # Obtener índices
        indice_origen = self.ciudades.index(origen)
        indice_destino = self.ciudades.index(destino)
        
        # Verificar si existe camino
        if matriz_distancias[indice_origen][indice_destino] == np.inf:
            return None
        
        # Reconstruir el camino
        camino = []
        actual = indice_origen
        camino.append(self.ciudades[actual])
        
        while actual != indice_destino:
            actual = matriz_rutas[actual][indice_destino]
            if actual == -1:  # No hay camino
                return None
            camino.append(self.ciudades[actual])
        
        return camino
    
    def calcular_centro_grafo(self, tipo_clima: str = 'normal') -> str:
        # Usar matriz ya calculada si existe, sino calcular
        if tipo_clima in self.matrices_distancia:
            matriz_distancias = self.matrices_distancia[tipo_clima]
        else:
            matriz_distancias, _ = self.aplicar_floyd_warshall(tipo_clima)
        
        if matriz_distancias.size == 0 or len(self.ciudades) == 0:
            return ""
        
        excentricidades = []
        
        # Calcular excentricidad de cada ciudad
        for i in range(len(self.ciudades)):
            distancias_validas = [matriz_distancias[i][j] for j in range(len(self.ciudades)) 
                                if i != j and matriz_distancias[i][j] != np.inf]
            
            if distancias_validas:
                excentricidad = max(distancias_validas)
            else:
                excentricidad = np.inf
            
            excentricidades.append(excentricidad)
        
        # Encontrar el índice con mínima excentricidad
        indice_centro = np.argmin(excentricidades)
        return self.ciudades[indice_centro]
    
    def agregar_conexion(self, ciudad1: str, ciudad2: str, tiempos: List[float]) -> bool:
        if len(tiempos) != 4:
            print("Error: Se requieren 4 tiempos (normal, lluvia, nieve, tormenta)")
            return False
        
        try:
            # Agregar conexión a cada grafo según el clima
            for i, clima in enumerate(self.climas):
                self.grafos[clima].add_edge(ciudad1, ciudad2, weight=tiempos[i])
            
            # Actualizar lista de ciudades si es necesario
            if ciudad1 not in self.ciudades:
                self.ciudades.append(ciudad1)
            if ciudad2 not in self.ciudades:
                self.ciudades.append(ciudad2)
            
            self.ciudades.sort()
            print(f"Conexion agregada entre {ciudad1} y {ciudad2}")
            return True
            
        except Exception as e:
            print(f"Error al agregar conexión: {e}")
            return False
    
    def eliminar_conexion(self, ciudad1: str, ciudad2: str) -> bool:
        try:
            # Eliminar conexión de todos los grafos
            for clima in self.climas:
                if self.grafos[clima].has_edge(ciudad1, ciudad2):
                    self.grafos[clima].remove_edge(ciudad1, ciudad2)
            
            print(f"Conexion eliminada entre {ciudad1} y {ciudad2}")
            return True
            
        except Exception as e:
            print(f"Error al eliminar conexión: {e}")
            return False
    
    def mostrar_matriz_adyacencia(self, tipo_clima: str = 'normal') -> None:
        if tipo_clima not in self.climas:
            print(f"Tipo de clima inválido: {tipo_clima}")
            return
        
        grafo = self.grafos[tipo_clima]
        
        print(f"=== MATRIZ DE ADYACENCIA - CLIMA: {tipo_clima.upper()} ===")
        
        # Crear matriz de adyacencia
        n = len(self.ciudades)
        matriz = np.full((n, n), np.inf)
        
        for i, ciudad_i in enumerate(self.ciudades):
            for j, ciudad_j in enumerate(self.ciudades):
                if i == j:
                    matriz[i][j] = 0
                elif grafo.has_edge(ciudad_i, ciudad_j):
                    matriz[i][j] = grafo[ciudad_i][ciudad_j]['weight']
        
        # Mostrar encabezados
        print(f"{'':>15}", end="")
        for ciudad in self.ciudades:
            print(f"{ciudad:>15}", end="")
        print()
        
        # Mostrar filas
        for i, ciudad in enumerate(self.ciudades):
            print(f"{ciudad:>15}", end="")
            for j in range(n):
                if matriz[i][j] == np.inf:
                    print(f"{'∞':>15}", end="")
                else:
                    print(f"{matriz[i][j]:>15.1f}", end="")
            print()
    
    def obtener_distancia(self, origen: str, destino: str, tipo_clima: str = 'normal') -> float:
        if origen not in self.ciudades or destino not in self.ciudades:
            return np.inf
        
        # Usar matriz ya calculada si existe, sino calcular
        if tipo_clima in self.matrices_distancia:
            matriz_distancias = self.matrices_distancia[tipo_clima]
        else:
            matriz_distancias, _ = self.aplicar_floyd_warshall(tipo_clima)
        
        if matriz_distancias.size == 0:
            return np.inf
        
        try:
            indice_origen = self.ciudades.index(origen)
            indice_destino = self.ciudades.index(destino)
            return matriz_distancias[indice_origen][indice_destino]
        except (ValueError, IndexError):
            return np.inf
    
    def get_ciudades(self) -> List[str]:
        return self.ciudades.copy()
    
    def get_num_ciudades(self) -> int:
    
        return len(self.ciudades)


class ProgramaPrincipalPython:
    
    
    def __init__(self):
        
        self.grafo = GrafoFloydPython()
        self.clima_actual = 'normal'
        
    def ejecutar(self) -> None:
        
        print("=== Sistema de Optimizacion Logistica - Python + NetworkX ===")
        print("Cargando grafo desde archivo guategrafo.txt...")
        
        # Cargar archivo de datos
        if not self.grafo.cargar_desde_archivo("guategrafo.txt"):
            print("Error: No se pudo cargar el archivo de datos.")
            return
        
        print("Grafo cargado exitosamente.")
        
        # Aplicar algoritmo inicial
        self.grafo.aplicar_floyd_warshall(self.clima_actual)
        
        # Mostrar información inicial
        self.grafo.mostrar_matriz_adyacencia(self.clima_actual)
        centro = self.grafo.calcular_centro_grafo(self.clima_actual)
        print(f"\nCentro del grafo: {centro}")
        
        # Bucle principal del menú
        while True:
            self.mostrar_menu()
            opcion = self.leer_opcion()
            
            if opcion == 1:
                self.consultar_ruta_mas_corta()
            elif opcion == 2:
                self.mostrar_centro_grafo()
            elif opcion == 3:
                self.modificar_grafo()
            elif opcion == 4:
                self.mostrar_estadisticas_networkx()
            elif opcion == 5:
                print("Gracias por usar el sistema!")
                break
            else:
                print("Opcion no valida.")
    
    def mostrar_menu(self) -> None:
        
        print(f"\n=== MENU PRINCIPAL ===")
        print("1. Consultar ruta mas corta entre ciudades")
        print("2. Mostrar centro del grafo")
        print("3. Modificar grafo")
        print("4. Mostrar estadisticas avanzadas")
        print("5. Salir")
        print("Seleccione una opcion: ", end="")
    
    def leer_opcion(self) -> int:
        
        try:
            return int(input())
        except ValueError:
            return -1
    
    def consultar_ruta_mas_corta(self) -> None:
        
        print("\nCONSULTAR RUTA MAS CORTA")
        
        origen = input("Ingrese ciudad origen: ").strip()
        destino = input("Ingrese ciudad destino: ").strip()
        
        # Obtener camino y distancia
        camino = self.grafo.obtener_camino(origen, destino, self.clima_actual)
        distancia = self.grafo.obtener_distancia(origen, destino, self.clima_actual)
        
        if camino is None or distancia == np.inf:
            print(f"No existe ruta entre {origen} y {destino}")
        else:
            print(f"\nRuta mas corta de {origen} a {destino}:")
            print(f"Distancia total: {distancia:.1f} horas")
            print(f"Camino: {' -> '.join(camino)}")
    
    def mostrar_centro_grafo(self) -> None:
       
        centro = self.grafo.calcular_centro_grafo(self.clima_actual)
        print(f"\nEl centro del grafo es: {centro}")
    
    def modificar_grafo(self) -> None:
       
        print("\n=== MODIFICAR GRAFO ===")
        print("1. Interrumpir trafico entre ciudades")
        print("2. Establecer nueva conexion")
        print("3. Cambiar condicion climatica")
        print("Seleccione una opcion: ", end="")
        
        opcion = self.leer_opcion()
        
        if opcion == 1:
            self.interrumpir_trafico()
        elif opcion == 2:
            self.establecer_conexion()
        elif opcion == 3:
            self.cambiar_clima()
        else:
            print("Opcion no valida.")
            return
        
        # Recalcular después de modificaciones
        if opcion in [1, 2]:
            self.grafo.aplicar_floyd_warshall(self.clima_actual)
            nuevo_centro = self.grafo.calcular_centro_grafo(self.clima_actual)
            print(f"Nuevo centro del grafo: {nuevo_centro}")
    
    def interrumpir_trafico(self) -> None:
        
        print("Ciudad 1: ", end="")
        ciudad1 = input().strip()
        print("Ciudad 2: ", end="")
        ciudad2 = input().strip()
        
        if self.grafo.eliminar_conexion(ciudad1, ciudad2):
            print(f"Conexion eliminada entre {ciudad1} y {ciudad2}")

    def establecer_conexion(self) -> None:
    
        print("Ciudad origen: ", end="")
        ciudad1 = input().strip()
        print("Ciudad destino: ", end="")
        ciudad2 = input().strip()
        
        try:
            print("Tiempo con clima normal: ", end="")
            tiempo_normal = float(input())
            print("Tiempo con lluvia: ", end="")
            tiempo_lluvia = float(input())
            print("Tiempo con nieve: ", end="")
            tiempo_nieve = float(input())
            print("Tiempo con tormenta: ", end="")
            tiempo_tormenta = float(input())
            
            tiempos = [tiempo_normal, tiempo_lluvia, tiempo_nieve, tiempo_tormenta]
            
            if self.grafo.agregar_conexion(ciudad1, ciudad2, tiempos):
                print("Nueva conexion establecida.")
                
        except ValueError:
            print("Error: Ingrese valores numericos validos.")
    
    def cambiar_clima(self) -> None:
    
        print("Tipos de clima:")
        print("0 - Normal, 1 - Lluvia, 2 - Nieve, 3 - Tormenta")
        print("Seleccione tipo de clima: ", end="")
        
        try:
            opcion = int(input())
            if 0 <= opcion < len(self.grafo.climas):
                self.clima_actual = self.grafo.climas[opcion]
                self.grafo.aplicar_floyd_warshall(self.clima_actual)
                nombres = ["Normal", "Lluvia", "Nieve", "Tormenta"]
                print(f"Algoritmo aplicado con clima: {nombres[opcion]}")
                self.grafo.mostrar_matriz_adyacencia(self.clima_actual)
            else:
                print("Tipo de clima no valido.")
        except ValueError:
            print("Error: Ingrese un numero valido.")
    
    def mostrar_estadisticas_networkx(self) -> None:
    
        print("\n=== ESTADISTICAS AVANZADAS ===")
        
        grafo = self.grafo.grafos[self.clima_actual]
        
        print(f"Estadisticas del grafo:")
        print(f"Numero de nodos: {grafo.number_of_nodes()}")
        print(f"Numero de aristas: {grafo.number_of_edges()}")
        print(f"Densidad: {nx.density(grafo):.4f}")
        
        # Verificar si el grafo es fuertemente conectado
        if nx.is_strongly_connected(grafo):
            print("El grafo es fuertemente conectado")
        else:
            print("El grafo NO es fuertemente conectado")
            componentes = list(nx.strongly_connected_components(grafo))
            print(f"Numero de componentes fuertemente conectados: {len(componentes)}")
        
        # Centralidad de cercanía
        try:
            centralidad = nx.closeness_centrality(grafo, distance='weight')
            ciudad_mas_central = max(centralidad, key=centralidad.get)
            print(f"Ciudad con mayor centralidad de cercania: {ciudad_mas_central}")
            print(f"Centralidad: {centralidad[ciudad_mas_central]:.4f}")
        except:
            print("No se pudo calcular centralidad de cercania")
        
        # Grado promedio
        grados = [grafo.degree(nodo) for nodo in grafo.nodes()]
        if grados:
            print(f"Grado promedio: {np.mean(grados):.2f}")
            print(f"Grado maximo: {max(grados)}")
            print(f"Grado minimo: {min(grados)}")


def main():
    programa = ProgramaPrincipalPython()
    programa.ejecutar()

main()