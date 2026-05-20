# 🥐 La Croassantina - Proyecto Intermodular

Este repositorio contiene la **API REST** central del proyecto **La Croassantina**, un sistema integral de e-commerce y gestión para una panadería moderna. El ecosistema se completa con una aplicación web (React) y una aplicación móvil (Android/Kotlin).

---

## 📋 Descripción General

**La Croassantina** es una solución tecnológica diseñada para digitalizar por completo las operaciones de una panadería. Permite a los clientes explorar el catálogo, aprovechar promociones dinámicas y realizar compras desde cualquier dispositivo, mientras proporciona a los administradores herramientas robustas para la gestión de inventario, usuarios y ventas.

El sistema se divide en tres componentes principales:
1.  **Backend (Este repositorio):** API REST robusta que centraliza la lógica de negocio y los datos.
2.  **Frontend Web:** Aplicación Single Page (SPA) para clientes y administración.
3.  **Aplicación Móvil:** Experiencia nativa Android optimizada para el usuario final.

---

## 🏗️ Arquitectura del Sistema

El proyecto sigue principios de **Clean Code** y separación de responsabilidades:

### Backend (API)
Sigue una **Arquitectura por Capas** clásica:
- **Capa de Controladores:** Gestión de endpoints REST y validación de entrada.
- **Capa de Servicios:** Lógica de negocio, reglas de promoción y orquestación.
- **Capa de Persistencia:** Repositorios JPA para comunicación con MySQL.
- **Capa de Dominio:** Entidades del modelo de datos y reglas de integridad.

### Frontend (Web & Móvil)
- **Web:** Arquitectura basada en componentes con React y gestión de estado mediante Context API.
- **Móvil:** Arquitectura **MVVM** (Model-View-ViewModel) con Jetpack Compose, garantizando una UI reactiva y desacoplada de la lógica de datos.

---

## 🛠️ Stack Tecnológico

### Backend (Core)
- **Java 21 (LTS)** & **Spring Boot 4.0**.
- **Seguridad:** Spring Security con **JWT** (Access & Refresh Tokens).
- **Base de Datos:** **MySQL 8.0** y migraciones con **Flyway**.
- **Logs:** SLF4J + Logback para trazabilidad completa.
- **Métricas:** Micrometer + Prometheus + Actuator.
- **Documentación:** Swagger / OpenAPI 3.
- **Limitación de Tasa:** Bucket4J para protección contra abusos.

### Frontend Web
- **React 19** + **Vite**.
- **Navegación:** React Router DOM.
- **Comunicación:** Fetch API centralizada.

### Aplicación Móvil
- **Kotlin 2.x** + **Jetpack Compose**.
- **DI:** Hilt (Dagger).
- **Networking:** Retrofit 2 + OkHttp.
- **Persistencia:** Jetpack DataStore.

---

## 🚀 Funcionalidades Principales

### 🔐 Seguridad y Usuarios
- Sistema de autenticación robusto con tokens de acceso (15 min) y refresco (7 días).
- Roles de usuario (**USER**, **ADMIN**) con permisos granulares.
- Gestión de perfiles con carga de imágenes (validación MIME y tamaño).
- Protección de endpoints mediante Rate Limiting.

### 📦 Catálogo y Stock
- Gestión completa de categorías y productos.
- **Control de Concurrencia:** Bloqueo optimista (@Version) en productos para evitar sobreventas en compras simultáneas.
- Paginación y filtros avanzados en todos los listados.

### 🎁 Sistema de Promociones
- Aplicación de descuentos porcentuales dinámicos.
- Validación automática de fechas de vigencia y estado de activación.

### 🛒 Compras y Pedidos
- Flujo de compra completo: Carrito -> Pedido -> Pago/Cancelación.
- Historial de compras detallado con estados en tiempo real (CREATED, PAID, CANCELED).

---

## 📂 Estructura del Proyecto (Backend)

```text
src/main/java/com/bakery/bakeryapi/
├── auth/           # Seguridad, JWT y Registro
├── category/       # Gestión de categorías de productos
├── product/        # Catálogo, stock y precios
├── purchase/       # Lógica de pedidos y transacciones
├── promotion/      # Reglas de descuento y ofertas
├── user/           # Gestión de usuarios y perfiles
├── shared/         # Excepciones globales y utilidades
└── infra/          # Configuraciones (Seguridad, OpenAPI, etc.)
```

---

## 📝 Endpoints Principales de la API

| Método | Endpoint | Descripción | Acceso |
| :--- | :--- | :--- | :--- |
| **POST** | `/auth/login` | Inicio de sesión (devuelve JWT + Refresh) | Público |
| **POST** | `/auth/refresh` | Renovar token de acceso | Público |
| **GET** | `/products` | Listado de productos (filtros/paginación) | Público |
| **GET** | `/promotions/active`| Ver ofertas vigentes | Público |
| **POST** | `/purchases` | Crear un nuevo pedido | Autenticado |
| **PATCH** | `/purchases/{id}/pay`| Confirmar pago de pedido | Autenticado |
| **POST** | `/categories` | Crear nueva categoría | **ADMIN** |
| **GET** | `/actuator/metrics` | Métricas de rendimiento | **ADMIN** |

---

## ⚙️ Configuración e Instalación

### Requisitos Previos
- Java 21 instalado.
- Servidor MySQL 8.0 activo.

### Pasos
1. Clonar el repositorio.
2. Copiar `.env.example` a `.env` y configurar las credenciales de BD y secreto JWT.
3. Ejecutar con Gradle:
   ```powershell
   ./gradlew bootRun
   ```

La documentación interactiva estará disponible en: `http://localhost:8080/swagger-ui/index.html`

---

## 🧪 Testing

El proyecto incluye una suite de pruebas completa:
- **Unitarias:** Lógica de negocio y cálculos de precios.
- **Integración:** Flujos de API con base de datos H2 en memoria.
- **E2E:** Pruebas de endpoints con RestAssured.

Ejecutar pruebas: `./gradlew test`

---

## 📄 Información del Proyecto
- **Título:** La Croassantina
- **Tipo:** Proyecto Intermodular (TFG)
- **Desarrollador:** Silvia Cachón Leiva
- **Centro:** IES Severo Ochoa Elche
- **Versión:** 1.0 (Mayo 2026)
