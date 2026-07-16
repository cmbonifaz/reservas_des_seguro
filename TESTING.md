# Pruebas del Sistema de Reservas

Este documento describe las 12 pruebas automatizadas del proyecto: 6 pruebas unitarias del
backend (JUnit 5 + Mockito) y 6 pruebas end-to-end del frontend (Playwright).

## 1. Pruebas unitarias (backend)

**Ubicación:** `backend/src/test/java/com/reservas/service/`
**Stack:** JUnit 5 + Mockito (incluidos en `spring-boot-starter-test`, ya declarado en `pom.xml`)
**Cómo correrlas:**

```bash
cd backend
./mvnw test        # o: mvn test
```

Cada servicio se prueba de forma aislada, simulando (`@Mock`) sus repositorios y colaboradores
con Mockito, sin necesidad de base de datos ni de levantar el contexto de Spring.

| # | Clase | Prueba | Qué valida |
|---|-------|--------|------------|
| 1 | `AuthServiceTest` | `login_conCredencialesValidas_retornaTokenYDatosDelUsuario` | Con email y password correctos, `AuthService.login` genera un token `admin-token-*` y devuelve nombre/email/rol del usuario. |
| 2 | `AuthServiceTest` | `login_conPasswordIncorrecto_lanzaExcepcion` | Si el password no coincide (`PasswordEncoder.matches` devuelve `false`), se lanza `RuntimeException("Contraseña incorrecta")`. |
| 3 | `ReservaServiceTest` | `crearReserva_conEmailNuevo_creaUsuarioYGuardaReservaComoPendiente` | Si el email de la reserva no existe como usuario, se crea uno nuevo con rol `CLIENTE` y la reserva se guarda con estado `Pendiente`. |
| 4 | `ReservaServiceTest` | `confirmarReserva_actualizaEstadoAConfirmada` | `confirmarReserva(id)` cambia el estado de la reserva de `Pendiente` a `Confirmada` y persiste el cambio. |
| 5 | `UsuarioServiceTest` | `crearUsuario_conEmailYaRegistrado_lanzaExcepcion` | No permite crear un usuario con un email duplicado; lanza `RuntimeException` y nunca llama a `save`. |
| 6 | `UsuarioServiceTest` | `crearUsuario_conPassword_laGuardaCifrada` | Al crear un usuario con password, este se cifra con `PasswordEncoder.encode` antes de guardarse (nunca en texto plano). |

## 2. Pruebas end-to-end (frontend, Playwright)

**Ubicación:** `frontend/e2e/`
**Stack:** [`@playwright/test`](https://playwright.dev/) (agregado como `devDependency` en `frontend/package.json`)
**Requisito:** backend corriendo en `http://localhost:8080` con la base de datos `reservas_db`
cargada (usuario admin `admin@reservas.com` / password `password` y servicios de ejemplo, ver
`database/schema.sql`).

**Cómo correrlas:**

```bash
cd frontend
npx playwright install chromium   # una sola vez, descarga el navegador
npm run test:e2e                  # corre las 6 pruebas en modo headless
npm run test:e2e:ui               # modo interactivo (UI mode) para depurar
```

Estas pruebas navegan la aplicación real en un navegador Chromium controlado por Playwright,
haciendo clic, llenando formularios y verificando lo que un usuario vería en pantalla.

| # | Archivo | Prueba | Flujo cubierto |
|---|---------|--------|-----------------|
| 7 | `e2e/login.spec.js` | `la pagina de inicio muestra el formulario de login` | Al entrar a `/` se ve el formulario de login ("Iniciar Sesión") con sus campos. |
| 8 | `e2e/login.spec.js` | `muestra un error al iniciar sesion con credenciales invalidas` | Con password incorrecto, se muestra un toast de error y el usuario permanece en `/`. |
| 9 | `e2e/login.spec.js` | `permite iniciar sesion con credenciales validas y redirige al dashboard` | Con credenciales correctas, el login redirige a `/dashboard` y se ve el panel administrativo. |
| 10 | `e2e/dashboard.spec.js` | `redirige a la pagina de login al intentar acceder al dashboard sin autenticacion` | `ProtectedRoute` bloquea el acceso directo a `/dashboard` sin sesión iniciada y redirige a `/`. |
| 11 | `e2e/reserva.spec.js` | `carga la lista de servicios disponibles desde la API` | El formulario público `/reservar` consulta `GET /api/servicios` y llena el selector de servicios. |
| 12 | `e2e/reserva.spec.js` | `permite completar y enviar una nueva reserva exitosamente` | Un cliente completa el formulario (nombre, teléfono, email, servicio, fecha, hora) y al enviarlo recibe el mensaje "Reserva creada exitosamente". |

## 3. Integración de Playwright en el proyecto

- **Instalación:** Playwright se agregó como `devDependency` del frontend (`npm install --save-dev
  @playwright/test`) para no mezclarse con las dependencias de producción de React.
- **Configuración:** `frontend/playwright.config.js` define:
  - `testDir: './e2e'` — las pruebas viven junto al resto del código del frontend.
  - `baseURL: 'http://localhost:3000'` — permite usar rutas relativas (`page.goto('/')`) en los tests.
  - `webServer` — si el frontend no está corriendo, Playwright lo levanta automáticamente con
    `npm start` y espera a que responda antes de correr las pruebas; si ya está corriendo (como en
    desarrollo local), reutiliza esa instancia (`reuseExistingServer`).
  - `screenshot: 'only-on-failure'` y `trace: 'on-first-retry'` — evidencia automática para
    depurar fallos sin tener que reproducirlos manualmente.
- **Selectores robustos:** las pruebas usan roles/labels/placeholders visibles para el usuario
  (`getByRole`, `getByPlaceholder`, `getByText`) y el atributo `name` que React Hook Form asigna
  a cada input, en vez de depender de clases CSS de Tailwind (que cambian con el diseño).
- **Reporte:** `npm run test:e2e` genera un reporte HTML (`frontend/playwright-report/`) navegable
  con capturas y trazas de cada prueba; está excluido de git en `.gitignore`.
- **Backend real, no mockeado:** estas pruebas no interceptan las llamadas HTTP — ejercitan el
  backend Spring Boot real contra PostgreSQL, por lo que detectan también problemas de integración
  (CORS, contratos de la API, datos sembrados) que las pruebas unitarias no cubren.

## 4. Resumen de ejecución

| Suite | Comando | Resultado verificado |
|-------|---------|------------------------|
| Unitarias (backend) | `mvn test` | 6 tests, 0 fallos |
| E2E (frontend) | `npm run test:e2e` | 6 tests, 0 fallos |
