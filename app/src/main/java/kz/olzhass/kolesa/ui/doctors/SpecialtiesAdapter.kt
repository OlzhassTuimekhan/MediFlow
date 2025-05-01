package kz.olzhass.kolesa.ui.doctors

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kz.olzhass.kolesa.R

class SpecialtiesAdapter(
    private var specialties: List<Specialty>,
    private val onItemClick: (Specialty) -> Unit
) : RecyclerView.Adapter<SpecialtiesAdapter.SpecialtyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecialtyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_specialty, parent, false)
        return SpecialtyViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpecialtyViewHolder, position: Int) {
        val specialty = specialties[position]
        holder.bind(specialty, onItemClick)
    }

    override fun getItemCount(): Int = specialties.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newSpecialties: List<Specialty>) {
        specialties = newSpecialties
        notifyDataSetChanged()
    }

    class SpecialtyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val specialtyName: TextView = itemView.findViewById(R.id.specialtyName)

        fun bind(specialty: Specialty, onItemClick: (Specialty) -> Unit) {
            specialtyName.text = specialty.name
            itemView.setOnClickListener {
                Log.d("Specialty", "Clicked: ${specialty.name}")
                onItemClick(specialty) }
        }
    }
}
