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

class OrdenesCompra : AppCompatActivity() {

    // Configuración de la API
    private val BASE_URL = "https://smartflow-mwmm.onrender.com/api"
    private val ORDEN_ENDPOINT = "$BASE_URL/auditor/orden-compra"

    // UI Components principales
    private lateinit var etIdOrden: EditText
    private lateinit var btnBuscarOrden: Button

    // Cards principales
    private lateinit var cardOrden: LinearLayout
    private lateinit var cardPerfume: LinearLayout
    private lateinit var cardProveedor: LinearLayout

    // Orden de Compra Fields
    private lateinit var tvIdOrden: TextView
    private lateinit var tvFechaOrden: TextView
    private lateinit var tvEstatusOrden: TextView
    private lateinit var tvCantidad: TextView
    private lateinit var tvPrecioUnitario: TextView
    private lateinit var tvPrecioTotal: TextView

    // Perfume Fields
    private lateinit var tvNombrePerfume: TextView
    private lateinit var tvDescripcionPerfume: TextView
    private lateinit var tvCategoriaPerfume: TextView
    private lateinit var tvPrecioVentaPerfume: TextView
    private lateinit var tvStockPerfume: TextView
    private lateinit var tvUbicacionPerfume: TextView
    private lateinit var tvFechaExpiracionPerfume: TextView

