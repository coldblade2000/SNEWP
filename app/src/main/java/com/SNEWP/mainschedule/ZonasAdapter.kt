package com.SNEWP.mainschedule

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import kotlin.collections.ArrayList

class ZonasAdapter(private val myDataset: ArrayList<Zona>, val context:Context, val mInterface: zonasAdapInterface) :
        RecyclerView.Adapter<ZonasAdapter.ViewHolder>(){

    interface zonasAdapInterface{
        fun onZoneSelection(position: Int, boolean: Boolean)
    }
    val checkedZones = arrayListOf<Zona>()
    class ViewHolder(val checkBox: CheckBox, val context : Context, val mInterface: zonasAdapInterface) : RecyclerView.ViewHolder(checkBox)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val checkBox = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_zona_list_dialog_item, parent, false) as CheckBox
        return ViewHolder(checkBox, context, mInterface)
    }

    override fun getItemCount() = myDataset.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if(!payloads.isNullOrEmpty()){
            holder.checkBox.isChecked = !holder.checkBox.isChecked
//            myDataset[position].checked = !myDataset[position].checked!!
//            mInterface.onZoneSelection(position, myDataset[position].checked!!)
        }else{
            super.onBindViewHolder(holder, position, payloads)

        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        if(myDataset[position].checked != null){
            holder.checkBox.isChecked = myDataset[position].checked!!
            if(myDataset[position].checked!!){
                checkedZones.add(myDataset[position])
            }
        }else{
            holder.checkBox.isChecked = false
            myDataset[position].checked = false
        }
        holder.checkBox.text = myDataset[position].nombresAsString
        holder.checkBox.setOnCheckedChangeListener{ _, isChecked ->
            myDataset[position].apply {
                checked = isChecked
                if(isChecked == true){
                    checkedZones.add(this)
                    polygon?.strokeWidth = 6F
                }else{
                    checkedZones.remove(this)
                    polygon?.strokeWidth = 0.5f
                }
            }
            mInterface.onZoneSelection(position, isChecked)

        }

    }

}