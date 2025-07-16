package com.denicks21.speechandtext.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun ContactsPage(navController: NavHostController) {
    // Lista de ejemplo estática; luego la puedes poblar desde ContactsContract o tu fuente real
    val contacts = remember {
        listOf("Ana Pérez", "Carlos López", "María Gómez", "José Martínez")
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(contacts) { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            // navegación a detalle de contacto, si quisieras
                        }
                )
                Divider()
            }
        }
    }
}

