package com.denicks21.speechandtext.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.Geocoder
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.*
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import java.util.*

@SuppressLint("MissingPermission")
@Composable
fun rememberUserLocation(context: Context): State<String> {
    val locationResult = remember { mutableStateOf("Obteniendo ubicación...") }

    LaunchedEffect(Unit) {
        try {
            KunturLogger.i("Obteniendo FusedLocationProviderClient...", "LOCATION")

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            KunturLogger.i("Solicitando ubicación actual con PRIORITY_HIGH_ACCURACY...", "LOCATION")
            val location: Location? = fusedLocationClient
                .getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                )
                .await()

            if (location != null) {
                KunturLogger.i("Ubicación obtenida: lat=${location.latitude}, lon=${location.longitude}", "LOCATION")

                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses.first()
                    val city = address.locality ?: "Ciudad desconocida"
                    val region = address.adminArea ?: ""
                    val country = address.countryName ?: ""

                    locationResult.value = "$city, $region, $country"

                    KunturLogger.i("Dirección resuelta: $city, $region, $country", "LOCATION")
                } else {
                    KunturLogger.w("Geocoder no devolvió direcciones", "LOCATION")
                    locationResult.value = "Dirección no encontrada"
                }
            } else {
                KunturLogger.w("Ubicación devuelta es NULL", "LOCATION")
                locationResult.value = "Ubicación no disponible"
            }

        } catch (e: SecurityException) {
            KunturLogger.e("Permiso denegado para obtener ubicación", "LOCATION", e)
            locationResult.value = "Permiso denegado"
        } catch (e: Exception) {
            KunturLogger.e("Error inesperado al obtener ubicación: ${e.message}", "LOCATION", e)
            locationResult.value = "Error de ubicación"
        }
    }

    return locationResult
}

