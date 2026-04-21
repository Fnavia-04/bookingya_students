
# BookingYa — Actividad 2: TDD + BDD + ATDD

## Estructura de archivos entregados

```
actividad2/
├── .github/workflows/ci.yml          → Pipeline CI/CD para las 3 fases
│
├── fase1/                            → FASE 1: TDD (JUnit 5 + Mockito)
│   ├── pom.xml                       → dependencias Spring Boot Test + H2
│   └── src/test/java/.../
│       └── ReservationServiceTDDTest.java   → 20 pruebas unitarias
│
├── fase2/                            → FASE 2: BDD (Serenity + Cucumber)
│   ├── pom.xml                       → dependencias Serenity + JUnit 4
│   └── src/test/
│       ├── resources/
│       │   ├── features/reservas.feature   → escenarios en Gherkin (ES)
│       │   └── serenity.conf               → configuración Serenity
│       └── java/.../
│           ├── CucumberRunner.java         → runner con Serenity
│           └── steps/ReservationSteps.java → step definitions
│
└── fase3/                            → FASE 3: ATDD (Playwright + TypeScript)
    ├── package.json
    ├── playwright.config.ts
    └── tests/
        ├── helpers.ts               → funciones de setup reutilizables
        └── reservation.spec.ts      → 14 pruebas de aceptación (AC-01 a AC-13)
```

---

## Cómo integrar al repositorio BookingYa

### Paso 1: Clonar el fork
```bash
git clone https://github.com/TU_USUARIO/bookingya_students.git
cd bookingya_students
git checkout estudiantes
```

### Paso 2: Copiar los archivos de prueba

**Fase 1 — TDD:**
Copiar `ReservationServiceTDDTest.java` en:
```
src/test/java/com/project/bookingya/ReservationServiceTDDTest.java
```
Agregar en el `pom.xml` existente (ya debería tener `spring-boot-starter-test`):
```xml
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <scope>test</scope>
</dependency>
```

**Fase 2 — BDD:**
Crear carpeta `fase2/` en la raíz del proyecto y copiar todo el contenido.

**Fase 3 — ATDD:**
Crear carpeta `fase3/` en la raíz del proyecto y copiar todo el contenido.

**CI/CD:**
Copiar `.github/workflows/ci.yml` (reemplaza el existente o merge manual).

---

## Cómo ejecutar localmente

### Fase 1 — TDD (sin BD, solo Mockito)
```bash
# Desde la raíz del proyecto principal
mvn test -Dtest=ReservationServiceTDDTest
```

### Fase 2 — BDD (requiere app corriendo)
```bash
# Terminal 1: levantar la app
cp .env.example .env  # configurar credenciales PostgreSQL
./mvnw spring-boot:run

# Terminal 2: ejecutar Serenity + Cucumber
cd fase2
mvn verify

# Ver reporte HTML
open fase2/target/site/serenity/index.html
```

### Fase 3 — ATDD (requiere app corriendo)
```bash
# Terminal 1: app ya corriendo (misma que en Fase 2)

# Terminal 2: ejecutar Playwright
cd fase3
npm install
npx playwright install chromium
npx playwright test

# Ver reporte
npx playwright show-report
```

---

## Cobertura de escenarios

| # | Escenario | Fase 1 TDD | Fase 2 BDD | Fase 3 ATDD |
|---|---|:---:|:---:|:---:|
| 1 | Crear reserva válida | TDD-01 | ✅ | AC-01 |
| 2 | checkIn >= checkOut → 400 | TDD-02 | ✅ | AC-02 |
| 3 | Habitación no existe → 404 | TDD-03 | — | — |
| 4 | Huésped no existe → 404 | TDD-04 | — | — |
| 5 | Habitación no disponible → 400 | TDD-05 | ✅ | AC-04 |
| 6 | Capacidad excedida → 400 | TDD-06 | ✅ | AC-03 |
| 7 | Solapamiento habitación → 400 | TDD-07 | ✅ | AC-05 |
| 8 | Solapamiento huésped → 400 | TDD-08 | — | AC-06 |
| 9 | Listar todas las reservas | TDD-09 | ✅ | AC-07 |
| 10 | Obtener por ID existente | TDD-10,11 | ✅ | AC-08,09 |
| 11 | Filtrar por habitación | TDD-12 | — | — |
| 12 | Filtrar por huésped | TDD-13 | — | — |
| 13 | Actualizar reserva válida | TDD-14,15 | ✅ | AC-11 |
| 14 | Eliminar reserva | TDD-16,17 | ✅ | AC-12,13 |
| 15 | Consultar disponibilidad | TDD-18,19,20 | ✅ | AC-10a,10b |


Test
