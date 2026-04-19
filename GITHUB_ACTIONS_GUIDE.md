## 📋 Guía: Subir cambios a GitHub y ver en GitHub Actions

### 🔧 Paso 1: Inicializar Git (si aún no está configurado)

```bash
# Navega al directorio del proyecto
cd c:\Users\ACER-02509\Downloads\actividad2_bookingya\bookingya_actividad2

# Inicializa git si es necesario
git init

# Configura tu usuario
git config user.name "Tu Nombre"
git config user.email "tu.email@example.com"

# (OPCIONAL) Si ya tienes remoto configurado, salta esto
git remote add origin https://github.com/TU_USUARIO/bookingya_students.git
```

### ✅ Paso 2: Agregar cambios y hacer commit

```bash
# Ver estado actual
git status

# Agregar TODOS los cambios
git add .

# Commit con mensaje descriptivo
git commit -m "Fase 1: Implementar TDD con ReservationService - 20 test cases"

# Ver los commits
git log --oneline
```

### 🚀 Paso 3: Hacer push a GitHub

```bash
# Si aún no has hecho push a esta rama, usa:
git push -u origin main

# Para siguiente pushes:
git push origin main

# O si trabajas en rama 'estudiantes':
git push -u origin estudiantes
```

### 📊 Paso 4: Ver los resultados en GitHub Actions

1. Abre tu repositorio en GitHub
2. Haz clic en la pestaña **"Actions"**
3. Verás los workflows ejecutándose:
   - ✅ **FASE 1 - TDD**: Pruebas unitarias con JUnit 5
   - ✅ **FASE 2 - BDD**: Pruebas de comportamiento con Serenity
4. Haz clic en el workflow para ver los detalles
5. Si todo está verde ✅, los tests pasaron correctamente

### 🎯 Verificar que GitHub Actions funciona

- **Badges de Estado**: Visible en el README.md del repo
- **Reportes**: Los reports de Serenity se suben como "Artifacts"
- **Historial**: Cada push dispara automáticamente el pipeline

### 💡 Comandos útiles

```bash
# Ver ramas locales
git branch

# Ver remoto configurado
git remote -v

# Cambiar de rama
git checkout estudiantes

# Ver historial de commits
git log --oneline -10

# Ver cambios sin hacer commit
git diff

# Deshacer último commit (sin perder cambios)
git reset --soft HEAD~1
```

### ⚠️ Si hay conflictos o errores

```bash
# Traer cambios del remoto
git fetch origin

# Sincronizar rama local con remota
git pull origin main

# Si hay conflictos, resuelve manualmente luego:
git add .
git commit -m "Resolver conflictos"
git push origin main
```

### 📄 Nota importante
Asegúrate de que en tu repositorio GitHub existe el archivo `.github/workflows/ci.yml` 
ya configurado con los jobs para FASE 1 y FASE 2.
