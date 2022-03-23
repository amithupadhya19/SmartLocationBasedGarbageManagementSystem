package com.example.garbagemanagementsystem_dataflair

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.PlaceReport
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.Autocomplete
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.ArrayList



class MainActivity : AppCompatActivity() {
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var database: DatabaseReference
    var binList= arrayListOf<bin>()
    val adapter=Adapter(binList)
    lateinit var marea:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //initialize db
        database=FirebaseDatabase.getInstance().getReference("bins")
        //get bins list
        getBinsList()
        //initialize fused location provider client
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)
        //initialize recyclerview

        println("amith 222")

        //initialize add bin button
        val mAddBinButton: Button =findViewById(R.id.addBinButton)
        //set on click listener
        mAddBinButton.setOnClickListener {
            openDialog()
        }
        //logout button on click listener
        val logout:ImageView=findViewById(R.id.LOGOUT)
        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent=Intent(this,Login::class.java)
            startActivity(intent)
        }

    }

    private fun getBinsList() {
        //initialize progress bar
        val mProgressBar:ProgressBar=findViewById(R.id.progressBar)
        mProgressBar.visibility=View.VISIBLE
        database.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    binList.clear()
                    for(binSnapShot in snapshot.children){
                            val bin=binSnapShot.getValue(bin::class.java)
                            binList.add(bin!!)
                    }
                    val mRecyclerView:RecyclerView=findViewById(R.id.recyclerVIEW)
                    val adapter=Adapter(binList)
                    mRecyclerView.adapter=adapter
                    adapter.notifyDataSetChanged()
                    mRecyclerView.setHasFixedSize(true)
                    mRecyclerView.layoutManager=LinearLayoutManager(this@MainActivity)
                    mProgressBar.visibility=View.INVISIBLE
                    println("amith 111")
                    //set on itemclick listener
                    adapter.setOnItemClickListener(object:Adapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            val areaHere=binList[position].area_name.toString()
                            val latitude=binList[position].latitude.toString()
                            val longitude=binList[position].longitude.toString()
                            val addressLine=binList[position].address.toString()
                            val intent=Intent(Intent.ACTION_VIEW, Uri.parse("google" +
                                    ".navigation:q="+latitude+","+longitude))
                            intent.setPackage("com.google.android.apps.maps")
                            startActivity(intent)
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity,error.message,Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openDialog() {
        println("amith 333")
        //inflate dialog from custom view
        val mDialogView=LayoutInflater.from(this).inflate(R.layout.add_bin_dialog,null)
        val mBuilder=AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("add a bin at your current location")
        //show dialog
        val mAlertDialog=mBuilder.show()
        //cancel button on click listener
        mDialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            mAlertDialog.dismiss()
        }
        //selectLocationBUtton onclick listener
        mDialogView.findViewById<Button>(R.id.selectLocationButton).setOnClickListener {
            if (mDialogView.findViewById<EditText>(R.id.areaField).text.isEmpty()){
                mDialogView.findViewById<EditText>(R.id.areaField).error="enter area name"
                mDialogView.findViewById<EditText>(R.id.areaField).requestFocus()
            }
            else {
                marea = mDialogView.findViewById<EditText>(R.id.areaField).text.toString()

                val intent = Intent(this, MapActivity2::class.java)
                intent.putExtra("area", marea)
                startActivity(intent)
            }
        }
        //add button on click listener

    }


}