    // Proveedor Fields
    private lateinit var tvNombreProveedor: TextView
    private lateinit var tvRfcProveedor: TextView
    private lateinit var tvContactoProveedor: TextView
    private lateinit var tvTelefonoProveedor: TextView
    private lateinit var tvEmailProveedor: TextView
    private lateinit var tvDireccionProveedor: TextView

    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ordenes_compras)

        initializeViews()
        setupClickListeners()

        requestQueue = Volley.newRequestQueue(this)

        Log.d("OrdenesCompra", "Actividad de Órdenes de Compra iniciada")
    }

    private fun initializeViews() {
        // Búsqueda
        etIdOrden = findViewById(R.id.et_id_orden)
        btnBuscarOrden = findViewById(R.id.btn_buscar_orden)

        // Cards principales
        cardOrden = findViewById(R.id.card_orden)
        cardPerfume = findViewById(R.id.card_perfume)
        cardProveedor = findViewById(R.id.card_proveedor)

        // Orden de Compra Fields
        tvIdOrden = findViewById(R.id.tv_id_orden)
        tvFechaOrden = findViewById(R.id.tv_fecha_orden)
        tvEstatusOrden = findViewById(R.id.tv_estatus_orden)
        tvCantidad = findViewById(R.id.tv_cantidad)
        tvPrecioUnitario = findViewById(R.id.tv_precio_unitario)
        tvPrecioTotal = findViewById(R.id.tv_precio_total)

        // Perfume Fields
        tvNombrePerfume = findViewById(R.id.tv_nombre_perfume)
        tvDescripcionPerfume = findViewById(R.id.tv_descripcion_perfume)
        tvCategoriaPerfume = findViewById(R.id.tv_categoria_perfume)
        tvPrecioVentaPerfume = findViewById(R.id.tv_precio_venta_perfume)
        tvStockPerfume = findViewById(R.id.tv_stock_perfume)
        tvUbicacionPerfume = findViewById(R.id.tv_ubicacion_perfume)
        tvFechaExpiracionPerfume = findViewById(R.id.tv_fecha_expiracion_perfume)

        // Proveedor Fields
        tvNombreProveedor = findViewById(R.id.tv_nombre_proveedor)
        tvRfcProveedor = findViewById(R.id.tv_rfc_proveedor)
        tvContactoProveedor = findViewById(R.id.tv_contacto_proveedor)
        tvTelefonoProveedor = findViewById(R.id.tv_telefono_proveedor)
        tvEmailProveedor = findViewById(R.id.tv_email_proveedor)
        tvDireccionProveedor = findViewById(R.id.tv_direccion_proveedor)
    }

    private fun setupClickListeners() {
        btnBuscarOrden.setOnClickListener {
            val numeroOrden = etIdOrden.text.toString().trim()
            if (numeroOrden.isNotEmpty()) {
                Log.d("OrdenesCompra", "🔍 Búsqueda iniciada con número: '$numeroOrden'")
                buscarOrden(numeroOrden)
            } else {
                Toast.makeText(this, "Por favor ingresa un número de orden", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buscarOrden(numeroOrden: String) {
        showLoading(true)

        val token = getAuthToken()
        if (token.isNullOrEmpty()) {
            showError("Token de autenticación no encontrado")
            return
        }

        val url = "$ORDEN_ENDPOINT/$numeroOrden"
        Log.d("OrdenesCompra", "🔍 Haciendo petición a: $url")

        val request = object : JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                Log.d("OrdenesCompra", "✅ Respuesta recibida: $response")
                showLoading(false)
                handleApiResponse(response)
            },
            { error ->
                Log.e("OrdenesCompra", "❌ Error en petición", error)
                Log.e("OrdenesCompra", "❌ Status code: ${error.networkResponse?.statusCode}")

                val responseData = error.networkResponse?.data?.toString(Charsets.UTF_8)
                Log.e("OrdenesCompra", "❌ Response body: $responseData")

                showLoading(false)

                val errorMessage = when (error.networkResponse?.statusCode) {
                    400 -> "Número de orden inválido"
                    401 -> "No autorizado. Inicia sesión nuevamente"
                    403 -> "No tienes permisos para acceder a esta información"
                    404 -> "Orden de compra no encontrada"
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
            val ordenCompra = data.getJSONObject("orden_compra")
            val perfume = data.optJSONObject("perfume")
            val proveedor = data.optJSONObject("proveedor")
            val usuarioSolicitante = data.optJSONObject("usuario_solicitante")

            displayOrdenData(ordenCompra, perfume, proveedor, usuarioSolicitante)

            // Mostrar los cards
            cardOrden.visibility = View.VISIBLE

            if (perfume != null) {
                cardPerfume.visibility = View.VISIBLE
            }

            if (proveedor != null) {
                cardProveedor.visibility = View.VISIBLE
            }

        } catch (e: Exception) {
            Log.e("OrdenesCompra", "❌ Error procesando respuesta", e)
            showError("Error procesando los datos recibidos: ${e.message}")
        }
    }

    private fun displayOrdenData(
        ordenCompra: JSONObject,
        perfume: JSONObject?,
        proveedor: JSONObject?,
        usuarioSolicitante: JSONObject?
    ) {
        // Mostrar datos de la orden de compra
        tvIdOrden.text = "ID: ${ordenCompra.optString("n_orden_compra", "No disponible")}"
        tvFechaOrden.text = "Fecha: ${formatDate(ordenCompra.optString("fecha", ""))}"
        tvEstatusOrden.text = "Estatus: ${ordenCompra.optString("estado", "No disponible").uppercase()}"
        tvCantidad.text = "Cantidad: ${ordenCompra.optInt("cantidad", 0)}"
        tvPrecioUnitario.text = "Precio unitario: $${ordenCompra.optDouble("precio_unitario", 0.0)}"
        tvPrecioTotal.text = "Precio total: $${ordenCompra.optDouble("precio_total", 0.0)}"

        // Configurar color del estatus
        val estado = ordenCompra.optString("estado", "").lowercase()
        tvEstatusOrden.setTextColor(ContextCompat.getColor(this, when(estado) {
            "pendiente" -> android.R.color.holo_orange_dark
            "completada", "completado" -> android.R.color.holo_green_dark
            "cancelada", "cancelado" -> android.R.color.holo_red_dark
            else -> android.R.color.darker_gray
        }))

        // Mostrar datos del perfume
        if (perfume != null) {
            tvNombrePerfume.text = "Nombre: ${perfume.optString("name_per", "No disponible")}"
            tvDescripcionPerfume.text = "Descripción: ${perfume.optString("descripcion_per", "No disponible")}"
            tvCategoriaPerfume.text = "Categoría: ${perfume.optString("categoria_per", "No disponible")}"
            tvPrecioVentaPerfume.text = "Precio venta: $${perfume.optDouble("precio_venta_per", 0.0)}"
            tvStockPerfume.text = "Stock: ${perfume.optInt("stock_per", 0)}"
            tvUbicacionPerfume.text = "Ubicación: ${perfume.optString("ubicacion_per", "No disponible")}"
            tvFechaExpiracionPerfume.text = "Expira: ${formatDate(perfume.optString("fecha_expiracion", ""))}"
        } else {
            tvNombrePerfume.text = "Nombre: No disponible"
            tvDescripcionPerfume.text = "Descripción: No disponible"
            tvCategoriaPerfume.text = "Categoría: No disponible"
            tvPrecioVentaPerfume.text = "Precio venta: No disponible"
            tvStockPerfume.text = "Stock: No disponible"
            tvUbicacionPerfume.text = "Ubicación: No disponible"
            tvFechaExpiracionPerfume.text = "Expira: No disponible"
        }

        // Mostrar datos del proveedor
        if (proveedor != null) {
            tvNombreProveedor.text = "Nombre: ${proveedor.optString("nombre_proveedor", "No disponible")}"
            tvRfcProveedor.text = "RFC: ${proveedor.optString("rfc", "No disponible")}"
            tvContactoProveedor.text = "Contacto: ${proveedor.optString("contacto", "No disponible")}"
            tvTelefonoProveedor.text = "Teléfono: ${proveedor.optString("telefono", "No disponible")}"
            tvEmailProveedor.text = "Email: ${proveedor.optString("email", "No disponible")}"
            tvDireccionProveedor.text = "Dirección: ${proveedor.optString("direccion", "No disponible")}"
        } else {
            tvNombreProveedor.text = "Nombre: No disponible"
            tvRfcProveedor.text = "RFC: No disponible"
            tvContactoProveedor.text = "Contacto: No disponible"
            tvTelefonoProveedor.text = "Teléfono: No disponible"
            tvEmailProveedor.text = "Email: No disponible"
            tvDireccionProveedor.text = "Dirección: No disponible"
        }

        Log.d("OrdenesCompra", "✅ Datos mostrados correctamente")
        Log.d("OrdenesCompra", "📋 Orden: ${ordenCompra.optString("n_orden_compra")}")
        Log.d("OrdenesCompra", "🌸 Perfume: ${perfume?.optString("name_per") ?: "No disponible"}")
        Log.d("OrdenesCompra", "🏢 Proveedor: ${proveedor?.optString("nombre_proveedor") ?: "No disponible"}")
    }

    private fun formatDate(dateString: String): String {
        return try {
            if (dateString.isEmpty()) return "No disponible"
            val date = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).parse(dateString)
            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(date!!)
        } catch (e: Exception) {
            // Si falla el formato con milisegundos, intentar sin ellos
            try {
                val date = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).parse(dateString)
                java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(date!!)
            } catch (e2: Exception) {
                if (dateString.isEmpty()) "No disponible" else dateString
            }
        }
    }

    private fun showLoading(show: Boolean) {
        btnBuscarOrden.isEnabled = !show
        btnBuscarOrden.text = if (show) "Buscando..." else "Buscar"
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        // Ocultar los cards si hay error
        cardOrden.visibility = View.GONE
        cardPerfume.visibility = View.GONE
        cardProveedor.visibility = View.GONE
    }

    private fun getAuthToken(): String? {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }
}
