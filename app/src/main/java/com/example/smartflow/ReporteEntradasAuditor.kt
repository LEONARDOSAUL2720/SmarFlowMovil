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
import android.webkit.WebSettings
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

class ReporteEntradasAuditor : AppCompatActivity() {

    // ============================================================================
    // COMPONENTES DE LA UI
    // ============================================================================
    private lateinit var etFechaDesde: Button
    private lateinit var etFechaHasta: Button
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
    private var reporteActual: ReporteData? = null
    private var pdfGenerado: File? = null

    // Variables de fechas
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Opciones de filtros
    private val tiposReporte = arrayOf("Reporte Completo", "Resumen Ejecutivo", "Enfoque Auditoría")
    private val tiposReporteValues = arrayOf("completo", "ejecutivo", "auditoria")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reporte_pdf_auditor)

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

        Log.d("ReportePDF", "✅ Actividad PDF inicializada correctamente")
    }

    // ============================================================================
    // INICIALIZACIÓN DE COMPONENTES
    // ============================================================================
    private fun initializeComponents() {
        etFechaDesde = findViewById(R.id.btn_fecha_desde_pdf)
        etFechaHasta = findViewById(R.id.btn_fecha_hasta_pdf)
        spinnerTipoReporte = findViewById(R.id.spinner_tipo_reporte_pdf)
        btnGenerarReporte = findViewById(R.id.btn_generar_reporte_pdf)
        btnDescargarPDF = findViewById(R.id.btn_descargar_pdf)
        btnCompartirPDF = findViewById(R.id.btn_compartir_pdf)
        webViewPreview = findViewById(R.id.webview_preview)
        progressBar = findViewById(R.id.progress_bar_pdf)
        layoutBotones = findViewById(R.id.layout_botones_pdf)
        tvEstadoReporte = findViewById(R.id.tv_estado_reporte)
        tvPlaceholderPreview = findViewById(R.id.tv_placeholder_preview)

        // Inicialmente ocultar botones y WebView
        layoutBotones.visibility = View.GONE
        webViewPreview.visibility = View.GONE
        tvPlaceholderPreview.visibility = View.VISIBLE
        tvEstadoReporte.text = "Seleccione fechas y genere el reporte"
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

            // ✅ AÑADIR ESTAS LÍNEAS PARA MEJOR SCROLL:
            layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
            setRenderPriority(WebSettings.RenderPriority.HIGH)
            cacheMode = WebSettings.LOAD_NO_CACHE
        }

        webViewPreview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("ReportePDF", "📄 Preview cargado exitosamente")
            }
        }

        // ✅ AÑADIR CONFIGURACIÓN ADICIONAL DE SCROLL:
        webViewPreview.isVerticalScrollBarEnabled = true
        webViewPreview.isHorizontalScrollBarEnabled = true
        webViewPreview.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
    }

    private fun setupDatePickers() {
        etFechaDesde.setOnClickListener { showDatePicker(true) }
        etFechaHasta.setOnClickListener { showDatePicker(false) }
    }

    private fun setupSpinners() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposReporte)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoReporte.adapter = adapter
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

                Log.d("ReportePDF", "📅 Fecha seleccionada: $formattedDate")
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

        Log.d("ReportePDF", "🚀 Iniciando generación de reporte PDF...")

        // UI Loading State
        progressBar.visibility = View.VISIBLE
        btnGenerarReporte.isEnabled = false
        btnGenerarReporte.text = "Generando..."
        webViewPreview.visibility = View.GONE
        tvPlaceholderPreview.visibility = View.VISIBLE
        layoutBotones.visibility = View.GONE
        tvEstadoReporte.text = "Obteniendo datos del servidor..."

        // Preparar parámetros
        val fechaDesde = convertToApiDate(etFechaDesde.text.toString())
        val fechaHasta = convertToApiDate(etFechaHasta.text.toString())
        val tipoReporte = tiposReporteValues[spinnerTipoReporte.selectedItemPosition]

        Log.d("ReportePDF", "📊 Parámetros: desde=$fechaDesde, hasta=$fechaHasta, tipo=$tipoReporte")

        // Llamada a la API
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val resultado = llamarApiReporte(fechaDesde, fechaHasta, tipoReporte, true)

                withContext(Dispatchers.Main) {
                    if (resultado.success) {
                        tvEstadoReporte.text = "Generando vista previa del PDF..."
                        procesarRespuestaYGenerarPreview(resultado.data)
                    } else {
                        Toast.makeText(this@ReporteEntradasAuditor, "❌ Error: ${resultado.message}", Toast.LENGTH_LONG).show()
                        tvEstadoReporte.text = "Error al generar el reporte"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ReportePDF", "❌ Error generando reporte", e)
                    Toast.makeText(this@ReporteEntradasAuditor, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                    tvEstadoReporte.text = "Error de conexión"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnGenerarReporte.isEnabled = true
                    btnGenerarReporte.text = "Generar Reporte"
                }
            }
        }
    }

    private suspend fun llamarApiReporte(
        fechaDesde: String,
        fechaHasta: String,
        tipoReporte: String,
        incluirRechazadas: Boolean
    ): ApiResponse {

        return withContext(Dispatchers.IO) {
            try {
                val url = HttpUrl.Builder()
                    .scheme("https")
                    .host("smartflow-mwmm.onrender.com")
                    .addPathSegments("api/auditor/reportes/entradas")
                    .addQueryParameter("fecha_desde", fechaDesde)
                    .addQueryParameter("fecha_hasta", fechaHasta)
                    .addQueryParameter("tipo_reporte", tipoReporte)
                    .addQueryParameter("incluir_rechazadas", incluirRechazadas.toString())
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $authToken")
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                Log.d("ReportePDF", "🔍 Response Code: ${response.code}")
                Log.d("ReportePDF", "🔍 Response Headers: ${response.headers}")
                Log.d("ReportePDF", "🔍 Response Body Length: ${responseBody.length}")
                Log.d("ReportePDF", "🔍 Response Body Preview: ${responseBody.take(500)}...")

                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(responseBody)
                    Log.d("ReportePDF", "🔍 JSON Response Keys: ${jsonResponse.keys().asSequence().toList()}")

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
                Log.e("ReportePDF", "❌ Error en llamada API", e)
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
            Log.e("ReportePDF", "❌ Datos del reporte nulos")
            tvEstadoReporte.text = "Error: Sin datos para mostrar"
            return
        }

        try {
            // Debug: Log the complete response structure
            Log.d("ReportePDF", "🔍 Estructura completa de respuesta: ${data.toString()}")
            Log.d("ReportePDF", "🔍 Claves principales: ${data.keys().asSequence().toList()}")

            // Verificar que existen las claves necesarias
            if (!data.has("estadisticas")) {
                Log.e("ReportePDF", "❌ Falta clave 'estadisticas' en respuesta")
                tvEstadoReporte.text = "Error: Estructura de datos incorrecta"
                return
            }

            if (!data.has("entradas")) {
                Log.e("ReportePDF", "❌ Falta clave 'entradas' en respuesta")
                tvEstadoReporte.text = "Error: Sin datos de entradas"
                return
            }

            val entradas = data.getJSONArray("entradas")
            Log.d("ReportePDF", "🔍 Número de entradas recibidas: ${entradas.length()}")

            if (entradas.length() > 0) {
                val primeraEntrada = entradas.getJSONObject(0)
                Log.d("ReportePDF", "🔍 Primera entrada completa: ${primeraEntrada.toString()}")
            }

            // Guardar datos del reporte
            reporteActual = ReporteData(
                estadisticas = data.getJSONObject("estadisticas"),
                entradas = entradas,
                alertas = data.optJSONArray("alertas"),
                metadata = data.getJSONObject("metadata")
            )

            // Generar HTML para preview
            val htmlContent = generarHTMLReporte(reporteActual!!)

            // Mostrar en WebView y ocultar placeholder
            webViewPreview.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            webViewPreview.visibility = View.VISIBLE
            tvPlaceholderPreview.visibility = View.GONE
            layoutBotones.visibility = View.VISIBLE
            tvEstadoReporte.text = "✅ Reporte generado - Listo para descargar"

            Log.d("ReportePDF", "✅ Preview generado exitosamente")

        } catch (e: Exception) {
            Log.e("ReportePDF", "❌ Error procesando respuesta", e)
            tvEstadoReporte.text = "Error procesando los datos"
            Toast.makeText(this, "Error procesando los datos del reporte", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generarHTMLReporte(reporte: ReporteData): String {
        val estadisticas = reporte.estadisticas
        val entradas = reporte.entradas
        val metadata = reporte.metadata

        val resumenGeneral = estadisticas.getJSONObject("resumen_general")
        val porEstatus = estadisticas.getJSONObject("por_estatus")
        val metricas = estadisticas.getJSONObject("metricas_financieras")
        val tendencias = estadisticas.getJSONObject("tendencias")

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Reporte de Entradas - SmartFlow</title>
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
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
                    border-left: 4px solid #667eea;
                }
                .stat-number {
                    font-size: 24px;
                    font-weight: bold;
                    color: #667eea;
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
                .status-validado { color: #28a745; font-weight: bold; }
                .status-rechazado { color: #dc3545; font-weight: bold; }
                .status-registrado { color: #ffc107; font-weight: bold; }
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
                    .header { background: #667eea !important; }
                }
            </style>
        </head>
        <body>
            <div class="header">
                <div class="logo">📊 SmartFlow - Reporte de Entradas</div>
                <div class="subtitle">Reporte Ejecutivo de Auditoría</div>
                <div class="subtitle">Periodo: ${resumenGeneral.getString("periodo")}</div>
            </div>

            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-number">${resumenGeneral.getInt("total_entradas")}</div>
                    <div class="stat-label">Total Entradas</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number">${porEstatus.getInt("validadas")}</div>
                    <div class="stat-label">Validadas</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number">${porEstatus.getInt("rechazadas")}</div>
                    <div class="stat-label">Rechazadas</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number">${porEstatus.getInt("pendientes")}</div>
                    <div class="stat-label">Pendientes</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number">$${String.format("%.2f", metricas.getDouble("valor_total_inventario"))}</div>
                    <div class="stat-label">Valor Total</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number">${tendencias.getString("porcentaje_aprobacion")}%</div>
                    <div class="stat-label">% Aprobación</div>
                </div>
            </div>

            <div class="section">
                <div class="section-title">📦 Detalle de Entradas</div>
                <table class="table">
                    <thead>
                        <tr>
                            <th>Número</th>
                            <th>Tipo</th>
                            <th>Perfume</th>
                            <th>Cantidad</th>
                            <th>Proveedor</th>
                            <th>Fecha</th>
                            <th>Estado</th>
                            <th>Valor</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${generarFilasEntradas(entradas)}
                    </tbody>
                </table>
            </div>

            <div class="footer">
                <p>Generado el ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}</p>
                <p>SmartFlow Auditor - Sistema de Gestión de Inventario</p>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    private fun generarFilasEntradas(entradas: JSONArray): String {
        val sb = StringBuilder()

        for (i in 0 until entradas.length()) {
            try {
                val entrada = entradas.getJSONObject(i)

                // Debug: Log the structure of the first entry
                if (i == 0) {
                    Log.d("ReportePDF", "🔍 Estructura de entrada: ${entrada.toString()}")
                    Log.d("ReportePDF", "🔍 Claves disponibles: ${entrada.keys().asSequence().toList()}")
                }

                val perfume = entrada.optJSONObject("perfume")
                val proveedor = entrada.optJSONObject("proveedor")
                val metricas = entrada.getJSONObject("metricas")

                // Usar método seguro para obtener estatus_validacion
                val estatusValidacion = entrada.optString("estatus_validacion", "registrado")
                val estadoClass = when (estatusValidacion) {
                    "validado" -> "status-validado"
                    "rechazado" -> "status-rechazado"
                    "registrado" -> "status-registrado"
                    else -> ""
                }

                sb.append("""
                    <tr>
                        <td>${entrada.optString("numero_entrada", "N/A")}</td>
                        <td>${entrada.optString("tipo", "N/A")}</td>
                        <td>${perfume?.optString("nombre", "N/A") ?: "N/A"}</td>
                        <td>${entrada.optInt("cantidad", 0)}</td>
                        <td>${proveedor?.optString("nombre", "N/A") ?: "N/A"}</td>
                        <td>${entrada.optString("fecha_entrada", "N/A").substring(0, minOf(10, entrada.optString("fecha_entrada", "N/A").length))}</td>
                        <td class="$estadoClass">${estatusValidacion.uppercase()}</td>
                        <td>$${String.format("%.2f", metricas.optDouble("valor_total_estimado", 0.0))}</td>
                    </tr>
                """.trimIndent())
            } catch (e: Exception) {
                Log.e("ReportePDF", "❌ Error procesando entrada $i", e)
                // Continuar con la siguiente entrada en caso de error
                continue
            }
        }

        return sb.toString()
    }

    // ============================================================================
    // GENERACIÓN Y DESCARGA DE PDF
    // ============================================================================
    private fun descargarPDF() {
        if (reporteActual == null) {
            Toast.makeText(this, "Debe generar un reporte primero", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("ReportePDF", "📄 Iniciando generación de PDF...")
        tvEstadoReporte.text = "Generando archivo PDF..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pdfFile = generarPDFDesdeHTML()

                withContext(Dispatchers.Main) {
                    if (pdfFile != null) {
                        pdfGenerado = pdfFile
                        abrirPDF(pdfFile)
                        tvEstadoReporte.text = "✅ PDF generado y guardado exitosamente"
                        Toast.makeText(this@ReporteEntradasAuditor, "PDF guardado en: ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
                    } else {
                        tvEstadoReporte.text = "❌ Error generando el PDF"
                        Toast.makeText(this@ReporteEntradasAuditor, "Error generando el PDF", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ReportePDF", "❌ Error generando PDF", e)
                    tvEstadoReporte.text = "❌ Error generando el PDF"
                    Toast.makeText(this@ReporteEntradasAuditor, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun generarPDFDesdeHTML(): File? {
        try {
            // Crear archivo PDF en el directorio de descargas
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "SmartFlow_Reporte_Entradas_$timestamp.pdf"

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val pdfFile = File(downloadsDir, filename)

            // Crear documento PDF
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)

            val canvas = page.canvas
            val paint = Paint()

            // Generar contenido del PDF
            generarContenidoPDF(canvas, paint)

            pdfDocument.finishPage(page)

            // Guardar archivo
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            Log.d("ReportePDF", "✅ PDF generado: ${pdfFile.absolutePath}")
            return pdfFile

        } catch (e: Exception) {
            Log.e("ReportePDF", "❌ Error generando PDF", e)
            return null
        }
    }

    private fun generarContenidoPDF(canvas: Canvas, paint: Paint) {
        val estadisticas = reporteActual?.estadisticas ?: return
        val resumenGeneral = estadisticas.getJSONObject("resumen_general")
        val porEstatus = estadisticas.getJSONObject("por_estatus")
        val metricas = estadisticas.getJSONObject("metricas_financieras")

        var yPosition = 50f

        // Título
        paint.textSize = 24f
        paint.color = Color.BLACK
        paint.isFakeBoldText = true
        canvas.drawText("SmartFlow - Reporte de Entradas", 50f, yPosition, paint)
        yPosition += 40f

        paint.textSize = 16f
        paint.isFakeBoldText = false
        canvas.drawText("Periodo: ${resumenGeneral.getString("periodo")}", 50f, yPosition, paint)
        yPosition += 60f

        // Estadísticas principales
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Resumen Ejecutivo", 50f, yPosition, paint)
        yPosition += 30f

        paint.textSize = 14f
        paint.isFakeBoldText = false

        val stats = arrayOf(
            "Total de Entradas: ${resumenGeneral.getInt("total_entradas")}",
            "Entradas Validadas: ${porEstatus.getInt("validadas")}",
            "Entradas Rechazadas: ${porEstatus.getInt("rechazadas")}",
            "Entradas Pendientes: ${porEstatus.getInt("pendientes")}",
            "Valor Total del Inventario: $${String.format("%.2f", metricas.getDouble("valor_total_inventario"))}",
            "Cantidad Total de Productos: ${metricas.getInt("cantidad_total_productos")}"
        )

        for (stat in stats) {
            canvas.drawText("• $stat", 70f, yPosition, paint)
            yPosition += 25f
        }

        yPosition += 20f

        // Detalle de entradas (primeras 10)
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Detalle de Entradas (Principales)", 50f, yPosition, paint)
        yPosition += 30f

        paint.textSize = 12f
        paint.isFakeBoldText = false

        val entradas = reporteActual?.entradas ?: return
        val maxEntradas = minOf(10, entradas.length())

        for (i in 0 until maxEntradas) {
            if (yPosition > 750) break // Evitar overflow de página

            try {
                val entrada = entradas.getJSONObject(i)
                val perfume = entrada.optJSONObject("perfume")
                val numeroEntrada = entrada.optString("numero_entrada", "N/A")
                val nombrePerfume = perfume?.optString("nombre", "N/A") ?: "N/A"
                val cantidad = entrada.optInt("cantidad", 0)
                val estatusValidacion = entrada.optString("estatus_validacion", "registrado")

                val texto = "$numeroEntrada - $nombrePerfume - $cantidad unidades - ${estatusValidacion.uppercase()}"

                canvas.drawText(texto, 70f, yPosition, paint)
                yPosition += 20f
            } catch (e: Exception) {
                Log.e("ReportePDF", "❌ Error procesando entrada $i para PDF", e)
                // Continuar con la siguiente entrada
                continue
            }
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
            Log.e("ReportePDF", "❌ Error abriendo PDF", e)
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
                putExtra(Intent.EXTRA_SUBJECT, "Reporte de Entradas - SmartFlow")
                putExtra(Intent.EXTRA_TEXT, "Adjunto el reporte de entradas generado desde SmartFlow Auditor")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(Intent.createChooser(intent, "Compartir PDF"))
        } catch (e: Exception) {
            Log.e("ReportePDF", "❌ Error compartiendo PDF", e)
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
            Log.e("ReportePDF", "❌ Error convirtiendo fecha", e)
            displayDate
        }
    }
}

// ============================================================================
// CLASES DE DATOS
// ============================================================================
data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: JSONObject?
)

data class ReporteData(
    val estadisticas: JSONObject,
    val entradas: JSONArray,
    val alertas: JSONArray?,
    val metadata: JSONObject
)
