package com.example.garbagemanagementsystem_dataflair

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.github.florent37.runtimepermission.RuntimePermission.askPermission
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.IOException
import java.lang.Exception
import java.lang.IndexOutOfBoundsException
import java.util.*


class MapActivity2 : AppCompatActivity(),OnMapReadyCallback,LocationListener,
                GoogleMap.OnCameraMoveListener,GoogleMap.OnCameraMoveStartedListener,
                GoogleMap.OnCameraIdleListener{
    var addressLine:String=""
    var latitude:String=""
    var longitude:String=""
    private var mMap:GoogleMap?=null
    lateinit var mapView:MapView
    private val MAP_VIEW_BUNDLE_KEY="MapViewBundleKey"
    private var fusedLocationProviderClient:FusedLocationProviderClient?=null
    private lateinit var database: DatabaseReference
    private lateinit var area:String

    override fun onMapReady(googleMap: GoogleMap) {
        mapView.onResume()
        mMap=googleMap
        askGalleryPermissionLocation()
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )!=PackageManager.PERMISSION_GRANTED){
            return
        }
        mMap!!.isMyLocationEnabled=true
        mMap!!.setOnCameraMoveListener(this)
        mMap!!.setOnCameraMoveStartedListener(this)
        mMap!!.setOnCameraIdleListener(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map2)
        //initialize db
        database= FirebaseDatabase.getInstance().getReference("bins")
        //get area name from intent
        area=intent.getStringExtra("area").toString()
        mapView=findViewById(R.id.map1)
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),101)
            return
        }
        var mapViewBundle:Bundle?=null
        if(savedInstanceState!=null){
            mapViewBundle=savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)
        //select address button on click listener
        val selectAddressButton:Button=findViewById(R.id.selectAddressButton)
        selectAddressButton.setOnClickListener {
            val intent=Intent(this,MainActivity::class.java)
            intent.putExtra("address Line",addressLine)
            intent.putExtra("latitude",latitude)
            intent.putExtra("longitude",longitude)
            val binitem=bin(area,addressLine, latitude, longitude)
            database.child(area).setValue(binitem).addOnSuccessListener {
                Toast.makeText(this,"bin added at your current location",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this,"couldn't add bin !try again",Toast.LENGTH_SHORT).show()
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        askGalleryPermissionLocation()
        var mapViewBundle=outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if(mapViewBundle==null){
            mapViewBundle= Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY,mapViewBundle)
        }
        mapView.onSaveInstanceState(mapViewBundle)
    }

    private fun askGalleryPermissionLocation() {
        askPermission(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ){
            getCurrentLocation()
        }.onDeclined{
            e ->
            if (e.hasDenied()){
                e.denied.forEach{

                }
            AlertDialog.Builder(this)
                .setMessage("accept permissions")
                .setPositiveButton("yes"){_,_ ->
                    e.askAgain()
                }.setNegativeButton("no"){dialog,_ ->
                    dialog.dismiss()
                }.show()
            if(e.hasForeverDenied()){
                e.foreverDenied.forEach{}
                e.goToSettings()

            }            }
        }
    }

    private fun getCurrentLocation() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=
            PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )!=PackageManager.PERMISSION_GRANTED){
            return
        }
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)
        try {
            @SuppressLint("missing permission") val location=
                fusedLocationProviderClient!!.lastLocation
            location.addOnCompleteListener(object : OnCompleteListener<Location> {
                override fun onComplete(p0: Task<Location>){
                    if(p0.isSuccessful){
                        val currentLocation=p0.result as Location?
                        if(currentLocation!=null){
                            moveCamera(
                                LatLng(currentLocation.latitude,currentLocation.longitude),
                                15f
                            )
                        }
                    }else{
                        askGalleryPermissionLocation()
                    }
                }
            })
        }catch (se:Exception){
            Log.e("TAG","Security Exception")
        }

    }

    private fun moveCamera(latLng: LatLng, fl: Float) {
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,fl))
    }

    override fun onLocationChanged(location: Location) {
        val geocoder=Geocoder(this, Locale.getDefault())
        var addresses:List<Address>?=null
        try {
            addresses=geocoder.getFromLocation(location!!.latitude,location.longitude,1)
        }catch (e:IOException){
            e.printStackTrace()
        }
        setAddress(addresses!![0])
    }

    private fun setAddress(addresses: Address) {
        addressLine=""
        if (addresses!=null){
            if(addresses.getAddressLine(0)!=null){
                addressLine=addressLine+addresses.getAddressLine(0).toString()
                latitude=addresses.latitude.toString()
                longitude=addresses.longitude.toString()
            }
            if(addresses.getAddressLine(1)!=null){
                addressLine=addressLine+addresses.getAddressLine(1).toString()
            }
            println("location here amith"+addressLine+"   "+latitude+"   "+longitude)
            val textView:TextView=findViewById(R.id.addressMAP)
            textView.setText(addressLine)
        }
    }

    override fun onCameraMove() {

    }

    override fun onCameraMoveStarted(p0: Int) {

    }

    override fun onCameraIdle() {
        var addresses:List<Address>?=null
        val geocoder=Geocoder(this, Locale.getDefault())
        try{
            addresses=geocoder.getFromLocation(mMap!!.cameraPosition.target.latitude,mMap!!.cameraPosition.target.longitude,1)
            setAddress(addresses!![0] )
        }catch (e:IndexOutOfBoundsException){
            e.printStackTrace()
        }catch (e:IOException){
            e.printStackTrace()
        }
    }


}