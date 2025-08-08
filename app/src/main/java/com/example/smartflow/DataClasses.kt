package com.example.smartflow

// Data class para representar una salida
data class Salida(
    val id: String,
    val numeroSalida: String,
    val nombrePerfume: String,
    val cantidad: Int,
    val tipo: String,
    val almacenSalida: String,
    val fechaSalida: String,
    val usuarioRegistro: String,
    val motivo: String?,
    val estatusAuditoria: String,
    // Campos específicos para ventas
    val precioUnitario: Double?,
    val precioTotal: Double?,
    val cliente: String?,
    val numeroFactura: String?,
    // Campos específicos para mermas
    val descripcionMerma: String?,
    // Campos de auditoría
    val auditorPor: String?,
    val fechaAuditoria: String?,
    val observacionesAuditor: String?
)

// Data class para filtros
data class FiltrosSalidas(
    var tipo: String = "",
    var estatusAuditoria: String = "",
    var almacenSalida: String = "",
    var fechaInicio: String = "",
    var fechaFin: String = "",
    var usuarioRegistro: String = ""
)
