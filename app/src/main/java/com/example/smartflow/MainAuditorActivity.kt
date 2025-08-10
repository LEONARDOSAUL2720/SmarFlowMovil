package com.example.smartflow

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import android.widget.ImageView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import com.bumptech.glide.Glide

import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target


class MainAuditorActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_auditor)

        // Obtener el nombre del usuario desde el Intent o SharedPreferences (clave unificada)
        val prefs: SharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val nombreUsuario = prefs.getString("user_name", "Usuario") ?: "Usuario"

        // Asignar el nombre al header superior
        val tvHeaderUserName = findViewById<TextView>(R.id.tv_header_user_name)
        tvHeaderUserName.text = nombreUsuario

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Pasar el nombre del usuario al header del Navigation Drawer
        setupUserHeader(nombreUsuario)

        // Configurar navegación
        setupNavigation()

        // Botón para abrir el menú lateral desde el header
        val btnOpenDrawer = findViewById<ImageButton>(R.id.btn_open_drawer)
        btnOpenDrawer.setOnClickListener {
            drawerLayout.openDrawer(navView)
        }
    }

    // Ahora recibe el nombre del usuario como parámetro
    private fun setupUserHeader(nombreUsuario: String) {
        val sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE)

        // Configurar header del Navigation Drawer
        val headerView: View = navView.getHeaderView(0)
        val tvUserName: TextView = headerView.findViewById(R.id.tv_user_name)
        val tvUserMessage: TextView = headerView.findViewById(R.id.tv_user_message)
        val imgUser: ImageView = headerView.findViewById(R.id.img_user)

        tvUserName.text = nombreUsuario
        tvUserMessage.text = "¡Bienvenido, $nombreUsuario!"

        // Obtener imagen del usuario (Base64 o URL)
        val userImageBase64 = sharedPrefs.getString("user_image_base64", null)
        val userImageUrl = sharedPrefs.getString("user_image_url", null)

        Log.d("MainAuditorActivity", "=== DEBUG CARGA IMAGEN ===")
        Log.d("MainAuditorActivity", "Base64 disponible: ${!userImageBase64.isNullOrEmpty()}")
        Log.d("MainAuditorActivity", "URL disponible: ${!userImageUrl.isNullOrEmpty()}")

        // Cargar imagen según el tipo disponible
        when {
            !userImageBase64.isNullOrEmpty() -> {
                Log.d("MainAuditorActivity", "🔄 Cargando imagen desde Base64")
                loadImageFromBase64(userImageBase64, imgUser)
            }
            !userImageUrl.isNullOrEmpty() -> {
                Log.d("MainAuditorActivity", "🔄 Cargando imagen desde URL: $userImageUrl")
                loadImageFromUrl(userImageUrl, imgUser)
            }
            else -> {
                Log.d("MainAuditorActivity", "❌ No hay imagen disponible")
                imgUser.setImageResource(R.drawable.ic_person)
            }
        }
    }

    private fun loadImageFromBase64(base64String: String, imageView: ImageView) {
        try {
            // Extraer solo la parte Base64 (sin el prefijo data:image/...)
            val base64Data = if (base64String.contains(",")) {
                base64String.split(",")[1]
            } else {
                base64String
            }

            // Convertir Base64 a bytes
            val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)

            Log.d("MainAuditorActivity", "✅ Base64 decodificado: ${imageBytes.size} bytes")

            // Cargar con Glide
            Glide.with(this)
                .load(imageBytes)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("MainAuditorActivity", "❌ Error cargando Base64: ${e?.message}")
                        return false
                    }
                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("MainAuditorActivity", "✅ Base64 cargada exitosamente")
                        return false
                    }
                })
                .circleCrop()
                .into(imageView)

        } catch (e: Exception) {
            Log.e("MainAuditorActivity", "❌ Error procesando Base64: ${e.message}")
            imageView.setImageResource(R.drawable.ic_person)
        }
    }

    private fun loadImageFromUrl(imageUrl: String, imageView: ImageView) {
        Glide.with(this)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e("MainAuditorActivity", "❌ Error cargando URL: ${e?.message}")
                    Log.e("MainAuditorActivity", "URL que falló: $model")
                    return false
                }
                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d("MainAuditorActivity", "✅ URL cargada exitosamente: $model")
                    return false
                }
            })
            .circleCrop()
            .into(imageView)
    }

    private fun setupNavigation() {
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_ordenes_compra -> {
                    startActivity(Intent(this, OrdenesCompra::class.java))
                    true
                }
                R.id.nav_entradas -> {
                    startActivity(Intent(this, EntradasAuditor::class.java))
                    true
                }
                R.id.nav_opcion3 -> {
                    // Cerrar sesión
                    logout()
                    true
                }
                R.id.nav_stock -> {
                    val intent = Intent(this, StockAuditor::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_salidas -> {
                    val intent = Intent(this, SalidasAuditor::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_reporte_salidas -> {
                    val intent = Intent(this, ReporteSalidasAuditor::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_reporte_entradas -> {
                    val intent = Intent(this, ReporteEntradasAuditor::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
    }

    // Función para obtener el token de autenticación
    private fun getAuthToken(): String? {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }

    // Función para verificar si hay una sesión activa
    private fun isUserLoggedIn(): Boolean {
        val token = getAuthToken()
        val role = getUserRole()
        return !token.isNullOrEmpty() && !role.isNullOrEmpty()
    }

    // Función para obtener el rol del usuario guardado
    private fun getUserRole(): String? {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("user_role", null)
    }

    // Cerrar sesión
    private fun logout() {
        // Limpiar SharedPreferences
        val sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()

        // Mostrar mensaje de confirmación
        Toast.makeText(this, "Sesión cerrada exitosamente", Toast.LENGTH_SHORT).show()

        // Redirigir al login
        redirectToLogin()
    }

    // Redirigir al login
    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(navView)
            return true
        }
        when (item.itemId) {
            R.id.nav_stock -> {
                val intent = Intent(this, StockAuditor::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
