package com.example.smartflow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject

class PerfumesAdapter(
    private var perfumes: MutableList<JSONObject> = mutableListOf()
) : RecyclerView.Adapter<PerfumesAdapter.PerfumeViewHolder>() {

    class PerfumeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tv_nombre_perfume)
        val tvMarca: TextView = itemView.findViewById(R.id.tv_marca_perfume)
        val tvCategoria: TextView = itemView.findViewById(R.id.tv_categoria_perfume)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tv_descripcion_perfume)
        val tvStock: TextView = itemView.findViewById(R.id.tv_stock_perfume)
        val tvPrecio: TextView = itemView.findViewById(R.id.tv_precio_perfume)
        val tvAlmacen: TextView = itemView.findViewById(R.id.tv_almacen_perfume)
        val tvEstado: TextView = itemView.findViewById(R.id.tv_estado_perfume)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PerfumeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_perfume_styled, parent, false)
        return PerfumeViewHolder(view)
    }

    override fun onBindViewHolder(holder: PerfumeViewHolder, position: Int) {
        val perfume = perfumes[position]

        try {
            // DEBUG: Log para ver qué campos tiene cada perfume
            android.util.Log.d("PerfumesAdapter", "🔍 Perfume $position: $perfume")

            // CORREGIDO: Usar los nombres de campos que envía la API
            holder.tvNombre.text = perfume.getString("name_per")

            // Verificar si marca existe, sino usar valor por defecto
            val marca = if (perfume.has("marca") && !perfume.isNull("marca")) {
                perfume.getString("marca")
            } else {
                "SmartFlow"
            }
            holder.tvMarca.text = "🏷️ Marca: $marca"

            holder.tvCategoria.text = "📂 Categoría: ${perfume.getString("categoria_per")}"

            // Mostrar descripción del perfume
            val descripcion = if (perfume.has("descripcion_per") && !perfume.isNull("descripcion_per")) {
                perfume.getString("descripcion_per")
            } else {
                "Sin descripción disponible"
            }
            holder.tvDescripcion.text = "📝 $descripcion"

            val stockActual = perfume.getInt("stock_per")
            val stockMinimo = perfume.getInt("stock_minimo_per")
            holder.tvStock.text = "$stockActual / Min: $stockMinimo"

            // Cambiar color según el stock
            val context = holder.itemView.context
            if (stockActual < stockMinimo) {
                holder.tvStock.setTextColor(context.getColor(R.color.rojo_vino_tenue))
            } else if (stockActual <= stockMinimo * 1.5) {
                holder.tvStock.setTextColor(context.getColor(R.color.oro_palido))
            } else {
                holder.tvStock.setTextColor(context.getColor(R.color.verde_salvia))
            }

            // Formatear precio
            val precio = perfume.getDouble("precio_venta_per")
            holder.tvPrecio.text = "$${String.format("%.2f", precio)}"

            // Almacén/Ubicación
            holder.tvAlmacen.text = "🏪 Almacén: ${perfume.getString("ubicacion_per")}"

            // Configurar estado con color dinámico
            val estado = perfume.getString("estado")
            holder.tvEstado.text = estado
            when (estado.lowercase()) {
                "activo" -> {
                    holder.tvEstado.setBackgroundColor(context.getColor(R.color.verde_salvia))
                    holder.tvEstado.setTextColor(context.getColor(R.color.white))
                }
                "inactivo" -> {
                    holder.tvEstado.setBackgroundColor(context.getColor(R.color.rojo_vino_tenue))
                    holder.tvEstado.setTextColor(context.getColor(R.color.white))
                }
                else -> {
                    holder.tvEstado.setBackgroundColor(context.getColor(R.color.oro_palido))
                    holder.tvEstado.setTextColor(context.getColor(R.color.white))
                }
            }

            android.util.Log.d("PerfumesAdapter", "✅ Perfume $position cargado: ${perfume.getString("name_per")}")

        } catch (e: Exception) {
            e.printStackTrace()
            // Log detallado para debugging
            android.util.Log.e("PerfumesAdapter", "❌ Error binding perfume data en posición $position: ${e.message}")
            android.util.Log.e("PerfumesAdapter", "📋 Perfume JSON completo: $perfume")
            android.util.Log.e("PerfumesAdapter", "🔍 Claves disponibles: ${perfume.keys().asSequence().toList()}")

            // Mostrar valores por defecto para evitar crash
            holder.tvNombre.text = "Error cargando perfume"
            holder.tvMarca.text = "🏷️ Marca: N/A"
            holder.tvCategoria.text = "📂 Categoría: N/A"
            holder.tvDescripcion.text = "📝 Sin descripción"
            holder.tvStock.text = "0 / Min: 0"
            holder.tvPrecio.text = "$0.00"
            holder.tvAlmacen.text = "🏪 Almacén: N/A"
            holder.tvEstado.text = "Error"
        }
    }

    override fun getItemCount(): Int = perfumes.size

    fun updatePerfumes(nuevosPerfumes: List<JSONObject>) {
        android.util.Log.d("PerfumesAdapter", "🔄 Actualizando adapter con ${nuevosPerfumes.size} perfumes")

        // Log para ver el primer perfume como ejemplo
        if (nuevosPerfumes.isNotEmpty()) {
            android.util.Log.d("PerfumesAdapter", "📋 Ejemplo de perfume recibido: ${nuevosPerfumes[0]}")
            android.util.Log.d("PerfumesAdapter", "🔍 Claves del primer perfume: ${nuevosPerfumes[0].keys().asSequence().toList()}")
        }

        perfumes.clear()
        perfumes.addAll(nuevosPerfumes)
        notifyDataSetChanged()

        android.util.Log.d("PerfumesAdapter", "✅ Adapter actualizado. Total items: ${itemCount}")
    }

    fun limpiarPerfumes() {
        android.util.Log.d("PerfumesAdapter", "🗑️ Limpiando perfumes del adapter")
        perfumes.clear()
        notifyDataSetChanged()
    }
}
