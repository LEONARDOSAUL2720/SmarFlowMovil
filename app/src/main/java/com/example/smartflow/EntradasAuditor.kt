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

    // Entrada Fields (AGREGADOS - estaban faltando)
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

    // Validación Fields
    private lateinit var tvEstadoValidacion: TextView
    private lateinit var tvProveedorValidacion: TextView
    private lateinit var tvCantidadValidacion: TextView
    private lateinit var tvFechaValidacion: TextView
    private lateinit var layoutDiscrepancias: LinearLayout
    private lateinit var containerDiscrepancias: LinearLayout

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

        // Entrada Fields (CORREGIDOS)
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

        // Validaciones
        tvEstadoValidacion = findViewById(R.id.tv_estado_validacion)
        tvProveedorValidacion = findViewById(R.id.tv_proveedor_validacion)
        tvCantidadValidacion = findViewById(R.id.tv_cantidad_validacion)
        tvFechaValidacion = findViewById(R.id.tv_fecha_validacion)
        layoutDiscrepancias = findViewById(R.id.layout_discrepancias)
        containerDiscrepancias = findViewById(R.id.container_discrepancias)

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
            Toast.makeText(this, "Funcionalidad de validación pendiente", Toast.LENGTH_SHORT).show()
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
            val proveedor = data.optJSONObject("proveedor_detalle") // CORREGIDO: era "proveedor"
            val ordenCompra = data.optJSONObject("orden_compra_relacionada")
            val validacion = data.getJSONObject("validacion")

            displayEntradaData(entrada, perfume, proveedor, ordenCompra)
            displayValidaciones(validacion)

            // Mostrar el container principal de detalles
            containerDetalles.visibility = View.VISIBLE
            cardValidaciones.visibility = View.VISIBLE

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
        // CORREGIDO: Mostrar datos de la ENTRADA (estaban faltando)
        tvNumeroEntrada.text = "N° Entrada: ${entrada.optString("numero_entrada", "No disponible")}"
        tvCantidadEntrada.text = "Cantidad: ${entrada.optInt("cantidad", 0)}"

        // Proveedor de la entrada (viene embebido en entrada.proveedor)
        val proveedorEntrada = entrada.optJSONObject("proveedor")
        tvProveedorEntrada.text = "Proveedor: ${proveedorEntrada?.optString("nombre_proveedor", "No disponible") ?: "No disponible"}"
        tvFechaEntrada.text = "Fecha entrada: ${formatDate(entrada.optString("fecha_entrada", ""))}"
        tvEstatusEntrada.text = "Estatus: ${entrada.optString("estatus_validacion", "No disponible").uppercase()}"

        // CORREGIDO: Mostrar datos de la orden de compra relacionada
        if (ordenCompra != null) {
            tvNumeroOrden.text = "N° Orden: ${ordenCompra.optString("numero_orden", "No disponible")}" // CORREGIDO: era "n.orden_compra"
            tvCantidad.text = "Cantidad: ${ordenCompra.optInt("cantidad", 0)}"
            tvPrecioUnitario.text = "Precio unitario: $${ordenCompra.optDouble("precio_unitario", 0.0)}"
            tvPrecioTotal.text = "Precio total: $${ordenCompra.optDouble("precio_total", 0.0)}"
            tvFechaOrden.text = "Fecha: ${formatDate(ordenCompra.optString("fecha_orden", ""))}" // CORREGIDO: era "fecha_orden"
            tvEstatus.text = "Estatus: ${ordenCompra.optString("estado", "No disponible").uppercase()}" // CORREGIDO: era "estatus"
        } else {
            tvNumeroOrden.text = "N° Orden: No encontrada"
            tvCantidad.text = "Cantidad: No disponible"
            tvPrecioUnitario.text = "Precio unitario: No disponible"
            tvPrecioTotal.text = "Precio total: No disponible"
            tvFechaOrden.text = "Fecha: No disponible"
            tvEstatus.text = "Estatus: No disponible"
        }

        // Mostrar datos del perfume (los nombres están correctos)
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

        // Mostrar datos del proveedor (los nombres están correctos)
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

    private fun displayValidaciones(validacion: JSONObject) {
        try {
            val estadoGeneral = validacion.getString("estado_general")
            val proveedorCoincide = validacion.getBoolean("proveedor_coincide")
            val cantidadValida = validacion.getBoolean("cantidad_valida")
            val fechaCoherente = validacion.getBoolean("fecha_coherente")
            val discrepancias = validacion.getJSONArray("discrepancias")
            val advertencias = validacion.getJSONArray("advertencias")

            // Estado general con colores
            tvEstadoValidacion.text = "Estado: $estadoGeneral"
            tvEstadoValidacion.setTextColor(ContextCompat.getColor(this, when(estadoGeneral) {
                "VALIDA" -> android.R.color.holo_green_dark
                "CON_OBSERVACIONES" -> android.R.color.holo_orange_dark
                "REQUIERE_REVISION" -> android.R.color.holo_red_dark
                else -> android.R.color.darker_gray
            }))

            // Validaciones individuales con íconos
            tvProveedorValidacion.text = "Proveedor: ${if (proveedorCoincide) "✅ Coincide" else "❌ No coincide"}"
            tvProveedorValidacion.setTextColor(ContextCompat.getColor(this,
                if (proveedorCoincide) android.R.color.holo_green_dark else android.R.color.holo_red_dark))

            tvCantidadValidacion.text = "Cantidad: ${if (cantidadValida) "✅ Válida" else "❌ Inválida"}"
            tvCantidadValidacion.setTextColor(ContextCompat.getColor(this,
                if (cantidadValida) android.R.color.holo_green_dark else android.R.color.holo_red_dark))

            tvFechaValidacion.text = "Fechas: ${if (fechaCoherente) "✅ Coherentes" else "❌ Incoherentes"}"
            tvFechaValidacion.setTextColor(ContextCompat.getColor(this,
                if (fechaCoherente) android.R.color.holo_green_dark else android.R.color.holo_red_dark))

            // Mostrar discrepancias y advertencias
            if (discrepancias.length() > 0 || advertencias.length() > 0) {
                layoutDiscrepancias.visibility = View.VISIBLE
                containerDiscrepancias.removeAllViews()

                // Agregar discrepancias
                for (i in 0 until discrepancias.length()) {
                    val discrepancia = discrepancias.getJSONObject(i)
                    val mensaje = discrepancia.getString("mensaje")
                    val gravedad = discrepancia.getString("gravedad")

                    val textView = TextView(this)
                    textView.text = "🚨 $mensaje"
                    textView.setTextColor(ContextCompat.getColor(this, when(gravedad) {
                        "ALTA" -> android.R.color.holo_red_dark
                        "MEDIA" -> android.R.color.holo_orange_dark
                        else -> android.R.color.darker_gray
                    }))
                    textView.textSize = 14f
                    textView.setPadding(8, 4, 8, 4)

                    containerDiscrepancias.addView(textView)
                }

                // Agregar advertencias
                for (i in 0 until advertencias.length()) {
                    val advertencia = advertencias.getJSONObject(i)
                    val mensaje = advertencia.getString("mensaje")

                    val textView = TextView(this)
                    textView.text = "⚠️ $mensaje"
                    textView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
                    textView.textSize = 14f
                    textView.setPadding(8, 4, 8, 4)

                    containerDiscrepancias.addView(textView)
                }
            } else {
                layoutDiscrepancias.visibility = View.GONE
            }

            Log.d("EntradasAuditor", "✅ Validaciones mostradas: $estadoGeneral")

        } catch (e: Exception) {
            Log.e("EntradasAuditor", "❌ Error mostrando validaciones", e)
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

    private fun getAuthToken(): String? {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }
}
