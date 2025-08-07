package com.example.smartflow

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject

class EntradasAdapter(
    private val onEntradaClick: (String) -> Unit
) : RecyclerView.Adapter<EntradasAdapter.EntradaViewHolder>() {

    private var entradas = mutableListOf<JSONObject>()

    class EntradaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNumeroEntrada: TextView = itemView.findViewById(R.id.tv_numero_entrada_item)
        val tvTipoEntrada: TextView = itemView.findViewById(R.id.tv_tipo_entrada_item)
        val tvCantidadEntrada: TextView = itemView.findViewById(R.id.tv_cantidad_entrada_item)
        val tvProveedorEntrada: TextView = itemView.findViewById(R.id.tv_proveedor_entrada_item)
        val tvFechaEntrada: TextView = itemView.findViewById(R.id.tv_fecha_entrada_item)
        val tvEstatusEntrada: TextView = itemView.findViewById(R.id.tv_estatus_entrada_item)
        val tvReferenciaTraspaso: TextView = itemView.findViewById(R.id.tv_referencia_traspaso_item)
        val tvPerfumeEntrada: TextView = itemView.findViewById(R.id.tv_perfume_entrada_item)

        // NUEVOS CAMPOS PARA TRASPASOS
        val tvAlmacenOrigen: TextView = itemView.findViewById(R.id.tv_almacen_origen_item)
        val tvAlmacenDestino: TextView = itemView.findViewById(R.id.tv_almacen_destino_item)
        val tvFechaSalida: TextView = itemView.findViewById(R.id.tv_fecha_salida_item)

        val containerItem: View = itemView.findViewById(R.id.container_entrada_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntradaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_entrada_auditor, parent, false)
        return EntradaViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntradaViewHolder, position: Int) {
        val entrada = entradas[position]
        val context = holder.itemView.context

        try {
            // Datos básicos
            val numeroEntrada = entrada.optString("numero_entrada", "Sin número")
            val cantidad = entrada.optInt("cantidad", 0)
            val fechaEntrada = entrada.optString("fecha_entrada", "")

            // Manejo del estatus
            val estatusValidacion = obtenerEstatusValidacion(entrada)

            // Detectar tipo de entrada
            val tipo = entrada.optString("tipo", "")
            val referenciaTraspaso = entrada.optString("referencia_traspaso", "")
            val esTraspaso = tipo == "Traspaso" || referenciaTraspaso.isNotEmpty()
            val tipoMostrar = if (esTraspaso) "TRASPASO" else "COMPRA"

            // Información del proveedor y perfume
            val nombreProveedor = obtenerNombreProveedor(entrada)
            val nombrePerfume = obtenerNombrePerfume(entrada)

            Log.d("EntradasAdapterOptimized", "🎯 Procesando entrada #$numeroEntrada - Tipo: $tipoMostrar - Estatus: $estatusValidacion - Perfume: $nombrePerfume")

            // Asignar valores básicos
            holder.tvNumeroEntrada.text = "N° $numeroEntrada"
            holder.tvTipoEntrada.text = tipoMostrar
            holder.tvCantidadEntrada.text = "Cantidad: $cantidad"
            holder.tvProveedorEntrada.text = "Proveedor: $nombreProveedor"
            holder.tvFechaEntrada.text = "Fecha: ${formatDate(fechaEntrada)}"
            holder.tvPerfumeEntrada.text = "Perfume: $nombrePerfume"

            // CONFIGURAR CAMPOS ESPECÍFICOS PARA TRASPASOS
            if (esTraspaso) {
                Log.d("EntradasAdapterOptimized", "🔄 Configurando campos específicos de traspaso para #$numeroEntrada")

                // Mostrar referencia de traspaso
                if (referenciaTraspaso.isNotEmpty()) {
                    holder.tvReferenciaTraspaso.text = "Ref: $referenciaTraspaso"
                    holder.tvReferenciaTraspaso.visibility = View.VISIBLE
                } else {
                    holder.tvReferenciaTraspaso.visibility = View.GONE
                }

                // OBTENER Y MOSTRAR INFORMACIÓN ADICIONAL DE TRASPASOS
                val infoAdicional = obtenerInformacionAdicionalTraspaso(entrada)

                // Almacén de origen
                if (infoAdicional.almacenOrigen.isNotEmpty()) {
                    holder.tvAlmacenOrigen.text = "Origen: ${infoAdicional.almacenOrigen}"
                    holder.tvAlmacenOrigen.visibility = View.VISIBLE
                } else {
                    holder.tvAlmacenOrigen.visibility = View.GONE
                }

                // Almacén de destino
                if (infoAdicional.almacenDestino.isNotEmpty()) {
                    holder.tvAlmacenDestino.text = "Destino: ${infoAdicional.almacenDestino}"
                    holder.tvAlmacenDestino.visibility = View.VISIBLE
                } else {
                    holder.tvAlmacenDestino.visibility = View.GONE
                }

                // Fecha de salida
                if (infoAdicional.fechaSalida.isNotEmpty()) {
                    holder.tvFechaSalida.text = "Fecha Salida: ${formatDate(infoAdicional.fechaSalida)}"
                    holder.tvFechaSalida.visibility = View.VISIBLE
                } else {
                    holder.tvFechaSalida.visibility = View.GONE
                }

                Log.d("EntradasAdapterOptimized", "📍 Traspaso #$numeroEntrada: Origen=${infoAdicional.almacenOrigen}, Destino=${infoAdicional.almacenDestino}, FechaSalida=${infoAdicional.fechaSalida}")

            } else {
                // Ocultar campos específicos de traspaso para compras
                holder.tvReferenciaTraspaso.visibility = View.GONE
                holder.tvAlmacenOrigen.visibility = View.GONE
                holder.tvAlmacenDestino.visibility = View.GONE
                holder.tvFechaSalida.visibility = View.GONE
            }

            // Configurar estatus con colores
            holder.tvEstatusEntrada.text = estatusValidacion.uppercase()
            val colorEstatus = when (estatusValidacion.lowercase()) {
                "validado" -> R.color.verde_salvia
                "rechazado" -> R.color.rojo_vino_tenue
                "registrado" -> R.color.oro_palido
                else -> R.color.gris_oscuro
            }
            holder.tvEstatusEntrada.setTextColor(ContextCompat.getColor(context, colorEstatus))

            // Configurar colores según tipo
            val colorTipo = if (esTraspaso) R.color.lavanda_suave else R.color.verde_salvia
            holder.tvTipoEntrada.setTextColor(ContextCompat.getColor(context, colorTipo))

            // Configurar fondo del item según estatus
            val colorFondo = when (estatusValidacion.lowercase()) {
                "validado" -> R.color.verde_muy_claro
                "rechazado" -> R.color.rojo_muy_claro
                else -> R.color.gris_perla_claro
            }
            holder.containerItem.backgroundTintList = ContextCompat.getColorStateList(context, colorFondo)

            // Configurar click listener
            holder.itemView.setOnClickListener {
                Log.d("EntradasAdapterOptimized", "🔗 Click en entrada: $numeroEntrada")
                onEntradaClick(numeroEntrada)
            }

            // Efecto de ripple en el click
            holder.itemView.isClickable = true
            holder.itemView.isFocusable = true

        } catch (e: Exception) {
            Log.e("EntradasAdapterOptimized", "❌ Error en posición $position", e)

            // Valores por defecto en caso de error
            holder.tvNumeroEntrada.text = "Error en entrada"
            holder.tvTipoEntrada.text = "DESCONOCIDO"
            holder.tvCantidadEntrada.text = "Cantidad: 0"
            holder.tvProveedorEntrada.text = "Error cargando proveedor"
            holder.tvFechaEntrada.text = "Fecha: No disponible"
            holder.tvEstatusEntrada.text = "ERROR"
            holder.tvPerfumeEntrada.text = "Error cargando perfume"

            // Ocultar campos de traspaso en caso de error
            holder.tvReferenciaTraspaso.visibility = View.GONE
            holder.tvAlmacenOrigen.visibility = View.GONE
            holder.tvAlmacenDestino.visibility = View.GONE
            holder.tvFechaSalida.visibility = View.GONE

            holder.tvEstatusEntrada.setTextColor(ContextCompat.getColor(context, R.color.rojo_vino_tenue))
            holder.containerItem.backgroundTintList = ContextCompat.getColorStateList(context, R.color.rojo_muy_claro)
        }
    }

    override fun getItemCount() = entradas.size

    fun updateEntradas(nuevasEntradas: List<JSONObject>) {
        Log.d("EntradasAdapterOptimized", "📋 Actualizando adapter con ${nuevasEntradas.size} entradas")

        entradas.clear()
        entradas.addAll(nuevasEntradas)
        notifyDataSetChanged()

        // Log detallado de las entradas recibidas
        nuevasEntradas.forEachIndexed { index, entrada ->
            val numero = entrada.optString("numero_entrada", "Sin número")
            val tipo = entrada.optString("tipo", "")
            val referencia = entrada.optString("referencia_traspaso", "")
            val tipoDetectado = if (tipo == "Traspaso" || referencia.isNotEmpty()) "TRASPASO" else "COMPRA"

            Log.d("EntradasAdapterOptimized", "📝 Entrada [$index]: #$numero - $tipoDetectado")
        }
    }

    fun limpiarEntradas() {
        Log.d("EntradasAdapterOptimized", "🧹 Limpiando entradas del adapter")
        entradas.clear()
        notifyDataSetChanged()
    }

    private fun formatDate(dateString: String): String {
        return try {
            if (dateString.isEmpty()) return "No disponible"

            val date = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).parse(dateString)
            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(date!!)
        } catch (e: Exception) {
            Log.w("EntradasAdapterOptimized", "⚠️ Error formateando fecha: $dateString", e)
            if (dateString.isEmpty()) "No disponible" else dateString.take(10)
        }
    }

    fun getEntradaAt(position: Int): JSONObject? {
        return if (position >= 0 && position < entradas.size) {
            entradas[position]
        } else {
            null
        }
    }

    fun getNumeroEntradaAt(position: Int): String? {
        return getEntradaAt(position)?.optString("numero_entrada")
    }

    // FUNCIONES AUXILIARES PARA MANEJAR DATOS COMPLEJOS

    private fun obtenerEstatusValidacion(entrada: JSONObject): String {
        val estatus = when {
            entrada.has("estatus_validacion") && !entrada.isNull("estatus_validacion") -> {
                val estatusValue = entrada.optString("estatus_validacion", "")
                if (estatusValue.isNotEmpty() && estatusValue != "null" && estatusValue != "undefined") estatusValue else "registrado"
            }
            entrada.has("estatus") && !entrada.isNull("estatus") -> {
                val estatusValue = entrada.optString("estatus", "")
                if (estatusValue.isNotEmpty() && estatusValue != "null" && estatusValue != "undefined") estatusValue else "registrado"
            }
            entrada.has("estado") && !entrada.isNull("estado") -> {
                val estatusValue = entrada.optString("estado", "")
                if (estatusValue.isNotEmpty() && estatusValue != "null" && estatusValue != "undefined") estatusValue else "registrado"
            }
            else -> "registrado"
        }

        Log.d("EntradasAdapterOptimized", "🔍 Estatus detectado para ${entrada.optString("numero_entrada", "")}: '$estatus'")
        return estatus
    }

    private fun obtenerNombrePerfume(entrada: JSONObject): String {
        return try {
            val numeroEntrada = entrada.optString("numero_entrada", "???")
            Log.d("EntradasAdapterOptimized", "🧪 DEBUG COMPLETO Perfume para $numeroEntrada:")

            // Verificar todos los campos relacionados con perfume
            Log.d("EntradasAdapterOptimized", "   - Tiene campo 'perfume': ${entrada.has("perfume")}")
            Log.d("EntradasAdapterOptimized", "   - Valor crudo 'perfume': ${entrada.opt("perfume")}")
            Log.d("EntradasAdapterOptimized", "   - Tipo 'perfume': ${entrada.opt("perfume")?.javaClass?.simpleName}")

            when {
                // Verificar primero si perfume es STRING DIRECTO
                entrada.has("perfume") && entrada.opt("perfume") is String -> {
                    val perfumeStr = entrada.optString("perfume")
                    Log.d("EntradasAdapterOptimized", "   🔸 CASO STRING: Perfume es STRING: '$perfumeStr'")
                    if (perfumeStr.isNotEmpty() && perfumeStr != "null" && perfumeStr != "undefined") {
                        Log.d("EntradasAdapterOptimized", "   ✅ String válido: $perfumeStr")
                        perfumeStr
                    } else {
                        Log.d("EntradasAdapterOptimized", "   ❌ String inválido o vacío")
                        "No disponible"
                    }
                }
                // Caso 2: perfume es un objeto con propiedades
                entrada.has("perfume") && entrada.optJSONObject("perfume") != null -> {
                    Log.d("EntradasAdapterOptimized", "   🔸 CASO OBJETO: Perfume es OBJETO")
                    val perfumeObj = entrada.getJSONObject("perfume")
                    Log.d("EntradasAdapterOptimized", "   - Campos del objeto perfume: ${perfumeObj.keys().asSequence().toList()}")

                    when {
                        perfumeObj.has("name_per") -> {
                            val nombre = perfumeObj.optString("name_per", "Perfume sin nombre")
                            Log.d("EntradasAdapterOptimized", "   ✅ Nombre encontrado en 'name_per': $nombre")
                            nombre
                        }
                        perfumeObj.has("nombre") -> {
                            val nombre = perfumeObj.optString("nombre", "Perfume sin nombre")
                            Log.d("EntradasAdapterOptimized", "   ✅ Nombre encontrado en 'nombre': $nombre")
                            nombre
                        }
                        perfumeObj.has("name") -> {
                            val nombre = perfumeObj.optString("name", "Perfume sin nombre")
                            Log.d("EntradasAdapterOptimized", "   ✅ Nombre encontrado en 'name': $nombre")
                            nombre
                        }
                        else -> {
                            Log.d("EntradasAdapterOptimized", "   ❌ Objeto perfume sin campos reconocidos")
                            "Datos incompletos"
                        }
                    }
                }
                // Caso 3: Buscar en otros campos posibles
                entrada.has("perfume_nombre") -> {
                    val nombre = entrada.optString("perfume_nombre", "No disponible")
                    Log.d("EntradasAdapterOptimized", "   🔸 CASO 3: Encontrado en 'perfume_nombre': $nombre")
                    nombre
                }
                entrada.has("nombre_perfume") -> {
                    val nombre = entrada.optString("nombre_perfume", "No disponible")
                    Log.d("EntradasAdapterOptimized", "   🔸 CASO 4: Encontrado en 'nombre_perfume': $nombre")
                    nombre
                }
                else -> {
                    Log.d("EntradasAdapterOptimized", "   🔸 CASO 5: NO SE ENCONTRÓ información del perfume")
                    Log.d("EntradasAdapterOptimized", "   - Todos los campos disponibles: ${entrada.keys().asSequence().toList()}")
                    "No disponible"
                }
            }
        } catch (e: Exception) {
            Log.e("EntradasAdapterOptimized", "❌ Error obteniendo nombre de perfume", e)
            "Error cargando perfume"
        }
    }

    private fun obtenerNombreProveedor(entrada: JSONObject): String {
        return try {
            when {
                // Caso 1: proveedor es un objeto con propiedades
                entrada.has("proveedor") && entrada.optJSONObject("proveedor") != null -> {
                    val proveedorObj = entrada.getJSONObject("proveedor")
                    when {
                        proveedorObj.has("nombre_proveedor") -> proveedorObj.optString("nombre_proveedor", "Proveedor sin nombre")
                        proveedorObj.has("nombre") -> proveedorObj.optString("nombre", "Proveedor sin nombre")
                        proveedorObj.has("name") -> proveedorObj.optString("name", "Proveedor sin nombre")
                        else -> "Datos incompletos"
                    }
                }
                // Caso 2: proveedor es un string directo
                entrada.has("proveedor") && entrada.optString("proveedor").isNotEmpty() -> {
                    val proveedorStr = entrada.optString("proveedor")
                    if (proveedorStr != "null" && proveedorStr != "undefined") proveedorStr else "No disponible"
                }
                // Caso 3: Buscar en otros campos posibles
                entrada.has("proveedor_nombre") -> entrada.optString("proveedor_nombre", "No disponible")
                entrada.has("nombre_proveedor") -> entrada.optString("nombre_proveedor", "No disponible")
                else -> "No disponible"
            }
        } catch (e: Exception) {
            Log.e("EntradasAdapterOptimized", "❌ Error obteniendo nombre de proveedor", e)
            "Error cargando proveedor"
        }
    }

    // NUEVA FUNCIÓN PARA OBTENER INFORMACIÓN ADICIONAL DE TRASPASOS
    private fun obtenerInformacionAdicionalTraspaso(entrada: JSONObject): InformacionTraspaso {
        return try {
            val numeroEntrada = entrada.optString("numero_entrada", "???")
            Log.d("EntradasAdapterOptimized", "🔄 Extrayendo información adicional de traspaso para #$numeroEntrada")

            var almacenOrigen = ""
            var almacenDestino = ""
            var fechaSalida = ""

            // Buscar en el objeto informacion_adicional
            if (entrada.has("informacion_adicional") && entrada.optJSONObject("informacion_adicional") != null) {
                val infoAdicional = entrada.getJSONObject("informacion_adicional")
                Log.d("EntradasAdapterOptimized", "   📦 Objeto informacion_adicional encontrado")
                Log.d("EntradasAdapterOptimized", "   - Campos disponibles: ${infoAdicional.keys().asSequence().toList()}")

                // Almacén de origen
                almacenOrigen = when {
                    infoAdicional.has("almacen_origen") -> {
                        val origen = infoAdicional.optString("almacen_origen", "")
                        Log.d("EntradasAdapterOptimized", "   🏪 almacen_origen: '$origen'")
                        origen
                    }
                    infoAdicional.has("almacen_salida") -> {
                        val origen = infoAdicional.optString("almacen_salida", "")
                        Log.d("EntradasAdapterOptimized", "   🏪 almacen_salida: '$origen'")
                        origen
                    }
                    else -> ""
                }

                // Almacén de destino
                almacenDestino = when {
                    infoAdicional.has("almacen_destino") -> {
                        val destino = infoAdicional.optString("almacen_destino", "")
                        Log.d("EntradasAdapterOptimized", "   🏬 almacen_destino: '$destino'")
                        destino
                    }
                    infoAdicional.has("almacen_entrada") -> {
                        val destino = infoAdicional.optString("almacen_entrada", "")
                        Log.d("EntradasAdapterOptimized", "   🏬 almacen_entrada: '$destino'")
                        destino
                    }
                    else -> ""
                }

                // Fecha de salida
                fechaSalida = when {
                    infoAdicional.has("fecha_salida") -> {
                        val fecha = infoAdicional.optString("fecha_salida", "")
                        Log.d("EntradasAdapterOptimized", "   📅 fecha_salida: '$fecha'")
                        fecha
                    }
                    else -> ""
                }
            } else {
                Log.d("EntradasAdapterOptimized", "   ❌ No se encontró objeto informacion_adicional")

                // Buscar directamente en el objeto principal como respaldo
                almacenOrigen = entrada.optString("almacen_origen", entrada.optString("almacen_salida", ""))
                almacenDestino = entrada.optString("almacen_destino", "")
                fechaSalida = entrada.optString("fecha_salida", "")

                Log.d("EntradasAdapterOptimized", "   🔍 Búsqueda directa - Origen: '$almacenOrigen', Destino: '$almacenDestino', Fecha: '$fechaSalida'")
            }

            val resultado = InformacionTraspaso(almacenOrigen, almacenDestino, fechaSalida)
            Log.d("EntradasAdapterOptimized", "   ✅ Información extraída para #$numeroEntrada: $resultado")

            return resultado

        } catch (e: Exception) {
            Log.e("EntradasAdapterOptimized", "❌ Error obteniendo información adicional de traspaso", e)
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
