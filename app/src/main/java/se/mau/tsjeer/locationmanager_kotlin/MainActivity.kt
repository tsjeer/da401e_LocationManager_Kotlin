package se.mau.tsjeer.locationmanager_kotlin

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

private val TAG="MyLogging"
class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var tvLocation: TextView
    private lateinit var tvGps: TextView
    private lateinit var tvNetwork: TextView
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation:Location?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvLocation = findViewById(R.id.tvLocation)

        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).apply {
            setMinUpdateDistanceMeters(50f)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()
        getCurrentLocation()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                // Normally, you want to save a new location to a database.
                currentLocation = locationResult.lastLocation
                Log.e(TAG, "New location!")
            }
        }
        if(checkPermission())
          fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

    }

    private fun getCurrentLocation(){
        if(checkPermission()){
            Log.d(TAG, "getCurrentLocation")
            if(isLocationEnabled()){
               if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    !=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    !=PackageManager.PERMISSION_GRANTED){
                    requestPermission()
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this){ task->
                    val location:Location?=task.result

                    if(location==null){
                        Toast.makeText(this, "Null recieved", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(this, "Get success", Toast.LENGTH_SHORT).show()
                        val x = location.latitude
                        val y = location.longitude
                        tvLocation.text="Lat=" + y + ",    Long=" + x
                        currentLocation=location
                    }
                }
            }
            else{
                //setting open here
                Toast.makeText(applicationContext, "Turn on location", Toast.LENGTH_SHORT).show()
                val intent= Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                Log.d(TAG, "Turn on location")
                startActivity(intent)
            }
        }
        else{
            //request permission here
            requestPermission()
        }
    }

    private fun requestPermission(){
        Log.d(TAG, "requestPermission")
        ActivityCompat.requestPermissions(this, arrayOf( android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_ACCESS_LOCATION)
    }

    private fun checkPermission():Boolean{
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            ==PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            ==PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkPermission")

            return true
        }
        return false
    }

    private fun isLocationEnabled():Boolean{
        Log.d(TAG, "isLocationEnabled")
        val locationManager:LocationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    companion object{
        private const val PERMISSION_REQUEST_ACCESS_LOCATION=100
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult")
        if(requestCode== PERMISSION_REQUEST_ACCESS_LOCATION){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext, "Granted", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Granted")
                getCurrentLocation()
            }
            else{
                Toast.makeText(applicationContext, "Denied", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Denied")
            }
        }
    }
}



