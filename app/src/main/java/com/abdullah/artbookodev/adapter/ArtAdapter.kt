package com.abdullah.artbookodev.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.abdullah.artbookodev.databinding.ItemRowBinding
import com.abdullah.artbookodev.model.Art
import com.abdullah.artbookodev.view.ArtListFragmentDirections

class ArtAdapter(val artList:List<Art>): RecyclerView.Adapter<ArtAdapter.ArtHolder>() {

    class ArtHolder(val binding: ItemRowBinding): RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        val binding = ItemRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ArtHolder(binding)
    }

    override fun getItemCount(): Int {
        return artList.size

    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
        holder.binding.recyclerView.text = artList[position].artName
        holder.itemView.setOnClickListener{
            val action = ArtListFragmentDirections.actionArtListFragmentToDetailsFragment("old",id=artList[position].id)
            Navigation.findNavController(it).navigate(action)
        }
    }

}