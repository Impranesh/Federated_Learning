// CsvDataAdapter.kt
package com.example.federatedlearning

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CsvDataAdapter(private val csvDataList: List<Array<String>>) :
    RecyclerView.Adapter<CsvDataAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewCsvLine: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val csvLine = csvDataList[position].joinToString(", ")
        holder.textViewCsvLine.text = csvLine
    }

    override fun getItemCount(): Int {
        return csvDataList.size
    }
}
