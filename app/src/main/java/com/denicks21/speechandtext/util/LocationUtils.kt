package com.denicks21.speechandtext.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.Geocoder
import androidx.compose.runtime.*
import com.denicks21.speechandtext.api.UserLocation
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import java.util.*

@SuppressLint("MissingPermission")
@Composable
fun rememberUserLocation(context: Context): State<UserLocation> {
    val locationState = remember { mutableStateOf(UserLocation()) }

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
                val lat = location.latitude
                val lon = location.longitude

                KunturLogger.i("Ubicación obtenida: lat=$lat, lon=$lon", "LOCATION")

                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lon, 1)

                val address = if (!addresses.isNullOrEmpty()) {
                    val city = addresses[0].locality ?: "Ciudad desconocida"
                    val region = addresses[0].adminArea ?: ""
                    val country = addresses[0].countryName ?: ""
                    "$city, $region, $country"
                } else {
                    "Dirección no encontrada"
                }

                KunturLogger.i("Dirección resuelta: $address", "LOCATION")

                // ✅ Actualizamos el estado completo
                locationState.value = UserLocation(
                    latitude = lat,
                    longitude = lon,
                    address = address
                )
            } else {
                KunturLogger.w("Ubicación devuelta es NULL", "LOCATION")
                locationState.value = UserLocation(address = "Ubicación no disponible")
            }

        } catch (e: SecurityException) {
            KunturLogger.e("Permiso denegado para obtener ubicación", "LOCATION", e)
            locationState.value = UserLocation(address = "Permiso denegado")
        } catch (e: Exception) {
            KunturLogger.e("Error inesperado al obtener ubicación: ${e.message}", "LOCATION", e)
            locationState.value = UserLocation(address = "Error de ubicación")
        }
    }

    return locationState
}

