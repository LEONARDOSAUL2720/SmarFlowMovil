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
        val tvCodigo: TextView = itemView.findViewById(R.id.tv_codigo_perfume)
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
            holder.tvNombre.text = perfume.getString("nombre")
            holder.tvMarca.text = "🏷️ Marca: ${perfume.getString("marca")}"
            holder.tvCategoria.text = "📂 Categoría: ${perfume.getString("categoria")}"
            holder.tvCodigo.text = "🔢 Código: ${perfume.getString("codigo_producto")}"

            val stockActual = perfume.getInt("stock_actual")
            val stockMinimo = perfume.getInt("stock_minimo")
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

            holder.tvPrecio.text = "$${String.format("%.2f", perfume.getDouble("precio_venta"))}"
            holder.tvAlmacen.text = "🏪 Almacén: ${perfume.getString("almacen_id")}"

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

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }    override fun getItemCount(): Int = perfumes.size

    fun updatePerfumes(nuevosPerfumes: List<JSONObject>) {
        perfumes.clear()
        perfumes.addAll(nuevosPerfumes)
        notifyDataSetChanged()
    }

    fun limpiarPerfumes() {
        perfumes.clear()
        notifyDataSetChanged()
    }
}
