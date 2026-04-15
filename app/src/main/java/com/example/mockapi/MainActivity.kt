package com.example.mockapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mockapi.ui.theme.MockapiTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// 1. Modelo de datos (Ajustar según tu MockAPI)
data class User(
    val id: String,
    val name: String,
    val email: String,
)

// 2. Interfaz de Retrofit
interface ApiService {
    @GET("usuarios") // El endpoint de tu MockAPI
    suspend fun getUsers(): List<User>
}

// 3. Cliente Retrofit (Singleton simple)
object RetrofitClient {
    // REEMPLAZA CON LA URL DE TU PROPIO MOCKAPI
    private const val BASE_URL = "https://69df0e91d6de26e119287c3f.mockapi.io/Usuarios/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MockapiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UserListScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun UserListScreen(modifier: Modifier = Modifier) {
    // Estado local (Ya que no usamos ViewModel)
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Llamada a la API al iniciar la pantalla
    LaunchedEffect(Unit) {
        try {
            users = RetrofitClient.apiService.getUsers()
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Error al cargar datos: ${e.localizedMessage}"
            isLoading = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            }
            else -> {
                // 4. LazyColumn para mostrar la lista
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(users) { user ->
                        UserCard(user)
                    }
                }
            }
        }
    }
}

@Composable
fun UserCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "ID: ${user.id}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
