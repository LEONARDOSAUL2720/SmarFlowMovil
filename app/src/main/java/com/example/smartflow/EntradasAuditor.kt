package com.example.smartflow

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class EntradasAuditor : AppCompatActivity() {

    // Configuración de la API
    private val BASE_URL = "https://smartflow-mwmm.onrender.com/api"
    private val ENTRADA_ENDPOINT = "$BASE_URL/auditor/entrada"

    // UI Components principales
    private lateinit var etNumeroEntrada: EditText
    private lateinit var btnBuscarEntrada: Button

    // Container principal de detalles
    private lateinit var containerDetalles: LinearLayout

    // Card de validaciones
    private lateinit var cardValidaciones: LinearLayout

    // Entrada Fields
    private lateinit var tvNumeroEntrada: TextView
    private lateinit var tvCantidadEntrada: TextView
    private lateinit var tvProveedorEntrada: TextView
    private lateinit var tvFechaEntrada: TextView
    private lateinit var tvEstatusEntrada: TextView

    // Orden de Compra Fields
    private lateinit var tvNumeroOrden: TextView
    private lateinit var tvCantidad: TextView
    private lateinit var tvPrecioUnitario: TextView
    private lateinit var tvPrecioTotal: TextView
    private lateinit var tvFechaOrden: TextView
    private lateinit var tvEstatus: TextView

    // Perfume Fields
    private lateinit var tvNombrePerfume: TextView
    private lateinit var tvDescripcionPerfume: TextView
    private lateinit var tvCategoriaPerfume: TextView
    private lateinit var tvPrecioVentaPerfume: TextView
    private lateinit var tvStockPerfume: TextView
    private lateinit var tvStockMinimoPerfume: TextView
    private lateinit var tvUbicacionPerfume: TextView
    private lateinit var tvFechaExpiracionPerfume: TextView
    private lateinit var tvEstadoPerfume: TextView

    // Proveedor Fields
    private lateinit var tvNombreProveedor: TextView
    private lateinit var tvRfcProveedor: TextView
    private lateinit var tvContactoProveedor: TextView
    private lateinit var tvTelefonoProveedor: TextView
    private lateinit var tvEmailProveedor: TextView
    private lateinit var tvDireccionProveedor: TextView
    private lateinit var tvEstadoProveedor: TextView

    // Validación Fields - ACTUALIZADOS
    private lateinit var tvEstadoValidacion: TextView
    private lateinit var tvMensajePrincipal: TextView
    private lateinit var tvAccionRecomendada: TextView
    private lateinit var tvSiguientePaso: TextView
    private lateinit var tvTiempoResolucion: TextView
    private lateinit var tvPorcentajeCumplimiento: TextView
    private lateinit var tvNivelRiesgo: TextView

    // Validaciones individuales
    private lateinit var tvProveedorValidacion: TextView
    private lateinit var tvCantidadValidacion: TextView
    private lateinit var tvFechaValidacion: TextView
    private lateinit var tvPrecioValidacion: TextView
    private lateinit var tvEstadoOrdenValidacion: TextView

    // Contenedores de discrepancias
    private lateinit var layoutDiscrepancias: LinearLayout
    private lateinit var containerDiscrepanciasCriticas: LinearLayout
    private lateinit var containerDiscrepanciasImportantes: LinearLayout
    private lateinit var containerAdvertencias: LinearLayout
    private lateinit var containerRecomendaciones: LinearLayout

    // Botón validar
    private lateinit var btnValidarEntrada: Button

    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.entradas_auditor)

        initializeViews()
        setupClickListeners()

        requestQueue = Volley.newRequestQueue(this)

        Log.d("EntradasAuditor", "Actividad de Entradas Auditor iniciada")
    }

    private fun initializeViews() {
        // Búsqueda
        etNumeroEntrada = findViewById(R.id.et_numero_entrada)
        btnBuscarEntrada = findViewById(R.id.btn_buscar_entrada)

        // Container principal de detalles
        containerDetalles = findViewById(R.id.container_detalles)
        cardValidaciones = findViewById(R.id.card_validaciones)

        // Entrada Fields
        tvNumeroEntrada = findViewById(R.id.tv_numero_entrada)
        tvCantidadEntrada = findViewById(R.id.tv_cantidad_entrada)
        tvProveedorEntrada = findViewById(R.id.tv_proveedor_entrada)
        tvFechaEntrada = findViewById(R.id.tv_fecha_entrada)
        tvEstatusEntrada = findViewById(R.id.tv_estatus_entrada)

        // Orden de Compra
        tvNumeroOrden = findViewById(R.id.tv_numero_orden)
        tvCantidad = findViewById(R.id.tv_cantidad)
        tvPrecioUnitario = findViewById(R.id.tv_precio_unitario)
        tvPrecioTotal = findViewById(R.id.tv_precio_total)
        tvFechaOrden = findViewById(R.id.tv_fecha_orden)
        tvEstatus = findViewById(R.id.tv_estatus)

        // Perfume
        tvNombrePerfume = findViewById(R.id.tv_nombre_perfume)
        tvDescripcionPerfume = findViewById(R.id.tv_descripcion_perfume)
        tvCategoriaPerfume = findViewById(R.id.tv_categoria_perfume)
        tvPrecioVentaPerfume = findViewById(R.id.tv_precio_venta_perfume)
        tvStockPerfume = findViewById(R.id.tv_stock_perfume)
        tvStockMinimoPerfume = findViewById(R.id.tv_stock_minimo_perfume)
        tvUbicacionPerfume = findViewById(R.id.tv_ubicacion_perfume)
        tvFechaExpiracionPerfume = findViewById(R.id.tv_fecha_expiracion_perfume)
        tvEstadoPerfume = findViewById(R.id.tv_estado_perfume)

        // Proveedor
        tvNombreProveedor = findViewById(R.id.tv_nombre_proveedor)
        tvRfcProveedor = findViewById(R.id.tv_rfc_proveedor)
        tvContactoProveedor = findViewById(R.id.tv_contacto_proveedor)
        tvTelefonoProveedor = findViewById(R.id.tv_telefono_proveedor)
        tvEmailProveedor = findViewById(R.id.tv_email_proveedor)
        tvDireccionProveedor = findViewById(R.id.tv_direccion_proveedor)
        tvEstadoProveedor = findViewById(R.id.tv_estado_proveedor)

        // Validaciones - NUEVOS
        tvEstadoValidacion = findViewById(R.id.tv_estado_validacion)
        tvMensajePrincipal = findViewById(R.id.tv_mensaje_principal)
        tvAccionRecomendada = findViewById(R.id.tv_accion_recomendada)
        tvSiguientePaso = findViewById(R.id.tv_siguiente_paso)
        tvTiempoResolucion = findViewById(R.id.tv_tiempo_resolucion)
        tvPorcentajeCumplimiento = findViewById(R.id.tv_porcentaje_cumplimiento)
        tvNivelRiesgo = findViewById(R.id.tv_nivel_riesgo)

        // Validaciones individuales
        tvProveedorValidacion = findViewById(R.id.tv_proveedor_validacion)
        tvCantidadValidacion = findViewById(R.id.tv_cantidad_validacion)
        tvFechaValidacion = findViewById(R.id.tv_fecha_validacion)
        tvPrecioValidacion = findViewById(R.id.tv_precio_validacion)
        tvEstadoOrdenValidacion = findViewById(R.id.tv_estado_orden_validacion)

        // Contenedores de discrepancias
        layoutDiscrepancias = findViewById(R.id.layout_discrepancias)
        containerDiscrepanciasCriticas = findViewById(R.id.container_discrepancias_criticas)
        containerDiscrepanciasImportantes = findViewById(R.id.container_discrepancias_importantes)
        containerAdvertencias = findViewById(R.id.container_advertencias)
        containerRecomendaciones = findViewById(R.id.container_recomendaciones)

        // Botón validar
        btnValidarEntrada = findViewById(R.id.btn_validar_entrada)
    }

    private fun setupClickListeners() {
        btnBuscarEntrada.setOnClickListener {
            val numeroEntrada = etNumeroEntrada.text.toString().trim()
            if (numeroEntrada.isNotEmpty()) {
                Log.d("EntradasAuditor", "🔍 Búsqueda iniciada con número: '$numeroEntrada'")
                buscarEntrada(numeroEntrada)
            } else {
                Toast.makeText(this, "Por favor ingresa un número de entrada", Toast.LENGTH_SHORT).show()
            }
        }

        btnValidarEntrada.setOnClickListener {
            val numeroEntrada = etNumeroEntrada.text.toString().trim()
            if (numeroEntrada.isNotEmpty()) {
                Log.d("EntradasAuditor", "✅ Procesando validación para entrada: '$numeroEntrada'")
                procesarValidacionEntrada(numeroEntrada)
            } else {
                Toast.makeText(this, "No hay entrada seleccionada para validar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buscarEntrada(numeroEntrada: String) {
        showLoading(true)

        val token = getAuthToken()
        if (token.isNullOrEmpty()) {
            showError("Token de autenticación no encontrado")
            return
        }

        val url = "$ENTRADA_ENDPOINT/$numeroEntrada"
        Log.d("EntradasAuditor", "🔍 Haciendo petición a: $url")

        val request = object : JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                Log.d("EntradasAuditor", "✅ Respuesta recibida: $response")
                showLoading(false)
                handleApiResponse(response)
            },
            { error ->
                Log.e("EntradasAuditor", "❌ Error en petición", error)
                Log.e("EntradasAuditor", "❌ Status code: ${error.networkResponse?.statusCode}")

                val responseData = error.networkResponse?.data?.toString(Charsets.UTF_8)
                Log.e("EntradasAuditor", "❌ Response body: $responseData")

                showLoading(false)

                val errorMessage = when (error.networkResponse?.statusCode) {
                    400 -> "Número de entrada inválido"
                    401 -> "No autorizado. Inicia sesión nuevamente"
                    403 -> "No tienes permisos para acceder a esta información"
                    404 -> "Entrada no encontrada"
                    500 -> "Error interno del servidor"
                    else -> "Error de conexión. Verifica tu internet"
                }

                showError(errorMessage)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        requestQueue.add(request)
    }

    private fun handleApiResponse(response: JSONObject) {
        try {
            val data = response.getJSONObject("data")
            val entrada = data.getJSONObject("entrada")
            val perfume = data.optJSONObject("perfume")
            val proveedor = data.optJSONObject("proveedor_detalle")
            val ordenCompra = data.optJSONObject("orden_compra_relacionada")
            val validacion = data.getJSONObject("validacion")

            displayEntradaData(entrada, perfume, proveedor, ordenCompra)
            displayValidacionesDetalladas(validacion)

            // Mostrar el container principal de detalles
            containerDetalles.visibility = View.VISIBLE
            cardValidaciones.visibility = View.VISIBLE

            // Configurar botón de validación según el estado actual y las validaciones
            configurarBotonValidacion(entrada, validacion)

        } catch (e: Exception) {
            Log.e("EntradasAuditor", "❌ Error procesando respuesta", e)
            showError("Error procesando los datos recibidos: ${e.message}")
        }
    }

    private fun displayEntradaData(
        entrada: JSONObject,
        perfume: JSONObject?,
        proveedor: JSONObject?,
        ordenCompra: JSONObject?
    ) {
        // Mostrar datos de la ENTRADA
        tvNumeroEntrada.text = "N° Entrada: ${entrada.optString("numero_entrada", "No disponible")}"
        tvCantidadEntrada.text = "Cantidad: ${entrada.optInt("cantidad", 0)}"

        // Proveedor de la entrada
        val proveedorEntrada = entrada.optJSONObject("proveedor")
        tvProveedorEntrada.text = "Proveedor: ${proveedorEntrada?.optString("nombre_proveedor", "No disponible") ?: "No disponible"}"
        tvFechaEntrada.text = "Fecha entrada: ${formatDate(entrada.optString("fecha_entrada", ""))}"
        tvEstatusEntrada.text = "Estatus: ${entrada.optString("estatus_validacion", "No disponible").uppercase()}"

        // Mostrar datos de la orden de compra relacionada
        if (ordenCompra != null) {
            tvNumeroOrden.text = "N° Orden: ${ordenCompra.optString("numero_orden", "No disponible")}"
            tvCantidad.text = "Cantidad: ${ordenCompra.optInt("cantidad", 0)}"
            tvPrecioUnitario.text = "Precio unitario: $${ordenCompra.optDouble("precio_unitario", 0.0)}"
            tvPrecioTotal.text = "Precio total: $${ordenCompra.optDouble("precio_total", 0.0)}"
            tvFechaOrden.text = "Fecha: ${formatDate(ordenCompra.optString("fecha_orden", ""))}"
            tvEstatus.text = "Estatus: ${ordenCompra.optString("estado", "No disponible").uppercase()}"
        } else {
            tvNumeroOrden.text = "N° Orden: No encontrada"
            tvCantidad.text = "Cantidad: No disponible"
            tvPrecioUnitario.text = "Precio unitario: No disponible"
            tvPrecioTotal.text = "Precio total: No disponible"
            tvFechaOrden.text = "Fecha: No disponible"
            tvEstatus.text = "Estatus: No disponible"
        }

        // Mostrar datos del perfume
        if (perfume != null) {
            tvNombrePerfume.text = "Nombre: ${perfume.optString("name_per", "No disponible")}"
            tvDescripcionPerfume.text = "Descripción: ${perfume.optString("descripcion_per", "No disponible")}"
            tvCategoriaPerfume.text = "Categoría: ${perfume.optString("categoria_per", "No disponible")}"
            tvPrecioVentaPerfume.text = "Precio venta: $${perfume.optDouble("precio_venta_per", 0.0)}"
            tvStockPerfume.text = "Stock actual: ${perfume.optInt("stock_per", 0)}"
            tvStockMinimoPerfume.text = "Stock mínimo: ${perfume.optInt("stock_minimo_per", 0)}"
            tvUbicacionPerfume.text = "Ubicación: ${perfume.optString("ubicacion_per", "No disponible")}"
            tvFechaExpiracionPerfume.text = "Expira: ${formatDate(perfume.optString("fecha_expiracion", ""))}"
            tvEstadoPerfume.text = "Estado: ${perfume.optString("estado", "No disponible")}"
        } else {
            tvNombrePerfume.text = "Nombre: No disponible"
            tvDescripcionPerfume.text = "Descripción: No disponible"
            tvCategoriaPerfume.text = "Categoría: No disponible"
            tvPrecioVentaPerfume.text = "Precio venta: No disponible"
            tvStockPerfume.text = "Stock actual: No disponible"
            tvStockMinimoPerfume.text = "Stock mínimo: No disponible"
            tvUbicacionPerfume.text = "Ubicación: No disponible"
            tvFechaExpiracionPerfume.text = "Expira: No disponible"
            tvEstadoPerfume.text = "Estado: No disponible"
        }

        // Mostrar datos del proveedor
        if (proveedor != null) {
            tvNombreProveedor.text = "Nombre: ${proveedor.optString("nombre_proveedor", "No disponible")}"
            tvRfcProveedor.text = "RFC: ${proveedor.optString("rfc", "No disponible")}"
            tvContactoProveedor.text = "Contacto: ${proveedor.optString("contacto", "No disponible")}"
            tvTelefonoProveedor.text = "Teléfono: ${proveedor.optString("telefono", "No disponible")}"
            tvEmailProveedor.text = "Email: ${proveedor.optString("email", "No disponible")}"
            tvDireccionProveedor.text = "Dirección: ${proveedor.optString("direccion", "No disponible")}"
            tvEstadoProveedor.text = "Estado: ${proveedor.optString("estado", "No disponible")}"
        } else {
            tvNombreProveedor.text = "Nombre: No disponible"
            tvRfcProveedor.text = "RFC: No disponible"
            tvContactoProveedor.text = "Contacto: No disponible"
            tvTelefonoProveedor.text = "Teléfono: No disponible"
            tvEmailProveedor.text = "Email: No disponible"
            tvDireccionProveedor.text = "Dirección: No disponible"
            tvEstadoProveedor.text = "Estado: No disponible"
        }
    }

    private fun displayValidacionesDetalladas(validacion: JSONObject) {
        try {
            // RESUMEN EJECUTIVO
            val resumenEjecutivo = validacion.getJSONObject("resumen_ejecutivo")
            val estado = resumenEjecutivo.getString("estado")
            val color = resumenEjecutivo.getString("color")
            val icono = resumenEjecutivo.getString("icono")
            val mensajePrincipal = resumenEjecutivo.getString("mensaje_principal")
            val accionRecomendada = resumenEjecutivo.getString("accion_recomendada")
            val siguientePaso = resumenEjecutivo.getString("siguiente_paso")
            val tiempoResolucion = resumenEjecutivo.getString("tiempo_estimado_resolucion")
            val puedeProceser = resumenEjecutivo.getBoolean("puede_procesar")

            // Estado general con colores
            tvEstadoValidacion.text = "$icono $estado"
            val colorInt = when(estado) {
                "APROBADA" -> android.R.color.holo_green_dark
                "CONDICIONAL" -> android.R.color.holo_orange_light
                "REQUIERE_REVISION_GERENCIAL" -> android.R.color.holo_orange_dark
                "RECHAZADA" -> android.R.color.holo_red_dark
                else -> android.R.color.darker_gray
            }
            tvEstadoValidacion.setTextColor(ContextCompat.getColor(this, colorInt))

            // Información ejecutiva
            tvMensajePrincipal.text = mensajePrincipal
            tvAccionRecomendada.text = "Acción: $accionRecomendada"
            tvSiguientePaso.text = "Siguiente paso: $siguientePaso"
            tvTiempoResolucion.text = "Tiempo estimado: $tiempoResolucion"

            // Métricas
            val porcentajeCumplimiento = validacion.getInt("porcentaje_cumplimiento")
            val nivelRiesgo = validacion.getString("nivel_riesgo")

            tvPorcentajeCumplimiento.text = "Cumplimiento: ${porcentajeCumplimiento}%"
            tvNivelRiesgo.text = "Riesgo: $nivelRiesgo"

            val colorRiesgo = when(nivelRiesgo) {
                "BAJO" -> android.R.color.holo_green_dark
                "MEDIO-BAJO" -> android.R.color.holo_green_light
                "MEDIO" -> android.R.color.holo_orange_light
                "ALTO" -> android.R.color.holo_red_dark
                else -> android.R.color.darker_gray
            }
            tvNivelRiesgo.setTextColor(ContextCompat.getColor(this, colorRiesgo))

            // VALIDACIONES INDIVIDUALES
            val proveedorCoincide = validacion.getBoolean("proveedor_coincide")
            val cantidadValida = validacion.getBoolean("cantidad_valida")
            val fechaCoherente = validacion.getBoolean("fecha_coherente")
            val precioCoherente = validacion.getBoolean("precio_coherente")
            val estadoOrdenValido = validacion.getBoolean("estado_orden_valido")

            tvProveedorValidacion.text = "Proveedor: ${if (proveedorCoincide) "✅ Coincide" else "❌ No coincide"}"
            tvProveedorValidacion.setTextColor(ContextCompat.getColor(this,
                if (proveedorCoincide) android.R.color.holo_green_dark else android.R.color.holo_red_dark))

            tvCantidadValidacion.text = "Cantidad: ${if (cantidadValida) "✅ Válida" else "❌ Inválida"}"
            tvCantidadValidacion.setTextColor(ContextCompat.getColor(this,
                if (cantidadValida) android.R.color.holo_green_dark else android.R.color.holo_red_dark))

            tvFechaValidacion.text = "Fechas: ${if (fechaCoherente) "✅ Coherentes" else "❌ Incoherentes"}"
            tvFechaValidacion.setTextColor(ContextCompat.getColor(this,
                if (fechaCoherente) android.R.color.holo_green_dark else android.R.color.holo_red_dark))

            tvPrecioValidacion.text = "Precio: ${if (precioCoherente) "✅ Coherente" else "❌ Incoherente"}"
            tvPrecioValidacion.setTextColor(ContextCompat.getColor(this,
                if (precioCoherente) android.R.color.holo_green_dark else android.R.color.holo_red_dark))

            tvEstadoOrdenValidacion.text = "Estado Orden: ${if (estadoOrdenValido) "✅ Válido" else "❌ Inválido"}"
            tvEstadoOrdenValidacion.setTextColor(ContextCompat.getColor(this,
                if (estadoOrdenValido) android.R.color.holo_green_dark else android.R.color.holo_red_dark))

            // DISCREPANCIAS Y OBSERVACIONES
            mostrarDiscrepanciasDetalladas(validacion)

            Log.d("EntradasAuditor", "✅ Validaciones detalladas mostradas: $estado")

        } catch (e: Exception) {
            Log.e("EntradasAuditor", "❌ Error mostrando validaciones detalladas", e)
        }
    }

    private fun mostrarDiscrepanciasDetalladas(validacion: JSONObject) {
        try {
            // Limpiar contenedores
            containerDiscrepanciasCriticas.removeAllViews()
            containerDiscrepanciasImportantes.removeAllViews()
            containerAdvertencias.removeAllViews()
            containerRecomendaciones.removeAllViews()

            var hayDiscrepancias = false

            // DISCREPANCIAS CRÍTICAS
            val discrepanciasCriticas = validacion.getJSONArray("discrepancias_criticas")
            if (discrepanciasCriticas.length() > 0) {
                hayDiscrepancias = true
                mostrarCategoria("🚨 DISCREPANCIAS CRÍTICAS", discrepanciasCriticas, containerDiscrepanciasCriticas, android.R.color.holo_red_dark)
            }

            // DISCREPANCIAS IMPORTANTES
            val discrepanciasImportantes = validacion.getJSONArray("discrepancias_importantes")
            if (discrepanciasImportantes.length() > 0) {
                hayDiscrepancias = true
                mostrarCategoria("⚠️ DISCREPANCIAS IMPORTANTES", discrepanciasImportantes, containerDiscrepanciasImportantes, android.R.color.holo_orange_dark)
            }

            // ADVERTENCIAS
            val advertencias = validacion.getJSONArray("advertencias")
            if (advertencias.length() > 0) {
                hayDiscrepancias = true
                mostrarCategoria("📋 ADVERTENCIAS", advertencias, containerAdvertencias, android.R.color.holo_orange_light)
            }

            // RECOMENDACIONES
            val recomendaciones = validacion.getJSONArray("recomendaciones")
            if (recomendaciones.length() > 0) {
                mostrarCategoria("✅ RECOMENDACIONES", recomendaciones, containerRecomendaciones, android.R.color.holo_green_dark)
            }

            // Mostrar u ocultar el layout de discrepancias
            layoutDiscrepancias.visibility = if (hayDiscrepancias) View.VISIBLE else View.GONE

        } catch (e: Exception) {
            Log.e("EntradasAuditor", "❌ Error mostrando discrepancias detalladas", e)
        }
    }

    private fun mostrarCategoria(titulo: String, items: JSONArray, container: LinearLayout, color: Int) {
        // Título de la categoría
        val tituloView = TextView(this)
        tituloView.text = titulo
        tituloView.setTextColor(ContextCompat.getColor(this, color))
        tituloView.textSize = 16f
        tituloView.setTypeface(null, android.graphics.Typeface.BOLD)
        tituloView.setPadding(0, 8, 0, 4)
        container.addView(tituloView)

        // Items de la categoría
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            mostrarItemDiscrepancia(item, container, color)
        }
    }

    private fun mostrarItemDiscrepancia(item: JSONObject, container: LinearLayout, color: Int) {
        // Título del item
        val titulo = item.optString("titulo", item.optString("mensaje", "Sin título"))
        val tituloView = TextView(this)
        tituloView.text = titulo
        tituloView.setTextColor(ContextCompat.getColor(this, color))
        tituloView.textSize = 14f
        tituloView.setTypeface(null, android.graphics.Typeface.BOLD)
        tituloView.setPadding(16, 4, 8, 2)
        container.addView(tituloView)

        // Descripción si existe
        val descripcion = item.optString("descripcion", "")
        if (descripcion.isNotEmpty()) {
            val descripcionView = TextView(this)
            descripcionView.text = descripcion
            descripcionView.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
            descripcionView.textSize = 12f
            descripcionView.setPadding(32, 2, 8, 4)
            container.addView(descripcionView)
        }

        // Qué hacer
        val queHacer = item.optString("que_hacer", "")
        if (queHacer.isNotEmpty()) {
            val queHacerView = TextView(this)
            queHacerView.text = "💡 $queHacer"
            queHacerView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
            queHacerView.textSize = 12f
            queHacerView.setTypeface(null, android.graphics.Typeface.BOLD)
            queHacerView.setPadding(32, 2, 8, 4)
            container.addView(queHacerView)
        }

        // Acciones sugeridas
        val accionesSugeridas = item.optJSONArray("acciones_sugeridas")
        if (accionesSugeridas != null && accionesSugeridas.length() > 0) {
            for (j in 0 until accionesSugeridas.length()) {
                val accion = accionesSugeridas.getString(j)
                val accionView = TextView(this)
                accionView.text = "  • $accion"
                accionView.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
                accionView.textSize = 11f
                accionView.setPadding(40, 1, 8, 1)
                container.addView(accionView)
            }
        }

        // Espaciador
        val espaciador = View(this)
        espaciador.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            8
        )
        container.addView(espaciador)
    }

    private fun configurarBotonValidacion(entrada: JSONObject, validacion: JSONObject) {
        val estatusValidacion = entrada.optString("estatus_validacion", "registrado")
        val resumenEjecutivo = validacion.getJSONObject("resumen_ejecutivo")
        val puedeProceser = resumenEjecutivo.getBoolean("puede_procesar")
        val estado = resumenEjecutivo.getString("estado")

        when {
            estatusValidacion == "validado" -> {
                btnValidarEntrada.isEnabled = false
                btnValidarEntrada.text = "✅ Ya Validada"
                btnValidarEntrada.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_green_dark)
            }
            estado == "RECHAZADA" || !puedeProceser -> {
                btnValidarEntrada.isEnabled = false
                btnValidarEntrada.text = "❌ No se puede validar"
                btnValidarEntrada.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_red_dark)
            }
            estado == "REQUIERE_REVISION_GERENCIAL" -> {
                btnValidarEntrada.isEnabled = true
                btnValidarEntrada.text = "⚠️ Validar (Requiere Aprobación)"
                btnValidarEntrada.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_orange_dark)
            }
            estado == "CONDICIONAL" -> {
                btnValidarEntrada.isEnabled = true
                btnValidarEntrada.text = "⚡ Validar con Observaciones"
                btnValidarEntrada.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_orange_light)
            }
            else -> {
                btnValidarEntrada.isEnabled = true
                btnValidarEntrada.text = "✅ Validar Entrada"
                btnValidarEntrada.backgroundTintList = ContextCompat.getColorStateList(this, R.color.lavanda_suave)
            }
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            if (dateString.isEmpty()) return "No disponible"
            val date = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).parse(dateString)
            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(date!!)
        } catch (e: Exception) {
            if (dateString.isEmpty()) "No disponible" else dateString
        }
    }

    private fun showLoading(show: Boolean) {
        btnBuscarEntrada.isEnabled = !show
        btnBuscarEntrada.text = if (show) "Buscando..." else "Buscar Entrada"
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        // Ocultar el container de detalles si hay error
        containerDetalles.visibility = View.GONE
        cardValidaciones.visibility = View.GONE
    }

    private fun procesarValidacionEntrada(numeroEntrada: String) {
        // Mostrar diálogo de confirmación
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Confirmar Validación")
        builder.setMessage("¿Estás seguro de que deseas validar esta entrada?\n\nEsto actualizará:\n• Estado de la orden de compra a 'Completada'\n• Stock del perfume con la cantidad de la entrada\n• Estado de validación de la entrada")

        builder.setPositiveButton("Validar") { _, _ ->
            ejecutarValidacionEntrada(numeroEntrada)
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun ejecutarValidacionEntrada(numeroEntrada: String) {
        showLoading(true)
        btnValidarEntrada.isEnabled = false
        btnValidarEntrada.text = "Procesando..."

        val token = getAuthToken()
        if (token.isNullOrEmpty()) {
            showError("Token de autenticación no encontrado")
            return
        }

        val url = "$BASE_URL/auditor/validar-entrada/$numeroEntrada"
        Log.d("EntradasAuditor", "✅ Procesando validación en: $url")

        val request = object : JsonObjectRequest(
            Request.Method.POST,
            url,
            null,
            { response ->
                Log.d("EntradasAuditor", "✅ Validación procesada exitosamente: $response")
                showLoading(false)
                btnValidarEntrada.isEnabled = false
                btnValidarEntrada.text = "✅ Validada"
                btnValidarEntrada.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_green_dark)

                handleValidacionResponse(response)
            },
            { error ->
                Log.e("EntradasAuditor", "❌ Error en validación", error)
                Log.e("EntradasAuditor", "❌ Status code: ${error.networkResponse?.statusCode}")

                val responseData = error.networkResponse?.data?.toString(Charsets.UTF_8)
                Log.e("EntradasAuditor", "❌ Response body: $responseData")

                showLoading(false)
                btnValidarEntrada.isEnabled = true
                btnValidarEntrada.text = "Validar Entrada"

                val errorMessage = when (error.networkResponse?.statusCode) {
                    400 -> "La validación no pudo completarse. Verifica los datos"
                    401 -> "No autorizado. Inicia sesión nuevamente"
                    403 -> "No tienes permisos para validar entradas"
                    404 -> "Entrada u orden de compra no encontrada"
                    500 -> "Error interno del servidor"
                    else -> "Error de conexión. Verifica tu internet"
                }

                showError(errorMessage)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        requestQueue.add(request)
    }

    private fun handleValidacionResponse(response: JSONObject) {
        try {
            val success = response.getBoolean("success")
            val message = response.getString("message")
            val data = response.getJSONObject("data")

            if (success) {
                // Mostrar información de la validación
                val entrada = data.getJSONObject("entrada")
                val ordenCompra = data.getJSONObject("orden_compra")
                val perfume = data.getJSONObject("perfume")
                val auditor = data.getJSONObject("auditor")

                val detalleValidacion = """
                    ✅ VALIDACIÓN COMPLETADA EXITOSAMENTE
                    
                    📋 ENTRADA:
                    • Número: ${entrada.getString("numero_entrada")}
                    • Estado: ${entrada.getString("estatus_nuevo")}
                    • Cantidad procesada: ${entrada.getInt("cantidad")}
                    
                    🛒 ORDEN DE COMPRA:
                    • Número: ${ordenCompra.getString("numero_orden")}
                    • Estado anterior: ${ordenCompra.getString("estado_anterior")}
                    • Estado nuevo: ${ordenCompra.getString("estado_nuevo")}
                    
                    💎 PERFUME:
                    • ${perfume.getString("nombre")}
                    • Stock anterior: ${perfume.getInt("stock_anterior")}
                    • Stock nuevo: ${perfume.getInt("stock_nuevo")}
                    • Cantidad agregada: +${perfume.getInt("cantidad_agregada")}
                    
                    👤 AUDITOR RESPONSABLE:
                    • Nombre: ${auditor.optString("nombre", "No disponible")}${if (auditor.optString("apellido", "").isNotEmpty()) " ${auditor.getString("apellido")}" else ""}
                    • Email: ${auditor.optString("email", "No disponible")}
                    • Rol: ${auditor.optString("rol", "Auditor")}
                    • ID Usuario: ${auditor.optString("id", "No disponible")}
                    • Fecha: ${auditor.optString("fecha_validacion_formateada", formatDate(auditor.optString("fecha_validacion", "")))}
                """.trimIndent()

                // Mostrar diálogo con detalles
                val builder = androidx.appcompat.app.AlertDialog.Builder(this)
                builder.setTitle("Validación Completada")
                builder.setMessage(detalleValidacion)
                builder.setPositiveButton("Entendido") { dialog, _ ->
                    dialog.dismiss()
                    // Actualizar la vista con los nuevos datos
                    buscarEntrada(entrada.getString("numero_entrada"))
                }
                builder.show()

                Toast.makeText(this, message, Toast.LENGTH_LONG).show()

            } else {
                showError("Error en la validación: $message")
            }

        } catch (e: Exception) {
            Log.e("EntradasAuditor", "❌ Error procesando respuesta de validación", e)
            showError("Error procesando la respuesta de validación: ${e.message}")
        }
    }

    private fun getAuthToken(): String? {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }
}
