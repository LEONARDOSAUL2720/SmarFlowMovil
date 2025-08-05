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

        val request = object : JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    Log.d("StockAuditor", "✅ Perfumes recibidos: $response")
                    showLoading(false)

                    val data = response.getJSONObject("data")
                    val perfumesArray = data.getJSONArray("perfumes")
                    val metadatos = data.getJSONObject("metadatos")

                    // Convertir JSONArray a List<JSONObject>
                    val listaPerfumes = mutableListOf<JSONObject>()
                    for (i in 0 until perfumesArray.length()) {
                        listaPerfumes.add(perfumesArray.getJSONObject(i))
                    }

                    // Actualizar adapter
                    perfumesAdapter.updatePerfumes(listaPerfumes)

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

                } catch (e: Exception) {
                    Log.e("StockAuditor", "❌ Error procesando perfumes", e)
                    showLoading(false)
                    showError("Error procesando los datos: ${e.message}")
                }
            },
            { error ->
                Log.e("StockAuditor", "❌ Error cargando perfumes", error)
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
                return headers
            }
        }

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
