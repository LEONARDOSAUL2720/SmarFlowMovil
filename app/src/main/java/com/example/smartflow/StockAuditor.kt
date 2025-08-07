package com.example.smartflow

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class StockAuditor : AppCompatActivity() {

    // Configuración de la API
    private val BASE_URL = "https://smartflow-mwmm.onrender.com/api"
    private val PERFUMES_ENDPOINT = "$BASE_URL/auditor/perfumes"
    private val FILTROS_ENDPOINT = "$BASE_URL/auditor/perfumes/filtros"

    // UI Components
    private lateinit var etBuscarPerfume: EditText
    private lateinit var spinnerFiltroAlmacen: Spinner
    private lateinit var spinnerFiltroCategoria: Spinner
    private lateinit var spinnerFiltroEstado: Spinner
    private lateinit var btnLimpiarFiltros: Button
    private lateinit var tvInfoResultados: TextView
    private lateinit var rvPerfumes: RecyclerView
    private lateinit var progressBar: ProgressBar

    // Adapter y datos
    private lateinit var perfumesAdapter: PerfumesAdapter
    private lateinit var requestQueue: RequestQueue

    // Listas para los spinners
    private var listaAlmacenes = mutableListOf<String>()
    private var listaCategorias = mutableListOf<String>()
    private var listaEstados = mutableListOf<String>()

    // Variables para filtros actuales
    private var filtroActualAlmacen = ""
    private var filtroActualCategoria = ""
    private var filtroActualEstado = ""
    private var busquedaActual = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stock_auditor_styled)

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        setupTextWatchers()

        requestQueue = Volley.newRequestQueue(this)

        // Cargar datos iniciales
        cargarOpcionesFiltros()
        cargarPerfumes()

        Log.d("StockAuditor", "Actividad de Stock Auditor iniciada")
    }

    private fun initializeViews() {
        etBuscarPerfume = findViewById(R.id.et_buscar_perfume)
        spinnerFiltroAlmacen = findViewById(R.id.spinner_filtro_almacen)
        spinnerFiltroCategoria = findViewById(R.id.spinner_filtro_categoria)
        spinnerFiltroEstado = findViewById(R.id.spinner_filtro_estado)
        btnLimpiarFiltros = findViewById(R.id.btn_limpiar_filtros)
        tvInfoResultados = findViewById(R.id.tv_info_resultados)
        rvPerfumes = findViewById(R.id.rv_perfumes)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupRecyclerView() {
        perfumesAdapter = PerfumesAdapter()
        rvPerfumes.apply {
            layoutManager = LinearLayoutManager(this@StockAuditor)
            adapter = perfumesAdapter
            // Optimizaciones para CoordinatorLayout
            isNestedScrollingEnabled = false
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        btnLimpiarFiltros.setOnClickListener {
            limpiarFiltros()
        }

        // Listeners para los spinners
        spinnerFiltroAlmacen.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val seleccion = parent?.getItemAtPosition(position).toString()
                filtroActualAlmacen = if (seleccion.contains("Todos") || position == 0) "" else seleccion
                aplicarFiltros()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerFiltroCategoria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val seleccion = parent?.getItemAtPosition(position).toString()
                filtroActualCategoria = if (seleccion.contains("Todas") || position == 0) "" else seleccion
                aplicarFiltros()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerFiltroEstado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val seleccion = parent?.getItemAtPosition(position).toString()
                filtroActualEstado = if (seleccion.contains("Todos") || position == 0) "" else seleccion
                aplicarFiltros()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupTextWatchers() {
        etBuscarPerfume.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                busquedaActual = s.toString().trim()
                aplicarFiltros()
            }
        })
    }

    private fun cargarOpcionesFiltros() {
        Log.d("StockAuditor", "🔍 Cargando opciones para filtros...")

        val token = getAuthToken()
        if (token.isNullOrEmpty()) {
            showError("Token de autenticación no encontrado")
            return
        }

        val request = object : JsonObjectRequest(
            Request.Method.GET,
            FILTROS_ENDPOINT,
            null,
            { response ->
                try {
                    Log.d("StockAuditor", "✅ Opciones de filtros recibidas: $response")

                    val data = response.getJSONObject("data")

                    // Procesar almacenes
                    val almacenesArray = data.getJSONArray("almacenes")
                    listaAlmacenes.clear()
                    listaAlmacenes.add("Todos los almacenes")
                    for (i in 0 until almacenesArray.length()) {
                        listaAlmacenes.add(almacenesArray.getString(i))
                    }

                    // Procesar categorías
                    val categoriasArray = data.getJSONArray("categorias")
                    listaCategorias.clear()
                    listaCategorias.add("Todas las categorías")
                    for (i in 0 until categoriasArray.length()) {
                        listaCategorias.add(categoriasArray.getString(i))
                    }

                    // Procesar estados
                    val estadosArray = data.getJSONArray("estados")
                    listaEstados.clear()
                    listaEstados.add("Todos los estados")
                    for (i in 0 until estadosArray.length()) {
                        listaEstados.add(estadosArray.getString(i))
                    }

                    // Poblar spinners
                    poblarSpinners()

                    Log.d("StockAuditor", "✅ Filtros cargados: ${listaAlmacenes.size} almacenes, ${listaCategorias.size} categorías, ${listaEstados.size} estados")

                } catch (e: Exception) {
                    Log.e("StockAuditor", "❌ Error procesando opciones de filtros", e)
                    showError("Error procesando opciones de filtros: ${e.message}")
                }
            },
            { error ->
                Log.e("StockAuditor", "❌ Error cargando opciones de filtros", error)
                showError("Error cargando opciones de filtros")

                // Cargar opciones por defecto
                cargarOpcionesPorDefecto()
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

    private fun cargarOpcionesPorDefecto() {
        listaAlmacenes.clear()
        listaAlmacenes.addAll(listOf("Todos los almacenes", "ALM001", "ALM002"))

        listaCategorias.clear()
        listaCategorias.addAll(listOf("Todas las categorías", "Masculino", "Femenino", "Unisex"))

        listaEstados.clear()
        listaEstados.addAll(listOf("Todos los estados", "Activo", "Inactivo"))

        poblarSpinners()
    }

    private fun poblarSpinners() {
        // Poblar spinner de almacenes
        val adapterAlmacenes = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaAlmacenes)
        adapterAlmacenes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFiltroAlmacen.adapter = adapterAlmacenes

        // Poblar spinner de categorías
        val adapterCategorias = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaCategorias)
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFiltroCategoria.adapter = adapterCategorias

        // Poblar spinner de estados
        val adapterEstados = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaEstados)
        adapterEstados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFiltroEstado.adapter = adapterEstados
    }

    private fun aplicarFiltros() {
        // Aplicar filtros con un pequeño delay para evitar demasiadas llamadas
        rvPerfumes.postDelayed({
            cargarPerfumes()
        }, 300)
    }

    private fun limpiarFiltros() {
        etBuscarPerfume.setText("")
        spinnerFiltroAlmacen.setSelection(0)
        spinnerFiltroCategoria.setSelection(0)
        spinnerFiltroEstado.setSelection(0)

        busquedaActual = ""
        filtroActualAlmacen = ""
        filtroActualCategoria = ""
        filtroActualEstado = ""

        cargarPerfumes()
    }

    private fun cargarPerfumes() {
        Log.d("StockAuditor", "🔍 Cargando perfumes...")
        showLoading(true)

        val token = getAuthToken()
        if (token.isNullOrEmpty()) {
            showError("Token de autenticación no encontrado")
            return
        }

        // Construir URL con parámetros de filtro
        var url = PERFUMES_ENDPOINT
        val parametros = mutableListOf<String>()

        if (busquedaActual.isNotEmpty()) {
            parametros.add("busqueda=${busquedaActual}")
        }
        if (filtroActualAlmacen.isNotEmpty()) {
            parametros.add("almacen=${filtroActualAlmacen}")
        }
        if (filtroActualCategoria.isNotEmpty()) {
            parametros.add("categoria=${filtroActualCategoria}")
        }
        if (filtroActualEstado.isNotEmpty()) {
            parametros.add("estado=${filtroActualEstado}")
        }

        if (parametros.isNotEmpty()) {
            url += "?" + parametros.joinToString("&")
        }

        Log.d("StockAuditor", "🔗 URL de consulta: $url")
        Log.d("StockAuditor", "📱 Token enviado: ${token.take(20)}...")

        val request = object : JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            JsonObjectRequest@{ response ->
                try {
                    Log.d("StockAuditor", "✅ Response completa recibida: $response")
                    showLoading(false)

                    // Verificar estructura de respuesta
                    if (!response.has("success")) {
                        Log.e("StockAuditor", "❌ Respuesta no tiene campo 'success'")
                        showError("Formato de respuesta inválido")
                        return@JsonObjectRequest
                    }

                    val success = response.getBoolean("success")
                    if (!success) {
                        Log.e("StockAuditor", "❌ API reportó error: ${response.optString("message", "Error desconocido")}")
                        showError("Error del servidor: ${response.optString("message", "Error desconocido")}")
                        return@JsonObjectRequest
                    }

                    val data = response.getJSONObject("data")
                    Log.d("StockAuditor", "📊 Objeto data: $data")

                    val perfumesArray = data.getJSONArray("perfumes")
                    val metadatos = data.getJSONObject("metadatos")

                    Log.d("StockAuditor", "📊 Total perfumes en response: ${perfumesArray.length()}")
                    Log.d("StockAuditor", "📊 Metadatos: $metadatos")

                    // Log del primer perfume para verificar estructura
                    if (perfumesArray.length() > 0) {
                        val primerPerfume = perfumesArray.getJSONObject(0)
                        Log.d("StockAuditor", "📋 Primer perfume recibido: $primerPerfume")
                        Log.d("StockAuditor", "🔍 Claves del primer perfume: ${primerPerfume.keys().asSequence().toList()}")

                        // Verificar campos específicos
                        Log.d("StockAuditor", "🔍 Verificando campos del primer perfume:")
                        Log.d("StockAuditor", "   - name_per: ${if (primerPerfume.has("name_per")) primerPerfume.optString("name_per") else "FALTANTE"}")
                        Log.d("StockAuditor", "   - categoria_per: ${if (primerPerfume.has("categoria_per")) primerPerfume.optString("categoria_per") else "FALTANTE"}")
                        Log.d("StockAuditor", "   - marca: ${if (primerPerfume.has("marca")) primerPerfume.optString("marca") else "FALTANTE"}")
                        Log.d("StockAuditor", "   - stock_per: ${if (primerPerfume.has("stock_per")) primerPerfume.optInt("stock_per") else "FALTANTE"}")
                        Log.d("StockAuditor", "   - stock_minimo_per: ${if (primerPerfume.has("stock_minimo_per")) primerPerfume.optInt("stock_minimo_per") else "FALTANTE"}")
                        Log.d("StockAuditor", "   - precio_venta_per: ${if (primerPerfume.has("precio_venta_per")) primerPerfume.optDouble("precio_venta_per") else "FALTANTE"}")
                        Log.d("StockAuditor", "   - ubicacion_per: ${if (primerPerfume.has("ubicacion_per")) primerPerfume.optString("ubicacion_per") else "FALTANTE"}")
                        Log.d("StockAuditor", "   - estado: ${if (primerPerfume.has("estado")) primerPerfume.optString("estado") else "FALTANTE"}")
                        Log.d("StockAuditor", "   - _id: ${if (primerPerfume.has("_id")) primerPerfume.optString("_id") else "FALTANTE"}")
                    }

                    // Convertir JSONArray a List<JSONObject>
                    val listaPerfumes = mutableListOf<JSONObject>()
                    for (i in 0 until perfumesArray.length()) {
                        val perfume = perfumesArray.getJSONObject(i)
                        listaPerfumes.add(perfume)
                        Log.d("StockAuditor", "📦 Perfume $i agregado a lista: ${perfume.optString("name_per", "SIN NOMBRE")}")
                    }

                    Log.d("StockAuditor", "📦 Lista convertida, tamaño final: ${listaPerfumes.size}")

                    // Actualizar adapter CON LOG
                    Log.d("StockAuditor", "🔄 Enviando lista al adapter...")
                    perfumesAdapter.updatePerfumes(listaPerfumes)
                    Log.d("StockAuditor", "✅ Adapter actualizado")

                    // Actualizar información de resultados
                    val total = metadatos.getInt("total")
                    val filtrosAplicados = data.getJSONObject("filtros_aplicados")

                    val infoText = buildString {
                        append("✅ Se encontraron $total perfumes")

                        val filtrosActivos = mutableListOf<String>()
                        if (!filtrosAplicados.isNull("busqueda")) {
                            filtrosActivos.add("búsqueda")
                        }
                        if (!filtrosAplicados.isNull("almacen")) {
                            filtrosActivos.add("almacén")
                        }
                        if (!filtrosAplicados.isNull("categoria")) {
                            filtrosActivos.add("categoría")
                        }
                        if (!filtrosAplicados.isNull("estado")) {
                            filtrosActivos.add("estado")
                        }

                        if (filtrosActivos.isNotEmpty()) {
                            append(" (con filtros: ${filtrosActivos.joinToString(", ")})")
                        }
                    }

                    tvInfoResultados.text = infoText
                    tvInfoResultados.setTextColor(getColor(R.color.verde_salvia))

                    Log.d("StockAuditor", "✅ Se cargaron ${listaPerfumes.size} perfumes de $total totales")
                    Log.d("StockAuditor", "📱 Texto de resultados actualizado: $infoText")

                    // Verificar estado final del RecyclerView
                    Log.d("StockAuditor", "🔍 Estado final del RecyclerView:")
                    Log.d("StockAuditor", "   - Adapter item count: ${perfumesAdapter.itemCount}")
                    Log.d("StockAuditor", "   - RecyclerView visibility: ${if (rvPerfumes.visibility == View.VISIBLE) "VISIBLE" else "GONE/INVISIBLE"}")
                    Log.d("StockAuditor", "   - ProgressBar visibility: ${if (progressBar.visibility == View.VISIBLE) "VISIBLE" else "GONE/INVISIBLE"}")

                } catch (e: Exception) {
                    Log.e("StockAuditor", "❌ Error procesando perfumes", e)
                    Log.e("StockAuditor", "🔍 Stack trace completo:", e)
                    showLoading(false)
                    showError("Error procesando los datos: ${e.message}")
                }
            },
            { error ->
                Log.e("StockAuditor", "❌ Error cargando perfumes", error)
                Log.e("StockAuditor", "🔍 Detalles del error de red:")
                Log.e("StockAuditor", "   - Código de estado: ${error.networkResponse?.statusCode}")
                Log.e("StockAuditor", "   - Datos de respuesta: ${error.networkResponse?.data?.let { String(it) }}")
                Log.e("StockAuditor", "   - Headers: ${error.networkResponse?.headers}")
                Log.e("StockAuditor", "   - Mensaje del error: ${error.message}")

                showLoading(false)

                val errorMessage = when (error.networkResponse?.statusCode) {
                    401 -> "No autorizado. Inicia sesión nuevamente"
                    403 -> "No tienes permisos para acceder a esta información"
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
                Log.d("StockAuditor", "📤 Headers enviados: $headers")
                return headers
            }
        }

        Log.d("StockAuditor", "📤 Enviando request a la API...")
        requestQueue.add(request)
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        rvPerfumes.visibility = if (show) View.GONE else View.VISIBLE

        if (show) {
            tvInfoResultados.text = "🔄 Cargando perfumes..."
            tvInfoResultados.setTextColor(getColor(R.color.lavanda_suave))
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        // Usar colores personalizados para el mensaje de error
        tvInfoResultados.text = "❌ Error: $message"
        tvInfoResultados.setTextColor(getColor(R.color.rojo_vino_tenue))

        perfumesAdapter.limpiarPerfumes()
    }

    private fun getAuthToken(): String? {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }
}
