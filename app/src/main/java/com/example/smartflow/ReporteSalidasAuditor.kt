package com.example.smartflow

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import com.example.smartflow.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ReporteSalidasAuditor : AppCompatActivity() {

    // ============================================================================
    // COMPONENTES DE LA UI
    // ============================================================================
    private lateinit var etFechaDesde: Button
    private lateinit var etFechaHasta: Button
    private lateinit var spinnerTipoSalida: Spinner
    private lateinit var spinnerTipoReporte: Spinner
    private lateinit var btnGenerarReporte: Button
    private lateinit var btnDescargarPDF: Button
    private lateinit var btnCompartirPDF: Button
    private lateinit var webViewPreview: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutBotones: LinearLayout
    private lateinit var tvEstadoReporte: TextView
    private lateinit var tvPlaceholderPreview: TextView

    // ============================================================================
    // VARIABLES DE DATOS
    // ============================================================================
    private val client = OkHttpClient()
    private var authToken: String = ""
    private var baseUrl: String = "https://smartflow-mwmm.onrender.com/api" // URL de producción
    private var reporteActual: ReporteSalidasData? = null
    private var pdfGenerado: File? = null

    // Variables de fechas
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Opciones de filtros
    private val tiposSalida = arrayOf("Todas las Salidas", "Solo Ventas", "Solo Mermas")
    private val tiposSalidaValues = arrayOf("todas", "venta", "merma")
    private val tiposReporte = arrayOf("Reporte Completo", "Resumen Ejecutivo", "Enfoque Auditoría")
    private val tiposReporteValues = arrayOf("completo", "ejecutivo", "auditoria")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reporte_pdf_auditor_salidas)

        // Obtener token de autenticación
        authToken = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getString("auth_token", "") ?: ""

        if (authToken.isEmpty()) {
            Toast.makeText(this, "Error de autenticación", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeComponents()
        setupWebView()
        setupDatePickers()
        setupSpinners()
        setupListeners()
        setupDefaultDates()

        Log.d("ReporteSalidasPDF", "✅ Actividad PDF de Salidas inicializada correctamente")
    }

    // ============================================================================
    // INICIALIZACIÓN DE COMPONENTES
    // ============================================================================
    private fun initializeComponents() {
        etFechaDesde = findViewById(R.id.btn_fecha_desde_salidas_pdf)
        etFechaHasta = findViewById(R.id.btn_fecha_hasta_salidas_pdf)
        spinnerTipoSalida = findViewById(R.id.spinner_tipo_salida_pdf)
        spinnerTipoReporte = findViewById(R.id.spinner_tipo_reporte_salidas_pdf)
        btnGenerarReporte = findViewById(R.id.btn_generar_reporte_salidas_pdf)
        btnDescargarPDF = findViewById(R.id.btn_descargar_salidas_pdf)
        btnCompartirPDF = findViewById(R.id.btn_compartir_salidas_pdf)
        webViewPreview = findViewById(R.id.webview_preview_salidas)
        progressBar = findViewById(R.id.progress_bar_salidas_pdf)
        layoutBotones = findViewById(R.id.layout_botones_salidas_pdf)
        tvEstadoReporte = findViewById(R.id.tv_estado_reporte_salidas)
        tvPlaceholderPreview = findViewById(R.id.tv_placeholder_preview_salidas)

        // Inicialmente ocultar botones y WebView
        layoutBotones.visibility = View.GONE
        webViewPreview.visibility = View.GONE
        tvPlaceholderPreview.visibility = View.VISIBLE
        tvEstadoReporte.text = "Seleccione fechas y genere el reporte de salidas"
    }

    private fun setupWebView() {
        webViewPreview.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }

        webViewPreview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("ReporteSalidasPDF", "📄 Preview de salidas cargado exitosamente")
            }
        }

        // Configuraciones adicionales para scroll
        webViewPreview.isVerticalScrollBarEnabled = true
        webViewPreview.isHorizontalScrollBarEnabled = true
        webViewPreview.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
    }

    private fun setupDatePickers() {
        etFechaDesde.setOnClickListener { showDatePicker(true) }
        etFechaHasta.setOnClickListener { showDatePicker(false) }
    }

    private fun setupSpinners() {
        // Spinner tipo de salida
        val adapterTipoSalida = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposSalida)
        adapterTipoSalida.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoSalida.adapter = adapterTipoSalida

        // Spinner tipo de reporte
        val adapterTipoReporte = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposReporte)
        adapterTipoReporte.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoReporte.adapter = adapterTipoReporte
    }

    private fun setupListeners() {
        btnGenerarReporte.setOnClickListener { generarReporte() }
        btnDescargarPDF.setOnClickListener { descargarPDF() }
        btnCompartirPDF.setOnClickListener { compartirPDF() }
    }

    private fun setupDefaultDates() {
        val hoy = Calendar.getInstance()
        etFechaHasta.text = displayDateFormat.format(hoy.time)

        val hace30Dias = Calendar.getInstance()
        hace30Dias.add(Calendar.DAY_OF_MONTH, -30)
        etFechaDesde.text = displayDateFormat.format(hace30Dias.time)
    }

    // ============================================================================
    // MANEJO DE FECHAS
    // ============================================================================
    private fun showDatePicker(isFechaDesde: Boolean) {
        val currentDate = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val formattedDate = displayDateFormat.format(selectedDate.time)

                if (isFechaDesde) {
                    etFechaDesde.text = formattedDate
                } else {
                    etFechaHasta.text = formattedDate
                }

                Log.d("ReporteSalidasPDF", "📅 Fecha seleccionada: $formattedDate")
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun validateDates(): Boolean {
        val fechaDesdeText = etFechaDesde.text.toString()
        val fechaHastaText = etFechaHasta.text.toString()

        if (fechaDesdeText.isEmpty() || fechaHastaText.isEmpty()) {
            Toast.makeText(this, "Debe seleccionar ambas fechas", Toast.LENGTH_SHORT).show()
            return false
        }

        try {
            val fechaDesde = displayDateFormat.parse(fechaDesdeText)
            val fechaHasta = displayDateFormat.parse(fechaHastaText)

            if (fechaDesde != null && fechaHasta != null && fechaDesde.after(fechaHasta)) {
                Toast.makeText(this, "La fecha desde no puede ser posterior a la fecha hasta", Toast.LENGTH_SHORT).show()
                return false
            }

            return true
        } catch (e: Exception) {
            Toast.makeText(this, "Formato de fecha inválido", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    // ============================================================================
    // GENERACIÓN DE REPORTES
    // ============================================================================
    private fun generarReporte() {
        if (!validateDates()) return

        Log.d("ReporteSalidasPDF", "🚀 Iniciando generación de reporte PDF de salidas...")

        // UI Loading State
        progressBar.visibility = View.VISIBLE
        btnGenerarReporte.isEnabled = false
        btnGenerarReporte.text = "Generando..."
        webViewPreview.visibility = View.GONE
        tvPlaceholderPreview.visibility = View.VISIBLE
        layoutBotones.visibility = View.GONE
        tvEstadoReporte.text = "Obteniendo datos de salidas del servidor..."

        // Preparar parámetros
        val fechaDesde = convertToApiDate(etFechaDesde.text.toString())
        val fechaHasta = convertToApiDate(etFechaHasta.text.toString())
        val tipoSalida = tiposSalidaValues[spinnerTipoSalida.selectedItemPosition]
        val tipoReporte = tiposReporteValues[spinnerTipoReporte.selectedItemPosition]

        Log.d("ReporteSalidasPDF", "📊 Parámetros: desde=$fechaDesde, hasta=$fechaHasta, tipo_salida=$tipoSalida, tipo_reporte=$tipoReporte")

        // Llamada a la API
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val resultado = llamarApiReporteSalidas(fechaDesde, fechaHasta, tipoSalida, tipoReporte)

                withContext(Dispatchers.Main) {
                    if (resultado.success) {
                        tvEstadoReporte.text = "Generando vista previa del PDF de salidas..."
                        procesarRespuestaYGenerarPreview(resultado.data)
                    } else {
                        Toast.makeText(this@ReporteSalidasAuditor, "❌ Error: ${resultado.message}", Toast.LENGTH_LONG).show()
                        tvEstadoReporte.text = "Error al generar el reporte de salidas"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ReporteSalidasPDF", "❌ Error generando reporte de salidas", e)
                    Toast.makeText(this@ReporteSalidasAuditor, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                    tvEstadoReporte.text = "Error de conexión"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnGenerarReporte.isEnabled = true
                    btnGenerarReporte.text = "🚀 Generar Reporte PDF de Salidas"
                }
            }
        }
    }

    private suspend fun llamarApiReporteSalidas(
        fechaDesde: String,
        fechaHasta: String,
        tipoSalida: String,
        tipoReporte: String
    ): ApiResponse {

        return withContext(Dispatchers.IO) {
            try {
                val url = HttpUrl.Builder()
                    .scheme("https")
                    .host("smartflow-mwmm.onrender.com")
                    .addPathSegments("api/auditor/reportes/salidas")
                    .addQueryParameter("fecha_desde", fechaDesde)
                    .addQueryParameter("fecha_hasta", fechaHasta)
                    .addQueryParameter("tipo_salida", tipoSalida)
                    .addQueryParameter("tipo_reporte", tipoReporte)
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $authToken")
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                Log.d("ReporteSalidasPDF", "🔍 Response Code: ${response.code}")
                Log.d("ReporteSalidasPDF", "🔍 Response Body Length: ${responseBody.length}")
                Log.d("ReporteSalidasPDF", "🔍 Response Body Preview: ${responseBody.take(500)}...")

                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(responseBody)
                    Log.d("ReporteSalidasPDF", "🔍 JSON Response Keys: ${jsonResponse.keys().asSequence().toList()}")

                    ApiResponse(
                        success = jsonResponse.getBoolean("success"),
                        message = jsonResponse.getString("message"),
                        data = jsonResponse.optJSONObject("data")
                    )
                } else {
                    val errorJson = JSONObject(responseBody)
                    ApiResponse(
                        success = false,
                        message = errorJson.optString("message", "Error desconocido"),
                        data = null
                    )
                }
            } catch (e: Exception) {
                Log.e("ReporteSalidasPDF", "❌ Error en llamada API de salidas", e)
                ApiResponse(
                    success = false,
                    message = "Error de conexión: ${e.message}",
                    data = null
                )
            }
        }
    }

    // ============================================================================
    // GENERACIÓN DE PREVIEW HTML
    // ============================================================================
    private fun procesarRespuestaYGenerarPreview(data: JSONObject?) {
        if (data == null) {
            Log.e("ReporteSalidasPDF", "❌ Datos del reporte de salidas nulos")
            tvEstadoReporte.text = "Error: Sin datos para mostrar"
            return
        }

        try {
            Log.d("ReporteSalidasPDF", "🔍 Estructura completa de respuesta: ${data.toString()}")
            Log.d("ReporteSalidasPDF", "🔍 Claves principales: ${data.keys().asSequence().toList()}")

            // Verificar que existen las claves necesarias
            if (!data.has("estadisticas")) {
                Log.e("ReporteSalidasPDF", "❌ Falta clave 'estadisticas' en respuesta")
                tvEstadoReporte.text = "Error: Estructura de datos incorrecta"
                return
            }

            if (!data.has("salidas")) {
                Log.e("ReporteSalidasPDF", "❌ Falta clave 'salidas' en respuesta")
                tvEstadoReporte.text = "Error: Sin datos de salidas"
                return
            }

            val salidas = data.getJSONArray("salidas")
            Log.d("ReporteSalidasPDF", "🔍 Número de salidas recibidas: ${salidas.length()}")

            // Guardar datos del reporte
            reporteActual = ReporteSalidasData(
                estadisticas = data.getJSONObject("estadisticas"),
                salidas = salidas,
                alertas = data.optJSONArray("alertas"),
                metadata = data.getJSONObject("metadata")
            )

            // Generar HTML para preview
            val htmlContent = generarHTMLReporteSalidas(reporteActual!!)

            // Mostrar en WebView
            webViewPreview.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            webViewPreview.visibility = View.VISIBLE
            tvPlaceholderPreview.visibility = View.GONE
            layoutBotones.visibility = View.VISIBLE
            tvEstadoReporte.text = "✅ Reporte de salidas generado - Listo para descargar"

            Log.d("ReporteSalidasPDF", "✅ Preview de salidas generado exitosamente")

        } catch (e: Exception) {
            Log.e("ReporteSalidasPDF", "❌ Error procesando respuesta de salidas", e)
            tvEstadoReporte.text = "Error procesando los datos de salidas"
            Toast.makeText(this, "Error procesando los datos del reporte de salidas", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generarHTMLReporteSalidas(reporte: ReporteSalidasData): String {
        try {
            val estadisticas = reporte.estadisticas
            val salidas = reporte.salidas
            val metadata = reporte.metadata

            // Información general
            val fechaDesde = metadata.optString("fecha_desde", "No especificado")
            val fechaHasta = metadata.optString("fecha_hasta", "No especificado")
            val tipoFiltro = metadata.optString("tipo_filtro", "Todos")

            // Estadísticas principales - adaptadas a la nueva estructura
            val totalSalidas = estadisticas.optInt("total_salidas", 0)
            val totalVentas = estadisticas.optInt("total_ventas", 0)
            val totalMermas = estadisticas.optInt("total_mermas", 0)
            val valorTotal = estadisticas.optDouble("valor_total_estimado", 0.0)
            val valorVentas = estadisticas.optDouble("valor_ventas", 0.0)
            val valorMermas = estadisticas.optDouble("valor_mermas", 0.0)

            // Métricas detalladas
            val metricas = estadisticas.optJSONArray("metricas") ?: JSONArray()

            // Análisis por usuarios
            val analisisUsuarios = estadisticas.optJSONArray("analisis_usuarios") ?: JSONArray()

            // Análisis por perfumes
            val analisisPerfumes = estadisticas.optJSONArray("analisis_perfumes") ?: JSONArray()

            return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Reporte de Salidas - SmartFlow</title>
            <style>
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    margin: 20px;
                    background-color: #f8f9fa;
                    color: #333;
                }
                .header {
                    text-align: center;
                    margin-bottom: 30px;
                    padding: 20px;
                    background: linear-gradient(135deg, #B497BD 0%, #9DBF9E 100%);
                    color: white;
                    border-radius: 10px;
                }
                .logo {
                    font-size: 28px;
                    font-weight: bold;
                    margin-bottom: 10px;
                }
                .subtitle {
                    font-size: 16px;
                    opacity: 0.9;
                }
                .stats-grid {
                    display: grid;
                    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                    gap: 15px;
                    margin-bottom: 30px;
                }
                .stat-card {
                    background: white;
                    padding: 20px;
                    border-radius: 8px;
                    text-align: center;
                    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    border-left: 4px solid #B497BD;
                }
                .stat-number {
                    font-size: 24px;
                    font-weight: bold;
                    color: #B497BD;
                    margin-bottom: 5px;
                }
                .stat-label {
                    font-size: 14px;
                    color: #666;
                    text-transform: uppercase;
                }
                .section {
                    background: white;
                    margin-bottom: 20px;
                    padding: 20px;
                    border-radius: 8px;
                    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                }
                .section-title {
                    font-size: 18px;
                    font-weight: bold;
                    margin-bottom: 15px;
                    color: #333;
                    border-bottom: 2px solid #eee;
                    padding-bottom: 10px;
                }
                .table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-top: 10px;
                }
                .table th, .table td {
                    padding: 12px;
                    text-align: left;
                    border-bottom: 1px solid #eee;
                }
                .table th {
                    background-color: #f8f9fa;
                    font-weight: bold;
                    color: #495057;
                }
                .tipo-venta { color: #28a745; font-weight: bold; }
                .tipo-merma { color: #dc3545; font-weight: bold; }
                .footer {
                    text-align: center;
                    margin-top: 30px;
                    padding: 20px;
                    background-color: #e9ecef;
                    border-radius: 8px;
                    font-size: 12px;
                    color: #666;
                }
                @media print {
                    body { margin: 0; background: white; }
                    .header { background: #B497BD !important; }
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>� REPORTE DE SALIDAS</h1>
                    <div class="subtitle">Sistema de Gestión SmartFlow</div>
                    <div class="subtitle">Análisis detallado de salidas de inventario</div>
                </div>
                
                <div class="info-section">
                    <h3>📅 Información del Reporte</h3>
                    <p><strong>Período:</strong> ${fechaDesde} - ${fechaHasta}</p>
                    <p><strong>Filtro aplicado:</strong> ${tipoFiltro}</p>
                    <p><strong>Fecha de generación:</strong> ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}</p>
                </div>
                
                <div class="stats-grid">
                    <div class="stat-card">
                        <h3>Total Salidas</h3>
                        <div class="value">${totalSalidas}</div>
                    </div>
                    <div class="stat-card">
                        <h3>Ventas</h3>
                        <div class="value">${totalVentas}</div>
                    </div>
                    <div class="stat-card">
                        <h3>Mermas</h3>
                        <div class="value">${totalMermas}</div>
                    </div>
                    <div class="stat-card">
                        <h3>Valor Total</h3>
                        <div class="value">$${String.format("%.2f", valorTotal)}</div>
                    </div>
                </div>
                
                <div class="highlight-box">
                    <strong>💰 Resumen Financiero:</strong> 
                    Ventas: $${String.format("%.2f", valorVentas)} | 
                    Mermas: $${String.format("%.2f", valorMermas)} | 
                    Eficiencia: ${if (valorTotal > 0) String.format("%.1f", (valorVentas / valorTotal) * 100) else "0"}%
                </div>
                
                <h2 class="section-title">📦 Detalle por Perfumes</h2>
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>Perfume</th>
                                <th>Categoría</th>
                                <th>Total Salidas</th>
                                <th>Ventas</th>
                                <th>Mermas</th>
                                <th>Valor Total</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${generatePerfumeRows(metricas)}
                        </tbody>
                    </table>
                </div>
                
                <h2 class="section-title">� Análisis por Usuarios</h2>
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>Usuario</th>
                                <th>Email</th>
                                <th>Total Salidas</th>
                                <th>Ventas</th>
                                <th>Mermas</th>
                                <th>Valor Generado</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${generateUsuarioRows(analisisUsuarios)}
                        </tbody>
                    </table>
                </div>
                
                <h2 class="section-title">🏪 Análisis por Categorías</h2>
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>Categoría</th>
                                <th>Cantidad Perfumes</th>
                                <th>Total Salidas</th>
                                <th>Ventas</th>
                                <th>Mermas</th>
                                <th>Valor Total</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${generateCategoriaRows(analisisPerfumes)}
                        </tbody>
                    </table>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()

        } catch (e: Exception) {
            Log.e("ReporteSalidasAuditor", "Error generando HTML: ${e.message}")
            return generateErrorHTML("Error al generar el reporte: ${e.message}")
        }
    }

    private fun generarFilasSalidas(salidas: JSONArray): String {
        val sb = StringBuilder()

        for (i in 0 until salidas.length()) {
            try {
                val salida = salidas.getJSONObject(i)

                if (i == 0) {
                    Log.d("ReporteSalidasPDF", "🔍 Estructura de salida: ${salida.toString()}")
                }

                val metricas = salida.getJSONObject("metricas")
                val tipo = salida.optString("tipo", "Venta")
                val tipoClass = when (tipo) {
                    "Venta" -> "tipo-venta"
                    "Merma" -> "tipo-merma"
                    else -> ""
                }

                sb.append("""
                    <tr>
                        <td>${salida.optString("nombre_perfume", "N/A")}</td>
                        <td>${salida.optString("almacen_salida", "N/A")}</td>
                        <td>${salida.optInt("cantidad", 0)}</td>
                        <td class="$tipoClass">${tipo.uppercase()}</td>
                        <td>${salida.optString("fecha_salida", "N/A").substring(0, minOf(10, salida.optString("fecha_salida", "N/A").length))}</td>
                        <td>$${String.format("%.2f", metricas.optDouble("valor_total_estimado", 0.0))}</td>
                    </tr>
                """.trimIndent())
            } catch (e: Exception) {
                Log.e("ReporteSalidasPDF", "❌ Error procesando salida $i", e)
                continue
            }
        }

        return sb.toString()
    }

    // ============================================================================
    // GENERACIÓN Y DESCARGA DE PDF (Similar a entradas pero adaptado)
    // ============================================================================
    private fun descargarPDF() {
        if (reporteActual == null) {
            Toast.makeText(this, "Debe generar un reporte primero", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("ReporteSalidasPDF", "📄 Iniciando generación de PDF de salidas...")
        tvEstadoReporte.text = "Generando archivo PDF de salidas..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pdfFile = generarPDFDesdeSalidas()

                withContext(Dispatchers.Main) {
                    if (pdfFile != null) {
                        pdfGenerado = pdfFile
                        abrirPDF(pdfFile)
                        tvEstadoReporte.text = "✅ PDF de salidas generado y guardado exitosamente"
                        Toast.makeText(this@ReporteSalidasAuditor, "PDF guardado en: ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
                    } else {
                        tvEstadoReporte.text = "❌ Error generando el PDF de salidas"
                        Toast.makeText(this@ReporteSalidasAuditor, "Error generando el PDF", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ReporteSalidasPDF", "❌ Error generando PDF de salidas", e)
                    tvEstadoReporte.text = "❌ Error generando el PDF de salidas"
                    Toast.makeText(this@ReporteSalidasAuditor, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun generarPDFDesdeSalidas(): File? {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "SmartFlow_Reporte_Salidas_$timestamp.pdf"

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val pdfFile = File(downloadsDir, filename)

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)

            val canvas = page.canvas
            val paint = Paint()

            generarContenidoPDFSalidas(canvas, paint)

            pdfDocument.finishPage(page)

            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            Log.d("ReporteSalidasPDF", "✅ PDF de salidas generado: ${pdfFile.absolutePath}")
            return pdfFile

        } catch (e: Exception) {
            Log.e("ReporteSalidasPDF", "❌ Error generando PDF de salidas", e)
            return null
        }
    }

    private fun generarContenidoPDFSalidas(canvas: Canvas, paint: Paint) {
        val estadisticas = reporteActual?.estadisticas ?: return
        val resumenGeneral = estadisticas.getJSONObject("resumen_general")
        val porTipo = estadisticas.getJSONObject("por_tipo")
        val metricas = estadisticas.getJSONObject("metricas_financieras")

        var yPosition = 50f

        // Título
        paint.textSize = 24f
        paint.color = Color.BLACK
        paint.isFakeBoldText = true
        canvas.drawText("SmartFlow - Reporte de Salidas", 50f, yPosition, paint)
        yPosition += 40f

        paint.textSize = 16f
        paint.isFakeBoldText = false
        canvas.drawText("Periodo: ${resumenGeneral.getString("periodo")}", 50f, yPosition, paint)
        yPosition += 60f

        // Estadísticas principales
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Resumen Ejecutivo de Salidas", 50f, yPosition, paint)
        yPosition += 30f

        paint.textSize = 14f
        paint.isFakeBoldText = false

        val stats = arrayOf(
            "Total de Salidas: ${resumenGeneral.getInt("total_salidas")}",
            "Ventas: ${porTipo.getInt("ventas")}",
            "Mermas: ${porTipo.getInt("mermas")}",
            "Valor Total Ventas: $${String.format("%.2f", metricas.getDouble("valor_total_ventas"))}",
            "Valor Total Mermas: $${String.format("%.2f", metricas.getDouble("valor_total_mermas"))}",
            "Cantidad Total: ${metricas.getInt("cantidad_total_productos")}"
        )

        for (stat in stats) {
            canvas.drawText("• $stat", 70f, yPosition, paint)
            yPosition += 25f
        }

        // Footer
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText("Generado el ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}", 50f, 800f, paint)
        canvas.drawText("SmartFlow Auditor - Sistema de Gestión de Inventario", 50f, 820f, paint)
    }

    private fun abrirPDF(pdfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", pdfFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("ReporteSalidasPDF", "❌ Error abriendo PDF de salidas", e)
            Toast.makeText(this, "No se encontró una aplicación para abrir PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun compartirPDF() {
        val archivo = pdfGenerado
        if (archivo == null) {
            Toast.makeText(this, "Debe generar el PDF primero", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", archivo)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Reporte de Salidas - SmartFlow")
                putExtra(Intent.EXTRA_TEXT, "Adjunto el reporte de salidas generado desde SmartFlow Auditor")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(Intent.createChooser(intent, "Compartir PDF"))
        } catch (e: Exception) {
            Log.e("ReporteSalidasPDF", "❌ Error compartiendo PDF de salidas", e)
            Toast.makeText(this, "Error al compartir el PDF", Toast.LENGTH_SHORT).show()
        }
    }

    // ============================================================================
    // FUNCIONES AUXILIARES
    // ============================================================================
    private fun convertToApiDate(displayDate: String): String {
        return try {
            val date = displayDateFormat.parse(displayDate)
            dateFormat.format(date!!)
        } catch (e: Exception) {
            Log.e("ReporteSalidasPDF", "❌ Error convirtiendo fecha", e)
            displayDate
        }
    }

    // Función auxiliar para generar filas de perfumes
    private fun generatePerfumeRows(metricas: JSONArray): String {
        val rows = StringBuilder()

        for (i in 0 until metricas.length()) {
            try {
                val metrica = metricas.getJSONObject(i)
                val perfume = metrica.optString("perfume", "Sin especificar")
                val categoria = metrica.optString("categoria_perfume", "Sin categoría")
                val totalSalidas = metrica.optInt("total_salidas", 0)
                val totalVentas = metrica.optInt("total_ventas", 0)
                val totalMermas = metrica.optInt("total_mermas", 0)
                val valorTotal = metrica.optDouble("valor_total_estimado", 0.0)

                rows.append("""
                    <tr>
                        <td><strong>${perfume}</strong></td>
                        <td><span class="badge" style="background: #e3f2fd; color: #1976d2;">${categoria}</span></td>
                        <td>${totalSalidas}</td>
                        <td><span class="badge badge-venta">${totalVentas}</span></td>
                        <td><span class="badge badge-merma">${totalMermas}</span></td>
                        <td class="money">$${String.format("%.2f", valorTotal)}</td>
                    </tr>
                """.trimIndent())
            } catch (e: Exception) {
                Log.w("ReporteSalidasAuditor", "Error procesando métrica $i: ${e.message}")
            }
        }

        return if (rows.isEmpty()) {
            "<tr><td colspan='6' style='text-align: center; color: #666; font-style: italic;'>No hay datos de perfumes disponibles</td></tr>"
        } else {
            rows.toString()
        }
    }

    // Función auxiliar para generar filas de usuarios
    private fun generateUsuarioRows(analisisUsuarios: JSONArray): String {
        val rows = StringBuilder()

        for (i in 0 until analisisUsuarios.length()) {
            try {
                val usuario = analisisUsuarios.getJSONObject(i)
                val nombre = usuario.optString("nombre_usuario", "Usuario desconocido")
                val email = usuario.optString("email_usuario", "Sin email")
                val totalSalidas = usuario.optInt("total_salidas", 0)
                val totalVentas = usuario.optInt("total_ventas", 0)
                val totalMermas = usuario.optInt("total_mermas", 0)
                val valorTotal = usuario.optDouble("valor_total_estimado", 0.0)

                rows.append("""
                    <tr>
                        <td><strong>${nombre}</strong></td>
                        <td style="color: #666;">${email}</td>
                        <td>${totalSalidas}</td>
                        <td><span class="badge badge-venta">${totalVentas}</span></td>
                        <td><span class="badge badge-merma">${totalMermas}</span></td>
                        <td class="money">$${String.format("%.2f", valorTotal)}</td>
                    </tr>
                """.trimIndent())
            } catch (e: Exception) {
                Log.w("ReporteSalidasAuditor", "Error procesando usuario $i: ${e.message}")
            }
        }

        return if (rows.isEmpty()) {
            "<tr><td colspan='6' style='text-align: center; color: #666; font-style: italic;'>No hay datos de usuarios disponibles</td></tr>"
        } else {
            rows.toString()
        }
    }

    // Función auxiliar para generar filas de categorías
    private fun generateCategoriaRows(analisisPerfumes: JSONArray): String {
        val categorias = mutableMapOf<String, MutableMap<String, Any>>()

        // Agrupar por categorías
        for (i in 0 until analisisPerfumes.length()) {
            try {
                val perfume = analisisPerfumes.getJSONObject(i)
                val categoria = perfume.optString("categoria", "Sin categoría")
                val totalSalidas = perfume.optInt("total_salidas", 0)
                val totalVentas = perfume.optInt("total_ventas", 0)
                val totalMermas = perfume.optInt("total_mermas", 0)
                val valorTotal = perfume.optDouble("valor_total_estimado", 0.0)

                if (!categorias.containsKey(categoria)) {
                    categorias[categoria] = mutableMapOf(
                        "cantidad_perfumes" to 0,
                        "total_salidas" to 0,
                        "total_ventas" to 0,
                        "total_mermas" to 0,
                        "valor_total" to 0.0
                    )
                }

                val cat = categorias[categoria]!!
                cat["cantidad_perfumes"] = (cat["cantidad_perfumes"] as Int) + 1
                cat["total_salidas"] = (cat["total_salidas"] as Int) + totalSalidas
                cat["total_ventas"] = (cat["total_ventas"] as Int) + totalVentas
                cat["total_mermas"] = (cat["total_mermas"] as Int) + totalMermas
                cat["valor_total"] = (cat["valor_total"] as Double) + valorTotal

            } catch (e: Exception) {
                Log.w("ReporteSalidasAuditor", "Error procesando perfume para categoría $i: ${e.message}")
            }
        }

        val rows = StringBuilder()
        for ((categoria, datos) in categorias) {
            rows.append("""
                <tr>
                    <td><strong>${categoria}</strong></td>
                    <td>${datos["cantidad_perfumes"]}</td>
                    <td>${datos["total_salidas"]}</td>
                    <td><span class="badge badge-venta">${datos["total_ventas"]}</span></td>
                    <td><span class="badge badge-merma">${datos["total_mermas"]}</span></td>
                    <td class="money">$${String.format("%.2f", datos["valor_total"] as Double)}</td>
                </tr>
            """.trimIndent())
        }

        return if (rows.isEmpty()) {
            "<tr><td colspan='6' style='text-align: center; color: #666; font-style: italic;'>No hay datos de categorías disponibles</td></tr>"
        } else {
            rows.toString()
        }
    }

    // Función auxiliar para manejar errores en HTML
    private fun generateErrorHTML(mensaje: String): String {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>Error - Reporte SmartFlow</title>
            <style>
                body { font-family: Arial, sans-serif; padding: 20px; }
                .error { color: #dc3545; background: #f8d7da; padding: 20px; border-radius: 5px; }
            </style>
        </head>
        <body>
            <div class="error">
                <h2>❌ Error al generar reporte</h2>
                <p>${mensaje}</p>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    // Función auxiliar para generar filas de salidas mejoradas
    private fun generarFilasSalidasMejoradas(salidas: JSONArray): String {
        val sb = StringBuilder()

        for (i in 0 until salidas.length()) {
            try {
                val salida = salidas.getJSONObject(i)

                val perfume = salida.optString("nombre_perfume", "Sin especificar")
                val almacen = salida.optString("almacen_salida", "N/A")
                val cantidad = salida.optInt("cantidad", 0)
                val tipo = salida.optString("tipo", "Venta")
                val fecha = salida.optString("fecha_salida", "N/A")
                val fechaFormateada = if (fecha.length >= 10) fecha.substring(0, 10) else fecha

                // Calcular valor estimado
                val valorEstimado = calcularValorEstimadoSimple(perfume, tipo, cantidad)

                val tipoClass = when (tipo) {
                    "Venta" -> "tipo-venta"
                    "Merma" -> "tipo-merma"
                    else -> ""
                }

                sb.append("""
                    <tr>
                        <td><strong>${perfume}</strong></td>
                        <td>${almacen}</td>
                        <td style="text-align: center;">${cantidad}</td>
                        <td><span class="$tipoClass">${tipo.uppercase()}</span></td>
                        <td>${fechaFormateada}</td>
                        <td class="money">$${String.format("%.2f", valorEstimado)}</td>
                    </tr>
                """.trimIndent())
            } catch (e: Exception) {
                Log.e("ReporteSalidasPDF", "❌ Error procesando salida $i", e)
                continue
            }
        }

        return if (sb.isEmpty()) {
            "<tr><td colspan='6' style='text-align: center; color: #666; font-style: italic;'>No hay salidas disponibles</td></tr>"
        } else {
            sb.toString()
        }
    }

    // Función auxiliar para calcular valor estimado simple
    private fun calcularValorEstimadoSimple(nombrePerfume: String, tipo: String, cantidad: Int): Double {
        val valoresEstimados = mapOf(
            "nautica voyage" to 850.0,
            "invictus victory" to 1200.0,
            "invictus" to 1100.0,
            "carolina herrera 212" to 950.0,
            "212" to 900.0
        )

        val perfumeLower = nombrePerfume.lowercase()
        var valorBase = 900.0 // valor por defecto

        for ((key, value) in valoresEstimados) {
            if (perfumeLower.contains(key)) {
                valorBase = value
                break
            }
        }

        // Ajustar valor por tipo (las mermas pueden tener menor valor de recuperación)
        if (tipo == "Merma") {
            valorBase *= 0.3 // Las mermas valen menos
        }

        return valorBase * cantidad
    }

    // Función auxiliar para generar análisis por perfume
    private fun generarAnalisisPorPerfume(salidas: JSONArray): String {
        val perfumeAnalisis = mutableMapOf<String, MutableMap<String, Any>>()

        // Procesar cada salida para agrupar por perfume
        for (i in 0 until salidas.length()) {
            try {
                val salida = salidas.getJSONObject(i)
                val perfume = salida.optString("nombre_perfume", "Sin especificar")
                val tipo = salida.optString("tipo", "Venta")
                val cantidad = salida.optInt("cantidad", 0)
                val valorEstimado = calcularValorEstimadoSimple(perfume, tipo, cantidad)

                if (!perfumeAnalisis.containsKey(perfume)) {
                    perfumeAnalisis[perfume] = mutableMapOf(
                        "total_salidas" to 0,
                        "total_ventas" to 0,
                        "total_mermas" to 0,
                        "valor_total" to 0.0,
                        "categoria" to categorizarPerfumeSimple(perfume)
                    )
                }

                val datos = perfumeAnalisis[perfume]!!
                datos["total_salidas"] = (datos["total_salidas"] as Int) + 1

                if (tipo == "Venta") {
                    datos["total_ventas"] = (datos["total_ventas"] as Int) + 1
                } else if (tipo == "Merma") {
                    datos["total_mermas"] = (datos["total_mermas"] as Int) + 1
                }

                datos["valor_total"] = (datos["valor_total"] as Double) + valorEstimado

            } catch (e: Exception) {
                Log.w("ReporteSalidasAuditor", "Error procesando salida para análisis de perfume $i: ${e.message}")
            }
        }

        if (perfumeAnalisis.isEmpty()) {
            return "<div class='section'><div class='section-title'>📦 Análisis por Perfumes</div><p>No hay datos de perfumes disponibles</p></div>"
        }

        val filas = StringBuilder()
        for ((perfume, datos) in perfumeAnalisis) {
            filas.append("""
                <tr>
                    <td><strong>${perfume}</strong></td>
                    <td><span style="background: #e3f2fd; color: #1976d2; padding: 4px 8px; border-radius: 4px;">${datos["categoria"]}</span></td>
                    <td style="text-align: center;">${datos["total_salidas"]}</td>
                    <td style="text-align: center;"><span class="tipo-venta">${datos["total_ventas"]}</span></td>
                    <td style="text-align: center;"><span class="tipo-merma">${datos["total_mermas"]}</span></td>
                    <td class="money">$${String.format("%.2f", datos["valor_total"] as Double)}</td>
                </tr>
            """.trimIndent())
        }

        return """
        <div class="section">
            <div class="section-title">📦 Análisis por Perfumes</div>
            <table class="table">
                <thead>
                    <tr>
                        <th>Perfume</th>
                        <th>Categoría</th>
                        <th>Total Salidas</th>
                        <th>Ventas</th>
                        <th>Mermas</th>
                        <th>Valor Total</th>
                    </tr>
                </thead>
                <tbody>
                    ${filas}
                </tbody>
            </table>
        </div>
        """.trimIndent()
    }

    // Función auxiliar para generar análisis por tipo
    private fun generarAnalisisPorTipo(salidas: JSONArray): String {
        var totalVentas = 0
        var totalMermas = 0
        var valorVentas = 0.0
        var valorMermas = 0.0

        for (i in 0 until salidas.length()) {
            try {
                val salida = salidas.getJSONObject(i)
                val tipo = salida.optString("tipo", "Venta")
                val perfume = salida.optString("nombre_perfume", "Sin especificar")
                val cantidad = salida.optInt("cantidad", 0)
                val valorEstimado = calcularValorEstimadoSimple(perfume, tipo, cantidad)

                if (tipo == "Venta") {
                    totalVentas++
                    valorVentas += valorEstimado
                } else if (tipo == "Merma") {
                    totalMermas++
                    valorMermas += valorEstimado
                }
            } catch (e: Exception) {
                Log.w("ReporteSalidasAuditor", "Error procesando salida para análisis de tipo $i: ${e.message}")
            }
        }

        val totalSalidas = totalVentas + totalMermas
        val porcentajeVentas = if (totalSalidas > 0) (totalVentas.toDouble() / totalSalidas * 100) else 0.0
        val porcentajeMermas = if (totalSalidas > 0) (totalMermas.toDouble() / totalSalidas * 100) else 0.0

        return """
        <div class="section">
            <div class="section-title">📊 Análisis por Tipo de Salida</div>
            <table class="table">
                <thead>
                    <tr>
                        <th>Tipo</th>
                        <th>Cantidad</th>
                        <th>Porcentaje</th>
                        <th>Valor Total</th>
                        <th>Valor Promedio</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td><span class="tipo-venta">VENTAS</span></td>
                        <td style="text-align: center;">${totalVentas}</td>
                        <td style="text-align: center;">${String.format("%.1f", porcentajeVentas)}%</td>
                        <td class="money">$${String.format("%.2f", valorVentas)}</td>
                        <td class="money">$${String.format("%.2f", if (totalVentas > 0) valorVentas / totalVentas else 0.0)}</td>
                    </tr>
                    <tr>
                        <td><span class="tipo-merma">MERMAS</span></td>
                        <td style="text-align: center;">${totalMermas}</td>
                        <td style="text-align: center;">${String.format("%.1f", porcentajeMermas)}%</td>
                        <td class="money">$${String.format("%.2f", valorMermas)}</td>
                        <td class="money">$${String.format("%.2f", if (totalMermas > 0) valorMermas / totalMermas else 0.0)}</td>
                    </tr>
                </tbody>
            </table>
        </div>
        """.trimIndent()
    }

    // Función auxiliar para categorizar perfumes simple
    private fun categorizarPerfumeSimple(nombrePerfume: String): String {
        val perfumeLower = nombrePerfume.lowercase()

        return when {
            perfumeLower.contains("nautica") -> "Deportivo"
            perfumeLower.contains("invictus") -> "Masculino Premium"
            perfumeLower.contains("carolina herrera") -> "Femenino Premium"
            perfumeLower.contains("212") -> "Urbano Moderno"
            else -> "Clásico"
        }
    }
}

// ============================================================================
// CLASES DE DATOS PARA SALIDAS
// ============================================================================
data class ReporteSalidasData(
    val estadisticas: JSONObject,
    val salidas: JSONArray,
    val alertas: JSONArray?,
    val metadata: JSONObject
)
