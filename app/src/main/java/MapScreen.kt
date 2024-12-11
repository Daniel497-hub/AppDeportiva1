package com.example.ejemplodatabase.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle(context)
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var currentMarker by remember { mutableStateOf<Marker?>(null) }
    var isLocationPermissionGranted by remember { mutableStateOf(false) }
    var startLatLng by remember { mutableStateOf<LatLng?>(null) }
    var endLatLng by remember { mutableStateOf<LatLng?>(null) }

    // Estados para los cuadros de texto
    var startLocationText by remember { mutableStateOf("") }
    var endLocationText by remember { mutableStateOf("") }

    // Marcadores de inicio y fin
    var startMarker by remember { mutableStateOf<Marker?>(null) }
    var endMarker by remember { mutableStateOf<Marker?>(null) }

    // Polyline
    var polyline by remember { mutableStateOf<Polyline?>(null) }

    // Firebase Authentication
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val firestore = FirebaseFirestore.getInstance()

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> isLocationPermissionGranted = granted }
    )

    LaunchedEffect(Unit) {
        if (user == null) {
            Toast.makeText(context, "Por favor, inicia sesión", Toast.LENGTH_SHORT).show()
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            isLocationPermissionGranted = true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Crear Ruta") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (isLocationPermissionGranted) {
                AndroidView(factory = { mapView }, modifier = Modifier.weight(1f)) { map ->
                    map.getMapAsync { googleMap ->
                        googleMap.uiSettings.isZoomControlsEnabled = true
                        googleMap.uiSettings.isMyLocationButtonEnabled = true

                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null && currentMarker == null) {
                                val userLocation = LatLng(location.latitude, location.longitude)
                                currentMarker = googleMap.addMarker(
                                    MarkerOptions().position(userLocation).title("Mi ubicación")
                                )
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                            }
                        }

                        googleMap.setOnMapClickListener { latLng ->
                            if (startLatLng == null) {
                                startLatLng = latLng
                                startLocationText = "${latLng.latitude}, ${latLng.longitude}"
                                startMarker = googleMap.addMarker(
                                    MarkerOptions().position(latLng).title("Inicio")
                                )
                            } else if (endLatLng == null) {
                                endLatLng = latLng
                                endLocationText = "${latLng.latitude}, ${latLng.longitude}"
                                endMarker = googleMap.addMarker(
                                    MarkerOptions().position(latLng).title("Fin")
                                )
                            }
                        }

                        // Dibujar Polyline cuando se actualicen los puntos
                        if (startLatLng != null && endLatLng != null) {
                            polyline?.remove() // Eliminar la Polyline existente si hay una
                            polyline = googleMap.addPolyline(
                                PolylineOptions()
                                    .add(startLatLng, endLatLng)
                                    .color(android.graphics.Color.BLUE)
                                    .width(10f)
                            )
                        }
                    }
                }
            } else {
                Text("Por favor, habilita los permisos de ubicación para usar esta funcionalidad.")
            }

            // Cuadros de texto y botón para guardar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = startLocationText,
                    onValueChange = {},
                    label = { Text("Ruta Inicial") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = endLocationText,
                    onValueChange = {},
                    label = { Text("Ruta Final") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (startLatLng != null && endLatLng != null) {
                            saveRouteInFirebase(startLatLng!!, endLatLng!!, context)
                            // Resetear marcadores y datos
                            startMarker?.remove()
                            endMarker?.remove()
                            startLatLng = null
                            endLatLng = null
                            startLocationText = ""
                            endLocationText = ""
                        } else {
                            Toast.makeText(context, "Por favor, selecciona ambas ubicaciones", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Guardar Ruta")
                }
            }
        }
    }
}

fun saveRouteInFirebase(startLatLng: LatLng, endLatLng: LatLng, context: android.content.Context) {
    val firestore = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    if (user != null) {
        val routeData = hashMapOf(
            "start_lat" to startLatLng.latitude,
            "start_lng" to startLatLng.longitude,
            "end_lat" to endLatLng.latitude,
            "end_lng" to endLatLng.longitude,
            "user_id" to user.uid,
            "created_at" to System.currentTimeMillis()
        )

        firestore.collection("routes")
            .add(routeData)
            .addOnSuccessListener {
                Toast.makeText(context, "Ruta guardada con éxito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al guardar la ruta", Toast.LENGTH_SHORT).show()
            }
    }
}

@Composable
fun rememberMapViewWithLifecycle(context: android.content.Context): MapView {
    val mapView = remember { MapView(context) }

    DisposableEffect(Unit) {
        val bundle = Bundle()
        mapView.onCreate(bundle)
        mapView.onStart()
        mapView.onResume()

        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }
    return mapView
}
