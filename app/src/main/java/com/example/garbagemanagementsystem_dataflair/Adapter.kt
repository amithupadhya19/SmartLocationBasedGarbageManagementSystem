package com.example.garbagemanagementsystem_dataflair

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Adapter(private var bins:ArrayList<bin>):RecyclerView.Adapter<Adapter.ViewHolder>() {

    private lateinit var mListener:onItemClickListener
    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener:onItemClickListener){
        mListener=listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemview=LayoutInflater.from(parent.context).inflate(R.layout.bin_layout,parent,false)
        return ViewHolder(itemview,mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentitem=bins[position]
        holder.mAreaField.text=currentitem.area_name
        holder.mAddressLineField.text=currentitem.address
    }

    override fun getItemCount(): Int {
        return bins.size
    }

    class ViewHolder(itemview:View,listener:onItemClickListener):RecyclerView.ViewHolder(itemview){
        val mAreaField:TextView=itemview.findViewById(R.id.areaField)
        val mAddressLineField:TextView=itemview.findViewById(R.id.addressLineField)
        init {
            itemview.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }
}