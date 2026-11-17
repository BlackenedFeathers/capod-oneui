# CAPod One UI Edition

Compilación personal y modificada de [CAPod](https://github.com/d4rken-org/capod) (d4rken), pensada
para un Samsung Galaxy S25 Ultra con AirPods Pro 2.

> **Aviso de modificación (GPLv3, sección 5a).** Este árbol de código es una versión modificada de
> CAPod 5.6.0-rc0 (rama `main` de upstream, commit `d0664602`), cambiada el 16 de noviembre de 2026.
> No es una versión oficial y no está respaldada por el autor original. Sigue bajo la licencia
> GPLv3 (archivo `LICENSE`). Si compartes la APK con alguien, tienes que ofrecerle también este
> código fuente.

**Estado: compila y pasa las pruebas unitarias; falta probarla en el teléfono.** La primera
ejecución de GitHub Actions (17 de noviembre de 2026) compiló `assembleFossDebug`, publicó la APK y
pasó las pruebas unitarias de la variante FOSS y las comprobaciones originales de CAPod
(`code-checks.yml`). Nada de esta edición se ha probado todavía en un Galaxy S25 Ultra con unos
AirPods reales.

## Qué cambia respecto a CAPod original

| Área | Cambio | Archivos |
|------|--------|----------|
| Ventana emergente | Tarjeta oscura fija estilo One UI: esquinas redondeadas, barras de batería azul claro que se llenan con una animación, porcentaje y etiqueta por auricular y estuche, icono de carga. Usa las imágenes originales del modelo (`device_airpods_pro2_left/right/case.png`), que Android elige en la densidad de la pantalla | `reaction/ui/popup/PopUpContent.kt` |
| Datos no actuales | Un valor que viene de la caché y no de una lectura en vivo se dibuja atenuado y la tarjeta indica «Último conocido · hace …». Un nivel desconocido muestra «—» y la barra vacía; nunca un número inventado | `PopUpContent.kt`, `monitor/core/PodDevice.kt` |
| Funciones avanzadas | Nueva pantalla en Ajustes con interruptores persistentes (DataStore), todos activados al inicio | `main/core/advanced/*`, `main/ui/settings/advanced/*`, `SettingsScreen.kt`, `Nav.kt`, `App.kt` |
| Interruptores | Cada interruptor corta su función en un solo punto | `PopUpWindow.kt`, `PlayPause.kt`, `ConversationReaction.kt`, `MonitorService.kt` |
| Desbloqueo FOSS | En la variante FOSS las funciones avanzadas vienen disponibles sin pasar por la pantalla de patrocinio | `foss/.../UpgradeRepoOneUi.kt`, `foss/.../UpgradeModule.kt` |
| Popup por defecto | Los perfiles nuevos se crean con «Mostrar pop-up de estuche» y «Mostrar pop-up de conexión» activados | `DeviceProfileCreationViewModel.kt` |
| Compilación | Workflow que compila `assembleFossDebug` y publica la APK | `.github/workflows/build-foss-apk.yml`, `.github/scripts/run-gradle.sh` |
| Versión | El nombre de versión de la variante FOSS termina en `-oneui` | `app/build.gradle.kts` |
| Textos | Cadenas nuevas en inglés y español | `res/values*/strings.xml` |

No se han tocado el escáner BLE, el decodificador de anuncios de Apple, la conexión AAP, el
servicio en segundo plano, los widgets, la notificación ni la lógica que decide cuándo mostrar y
ocultar la ventana. Todo eso es el código original de CAPod.

## Cómo se comporta la ventana emergente

Es el mecanismo original de CAPod; esta edición solo cambia el aspecto de la tarjeta.

- **Aparece** encima de cualquier aplicación cuando CAPod ve que la tapa del estuche pasa a
  «abierta», o cuando los AirPods se acaban de conectar al teléfono.
- **Se quita** cuando ve la tapa «cerrada», cuando los AirPods se desconectan, o a los pocos
  segundos de dejar de recibir anuncios con la tapa abierta. También con el botón Cerrar, tocando
  fuera o deslizándola hacia abajo.
- **No se duplica**: si ya está en pantalla, se actualizan sus datos.
- **No aparece mientras tienes CAPod abierto en primer plano**, porque ahí ya ves los datos.
- **Cuándo escucha CAPod.** Por defecto el servicio en segundo plano arranca cuando los AirPods se
  conectan al teléfono y se detiene poco después de que se desconectan; así casi no gasta batería.
  Con eso la tarjeta sale cada vez que abres el estuche y los AirPods se conectan al S25 Ultra. Si
  quieres que salga también cuando los AirPods estaban conectados a otro aparato, activa
  **Conexión automática** en el perfil del dispositivo: CAPod pasa entonces a escuchar siempre, que
  consume algo más y pide quitar la optimización de batería.
- **Precisión de los datos.** Sin las claves de cifrado del dispositivo, los anuncios BLE de Apple
  solo dan la batería en pasos de 10 %. El valor exacto llega cuando hay conexión AAP con los
  AirPods. El estuche solo se anuncia con al menos un auricular dentro y la tapa recién abierta.

## Funciones avanzadas

Ajustes → **Funciones avanzadas**. Cada interruptor apaga la función en todos los dispositivos; los
ajustes de cada dispositivo se conservan y vuelven al reactivarla.

| Interruptor | Qué controla |
|-------------|--------------|
| Ventana emergente automática | La tarjeta sobre otras apps al abrir el estuche o al conectar |
| Reproducción y pausa automáticas | Pausar, reanudar e iniciar música según la detección de oído |
| Reacción a la conversación | Pausar o bajar el volumen mientras hablas |
| Avisos de batería | Notificación de carga completa y de estuche con poca batería |

Cada una se configura además por dispositivo, en Ajustes → Dispositivos → (tu perfil).

Siempre disponibles, sin interruptor: varios dispositivos, controles de pulsación, temas, widgets,
acceso rápido de control de ruido y los controles del dispositivo (volumen de tonos, modo de
micrófono, ciclo de modos de escucha, detección de sueño). Solo hacen algo cuando los configuras,
así que un segundo interruptor no añadiría nada.

### Sobre las funciones «Pro» y la licencia

Funciones que CAPod original marca como Pro, revisadas en el código: reproducción y pausa
automáticas, reacción a la conversación, reacción al sueño, ventanas emergentes de estuche y de
conexión, avisos de batería, widgets, temas, y los controles del dispositivo (control de ruido,
volumen de tonos, modo de micrófono, controles de pulsación). Todas pasan por la misma comprobación
(`UpgradeRepo.isPro`), que es la que cambia en la variante FOSS de esta edición.

- CAPod es software libre GPLv3. En la variante **FOSS** el estado «Pro» no es una compra: es un
  registro local que la propia aplicación crea cuando el usuario visita la página de patrocinio.
  No hay servidor de licencias ni verificación de pago. Cambiar ese valor por defecto en una
  compilación propia es un uso que la GPLv3 permite.
- **No se ha modificado nada de la variante Google Play**: la facturación (`app/src/gplay`) sigue
  intacta y esta edición no simula, falsifica ni evita ninguna compra.
- La pantalla de patrocinio original sigue en Ajustes y muestra el estado real. Si la aplicación te
  resulta útil, patrocinar al autor es la forma de sostener el proyecto:
  <https://github.com/sponsors/d4rken>.

### Funciones que no se pueden habilitar

- **Compras de Google Play**: solo existen en la variante `gplay` y requieren la cuenta del
  desarrollador; no se usan.
- **Funciones que dependen de la conexión AAP** (control de ruido, renombrar, detección de sueño,
  pulsaciones, batería al 1 %): necesitan los AirPods emparejados y una conexión L2CAP que no
  todos los teléfonos permiten. Si el S25 Ultra no la admite, la aplicación lo indica y esas
  funciones no estarán disponibles; el código no puede forzarlo sin root.
- **Widget dentro del panel de notificaciones**: Android no permite colocar widgets ahí. El
  equivalente es la notificación de CAPod (ver más abajo).

## Compilar con GitHub Actions (sin Android Studio)

1. Crea un repositorio en GitHub y sube **todo** el contenido de esta carpeta, incluidas las
   carpetas ocultas `.github` y `gradle`. Lo más sencillo es GitHub Desktop («Add local
   repository» → «Publish repository»). La subida por la web admite 100 archivos por tanda y suele
   omitir las carpetas ocultas.
2. En el repositorio, abre **Actions**. Si GitHub lo pide, pulsa «I understand my workflows, go
   ahead and enable them».
3. Elige **Build FOSS APK** → **Run workflow**. También se ejecuta solo con cada subida a `main`.
4. Al terminar en verde, abre la ejecución y descarga el artefacto
   **`capod-oneui-fossDebug-apk`** (abajo, en «Artifacts»). Es un zip con la APK dentro.
5. Si termina en rojo, la página de resumen de la ejecución muestra los errores del compilador ya
   extraídos (archivo, línea y mensaje). El registro completo está en el artefacto `build-logs`.

El workflow instala JDK 17 y la plataforma Android 36, valida el Gradle Wrapper, comprueba que la
variante `fossDebug` existe, compila el código y después empaqueta la APK. Las pruebas unitarias se
ejecutan en un trabajo aparte que informa pero no bloquea la APK.

**Los workflows originales de CAPod siguen en el repositorio, sin cambios, y también se
ejecutarán** al subir a `main`: `code-checks.yml` (compila además la variante de Google Play y
ejecuta todas las pruebas) y `emulator.yml` (pruebas en emulador). `pages.yml` falla si el repositorio no tiene GitHub Pages activado. Nada de eso afecta a la APK de
**Build FOSS APK**. Si no los quieres, desactívalos en Actions →
(workflow) → «…» → «Disable workflow».

Cada ejecución firma la APK de depuración con una clave nueva. Para instalar una compilación
posterior encima de otra tendrás que desinstalar antes la anterior.

## Instalar en el Galaxy S25 Ultra

1. Si tienes instalado el CAPod oficial, desinstálalo primero: esta APK usa el mismo identificador
   (`eu.darken.capod`) con otra firma y Android rechazará instalarla encima.
2. Copia la APK al teléfono y ábrela desde **Mis archivos**. Acepta «Instalar aplicaciones
   desconocidas» para Mis archivos cuando lo pida. Si Play Protect avisa de que no reconoce al
   desarrollador, es lo esperado en una compilación propia de depuración: «Instalar de todos modos».
3. Empareja los AirPods Pro 2 en Ajustes → Conexiones → Bluetooth si aún no lo están.
4. Abre CAPod y completa la configuración inicial. Concede lo que pida la pantalla principal:
   - **Dispositivos cercanos** (Bluetooth),
   - **Notificaciones**,
   - **Aparecer encima** (superposición), necesario para la ventana emergente,
   - quitar la optimización de batería, solo si activas Conexión automática.
5. Crea el perfil del dispositivo: modelo **AirPods Pro 2** y tu dispositivo emparejado. Los dos
   pop-ups quedan activados en el perfil nuevo.
6. Para que la ventana aparezca con la app cerrada, en One UI: Ajustes → Aplicaciones → CAPod →
   Batería → **Sin restricciones**, y comprueba que CAPod no esté en «Aplicaciones en suspensión
   profunda» (Ajustes → Batería → Límites de uso en segundo plano).
7. **Widget**: mantén pulsado un hueco de la pantalla de inicio → Widgets → CAPod → batería.
8. **Estado en el panel de notificaciones**: mientras los AirPods están conectados, CAPod mantiene
   una notificación con la batería de cada auricular y del estuche. En Ajustes de CAPod → Ajustes
   puedes activar **Notificación adicional**, **Mantener la notificación después de la
   desconexión** (deja los últimos valores a la vista) y **Nivel de batería en la barra de
   estado** (el porcentaje como icono).

## Verificado y pendiente

Verificado en GitHub Actions:

- [x] El proyecto compila (`assembleFossDebug`) y la APK se publica como artefacto.
- [x] Las pruebas unitarias de la variante FOSS pasan.
- [x] Las comprobaciones originales de CAPod (`code-checks.yml`: lint, ambas variantes, pruebas) pasan.
- [x] Las imágenes `device_airpods_pro2_left/right/case.png` son las originales del repositorio.
- [x] La licencia es GPLv3 y no se ha tocado el código de facturación.

Pendiente (requiere el teléfono y los AirPods):

- [ ] Aspecto real de la tarjeta One UI en el S25 Ultra (tamaños, contraste, animación).
- [ ] Que la ventana aparezca al abrir el estuche y desaparezca al cerrarlo en el S25 Ultra.
- [ ] Indicador de carga de cada auricular y del estuche.
- [ ] Atenuado y etiqueta de «último conocido».
- [ ] Cada interruptor de Funciones avanzadas, y que se conserven tras reiniciar el teléfono.
- [ ] Widget de batería y notificación permanente.
- [ ] Consumo de batería en segundo plano durante un día de uso.
