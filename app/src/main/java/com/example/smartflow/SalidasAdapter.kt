package com.example.smartflow

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class SalidasAdapter(
    private var salidasList: MutableList<Salida>,
    private val onItemClick: (Salida) -> Unit
) : RecyclerView.Adapter<SalidasAdapter.SalidaViewHolder>() {

    companion object {
        private const val TAG = "SalidasAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalidaViewHolder {
        Log.d(TAG, "onCreateViewHolder llamado")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_salida, parent, false)
        Log.d(TAG, "View inflado correctamente: ${view != null}")
        return SalidaViewHolder(view)
    }

    override fun onBindViewHolder(holder: SalidaViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder llamado para posición $position de ${salidasList.size} items")
        if (position < salidasList.size) {
            val salida = salidasList[position]
            Log.d(TAG, "Binding salida: ${salida.numeroSalida} - ${salida.nombrePerfume}")
            holder.bind(salida, onItemClick)
        } else {
            Log.e(TAG, "ERROR: Posición $position fuera de rango (tamaño: ${salidasList.size})")
        }
    }

    override fun getItemCount(): Int {
        val count = salidasList.size
        Log.d(TAG, "getItemCount() devuelve: $count")
        Log.d(TAG, "Lista actual contiene: ${salidasList.map { it.numeroSalida }}")
        return count
    }

    fun updateSalidas(newSalidas: List<Salida>) {
        Log.d(TAG, "updateSalidas llamado con ${newSalidas.size} elementos")
        Log.d(TAG, "Lista actual tiene ${salidasList.size} elementos antes de limpiar")
        Log.d(TAG, "Nuevas salidas: ${newSalidas.map { "${it.numeroSalida} - ${it.nombrePerfume}" }}")

        // Crear una nueva lista en lugar de usar clear() para evitar problemas de referencia
        salidasList = newSalidas.toMutableList()
        Log.d(TAG, "Lista recreada con tamaño final: ${salidasList.size}")

        Log.d(TAG, "Llamando notifyDataSetChanged()")
        notifyDataSetChanged()
        Log.d(TAG, "notifyDataSetChanged() completado")
    }

    class SalidaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewNumeroSalida: TextView = itemView.findViewById(R.id.textViewNumeroSalida)
        private val textViewPerfume: TextView = itemView.findViewById(R.id.textViewPerfume)
        private val textViewCantidad: TextView = itemView.findViewById(R.id.textViewCantidad)
        private val textViewTipo: TextView = itemView.findViewById(R.id.textViewTipo)
        private val textViewFecha: TextView = itemView.findViewById(R.id.textViewFecha)
        private val textViewEstatus: TextView = itemView.findViewById(R.id.textViewEstatus)
        private val textViewAlmacen: TextView = itemView.findViewById(R.id.textViewAlmacen)
        private val textViewValor: TextView = itemView.findViewById(R.id.textViewValor)

        fun bind(salida: Salida, onItemClick: (Salida) -> Unit) {
            Log.d("SalidasAdapter", "ViewHolder.bind llamado para: ${salida.numeroSalida}")

            textViewNumeroSalida.text = salida.numeroSalida
            textViewPerfume.text = salida.nombrePerfume
            textViewCantidad.text = "${salida.cantidad} uds"
            textViewTipo.text = salida.tipo
            textViewAlmacen.text = salida.almacenSalida

            Log.d("SalidasAdapter", "Datos básicos asignados - Número: ${salida.numeroSalida}, Perfume: ${salida.nombrePerfume}")

            // Formatear fecha
            textViewFecha.text = formatearFecha(salida.fechaSalida)

            // TextView de estatus oculto ya que no existe ese dato
            textViewEstatus.visibility = View.GONE

            // Mostrar valor según el tipo
            when (salida.tipo) {
                "Venta" -> {
                    val valor = salida.precioTotal ?: 0.0
                    textViewValor.text = "💰 $${String.format("%.2f", valor)}"
                    textViewValor.setTextColor(ContextCompat.getColor(itemView.context, R.color.verde_salvia))
                    textViewValor.visibility = View.VISIBLE
                }
                "Merma" -> {
                    textViewValor.text = "📦 ${salida.motivo ?: "Merma"}"
                    textViewValor.setTextColor(ContextCompat.getColor(itemView.context, R.color.rojo_vino_tenue))
                    textViewValor.visibility = View.VISIBLE
                }
                else -> {
                    textViewValor.visibility = View.GONE
                }
            }

            // Emoji según tipo
            val emojiTipo = when (salida.tipo) {
                "Venta" -> "💰"
                "Merma" -> "📦"
                else -> "📤"
            }
            textViewTipo.text = "$emojiTipo ${salida.tipo}"

            // Click listener
            itemView.setOnClickListener {
                onItemClick(salida)
            }

            Log.d("SalidasAdapter", "ViewHolder.bind completado para: ${salida.numeroSalida}")
        }

        private fun formatearFecha(fechaISO: String): String {
            return try {
                // Convertir de ISO string a formato legible
                val partes = fechaISO.split("T")
                if (partes.size >= 2) {
                    val fecha = partes[0] // YYYY-MM-DD
                    val hora = partes[1].split(".")[0] // HH:mm:ss
                    val fechaPartes = fecha.split("-")
                    "${fechaPartes[2]}/${fechaPartes[1]}/${fechaPartes[0]} ${hora.substring(0, 5)}"
                } else {
                    fechaISO
                }
            } catch (e: Exception) {
                fechaISO
            }
        }
    }
}
