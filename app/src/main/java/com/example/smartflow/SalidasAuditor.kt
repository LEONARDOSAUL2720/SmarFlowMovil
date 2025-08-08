package com.example.smartflow

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class SalidasAuditor : AppCompatActivity() {

    // Views principales
    private lateinit var editTextBuscarId: EditText
    private lateinit var buttonBuscarId: Button
    private lateinit var recyclerViewSalidas: RecyclerView
    private lateinit var textViewEstadisticas: TextView
    private lateinit var progressBarCarga: ProgressBar

    // Filtros disponibles
    private lateinit var spinnerTipo: Spinner
    private lateinit var spinnerAlmacen: Spinner
    private lateinit var buttonAplicarFiltros: Button
    private lateinit var buttonLimpiarFiltros: Button

    // Adaptador y datos
    private lateinit var salidasAdapter: SalidasAdapter
    private var salidasList = mutableListOf<Salida>()
    private var filtrosActivos = FiltrosSalidas()

    // API y autenticación - usar la misma URL que en LoginActivity
    private lateinit var sharedPreferences: SharedPreferences
    private var authToken: String = ""
    // private val baseUrl = "http://10.0.2.2:3000/api/auditor/salidas" // Para emulador Android
    private val baseUrl = "https://smartflow-mwmm.onrender.com/api/auditor/salidas" // Para producción en Render

    // Coroutines
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.salidas_auditor)

        initializeViews()
        setupAuthentication()
        setupRecyclerView()
        setupEventListeners()
        setupSpinners()

        // Cargar datos iniciales
        cargarSalidas()
    }

    private fun initializeViews() {
        editTextBuscarId = findViewById(R.id.et_busqueda_salida)
        buttonBuscarId = findViewById(R.id.btn_buscar_salida)
        recyclerViewSalidas = findViewById(R.id.rv_salidas)
        textViewEstadisticas = findViewById(R.id.tv_info_salidas)
        progressBarCarga = findViewById(R.id.progress_bar_lista)

        // Filtros disponibles
        spinnerTipo = findViewById(R.id.spinner_filtro_tipo)
        spinnerAlmacen = findViewById(R.id.spinner_filtro_almacen)
        buttonAplicarFiltros = findViewById(R.id.btn_cargar_salidas)
        buttonLimpiarFiltros = findViewById(R.id.btn_limpiar_filtros_lista)
    }

    private fun setupAuthentication() {
        // Usar el mismo nombre de SharedPreferences que en LoginActivity
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        authToken = sharedPreferences.getString("auth_token", "") ?: ""

        if (authToken.isEmpty()) {
            Toast.makeText(this, "⚠️ No hay sesión activa. Redirigiendo al login...", Toast.LENGTH_LONG).show()
            // Redirigir al login si no hay token
            redirectToLogin()
        } else {
            // Token encontrado - mostrar información del usuario logueado
            val userName = sharedPreferences.getString("user_name", "Usuario") ?: "Usuario"
            val userRole = sharedPreferences.getString("user_role", "") ?: ""
            Toast.makeText(this, "✅ Sesión activa: $userName ($userRole)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun redirectToLogin() {
        val intent = android.content.Intent(this, LoginActivity::class.java)
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupRecyclerView() {
        Log.d("SalidasAuditor", "Iniciando setupRecyclerView")

        salidasAdapter = SalidasAdapter(salidasList) { salida: Salida ->
            mostrarDetalleSalida(salida)
        }
        Log.d("SalidasAuditor", "SalidasAdapter creado")

        recyclerViewSalidas.layoutManager = LinearLayoutManager(this)
        Log.d("SalidasAuditor", "LayoutManager asignado")

        recyclerViewSalidas.adapter = salidasAdapter
        Log.d("SalidasAuditor", "Adapter asignado al RecyclerView")

        // Verificar configuración
        Log.d("SalidasAuditor", "RecyclerView configurado correctamente")
        Log.d("SalidasAuditor", "LayoutManager: ${recyclerViewSalidas.layoutManager}")
        Log.d("SalidasAuditor", "Adapter: ${recyclerViewSalidas.adapter}")
        Log.d("SalidasAuditor", "RecyclerView visibility: ${recyclerViewSalidas.visibility}")
    }

    private fun setupEventListeners() {
        buttonBuscarId.setOnClickListener {
            val idBusqueda = editTextBuscarId.text.toString().trim()
            if (idBusqueda.isNotEmpty()) {
                buscarSalidaPorId(idBusqueda)
            } else {
                Toast.makeText(this, "Ingrese un ID de salida", Toast.LENGTH_SHORT).show()
            }
        }

        buttonAplicarFiltros.setOnClickListener {
            aplicarFiltros()
        }

        buttonLimpiarFiltros.setOnClickListener {
            limpiarFiltros()
        }
    }

    private fun setupSpinners() {
        // Spinner de tipos - solo Venta y Merma como en el modelo de tu compañero
        val tiposSalida = arrayOf("Todos", "Venta", "Merma")
        val tipoAdapter = ArrayAdapter(this, R.layout.spinner_item, tiposSalida)
        tipoAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        // Configurar spinner y limpiar cualquier tint
        spinnerTipo.adapter = tipoAdapter
        spinnerTipo.backgroundTintList = null
        spinnerTipo.backgroundTintMode = null

        // Configurar spinner de almacén con valores por defecto
        val almacenesDefault = arrayOf("Cargando...")
        val almacenAdapter = ArrayAdapter(this, R.layout.spinner_item, almacenesDefault)
        almacenAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerAlmacen.adapter = almacenAdapter
        spinnerAlmacen.backgroundTintList = null
        spinnerAlmacen.backgroundTintMode = null

        // Cargar opciones de almacenes dinámicamente
        cargarOpcionesFiltros()
    }

    private fun cargarSalidas(filtros: String = "") {
        mostrarCarga(true)

        mainScope.launch {
            try {
                val url = if (filtros.isNotEmpty()) "$baseUrl?$filtros" else baseUrl
                val response = withContext(Dispatchers.IO) {
                    realizarPeticionGET(url)
                }

                if (response != null) {
                    procesarRespuestaSalidas(response)
                } else {
                    mostrarError("Error al cargar salidas")
                }
            } catch (e: Exception) {
                Log.e("SalidasAuditor", "Error cargando salidas", e)
                mostrarError("Error: ${e.message}")
            } finally {
                mostrarCarga(false)
            }
        }
    }

    private fun buscarSalidaPorId(id: String) {
        mostrarCarga(true)

        mainScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    realizarPeticionGET("$baseUrl/$id")
                }

                if (response != null) {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        val salidaData = jsonResponse.getJSONObject("data")
                        val salida = parsearSalida(salidaData)
                        mostrarDetalleSalida(salida)
                    } else {
                        mostrarError("Salida no encontrada")
                    }
                } else {
                    mostrarError("Error al buscar salida")
                }
            } catch (e: Exception) {
                Log.e("SalidasAuditor", "Error buscando salida", e)
                mostrarError("Error: ${e.message}")
            } finally {
                mostrarCarga(false)
            }
        }
    }

    private fun aplicarFiltros() {
        val tipo = spinnerTipo.selectedItem.toString()
        val almacen = spinnerAlmacen.selectedItem.toString()

        val filtrosQuery = mutableListOf<String>()

        if (tipo != "Todos") {
            filtrosQuery.add("tipo=$tipo")
        }

        if (almacen != "Todos") {
            // Aquí podrías agregar lógica para obtener el ID del almacén
            // Por ahora filtramos por nombre
            filtrosQuery.add("almacen_salida=$almacen")
        }

        val queryString = filtrosQuery.joinToString("&")
        cargarSalidas(queryString)
    }

    private fun limpiarFiltros() {
        spinnerTipo.setSelection(0)
        spinnerAlmacen.setSelection(0)
        editTextBuscarId.setText("")

        cargarSalidas()
    }

    private fun cargarOpcionesFiltros() {
        mainScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    realizarPeticionGET("$baseUrl/opciones/filtros")
                }

                if (response != null) {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        val data = jsonResponse.getJSONObject("data")
                        val almacenes = data.getJSONArray("almacenes")

                        val almacenesList = mutableListOf<String>()
                        almacenesList.add("Todos")

                        for (i in 0 until almacenes.length()) {
                            val almacen = almacenes.getJSONObject(i)
                            almacenesList.add(almacen.getString("nombre"))
                        }

                        val almacenAdapter = ArrayAdapter(this@SalidasAuditor,
                            R.layout.spinner_item, almacenesList)
                        almacenAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                        spinnerAlmacen.adapter = almacenAdapter

                        // Limpiar cualquier tint que pueda estar aplicándose
                        spinnerAlmacen.backgroundTintList = null
                        spinnerAlmacen.backgroundTintMode = null
                    }
                }
            } catch (e: Exception) {
                Log.e("SalidasAuditor", "Error cargando opciones de filtros", e)
            }
        }
    }

    private fun mostrarDetalleSalida(salida: Salida) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_detalle_salida, null)

        // Llenar datos en el bottom sheet
        view.findViewById<TextView>(R.id.textViewNumeroSalida).text = salida.numeroSalida
        view.findViewById<TextView>(R.id.textViewNombrePerfume).text = salida.nombrePerfume
        view.findViewById<TextView>(R.id.textViewCantidad).text = salida.cantidad.toString()
        view.findViewById<TextView>(R.id.textViewTipo).text = salida.tipo
        view.findViewById<TextView>(R.id.textViewAlmacen).text = salida.almacenSalida
        view.findViewById<TextView>(R.id.textViewFecha).text = formatearFecha(salida.fechaSalida)
        view.findViewById<TextView>(R.id.textViewEstatus).text = salida.estatusAuditoria

        // Mostrar información adicional según el tipo
        when (salida.tipo) {
            "Venta" -> {
                view.findViewById<TextView>(R.id.textViewPrecioUnitario)?.let {
                    it.text = "Precio: $${salida.precioUnitario ?: 0.0}"
                    it.visibility = View.VISIBLE
                }
                view.findViewById<TextView>(R.id.textViewCliente)?.let {
                    it.text = "Cliente: ${salida.cliente ?: "N/A"}"
                    it.visibility = View.VISIBLE
                }
                view.findViewById<TextView>(R.id.textViewFactura)?.let {
                    it.text = "Factura: ${salida.numeroFactura ?: "N/A"}"
                    it.visibility = View.VISIBLE
                }
            }
            "Merma" -> {
                view.findViewById<TextView>(R.id.textViewMotivo)?.let {
                    it.text = "Motivo: ${salida.motivo ?: "N/A"}"
                    it.visibility = View.VISIBLE
                }
                view.findViewById<TextView>(R.id.textViewDescripcion)?.let {
                    it.text = "Descripción: ${salida.descripcionMerma ?: "N/A"}"
                    it.visibility = View.VISIBLE
                }
            }
        }

        // Botones de auditoría
        val buttonMarcarAuditado = view.findViewById<Button>(R.id.buttonMarcarAuditado)
        val buttonReportarInconsistencia = view.findViewById<Button>(R.id.buttonReportarInconsistencia)

        // Habilitar botones solo si está pendiente
        val esPendiente = salida.estatusAuditoria == "pendiente"
        buttonMarcarAuditado.isEnabled = esPendiente
        buttonReportarInconsistencia.isEnabled = esPendiente

        buttonMarcarAuditado.setOnClickListener {
            marcarComoAuditado(salida.id, "Salida verificada y aprobada")
            bottomSheetDialog.dismiss()
        }

        buttonReportarInconsistencia.setOnClickListener {
            mostrarDialogoInconsistencia(salida.id) {
                bottomSheetDialog.dismiss()
            }
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    private fun marcarComoAuditado(salidaId: String, observaciones: String) {
        mainScope.launch {
            try {
                val body = JSONObject()
                body.put("observaciones_auditor", observaciones)

                val response = withContext(Dispatchers.IO) {
                    realizarPeticionPUT("$baseUrl/$salidaId/auditar", body.toString())
                }

                if (response != null) {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        Toast.makeText(this@SalidasAuditor, "Salida marcada como auditada", Toast.LENGTH_SHORT).show()
                        cargarSalidas() // Recargar la lista
                    } else {
                        mostrarError(jsonResponse.getString("message"))
                    }
                } else {
                    mostrarError("Error al auditar salida")
                }
            } catch (e: Exception) {
                Log.e("SalidasAuditor", "Error marcando como auditado", e)
                mostrarError("Error: ${e.message}")
            }
        }
    }

    private fun mostrarDialogoInconsistencia(salidaId: String, onComplete: () -> Unit) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        val editText = EditText(this)
        editText.hint = "Describe la inconsistencia encontrada..."
        editText.minLines = 3

        builder.setTitle("Reportar Inconsistencia")
        builder.setMessage("Describe detalladamente la inconsistencia encontrada:")
        builder.setView(editText)

        builder.setPositiveButton("Reportar") { _, _ ->
            val observaciones = editText.text.toString().trim()
            if (observaciones.isNotEmpty()) {
                reportarInconsistencia(salidaId, "INCONSISTENCIA REPORTADA: $observaciones")
                onComplete()
            } else {
                Toast.makeText(this, "Debe describir la inconsistencia", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun reportarInconsistencia(salidaId: String, observaciones: String) {
        mainScope.launch {
            try {
                val body = JSONObject()
                body.put("observaciones_auditor", observaciones)

                val response = withContext(Dispatchers.IO) {
                    realizarPeticionPUT("$baseUrl/$salidaId/inconsistencia", body.toString())
                }

                if (response != null) {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        Toast.makeText(this@SalidasAuditor, "Inconsistencia reportada", Toast.LENGTH_SHORT).show()
                        cargarSalidas() // Recargar la lista
                    } else {
                        mostrarError(jsonResponse.getString("message"))
                    }
                } else {
                    mostrarError("Error al reportar inconsistencia")
                }
            } catch (e: Exception) {
                Log.e("SalidasAuditor", "Error reportando inconsistencia", e)
                mostrarError("Error: ${e.message}")
            }
        }
    }

    private fun procesarRespuestaSalidas(response: String) {
        try {
            Log.d("SalidasAuditor", "Respuesta del servidor: ${response.take(500)}...") // Solo los primeros 500 caracteres

            val jsonResponse = JSONObject(response)
            if (jsonResponse.getBoolean("success")) {
                val data = jsonResponse.getJSONObject("data")
                val salidasArray = data.getJSONArray("salidas")
                val estadisticas = data.getJSONObject("estadisticas")

                Log.d("SalidasAuditor", "Array de salidas tiene ${salidasArray.length()} elementos")

                salidasList.clear()
                for (i in 0 until salidasArray.length()) {
                    val salidaJson = salidasArray.getJSONObject(i)
                    Log.d("SalidasAuditor", "Procesando elemento $i: ${salidaJson.toString()}")
                    val salida = parsearSalida(salidaJson)
                    salidasList.add(salida)
                    Log.d("SalidasAuditor", "Salida agregada: ${salida.numeroSalida} - ${salida.tipo}")
                }

                Log.d("SalidasAuditor", "Procesadas ${salidasList.size} salidas")

                // Actualizar el adaptador en el hilo principal
                runOnUiThread {
                    Log.d("SalidasAuditor", "Iniciando actualización del adaptador en hilo principal")
                    Log.d("SalidasAuditor", "RecyclerView es null: ${recyclerViewSalidas == null}")
                    Log.d("SalidasAuditor", "Adapter es null: ${salidasAdapter == null}")
                    Log.d("SalidasAuditor", "Lista de salidas tamaño: ${salidasList.size}")

                    // Verificar estado del RecyclerView
                    Log.d("SalidasAuditor", "RecyclerView visibility antes: ${recyclerViewSalidas.visibility}")
                    Log.d("SalidasAuditor", "RecyclerView height: ${recyclerViewSalidas.height}")
                    Log.d("SalidasAuditor", "RecyclerView width: ${recyclerViewSalidas.width}")

                    salidasAdapter.updateSalidas(salidasList)
                    Log.d("SalidasAuditor", "Adaptador actualizado en hilo principal")

                    // Verificar estado después de la actualización
                    Log.d("SalidasAuditor", "Después de updateSalidas - itemCount: ${salidasAdapter.itemCount}")

                    // Asegurar que el RecyclerView sea visible si hay datos
                    if (salidasList.isNotEmpty()) {
                        recyclerViewSalidas.visibility = View.VISIBLE
                        Log.d("SalidasAuditor", "RecyclerView hecho visible con ${salidasList.size} elementos")
                    } else {
                        recyclerViewSalidas.visibility = View.GONE
                        Log.d("SalidasAuditor", "RecyclerView oculto - lista vacía")
                    }

                    Log.d("SalidasAuditor", "RecyclerView visibility después: ${recyclerViewSalidas.visibility}")
                }

                actualizarEstadisticas(estadisticas)

                // Mostrar mensaje si no hay resultados (mantener por compatibilidad)
                if (salidasList.isEmpty()) {
                    runOnUiThread {
                        textViewEstadisticas.text = "❌ No se encontraron salidas con los filtros aplicados"
                        Log.d("SalidasAuditor", "Mensaje de lista vacía mostrado")
                    }
                }
            } else {
                val message = jsonResponse.optString("message", "Error desconocido")
                Log.e("SalidasAuditor", "Error del servidor: $message")
                mostrarError(message)
            }
        } catch (e: Exception) {
            Log.e("SalidasAuditor", "Error procesando respuesta: ${e.message}", e)
            Log.e("SalidasAuditor", "Respuesta que causó error: $response")
            mostrarError("Error procesando datos: ${e.message}")
        }
    }

    private fun parsearSalida(salidaJson: JSONObject): Salida {
        // Log para debug
        Log.d("SalidasAuditor", "Parseando salida: ${salidaJson.toString()}")

        val id = salidaJson.getString("_id")
        val numeroSalida = if (salidaJson.has("numero_salida") && !salidaJson.isNull("numero_salida")) {
            salidaJson.getString("numero_salida")
        } else {
            "SAL-${id.takeLast(6).uppercase()}"
        }

        return Salida(
            id = id,
            numeroSalida = numeroSalida,
            nombrePerfume = salidaJson.optString("nombre_perfume", "N/A"),
            cantidad = salidaJson.optInt("cantidad", 0),
            tipo = salidaJson.optString("tipo", "N/A"),
            almacenSalida = salidaJson.optString("almacen_salida", "N/A"),
            fechaSalida = salidaJson.optString("fecha_salida", salidaJson.optString("updated_at", "")),
            usuarioRegistro = salidaJson.optString("usuario_registro", "N/A"),
            motivo = salidaJson.optString("motivo", ""),
            estatusAuditoria = salidaJson.optString("estatus_auditoria", "pendiente"),
            // Campos específicos por tipo (con valores por defecto si no existen)
            precioUnitario = if (salidaJson.has("precio_unitario")) salidaJson.getDouble("precio_unitario") else null,
            precioTotal = if (salidaJson.has("precio_total")) salidaJson.getDouble("precio_total") else null,
            cliente = salidaJson.optString("cliente", ""),
            numeroFactura = salidaJson.optString("numero_factura", ""),
            descripcionMerma = salidaJson.optString("descripcion_merma", ""),
            auditorPor = salidaJson.optString("auditado_por", ""),
            fechaAuditoria = salidaJson.optString("fecha_auditoria", ""),
            observacionesAuditor = salidaJson.optString("observaciones_auditor", "")
        )
    }

    private fun actualizarEstadisticas(estadisticas: JSONObject) {
        val texto = buildString {
            append("📊 Estadísticas:\n")
            append("Total: ${estadisticas.optInt("total_salidas", 0)} | ")
            append("Ventas: ${estadisticas.optInt("total_ventas", 0)} | ")
            append("Mermas: ${estadisticas.optInt("total_mermas", 0)}\n")
            append("Pendientes: ${estadisticas.optInt("pendientes_auditoria", 0)} | ")
            append("Auditadas: ${estadisticas.optInt("auditadas", 0)} | ")
            append("Inconsistencias: ${estadisticas.optInt("con_inconsistencias", 0)}")

            val valorTotal = estadisticas.optDouble("valor_total_ventas", 0.0)
            if (valorTotal > 0) {
                append("\nValor total ventas: $${String.format("%.2f", valorTotal)}")
            }
        }
        textViewEstadisticas.text = texto
    }

    private fun mostrarCarga(mostrar: Boolean) {
        progressBarCarga.visibility = if (mostrar) View.VISIBLE else View.GONE
        // Solo ocultar el RecyclerView durante la carga, no forzar la visibilidad al terminar
        if (mostrar) {
            recyclerViewSalidas.visibility = View.GONE
        }
        // La visibilidad se manejará en procesarRespuestaSalidas()
    }

    private fun mostrarError(mensaje: String) {
        runOnUiThread {
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
        }
    }

    private fun formatearFecha(fechaISO: String): String {
        return try {
            // Manejar diferentes formatos de fecha
            val inputFormat = when {
                fechaISO.contains("T") && fechaISO.contains("Z") ->
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                fechaISO.contains("T") && fechaISO.contains("+") ->
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
                fechaISO.contains("T") ->
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
                else ->
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            }
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(fechaISO)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            // Fallback: formateo manual si el parsing falla
            try {
                val partes = fechaISO.split("T")
                if (partes.size >= 2) {
                    val fecha = partes[0] // YYYY-MM-DD
                    val hora = partes[1].split(".")[0] // HH:mm:ss
                    val fechaPartes = fecha.split("-")
                    "${fechaPartes[2]}/${fechaPartes[1]}/${fechaPartes[0]} ${hora.substring(0, 5)}"
                } else {
                    fechaISO
                }
            } catch (e2: Exception) {
                fechaISO
            }
        }
    }

    // Métodos de red
    private fun realizarPeticionGET(urlString: String): String? {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $authToken")
            connection.setRequestProperty("Content-Type", "application/json")

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()
                response
            } else {
                Log.e("HTTP", "Error: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e("HTTP", "Error en petición GET", e)
            null
        }
    }

    private fun realizarPeticionPUT(urlString: String, body: String): String? {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"
            connection.setRequestProperty("Authorization", "Bearer $authToken")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            connection.outputStream.use { os ->
                os.write(body.toByteArray())
                os.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()
                response
            } else {
                Log.e("HTTP", "Error PUT: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e("HTTP", "Error en petición PUT", e)
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }
}
