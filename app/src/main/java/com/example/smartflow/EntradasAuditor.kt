package com.example.smartflow

import com.example.smartflow.EntradasAdapter
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class EntradasAuditor : AppCompatActivity() {

    // Configuración de la API
    private val BASE_URL = "https://smartflow-mwmm.onrender.com/api"
    private val ENTRADA_BUSQUEDA_INTELIGENTE = "$BASE_URL/auditor/entrada-busqueda"
    private val ENTRADAS_LISTA_ENDPOINT = "$BASE_URL/auditor/entradas"
    private val ENTRADA_ENDPOINT = "$BASE_URL/auditor/entrada"
    private val ENTRADA_TRASPASO_ENDPOINT = "$BASE_URL/auditor/entrada-traspaso"
    private val RECHAZAR_ENTRADA_ENDPOINT = "$BASE_URL/auditor/rechazar-entrada"

    // UI Containers principales
    private lateinit var containerBusqueda: LinearLayout
    private lateinit var containerListaEntradas: LinearLayout
    private lateinit var containerDetalles: LinearLayout

    // UI Components para búsqueda específica
    private lateinit var etNumeroEntrada: EditText
    private lateinit var btnBuscarEntrada: Button

    // UI Components para lista de entradas
    private lateinit var spinnerFiltroTipo: Spinner
    private lateinit var spinnerFiltroEstatusLista: Spinner
    private lateinit var btnCargarEntradas: Button
    private lateinit var btnLimpiarFiltrosLista: Button
    private lateinit var tvInfoEntradas: TextView
    private lateinit var rvEntradas: RecyclerView
    private lateinit var progressBarLista: ProgressBar

    // Paginación
    private lateinit var layoutPaginacion: LinearLayout
    private lateinit var btnPaginaAnterior: Button
    private lateinit var btnPaginaSiguiente: Button
    private lateinit var tvPaginaActual: TextView

    // NUEVO: Botón volver a lista
    private lateinit var btnVolverLista: Button

    // Card de validaciones
    private lateinit var cardValidaciones: LinearLayout

    // Entrada Fields (búsqueda específica)
    private lateinit var tvNumeroEntrada: TextView
    private lateinit var tvCantidadEntrada: TextView
    private lateinit var tvProveedorEntrada: TextView
    private lateinit var tvFechaEntrada: TextView
    private lateinit var tvEstatusEntrada: TextView
    private lateinit var tvTipoEntrada: TextView
    private lateinit var tvReferenciaTraspaso: TextView

    // Botones de acción
    private lateinit var btnValidarEntrada: Button
    private lateinit var btnRechazarEntrada: Button

    // Campos de validación (resumen ejecutivo)
    private lateinit var tvEstadoValidacion: TextView
    private lateinit var tvMensajePrincipal: TextView
    private lateinit var tvAccionRecomendada: TextView
    private lateinit var tvSiguientePaso: TextView
    private lateinit var tvTiempoResolucion: TextView
    private lateinit var tvPorcentajeCumplimiento: TextView
    private lateinit var tvNivelRiesgo: TextView

    // Campos de validaciones individuales
    private lateinit var tvProveedorValidacion: TextView
    private lateinit var tvCantidadValidacion: TextView
    private lateinit var tvFechaValidacion: TextView
    private lateinit var tvPrecioValidacion: TextView
    private lateinit var tvEstadoOrdenValidacion: TextView

    // Containers para discrepancias
    private lateinit var layoutDiscrepancias: LinearLayout
    private lateinit var containerDiscrepanciasCriticas: LinearLayout
    private lateinit var containerDiscrepanciasImportantes: LinearLayout
    private lateinit var containerAdvertencias: LinearLayout
    private lateinit var containerRecomendaciones: LinearLayout

    // Campos de perfume
    private lateinit var tvNombrePerfume: TextView
    private lateinit var tvDescripcionPerfume: TextView
    private lateinit var tvCategoriaPerfume: TextView
    private lateinit var tvPrecioVentaPerfume: TextView
    private lateinit var tvStockPerfume: TextView
    private lateinit var tvStockMinimoPerfume: TextView
    private lateinit var tvUbicacionPerfume: TextView
    private lateinit var tvFechaExpiracionPerfume: TextView
    private lateinit var tvEstadoPerfume: TextView

    // Campos de proveedor
    private lateinit var tvNombreProveedor: TextView
    private lateinit var tvRfcProveedor: TextView
    private lateinit var tvContactoProveedor: TextView
    private lateinit var tvTelefonoProveedor: TextView
    private lateinit var tvEmailProveedor: TextView
    private lateinit var tvDireccionProveedor: TextView
    private lateinit var tvEstadoProveedor: TextView

    // Campos de orden/traspaso
    private lateinit var tvTituloSeccionDos: TextView
    private lateinit var tvNumeroOrden: TextView
    private lateinit var tvCantidad: TextView
    private lateinit var tvPrecioUnitario: TextView
    private lateinit var tvPrecioTotal: TextView
    private lateinit var tvFechaOrden: TextView
    private lateinit var tvEstatus: TextView
    private lateinit var tvFechaSalida: TextView
    private lateinit var tvAlmacenSalida: TextView
    private lateinit var tvAlmacenEntrada: TextView

    // Adapter y datos para lista
    private lateinit var entradasAdapter: EntradasAdapter
    private lateinit var requestQueue: RequestQueue

    // Listas para los spinners de filtros
    private var listaTipos = mutableListOf<String>()
    private var listaEstatus = mutableListOf<String>()

    // Variables para filtros actuales de lista
    private var filtroActualTipo = ""
    private var filtroActualEstatusLista = ""

    // Variables de paginación
    private var paginaActual = 1
    private var totalPaginas = 1
    private var entradasPorPagina = 10

    // Variable para controlar el tipo de entrada actual
    private var tipoEntradaActual: String = "Compra"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.entradas_auditor)

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        setupSpinners()

        requestQueue = Volley.newRequestQueue(this)

        // Cargar lista inicial de entradas
        cargarListaEntradas()

        Log.d("EntradasAuditorOptimized", "Actividad optimizada iniciada")
    }

    private fun initializeViews() {
        // Containers principales
        containerBusqueda = findViewById(R.id.container_busqueda)
        containerListaEntradas = findViewById(R.id.container_lista_entradas)
        containerDetalles = findViewById(R.id.container_detalles)

        // Búsqueda específica
        etNumeroEntrada = findViewById(R.id.et_numero_entrada)
        btnBuscarEntrada = findViewById(R.id.btn_buscar_entrada)

        // NUEVO: Botón volver a lista
        btnVolverLista = findViewById(R.id.btn_volver_lista)

        // Card de validaciones
        cardValidaciones = findViewById(R.id.card_validaciones)

        // UI Components para lista de entradas
        spinnerFiltroTipo = findViewById(R.id.spinner_filtro_tipo)
        spinnerFiltroEstatusLista = findViewById(R.id.spinner_filtro_estatus_lista)
        btnCargarEntradas = findViewById(R.id.btn_cargar_entradas)
        btnLimpiarFiltrosLista = findViewById(R.id.btn_limpiar_filtros_lista)
        tvInfoEntradas = findViewById(R.id.tv_info_entradas)
        rvEntradas = findViewById(R.id.rv_entradas)
        progressBarLista = findViewById(R.id.progress_bar_lista)

        // Paginación
        layoutPaginacion = findViewById(R.id.layout_paginacion)
        btnPaginaAnterior = findViewById(R.id.btn_pagina_anterior)
        btnPaginaSiguiente = findViewById(R.id.btn_pagina_siguiente)
        tvPaginaActual = findViewById(R.id.tv_pagina_actual)

        // Entrada Fields (búsqueda específica)
        tvNumeroEntrada = findViewById(R.id.tv_numero_entrada)
        tvTipoEntrada = findViewById(R.id.tv_tipo_entrada)
        tvCantidadEntrada = findViewById(R.id.tv_cantidad_entrada)
        tvProveedorEntrada = findViewById(R.id.tv_proveedor_entrada)
        tvFechaEntrada = findViewById(R.id.tv_fecha_entrada)
        tvEstatusEntrada = findViewById(R.id.tv_estatus_entrada)
        tvReferenciaTraspaso = findViewById(R.id.tv_referencia_traspaso)

        // Botones de acción
        btnValidarEntrada = findViewById(R.id.btn_validar_entrada)
        btnRechazarEntrada = findViewById(R.id.btn_rechazar_entrada)

        // Campos de resumen ejecutivo
        tvEstadoValidacion = findViewById(R.id.tv_estado_validacion)
        tvMensajePrincipal = findViewById(R.id.tv_mensaje_principal)
        tvAccionRecomendada = findViewById(R.id.tv_accion_recomendada)
        tvSiguientePaso = findViewById(R.id.tv_siguiente_paso)
        tvTiempoResolucion = findViewById(R.id.tv_tiempo_resolucion)
        tvPorcentajeCumplimiento = findViewById(R.id.tv_porcentaje_cumplimiento)
        tvNivelRiesgo = findViewById(R.id.tv_nivel_riesgo)

        // Campos de validaciones individuales
        tvProveedorValidacion = findViewById(R.id.tv_proveedor_validacion)
        tvCantidadValidacion = findViewById(R.id.tv_cantidad_validacion)
        tvFechaValidacion = findViewById(R.id.tv_fecha_validacion)
        tvPrecioValidacion = findViewById(R.id.tv_precio_validacion)
        tvEstadoOrdenValidacion = findViewById(R.id.tv_estado_orden_validacion)

        // Containers para discrepancias
        layoutDiscrepancias = findViewById(R.id.layout_discrepancias)
        containerDiscrepanciasCriticas = findViewById(R.id.container_discrepancias_criticas)
        containerDiscrepanciasImportantes = findViewById(R.id.container_discrepancias_importantes)
        containerAdvertencias = findViewById(R.id.container_advertencias)
        containerRecomendaciones = findViewById(R.id.container_recomendaciones)

        // Campos de perfume
        tvNombrePerfume = findViewById(R.id.tv_nombre_perfume)
        tvDescripcionPerfume = findViewById(R.id.tv_descripcion_perfume)
        tvCategoriaPerfume = findViewById(R.id.tv_categoria_perfume)
        tvPrecioVentaPerfume = findViewById(R.id.tv_precio_venta_perfume)
        tvStockPerfume = findViewById(R.id.tv_stock_perfume)
        tvStockMinimoPerfume = findViewById(R.id.tv_stock_minimo_perfume)
        tvUbicacionPerfume = findViewById(R.id.tv_ubicacion_perfume)
        tvFechaExpiracionPerfume = findViewById(R.id.tv_fecha_expiracion_perfume)
        tvEstadoPerfume = findViewById(R.id.tv_estado_perfume)

        // Campos de proveedor
        tvNombreProveedor = findViewById(R.id.tv_nombre_proveedor)
        tvRfcProveedor = findViewById(R.id.tv_rfc_proveedor)
        tvContactoProveedor = findViewById(R.id.tv_contacto_proveedor)
        tvTelefonoProveedor = findViewById(R.id.tv_telefono_proveedor)
        tvEmailProveedor = findViewById(R.id.tv_email_proveedor)
        tvDireccionProveedor = findViewById(R.id.tv_direccion_proveedor)
        tvEstadoProveedor = findViewById(R.id.tv_estado_proveedor)

        // Campos de orden/traspaso
        tvTituloSeccionDos = findViewById(R.id.tv_titulo_seccion_dos)
        tvNumeroOrden = findViewById(R.id.tv_numero_orden)
        tvCantidad = findViewById(R.id.tv_cantidad)
        tvPrecioUnitario = findViewById(R.id.tv_precio_unitario)
        tvPrecioTotal = findViewById(R.id.tv_precio_total)
        tvFechaOrden = findViewById(R.id.tv_fecha_orden)
        tvEstatus = findViewById(R.id.tv_estatus)
        tvFechaSalida = findViewById(R.id.tv_fecha_salida)
        tvAlmacenSalida = findViewById(R.id.tv_almacen_salida)
        tvAlmacenEntrada = findViewById(R.id.tv_almacen_entrada)
    }

    private fun setupRecyclerView() {
        entradasAdapter = EntradasAdapter { numeroEntrada ->
            // Cuando se selecciona una entrada desde la lista
            Log.d("EntradasAuditorOptimized", "🎯 Entrada seleccionada desde lista: $numeroEntrada")
            etNumeroEntrada.setText(numeroEntrada)
            buscarEntradaInteligente(numeroEntrada)
        }

        rvEntradas.apply {
            layoutManager = LinearLayoutManager(this@EntradasAuditor)
            adapter = entradasAdapter
            isNestedScrollingEnabled = true
            setHasFixedSize(false)
            itemAnimator = null
            isVerticalScrollBarEnabled = true
            scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        }

        entradasAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                rvEntradas.post {
                    rvEntradas.requestLayout()
                }
            }
        })
    }

    private fun setupClickListeners() {
        // Búsqueda específica
        btnBuscarEntrada.setOnClickListener {
            val numeroEntrada = etNumeroEntrada.text.toString().trim()
            if (numeroEntrada.isNotEmpty()) {
                Log.d("EntradasAuditorOptimized", "🔍 Búsqueda específica iniciada: '$numeroEntrada'")
                buscarEntradaInteligente(numeroEntrada)
            } else {
                Toast.makeText(this, "Por favor ingresa un número de entrada", Toast.LENGTH_SHORT).show()
            }
        }

        // NUEVO: Botón volver a lista
        btnVolverLista.setOnClickListener {
            Log.d("EntradasAuditorOptimized", "🔙 Volviendo a la lista de entradas")
            mostrarVistaLista()
        }

        // Lista de entradas
        btnCargarEntradas.setOnClickListener {
            paginaActual = 1
            cargarListaEntradas()
        }

        btnLimpiarFiltrosLista.setOnClickListener {
            limpiarFiltrosLista()
        }

        // Paginación
        btnPaginaAnterior.setOnClickListener {
            if (paginaActual > 1) {
                paginaActual--
                cargarListaEntradas()
            }
        }

        btnPaginaSiguiente.setOnClickListener {
            if (paginaActual < totalPaginas) {
                paginaActual++
                cargarListaEntradas()
            }
        }

        // Botones de validación y rechazo
        btnValidarEntrada.setOnClickListener {
            val numeroEntrada = etNumeroEntrada.text.toString().trim()
            if (numeroEntrada.isNotEmpty()) {
                Log.d("EntradasAuditorOptimized", "✅ Procesando validación para: '$numeroEntrada'")
                procesarValidacionEntrada(numeroEntrada)
            } else {
                Toast.makeText(this, "No hay entrada seleccionada para validar", Toast.LENGTH_SHORT).show()
            }
        }

        btnRechazarEntrada.setOnClickListener {
            val numeroEntrada = etNumeroEntrada.text.toString().trim()
            if (numeroEntrada.isNotEmpty()) {
                Log.d("EntradasAuditorOptimized", "❌ Procesando rechazo para: '$numeroEntrada'")
                mostrarDialogoRechazo(numeroEntrada)
            } else {
                Toast.makeText(this, "No hay entrada seleccionada para rechazar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpinners() {
        // Configurar lista de tipos
        listaTipos.clear()
        listaTipos.addAll(listOf("Todos los tipos", "Compra", "Traspaso"))

        // Configurar lista de estatus
        listaEstatus.clear()
        listaEstatus.addAll(listOf("Todos los estatus", "registrado", "validado", "rechazado"))

        // Poblar spinners
        poblarSpinnersLista()

        // Configurar listeners
        spinnerFiltroTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val seleccion = parent?.getItemAtPosition(position).toString()
                filtroActualTipo = if (seleccion.contains("Todos") || position == 0) "" else seleccion.lowercase()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerFiltroEstatusLista.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val seleccion = parent?.getItemAtPosition(position).toString()
                filtroActualEstatusLista = if (seleccion.contains("Todos") || position == 0) "" else seleccion
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun poblarSpinnersLista() {
        val adapterTipos = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaTipos)
        adapterTipos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFiltroTipo.adapter = adapterTipos

        val adapterEstatus = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaEstatus)
        adapterEstatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFiltroEstatusLista.adapter = adapterEstatus
    }

    private fun limpiarFiltrosLista() {
        spinnerFiltroTipo.setSelection(0)
        spinnerFiltroEstatusLista.setSelection(0)
        filtroActualTipo = ""
        filtroActualEstatusLista = ""
        paginaActual = 1
        cargarListaEntradas()
    }

    // ======================================================================================
    // FUNCIONES PARA ALTERNAR ENTRE VISTAS
    // ======================================================================================

    private fun mostrarVistaLista() {
        Log.d("EntradasAuditorOptimized", "📋 Mostrando vista de lista")

        // Mostrar secciones de lista
        containerBusqueda.visibility = View.VISIBLE
        containerListaEntradas.visibility = View.VISIBLE

        // Ocultar sección de detalles
        containerDetalles.visibility = View.GONE
        cardValidaciones.visibility = View.GONE

        // Limpiar campo de búsqueda
        etNumeroEntrada.setText("")

        // Recargar lista para mostrar datos actualizados
        cargarListaEntradas()
    }

    private fun mostrarVistaDetalles() {
        Log.d("EntradasAuditorOptimized", "📝 Mostrando vista de detalles")

        // Ocultar sección de lista (MANTENER búsqueda visible)
        containerListaEntradas.visibility = View.GONE

        // Mostrar sección de detalles
        containerDetalles.visibility = View.VISIBLE

        // La card de validaciones se mostrará según la respuesta del backend
    }

    // ======================================================================================
    // FUNCIONES DE CARGA DE LISTA
    // ======================================================================================

    private fun cargarListaEntradas() {
        Log.d("EntradasAuditorOptimized", "📋 Cargando lista de entradas...")
        showLoadingLista(true)

        val token = getAuthToken()
        if (token.isNullOrEmpty()) {
            showErrorLista("Token de autenticación no encontrado")
            return
        }

        // Construir URL con parámetros
        var url = ENTRADAS_LISTA_ENDPOINT
        val parametros = mutableListOf<String>()

        parametros.add("page=$paginaActual")
        parametros.add("limit=$entradasPorPagina")

        if (filtroActualTipo.isNotEmpty()) {
            parametros.add("tipo=$filtroActualTipo")
        }
        if (filtroActualEstatusLista.isNotEmpty()) {
            parametros.add("estatus=$filtroActualEstatusLista")
        }

        if (parametros.isNotEmpty()) {
            url += "?" + parametros.joinToString("&")
        }

        Log.d("EntradasAuditorOptimized", "🔗 URL consulta: $url")

        val request = object : JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                Log.d("EntradasAuditorOptimized", "✅ Lista recibida")
                showLoadingLista(false)
                handleListaEntradasResponse(response)
            },
            { error ->
                Log.e("EntradasAuditorOptimized", "❌ Error cargando lista", error)
                showLoadingLista(false)

                val errorMessage = when (error.networkResponse?.statusCode) {
                    401 -> "No autorizado. Inicia sesión nuevamente"
                    403 -> "No tienes permisos para acceder"
                    500 -> "Error interno del servidor"
                    else -> "Error de conexión"
                }

                showErrorLista(errorMessage)
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

    private fun handleListaEntradasResponse(response: JSONObject) {
        try {
            val data = response.getJSONObject("data")
            val entradasArray = data.getJSONArray("entradas")
            val metadatos = data.getJSONObject("metadatos")
            val estadisticas = data.getJSONObject("estadisticas")

            // Convertir JSONArray a List<JSONObject>
            val listaEntradas = mutableListOf<JSONObject>()
            for (i in 0 until entradasArray.length()) {
                listaEntradas.add(entradasArray.getJSONObject(i))
            }

            // Actualizar adapter
            entradasAdapter.updateEntradas(listaEntradas)

            // Actualizar información de paginación
            paginaActual = metadatos.getInt("pagina_actual")
            totalPaginas = metadatos.getInt("total_paginas")

            // Actualizar información de resultados
            val total = estadisticas.getInt("total_entradas")
            val totalCompras = estadisticas.getInt("total_compras")
            val totalTraspasos = estadisticas.getInt("total_traspasos")
            val validadas = estadisticas.getInt("validadas")
            val pendientes = estadisticas.getInt("pendientes")
            val rechazadas = estadisticas.getInt("rechazadas")

            val infoText = buildString {
                append("📊 Total: $total entradas")
                if (totalCompras > 0 || totalTraspasos > 0) {
                    append(" (${totalCompras} compras, ${totalTraspasos} traspasos)")
                }
                append("\n")
                append("✅ Validadas: $validadas | ⏳ Pendientes: $pendientes | ❌ Rechazadas: $rechazadas")

                val filtrosActivos = mutableListOf<String>()
                if (filtroActualTipo.isNotEmpty()) {
                    filtrosActivos.add("tipo: $filtroActualTipo")
                }
                if (filtroActualEstatusLista.isNotEmpty()) {
                    filtrosActivos.add("estatus: $filtroActualEstatusLista")
                }

                if (filtrosActivos.isNotEmpty()) {
                    append("\n🔍 Filtros: ${filtrosActivos.joinToString(", ")}")
                }
            }

            tvInfoEntradas.text = infoText
            tvInfoEntradas.setTextColor(getColor(R.color.verde_salvia))

            // Actualizar controles de paginación
            actualizarPaginacion()

            Log.d("EntradasAuditorOptimized", "✅ Cargadas ${listaEntradas.size} entradas de $total")

        } catch (e: Exception) {
            Log.e("EntradasAuditorOptimized", "❌ Error procesando lista", e)
            showErrorLista("Error procesando datos: ${e.message}")
        }
    }

    private fun actualizarPaginacion() {
        layoutPaginacion.visibility = if (totalPaginas > 1) View.VISIBLE else View.GONE
        tvPaginaActual.text = "Página $paginaActual de $totalPaginas"

        btnPaginaAnterior.isEnabled = paginaActual > 1
        btnPaginaAnterior.backgroundTintList = ContextCompat.getColorStateList(
            this,
            if (paginaActual > 1) R.color.lavanda_suave else R.color.gris_oscuro
        )

        btnPaginaSiguiente.isEnabled = paginaActual < totalPaginas
        btnPaginaSiguiente.backgroundTintList = ContextCompat.getColorStateList(
            this,
            if (paginaActual < totalPaginas) R.color.lavanda_suave else R.color.gris_oscuro
        )
    }

    private fun showLoadingLista(show: Boolean) {
        progressBarLista.visibility = if (show) View.VISIBLE else View.GONE
        rvEntradas.visibility = if (show) View.GONE else View.VISIBLE

        if (show) {
            tvInfoEntradas.text = "🔄 Cargando lista de entradas..."
            tvInfoEntradas.setTextColor(getColor(R.color.lavanda_suave))
        }
    }

    private fun showErrorLista(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        tvInfoEntradas.text = "❌ Error: $message"
        tvInfoEntradas.setTextColor(getColor(R.color.rojo_vino_tenue))
        entradasAdapter.limpiarEntradas()
        layoutPaginacion.visibility = View.GONE
    }

    // ======================================================================================
    // FUNCIONES DE BÚSQUEDA ESPECÍFICA
    // ======================================================================================

    private fun buscarEntradaInteligente(numeroEntrada: String) {
        showLoading(true)

        val token = getAuthToken()
        if (token.isNullOrEmpty()) {
            showError("Token de autenticación no encontrado")
            return
        }

        val url = "$ENTRADA_BUSQUEDA_INTELIGENTE/$numeroEntrada"
        Log.d("EntradasAuditorOptimized", "🔍 Petición a: $url")

        val request = object : JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                Log.d("EntradasAuditorOptimized", "✅ Respuesta recibida")
                showLoading(false)
                handleApiResponseInteligente(response)
            },
            { error ->
                Log.e("EntradasAuditorOptimized", "❌ Error en petición", error)
                showLoading(false)

                val errorMessage = when (error.networkResponse?.statusCode) {
                    400 -> "Número de entrada inválido"
                    401 -> "No autorizado"
                    403 -> "Sin permisos"
                    404 -> "Entrada no encontrada"
                    500 -> "Error del servidor"
                    else -> "Error de conexión"
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

    private fun handleApiResponseInteligente(response: JSONObject) {
        try {
            val data = response.getJSONObject("data")
            val entrada = data.getJSONObject("entrada")

            // Detectar tipo automáticamente
            val tipo = entrada.optString("tipo", "")
            val referenciaTraspaso = entrada.optString("referencia_traspaso", "")

            tipoEntradaActual = when {
                tipo == "Traspaso" || referenciaTraspaso.isNotEmpty() -> "Traspaso"
                else -> "Compra"
            }

            Log.d("EntradasAuditorOptimized", "🎯 Tipo detectado: $tipoEntradaActual")

            // Obtener datos adicionales
            val perfume = data.optJSONObject("perfume")
            val proveedor = data.optJSONObject("proveedor_detalle")
            val validacion = data.optJSONObject("validacion")

            // Mostrar datos según el tipo detectado
            if (tipoEntradaActual == "Traspaso") {
                val traspasoOriginal = data.optJSONObject("traspaso_original")
                displayEntradaDataTraspaso(entrada, perfume, proveedor, traspasoOriginal)
            } else {
                val ordenCompra = data.optJSONObject("orden_compra_relacionada")
                displayEntradaDataCompra(entrada, perfume, proveedor, ordenCompra)
            }

            // Verificar si hay datos de validación
            if (validacion != null) {
                displayValidacionData(validacion)
                cardValidaciones.visibility = View.VISIBLE

                // Configurar botón de validación según el estado actual
                configurarBotonValidacion(entrada, validacion)
            } else {
                cardValidaciones.visibility = View.GONE
                // Si no hay validación, mostrar botones básicos
                btnValidarEntrada.isEnabled = true
                btnValidarEntrada.text = "✅ Validar Entrada"
                btnRechazarEntrada.visibility = View.VISIBLE
                btnRechazarEntrada.isEnabled = true
            }

            // CAMBIAR A VISTA DE DETALLES
            mostrarVistaDetalles()

        } catch (e: Exception) {
            Log.e("EntradasAuditorOptimized", "❌ Error procesando respuesta", e)
            showError("Error procesando datos: ${e.message}")
        }
    }

    private fun displayEntradaDataBasica(entrada: JSONObject) {
        tvNumeroEntrada.text = "N° Entrada: ${entrada.optString("numero_entrada", "No disponible")}"
        tvTipoEntrada.text = "Tipo: ${tipoEntradaActual.uppercase()}"
        tvCantidadEntrada.text = "Cantidad: ${entrada.optInt("cantidad", 0)}"

        val proveedorEntrada = entrada.optJSONObject("proveedor")
        tvProveedorEntrada.text = "Proveedor: ${proveedorEntrada?.optString("nombre_proveedor", "No disponible") ?: "No disponible"}"
        tvFechaEntrada.text = "Fecha entrada: ${formatDate(entrada.optString("fecha_entrada", ""))}"
        tvEstatusEntrada.text = "Estatus: ${entrada.optString("estatus_validacion", "No disponible").uppercase()}"

        if (tipoEntradaActual == "Traspaso") {
            tvReferenciaTraspaso.text = "Referencia: ${entrada.optString("referencia_traspaso", "No disponible")}"
            tvReferenciaTraspaso.visibility = View.VISIBLE
        } else {
            tvReferenciaTraspaso.visibility = View.GONE
        }
    }

    private fun displayValidacionData(validacion: JSONObject) {
        try {
            // Resumen ejecutivo
            if (validacion.has("resumen_ejecutivo")) {
                val resumen = validacion.getJSONObject("resumen_ejecutivo")

                tvEstadoValidacion.text = "Estado: ${resumen.optString("estado", "No disponible")}"
                tvMensajePrincipal.text = "Mensaje: ${resumen.optString("mensaje_principal", "No disponible")}"
                tvAccionRecomendada.text = "Acción: ${resumen.optString("accion_recomendada", "No disponible")}"
                tvSiguientePaso.text = "Siguiente paso: ${resumen.optString("siguiente_paso", "No disponible")}"
                tvTiempoResolucion.text = "Tiempo estimado: ${resumen.optString("tiempo_estimado_resolucion", "No disponible")}"
            }

            // Métricas
            tvPorcentajeCumplimiento.text = "Cumplimiento: ${validacion.optInt("porcentaje_cumplimiento", 0)}%"
            tvNivelRiesgo.text = "Riesgo: ${validacion.optString("nivel_riesgo", "No disponible")}"

            // Validaciones individuales según el tipo
            if (tipoEntradaActual == "Traspaso") {
                tvProveedorValidacion.text = "Proveedor coincide: ${if (validacion.optBoolean("proveedor_coincide", false)) "✅ SÍ" else "❌ NO"}"
                tvCantidadValidacion.text = "Cantidad coincide: ${if (validacion.optBoolean("cantidad_coincide", false)) "✅ SÍ" else "❌ NO"}"
                tvFechaValidacion.text = "Fechas coherentes: ${if (validacion.optBoolean("fecha_coherente", false)) "✅ SÍ" else "❌ NO"}"
                tvPrecioValidacion.text = "Almacenes diferentes: ${if (validacion.optBoolean("almacenes_diferentes", false)) "✅ SÍ" else "❌ NO"}"
                tvEstadoOrdenValidacion.text = "Estado traspaso: ${if (validacion.optBoolean("estado_traspaso_valido", false)) "✅ VÁLIDO" else "❌ INVÁLIDO"}"
            } else {
                // Validaciones para compras - CORREGIDOS los nombres de campos según la API
                tvProveedorValidacion.text = "Proveedor coincide: ${if (validacion.optBoolean("proveedor_coincide", false)) "✅ SÍ" else "❌ NO"}"
                tvCantidadValidacion.text = "Cantidad válida: ${if (validacion.optBoolean("cantidad_valida", false)) "✅ SÍ" else "❌ NO"}"
                tvFechaValidacion.text = "Fechas coherentes: ${if (validacion.optBoolean("fecha_coherente", false)) "✅ SÍ" else "❌ NO"}"
                tvPrecioValidacion.text = "Precio coherente: ${if (validacion.optBoolean("precio_coherente", false)) "✅ SÍ" else "❌ NO"}"
                tvEstadoOrdenValidacion.text = "Estado orden válido: ${if (validacion.optBoolean("estado_orden_valido", false)) "✅ VÁLIDO" else "❌ INVÁLIDO"}"
            }

            // Mostrar discrepancias si existen
            if (validacion.optInt("total_discrepancias", 0) > 0 ||
                validacion.optInt("total_advertencias", 0) > 0) {

                displayDiscrepancias(validacion)
                layoutDiscrepancias.visibility = View.VISIBLE
            } else {
                layoutDiscrepancias.visibility = View.GONE
            }

        } catch (e: Exception) {
            Log.e("EntradasAuditorOptimized", "❌ Error mostrando validación", e)
        }
    }

    private fun displayDiscrepancias(validacion: JSONObject) {
        // Limpiar containers
        containerDiscrepanciasCriticas.removeAllViews()
        containerDiscrepanciasImportantes.removeAllViews()
        containerAdvertencias.removeAllViews()
        containerRecomendaciones.removeAllViews()

        // Discrepancias críticas
        if (validacion.has("discrepancias_criticas")) {
            val criticas = validacion.getJSONArray("discrepancias_criticas")
            for (i in 0 until criticas.length()) {
                val discrepancia = criticas.getJSONObject(i)
                val textView = createDiscrepanciaTextView(discrepancia, R.color.rojo_vino_tenue)
                containerDiscrepanciasCriticas.addView(textView)
            }
        }

        // Discrepancias importantes
        if (validacion.has("discrepancias_importantes")) {
            val importantes = validacion.getJSONArray("discrepancias_importantes")
            for (i in 0 until importantes.length()) {
                val discrepancia = importantes.getJSONObject(i)
                val textView = createDiscrepanciaTextView(discrepancia, R.color.oro_palido)
                containerDiscrepanciasImportantes.addView(textView)
            }
        }

        // Advertencias
        if (validacion.has("advertencias")) {
            val advertencias = validacion.getJSONArray("advertencias")
            for (i in 0 until advertencias.length()) {
                val advertencia = advertencias.getJSONObject(i)
                val textView = createAdvertenciaTextView(advertencia)
                containerAdvertencias.addView(textView)
            }
        }

        // Recomendaciones
        if (validacion.has("recomendaciones")) {
            val recomendaciones = validacion.getJSONArray("recomendaciones")
            for (i in 0 until recomendaciones.length()) {
                val recomendacion = recomendaciones.getJSONObject(i)
                val textView = createRecomendacionTextView(recomendacion)
                containerRecomendaciones.addView(textView)
            }
        }
    }

    private fun createDiscrepanciaTextView(discrepancia: JSONObject, colorRes: Int): TextView {
        val textView = TextView(this)
        textView.text = "${discrepancia.optString("titulo", "Sin título")}\n${discrepancia.optString("descripcion", "Sin descripción")}"
        textView.setTextColor(ContextCompat.getColor(this, colorRes))
        textView.textSize = 12f
        textView.setPadding(16, 8, 16, 8)
        textView.background = ContextCompat.getDrawable(this, R.drawable.rounded_background)
        textView.backgroundTintList = ContextCompat.getColorStateList(this, R.color.gris_perla_claro)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, 8)
        textView.layoutParams = layoutParams

        return textView
    }

    private fun createAdvertenciaTextView(advertencia: JSONObject): TextView {
        val textView = TextView(this)
        textView.text = "⚠️ ${advertencia.optString("mensaje", "Advertencia sin mensaje")}"
        textView.setTextColor(ContextCompat.getColor(this, R.color.oro_palido))
        textView.textSize = 11f
        textView.setPadding(16, 8, 16, 8)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, 4)
        textView.layoutParams = layoutParams

        return textView
    }

    private fun createRecomendacionTextView(recomendacion: JSONObject): TextView {
        val textView = TextView(this)
        textView.text = "💡 ${recomendacion.optString("mensaje", "Recomendación sin mensaje")}"
        textView.setTextColor(ContextCompat.getColor(this, R.color.verde_salvia))
        textView.textSize = 11f
        textView.setPadding(16, 8, 16, 8)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, 4)
        textView.layoutParams = layoutParams

        return textView
    }

    private fun showLoading(show: Boolean) {
        btnBuscarEntrada.isEnabled = !show
        btnBuscarEntrada.text = if (show) "Buscando..." else "Buscar Entrada"
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        // No ocultar detalles en caso de error, solo mostrar el error
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

    // ======================================================================================
    // FUNCIONES COMPLETAS DE VALIDACIÓN Y PROCESAMIENTO (del código original)
    // ======================================================================================

    private fun displayEntradaDataCompra(
        entrada: JSONObject,
        perfume: JSONObject?,
        proveedor: JSONObject?,
        ordenCompra: JSONObject?
    ) {
        // Configurar campos para tipo COMPRA
        configurarCamposParaCompra()

        // Mostrar datos de la ENTRADA
        tvNumeroEntrada.text = "N° Entrada: ${entrada.optString("numero_entrada", "No disponible")}"
        tvTipoEntrada.text = "Tipo: COMPRA"
        tvCantidadEntrada.text = "Cantidad: ${entrada.optInt("cantidad", 0)}"

        // Proveedor de la entrada
        val proveedorEntrada = entrada.optJSONObject("proveedor")
        tvProveedorEntrada.text = "Proveedor: ${proveedorEntrada?.optString("nombre_proveedor", "No disponible") ?: "No disponible"}"
        tvFechaEntrada.text = "Fecha entrada: ${formatDate(entrada.optString("fecha_entrada", ""))}"
        tvEstatusEntrada.text = "Estatus: ${entrada.optString("estatus_validacion", "No disponible").uppercase()}"

        // Mostrar datos de la orden de compra
        if (ordenCompra != null) {
            tvTituloSeccionDos.text = "Orden de Compra"
            tvNumeroOrden.text = "N° Orden: ${ordenCompra.optString("numero_orden", "No disponible")}"
            tvCantidad.text = "Cantidad: ${ordenCompra.optInt("cantidad", 0)}"
            tvPrecioUnitario.text = "Precio unitario: $${ordenCompra.optDouble("precio_unitario", 0.0)}"
            tvPrecioTotal.text = "Precio total: $${ordenCompra.optDouble("precio_total", 0.0)}"
            tvFechaOrden.text = "Fecha: ${formatDate(ordenCompra.optString("fecha_orden", ""))}"
            tvEstatus.text = "Estatus: ${ordenCompra.optString("estado", "No disponible").uppercase()}"
        } else {
            tvTituloSeccionDos.text = "Orden de Compra"
            tvNumeroOrden.text = "N° Orden: No disponible"
            tvCantidad.text = "Cantidad: No disponible"
            tvPrecioUnitario.text = "Precio unitario: No disponible"
            tvPrecioTotal.text = "Precio total: No disponible"
            tvFechaOrden.text = "Fecha: No disponible"
            tvEstatus.text = "Estatus: No disponible"
        }

        // Mostrar datos del perfume y proveedor
        displayPerfumeData(perfume)
        displayProveedorData(proveedor)
    }

    private fun displayEntradaDataTraspaso(
        entrada: JSONObject,
        perfume: JSONObject?,
        proveedor: JSONObject?,
        traspasoOriginal: JSONObject?
    ) {
        // Configurar campos para tipo TRASPASO
        configurarCamposParaTraspaso()

        // Mostrar datos de la ENTRADA
        tvNumeroEntrada.text = "N° Entrada: ${entrada.optString("numero_entrada", "No disponible")}"
        tvTipoEntrada.text = "Tipo: TRASPASO"
        tvCantidadEntrada.text = "Cantidad: ${entrada.optInt("cantidad", 0)}"

        // Proveedor de la entrada
        val proveedorEntrada = entrada.optJSONObject("proveedor")
        tvProveedorEntrada.text = "Proveedor: ${proveedorEntrada?.optString("nombre_proveedor", "No disponible") ?: "No disponible"}"
        tvFechaEntrada.text = "Fecha entrada: ${formatDate(entrada.optString("fecha_entrada", ""))}"
        tvEstatusEntrada.text = "Estatus: ${entrada.optString("estatus_validacion", "No disponible").uppercase()}"
        tvReferenciaTraspaso.text = "Referencia: ${entrada.optString("referencia_traspaso", "No disponible")}"

        // EXTRAER INFORMACIÓN ADICIONAL DE TRASPASO
        Log.d("EntradasAuditorOptimized", "🔍 DEBUG - Entrada completa para traspaso:")
        Log.d("EntradasAuditorOptimized", "  - Campos disponibles en entrada: ${entrada.keys().asSequence().toList()}")
        Log.d("EntradasAuditorOptimized", "  - Tiene informacion_adicional: ${entrada.has("informacion_adicional")}")
        Log.d("EntradasAuditorOptimized", "  - almacen_destino directo: ${entrada.optString("almacen_destino", "NO ENCONTRADO")}")

        if (entrada.has("informacion_adicional")) {
            val infoAd = entrada.optJSONObject("informacion_adicional")
            if (infoAd != null) {
                Log.d("EntradasAuditorOptimized", "  - informacion_adicional campos: ${infoAd.keys().asSequence().toList()}")
                Log.d("EntradasAuditorOptimized", "  - informacion_adicional completo: $infoAd")
            } else {
                Log.d("EntradasAuditorOptimized", "  - informacion_adicional es null")
            }
        }

        val informacionAdicional = obtenerInformacionAdicionalTraspaso(entrada)
        Log.d("EntradasAuditorOptimized", "🔄 Información adicional extraída: $informacionAdicional")

        // DEBUG: Log del traspaso original completo
        if (traspasoOriginal != null) {
            Log.d("EntradasAuditorOptimized", "🔍 DEBUG - traspasoOriginal completo:")
            Log.d("EntradasAuditorOptimized", "  - Campos disponibles: ${traspasoOriginal.keys().asSequence().toList()}")
            Log.d("EntradasAuditorOptimized", "  - almacen_salida: ${traspasoOriginal.opt("almacen_salida")} (tipo: ${traspasoOriginal.opt("almacen_salida")?.javaClass?.simpleName})")
            Log.d("EntradasAuditorOptimized", "  - almacen_destino: ${traspasoOriginal.opt("almacen_destino")} (tipo: ${traspasoOriginal.opt("almacen_destino")?.javaClass?.simpleName})")
            Log.d("EntradasAuditorOptimized", "  - fecha_salida: ${traspasoOriginal.opt("fecha_salida")}")
            Log.d("EntradasAuditorOptimized", "  - createdAt: ${traspasoOriginal.opt("createdAt")}")
            Log.d("EntradasAuditorOptimized", "  - fecha_creacion: ${traspasoOriginal.opt("fecha_creacion")}")
        }

        // Mostrar datos del traspaso original
        if (traspasoOriginal != null) {
            tvTituloSeccionDos.text = "Información de Traspaso"
            tvNumeroOrden.text = "N° Traspaso: ${traspasoOriginal.optString("numero_traspaso", "No disponible")}"
            tvCantidad.text = "Cantidad: ${traspasoOriginal.optInt("cantidad", 0)}"

            // Intentar obtener fecha de creación de diferentes fuentes
            val fechaCreacion = when {
                traspasoOriginal.has("createdAt") -> traspasoOriginal.optString("createdAt", "")
                traspasoOriginal.has("fecha_creacion") -> traspasoOriginal.optString("fecha_creacion", "")
                traspasoOriginal.has("fecha_registro") -> traspasoOriginal.optString("fecha_registro", "")
                else -> entrada.optString("createdAt", "") // Como respaldo, usar fecha de la entrada
            }
            tvFechaOrden.text = "Fecha creación: ${formatDate(fechaCreacion)}"

            tvEstatus.text = "Estatus: ${traspasoOriginal.optString("estatus_validacion", "No disponible").uppercase()}"

            // Usar información adicional prioritariamente, luego traspaso original como respaldo
            val fechaSalida = if (informacionAdicional.fechaSalida.isNotEmpty()) {
                informacionAdicional.fechaSalida
            } else {
                traspasoOriginal.optString("fecha_salida", "")
            }
            tvFechaSalida.text = "Fecha salida: ${formatDate(fechaSalida)}"

            val almacenOrigen = if (informacionAdicional.almacenOrigen.isNotEmpty()) {
                informacionAdicional.almacenOrigen
            } else {
                // Buscar en traspaso original - manejar tanto objetos como strings
                when (val almacenObj = traspasoOriginal.opt("almacen_salida")) {
                    is String -> almacenObj
                    is JSONObject -> almacenObj.optString("codigo", almacenObj.optString("nombre_almacen", "No disponible"))
                    null -> "No disponible"
                    else -> "No disponible"
                }
            }
            tvAlmacenSalida.text = "Almacén origen: $almacenOrigen"

            val almacenDestino = if (informacionAdicional.almacenDestino.isNotEmpty()) {
                informacionAdicional.almacenDestino
            } else {
                // Buscar en traspaso original - manejar tanto objetos como strings
                when (val almacenObj = traspasoOriginal.opt("almacen_destino")) {
                    is String -> almacenObj
                    is JSONObject -> almacenObj.optString("codigo", almacenObj.optString("nombre_almacen", "No disponible"))
                    null -> "No disponible"
                    else -> "No disponible"
                }
            }
            tvAlmacenEntrada.text = "Almacén destino: $almacenDestino"

        } else {
            // Si no hay traspaso original, usar solo información adicional
            tvTituloSeccionDos.text = "Información de Traspaso"
            tvNumeroOrden.text = "N° Traspaso: ${entrada.optString("referencia_traspaso", "No disponible")}"
            tvCantidad.text = "Cantidad: ${entrada.optInt("cantidad", 0)}"
            tvFechaOrden.text = "Fecha creación: ${formatDate(entrada.optString("createdAt", ""))}"
            tvEstatus.text = "Estatus: ${entrada.optString("estatus_validacion", "No disponible").uppercase()}"

            // Usar información adicional - CORREGIDO: mostrar mensaje claro si no hay datos
            val fechaSalidaTexto = if (informacionAdicional.fechaSalida.isNotEmpty()) {
                formatDate(informacionAdicional.fechaSalida)
            } else {
                "No disponible"
            }
            tvFechaSalida.text = "Fecha salida: $fechaSalidaTexto"

            val almacenOrigenTexto = if (informacionAdicional.almacenOrigen.isNotEmpty()) {
                informacionAdicional.almacenOrigen
            } else {
                "No disponible"
            }
            tvAlmacenSalida.text = "Almacén origen: $almacenOrigenTexto"

            val almacenDestinoTexto = if (informacionAdicional.almacenDestino.isNotEmpty()) {
                informacionAdicional.almacenDestino
            } else {
                "No disponible"
            }
            tvAlmacenEntrada.text = "Almacén destino: $almacenDestinoTexto"
        }

        // Mostrar datos del perfume y proveedor
        displayPerfumeData(perfume)
        displayProveedorData(proveedor)
    }

    private fun configurarCamposParaCompra() {
        // Mostrar campos específicos de compra, ocultar los de traspaso
        tvReferenciaTraspaso.visibility = View.GONE

        // Mostrar campos de compra
        tvPrecioUnitario.visibility = View.VISIBLE
        tvPrecioTotal.visibility = View.VISIBLE

        // Ocultar campos específicos de traspaso
        tvFechaSalida.visibility = View.GONE
        tvAlmacenSalida.visibility = View.GONE
        tvAlmacenEntrada.visibility = View.GONE
    }

    private fun configurarCamposParaTraspaso() {
        // Mostrar campos específicos de traspaso
        tvReferenciaTraspaso.visibility = View.VISIBLE

        // Ocultar campos específicos de compra
        tvPrecioUnitario.visibility = View.GONE
        tvPrecioTotal.visibility = View.GONE

        // Mostrar campos específicos de traspaso
        tvFechaSalida.visibility = View.VISIBLE
        tvAlmacenSalida.visibility = View.VISIBLE
        tvAlmacenEntrada.visibility = View.VISIBLE
    }

    private fun displayPerfumeData(perfume: JSONObject?) {
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

            Log.d("EntradasAuditor", "✅ Datos del perfume mostrados: ${perfume.optString("name_per", "No disponible")}")
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

            Log.d("EntradasAuditor", "⚠️ No hay datos de perfume disponibles")
        }
    }

    private fun displayProveedorData(proveedor: JSONObject?) {
        if (proveedor != null) {
            tvNombreProveedor.text = "Nombre: ${proveedor.optString("nombre_proveedor", "No disponible")}"
            tvRfcProveedor.text = "RFC: ${proveedor.optString("rfc", "No disponible")}"
            tvContactoProveedor.text = "Contacto: ${proveedor.optString("contacto", "No disponible")}"
            tvTelefonoProveedor.text = "Teléfono: ${proveedor.optString("telefono", "No disponible")}"
            tvEmailProveedor.text = "Email: ${proveedor.optString("email", "No disponible")}"
            tvDireccionProveedor.text = "Dirección: ${proveedor.optString("direccion", "No disponible")}"
            tvEstadoProveedor.text = "Estado: ${proveedor.optString("estado", "No disponible")}"

            Log.d("EntradasAuditor", "✅ Datos del proveedor mostrados: ${proveedor.optString("nombre_proveedor", "No disponible")}")
        } else {
            tvNombreProveedor.text = "Nombre: No disponible"
            tvRfcProveedor.text = "RFC: No disponible"
            tvContactoProveedor.text = "Contacto: No disponible"
            tvTelefonoProveedor.text = "Teléfono: No disponible"
            tvEmailProveedor.text = "Email: No disponible"
            tvDireccionProveedor.text = "Dirección: No disponible"
            tvEstadoProveedor.text = "Estado: No disponible"

            Log.d("EntradasAuditor", "⚠️ No hay datos de proveedor disponibles")
        }
    }

    private fun procesarValidacionEntrada(numeroEntrada: String) {
        // Mostrar diálogo de confirmación
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Confirmar Validación")

        val mensaje = if (tipoEntradaActual == "Traspaso") {
            "¿Estás seguro de que deseas validar esta entrada de TRASPASO?\n\nEsto actualizará:\n• Estado del traspaso a 'Validado'\n• Stock del perfume con la cantidad de la entrada\n• Estado de validación de la entrada\n• Registros de almacenes"
        } else {
            "¿Estás seguro de que deseas validar esta entrada de COMPRA?\n\nEsto actualizará:\n• Estado de la orden de compra a 'Completada'\n• Stock del perfume con la cantidad de la entrada\n• Estado de validación de la entrada"
        }

        builder.setMessage(mensaje)

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
            Log.d("EntradasAuditor", "📥 RESPUESTA COMPLETA: $response")

            val success = response.optBoolean("success", false)
            val message = response.optString("message", "Sin mensaje")

            if (!response.has("data")) {
                Log.e("EntradasAuditor", "❌ Respuesta sin campo 'data'")
                showError("Respuesta del servidor incompleta")
                return
            }

            val data = response.getJSONObject("data")
            Log.d("EntradasAuditor", "📊 DATA: $data")

            if (success) {
                // Verificar que tenemos los campos esenciales
                if (!data.has("entrada")) {
                    Log.e("EntradasAuditor", "❌ Respuesta sin campo 'entrada'")
                    showError("Respuesta del servidor incompleta - falta información de entrada")
                    return
                }

                val entrada = data.getJSONObject("entrada")
                Log.d("EntradasAuditor", "📋 ENTRADA: $entrada")

                // Para auditor, usar datos básicos si no está presente el objeto completo
                val auditorInfo = if (data.has("auditor")) {
                    val auditor = data.getJSONObject("auditor")
                    "• Nombre: ${auditor.optString("nombre", "No disponible")}\n• Fecha: ${auditor.optString("fecha_validacion", "No disponible")}"
                } else {
                    "• Fecha: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}"
                }

                val detalleValidacion = if (tipoEntradaActual == "Traspaso") {
                    val perfume = if (data.has("perfume")) data.getJSONObject("perfume") else null
                    val traspaso = if (data.has("traspaso")) data.getJSONObject("traspaso") else null

                    """
                    ✅ VALIDACIÓN DE TRASPASO COMPLETADA
                    
                    📋 ENTRADA:
                    • Número: ${entrada.optString("numero_entrada", "No disponible")}
                    • Estado: ${entrada.optString("estatus_nuevo", "validado")}
                    • Cantidad procesada: ${entrada.optInt("cantidad", 0)}
                    • Referencia: ${entrada.optString("referencia_traspaso", "No disponible")}
                    
                    🔄 TRASPASO:
                    • Número: ${traspaso?.optString("numero_traspaso", "No disponible") ?: "No disponible"}
                    • Estado: ${traspaso?.optString("estado_nuevo", "Validado") ?: "Validado"}
                    
                    💎 PERFUME:
                    • ${perfume?.optString("nombre", "No disponible") ?: "No disponible"}
                    • Stock anterior: ${perfume?.optInt("stock_anterior", 0) ?: 0}
                    • Stock nuevo: ${perfume?.optInt("stock_nuevo", 0) ?: 0}
                    • Cantidad agregada: +${perfume?.optInt("cantidad_agregada", 0) ?: 0}
                    
                    👤 AUDITOR RESPONSABLE:
                    $auditorInfo
                    """.trimIndent()
                } else {
                    val ordenCompra = if (data.has("orden_compra")) data.getJSONObject("orden_compra") else null
                    val perfume = if (data.has("perfume")) data.getJSONObject("perfume") else null

                    """
                    ✅ VALIDACIÓN DE COMPRA COMPLETADA
                    
                    📋 ENTRADA:
                    • Número: ${entrada.optString("numero_entrada", "No disponible")}
                    • Estado: ${entrada.optString("estatus_nuevo", "validado")}
                    • Cantidad procesada: ${entrada.optInt("cantidad", 0)}
                    
                    🛒 ORDEN DE COMPRA:
                    • Número: ${ordenCompra?.optString("numero_orden", "No disponible") ?: "No disponible"}
                    • Estado anterior: ${ordenCompra?.optString("estado_anterior", "Pendiente") ?: "Pendiente"}
                    • Estado nuevo: ${ordenCompra?.optString("estado_nuevo", "Completada") ?: "Completada"}
                    
                    💎 PERFUME:
                    • ${perfume?.optString("nombre", "No disponible") ?: "No disponible"}
                    • Stock anterior: ${perfume?.optInt("stock_anterior", 0) ?: 0}
                    • Stock nuevo: ${perfume?.optInt("stock_nuevo", 0) ?: 0}
                    • Cantidad agregada: +${perfume?.optInt("cantidad_agregada", 0) ?: 0}
                    
                    👤 AUDITOR RESPONSABLE:
                    $auditorInfo
                    """.trimIndent()
                }

                // Mostrar diálogo con detalles
                val builder = androidx.appcompat.app.AlertDialog.Builder(this)
                builder.setTitle("✅ Validación Completada")
                builder.setMessage(detalleValidacion)
                builder.setPositiveButton("Entendido") { dialog, _ ->
                    dialog.dismiss()
                    // Actualizar la vista con los nuevos datos
                    buscarEntradaInteligente(entrada.optString("numero_entrada", etNumeroEntrada.text.toString()))
                }
                builder.show()

                Toast.makeText(this, "✅ $message", Toast.LENGTH_LONG).show()

            } else {
                Log.e("EntradasAuditor", "❌ Validación falló: $message")
                showError("Error en la validación: $message")
            }

        } catch (e: Exception) {
            Log.e("EntradasAuditor", "❌ Error procesando respuesta de validación", e)
            Log.e("EntradasAuditor", "📥 Respuesta que causó error: $response")
            showError("Error procesando la respuesta de validación: ${e.message}")
        }
    }

    private fun mostrarDialogoRechazo(numeroEntrada: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Rechazar Entrada")

        // Crear input para motivo de rechazo
        val input = EditText(this)
        input.hint = "Motivo del rechazo (opcional)"
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        input.maxLines = 3
        builder.setView(input)

        val mensaje = if (tipoEntradaActual == "Traspaso") {
            "¿Estás seguro de que deseas RECHAZAR esta entrada de TRASPASO?\n\nEsto actualizará:\n• Estado del traspaso a 'Rechazado'\n• Estado de validación de la entrada a 'rechazado'\n• NO SE MODIFICARÁ el stock del perfume\n• Se registrará el motivo del rechazo"
        } else {
            "¿Estás seguro de que deseas RECHAZAR esta entrada de COMPRA?\n\nEsto actualizará:\n• Estado de la orden de compra a 'Cancelada'\n• Estado de validación de la entrada a 'rechazado'\n• NO SE MODIFICARÁ el stock del perfume\n• Se registrará el motivo del rechazo"
        }

        builder.setMessage(mensaje)

        builder.setPositiveButton("Rechazar") { _, _ ->
            val motivoRechazo = input.text.toString().trim()
            ejecutarRechazoEntrada(numeroEntrada, motivoRechazo)
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun ejecutarRechazoEntrada(numeroEntrada: String, motivoRechazo: String) {
        showLoading(true)
        btnRechazarEntrada.isEnabled = false
        btnRechazarEntrada.text = "Procesando..."

        val token = getAuthToken()
        if (token.isNullOrEmpty()) {
            showError("Token de autenticación no encontrado")
            return
        }

        val url = "$RECHAZAR_ENTRADA_ENDPOINT/$numeroEntrada"
        Log.d("EntradasAuditor", "❌ Procesando rechazo en: $url")

        // Crear el JSON con el motivo de rechazo
        val requestBody = JSONObject()
        if (motivoRechazo.isNotEmpty()) {
            requestBody.put("motivo_rechazo", motivoRechazo)
        }

        val request = object : JsonObjectRequest(
            Request.Method.POST,
            url,
            requestBody,
            { response ->
                Log.d("EntradasAuditor", "❌ Rechazo procesado exitosamente: $response")
                showLoading(false)
                btnRechazarEntrada.isEnabled = false
                btnRechazarEntrada.text = "❌ Rechazada"
                btnRechazarEntrada.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_red_dark)

                handleRechazoResponse(response)
            },
            { error ->
                Log.e("EntradasAuditor", "❌ Error en rechazo", error)
                showLoading(false)
                btnRechazarEntrada.isEnabled = true
                btnRechazarEntrada.text = "❌ Rechazar Entrada"

                val errorMessage = when (error.networkResponse?.statusCode) {
                    400 -> "El rechazo no pudo completarse. Verifica los datos"
                    401 -> "No autorizado. Inicia sesión nuevamente"
                    403 -> "No tienes permisos para rechazar entradas"
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

    private fun handleRechazoResponse(response: JSONObject) {
        try {
            val success = response.getBoolean("success")
            val message = response.getString("message")
            val data = response.getJSONObject("data")

            if (success) {
                val entrada = data.getJSONObject("entrada")
                val auditor = data.getJSONObject("auditor")

                val detalleRechazo = """
                    ❌ RECHAZO COMPLETADO
                    
                    📋 ENTRADA:
                    • Número: ${entrada.getString("numero_entrada")}
                    • Estado anterior: ${entrada.getString("estatus_anterior")}
                    • Estado nuevo: ${entrada.getString("estatus_nuevo")}
                    • Cantidad: ${entrada.getInt("cantidad")}
                    • Motivo: ${entrada.getString("motivo_rechazo")}
                    
                    👤 AUDITOR RESPONSABLE:
                    • Nombre: ${auditor.optString("nombre", "No disponible")}
                    • Fecha: ${auditor.optString("fecha_rechazo_formateada", formatDate(auditor.optString("fecha_rechazo", "")))}
                    """.trimIndent()

                // Mostrar diálogo con detalles
                val builder = androidx.appcompat.app.AlertDialog.Builder(this)
                builder.setTitle("Rechazo Completado")
                builder.setMessage(detalleRechazo)
                builder.setPositiveButton("Entendido") { dialog, _ ->
                    dialog.dismiss()
                    // Actualizar la vista con los nuevos datos
                    buscarEntradaInteligente(entrada.getString("numero_entrada"))
                }
                builder.show()

                Toast.makeText(this, message, Toast.LENGTH_LONG).show()

            } else {
                showError("Error en el rechazo: $message")
            }

        } catch (e: Exception) {
            Log.e("EntradasAuditor", "❌ Error procesando respuesta de rechazo", e)
            showError("Error procesando la respuesta de rechazo: ${e.message}")
        }
    }

    private fun configurarBotonValidacion(entrada: JSONObject, validacion: JSONObject) {
        val estatusValidacion = entrada.optString("estatus_validacion", "registrado")
        val resumenEjecutivo = validacion.optJSONObject("resumen_ejecutivo")
        val puedeProceser = resumenEjecutivo?.optBoolean("puede_procesar", true) ?: true
        val estado = resumenEjecutivo?.optString("estado", "APROBADA") ?: "APROBADA"

        when {
            estatusValidacion == "validado" -> {
                btnValidarEntrada.isEnabled = false
                btnValidarEntrada.text = "✅ Ya Validada"
                btnValidarEntrada.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_green_dark)

                // Ocultar botón de rechazo
                btnRechazarEntrada.visibility = View.GONE
            }
            estatusValidacion == "rechazado" -> {
                btnValidarEntrada.isEnabled = false
                btnValidarEntrada.text = "❌ Ya Rechazada"
                btnValidarEntrada.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_red_dark)

                // Ocultar botón de rechazo
                btnRechazarEntrada.visibility = View.GONE
            }
            estado == "RECHAZADA" || !puedeProceser -> {
                btnValidarEntrada.isEnabled = false
                btnValidarEntrada.text = "❌ No se puede validar"
                btnValidarEntrada.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_red_dark)

                // Mostrar botón de rechazo disponible
                btnRechazarEntrada.visibility = View.VISIBLE
                btnRechazarEntrada.isEnabled = true
            }
            else -> {
                btnValidarEntrada.isEnabled = true
                btnValidarEntrada.text = "✅ Validar Entrada"
                btnValidarEntrada.backgroundTintList = ContextCompat.getColorStateList(this, R.color.lavanda_suave)

                // Mostrar botón de rechazo disponible
                btnRechazarEntrada.visibility = View.VISIBLE
                btnRechazarEntrada.isEnabled = true
            }
        }
    }

    private fun getAuthToken(): String? {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }

    // NUEVA FUNCIÓN PARA OBTENER INFORMACIÓN ADICIONAL DE TRASPASOS
    private fun obtenerInformacionAdicionalTraspaso(entrada: JSONObject): InformacionTraspaso {
        return try {
            val numeroEntrada = entrada.optString("numero_entrada", "???")
            Log.d("EntradasAuditorOptimized", "🔄 Extrayendo información adicional de traspaso para #$numeroEntrada")

            var almacenOrigen = ""
            var almacenDestino = ""
            var fechaSalida = ""

            // PRIMERO: Buscar en el objeto informacion_adicional
            if (entrada.has("informacion_adicional") && entrada.optJSONObject("informacion_adicional") != null) {
                val infoAdicional = entrada.getJSONObject("informacion_adicional")
                Log.d("EntradasAuditorOptimized", "   📦 Objeto informacion_adicional encontrado")
                Log.d("EntradasAuditorOptimized", "   - Campos disponibles: ${infoAdicional.keys().asSequence().toList()}")

                // Almacén de origen
                almacenOrigen = when {
                    infoAdicional.has("almacen_origen") -> {
                        val origenValue = infoAdicional.opt("almacen_origen")
                        val origen = when {
                            origenValue is String -> origenValue
                            origenValue is JSONObject -> origenValue.optString("codigo", origenValue.optString("_id", ""))
                            else -> origenValue?.toString() ?: ""
                        }
                        Log.d("EntradasAuditorOptimized", "   🏪 almacen_origen: '$origen' (tipo: ${origenValue?.javaClass?.simpleName})")
                        origen
                    }
                    infoAdicional.has("almacen_salida") -> {
                        val salidaValue = infoAdicional.opt("almacen_salida")
                        val origen = when {
                            salidaValue is String -> salidaValue
                            salidaValue is JSONObject -> salidaValue.optString("codigo", salidaValue.optString("_id", ""))
                            else -> salidaValue?.toString() ?: ""
                        }
                        Log.d("EntradasAuditorOptimized", "   🏪 almacen_salida: '$origen' (tipo: ${salidaValue?.javaClass?.simpleName})")
                        origen
                    }
                    else -> ""
                }

                // Almacén de destino
                almacenDestino = when {
                    infoAdicional.has("almacen_destino") -> {
                        val destinoValue = infoAdicional.opt("almacen_destino")
                        val destino = when {
                            destinoValue is String -> destinoValue
                            destinoValue is JSONObject -> destinoValue.optString("codigo", destinoValue.optString("_id", ""))
                            else -> destinoValue?.toString() ?: ""
                        }
                        Log.d("EntradasAuditorOptimized", "   🏬 almacen_destino: '$destino' (tipo: ${destinoValue?.javaClass?.simpleName})")
                        destino
                    }
                    infoAdicional.has("almacen_entrada") -> {
                        val entradaValue = infoAdicional.opt("almacen_entrada")
                        val destino = when {
                            entradaValue is String -> entradaValue
                            entradaValue is JSONObject -> entradaValue.optString("codigo", entradaValue.optString("_id", ""))
                            else -> entradaValue?.toString() ?: ""
                        }
                        Log.d("EntradasAuditorOptimized", "   🏬 almacen_entrada: '$destino' (tipo: ${entradaValue?.javaClass?.simpleName})")
                        destino
                    }
                    else -> ""
                }

                // Fecha de salida
                fechaSalida = when {
                    infoAdicional.has("fecha_salida") -> {
                        val fecha = infoAdicional.optString("fecha_salida", "")
                        Log.d("EntradasAuditorOptimized", "   📅 fecha_salida: '$fecha'")
                        fecha
                    }
                    else -> ""
                }
            }

            // SEGUNDO: Si no encontramos datos en informacion_adicional, buscar directamente en la entrada
            if (almacenDestino.isEmpty()) {
                val almacenDestinoValue = entrada.opt("almacen_destino")
                almacenDestino = when {
                    almacenDestinoValue is String -> almacenDestinoValue
                    almacenDestinoValue is JSONObject -> almacenDestinoValue.optString("codigo", almacenDestinoValue.optString("_id", ""))
                    else -> almacenDestinoValue?.toString() ?: ""
                }
                Log.d("EntradasAuditorOptimized", "   🔍 almacen_destino directo de entrada: '$almacenDestino' (tipo: ${almacenDestinoValue?.javaClass?.simpleName})")
            }

            if (almacenOrigen.isEmpty()) {
                val almacenOrigenValue = entrada.opt("almacen_origen") ?: entrada.opt("almacen_salida")
                almacenOrigen = when {
                    almacenOrigenValue is String -> almacenOrigenValue
                    almacenOrigenValue is JSONObject -> almacenOrigenValue.optString("codigo", almacenOrigenValue.optString("_id", ""))
                    else -> almacenOrigenValue?.toString() ?: ""
                }
                Log.d("EntradasAuditorOptimized", "   🔍 almacen_origen directo de entrada: '$almacenOrigen' (tipo: ${almacenOrigenValue?.javaClass?.simpleName})")
            }

            if (fechaSalida.isEmpty()) {
                fechaSalida = entrada.optString("fecha_salida", "")
                Log.d("EntradasAuditorOptimized", "   🔍 fecha_salida directa de entrada: '$fechaSalida'")
            }

            // TERCERO: Si aún no tenemos fecha_salida, buscar en el traspasoOriginal si está disponible
            // Esto lo haremos en la función que llama a esta, ya que aquí no tenemos acceso al traspasoOriginal

            val resultado = InformacionTraspaso(almacenOrigen, almacenDestino, fechaSalida)
            Log.d("EntradasAuditorOptimized", "   ✅ Información extraída para #$numeroEntrada: $resultado")

            return resultado

        } catch (e: Exception) {
            Log.e("EntradasAuditorOptimized", "❌ Error obteniendo información adicional de traspaso", e)
            return InformacionTraspaso("", "", "")
        }
    }

    // DATA CLASS PARA MANEJAR INFORMACIÓN DE TRASPASOS
    private data class InformacionTraspaso(
        val almacenOrigen: String,
        val almacenDestino: String,
        val fechaSalida: String
    )
}
