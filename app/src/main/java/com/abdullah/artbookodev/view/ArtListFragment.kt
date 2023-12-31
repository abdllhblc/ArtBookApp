package com.abdullah.artbookodev.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.abdullah.artbookodev.R
import com.abdullah.artbookodev.adapter.ArtAdapter
import com.abdullah.artbookodev.databinding.FragmentArtListBinding
import com.abdullah.artbookodev.model.Art
import com.abdullah.artbookodev.roomdb.ArtDao
import com.abdullah.artbookodev.roomdb.ArtDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class ArtListFragment : Fragment() {
    private lateinit var artAdapter : ArtAdapter
    private lateinit var binding: FragmentArtListBinding
    private val compositeDisposable = CompositeDisposable()
    private lateinit var artDao: ArtDao
    private lateinit var artDatabase: ArtDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        artDatabase = Room.databaseBuilder(requireContext(),ArtDatabase::class.java,"Arts").build()
        artDao = artDatabase.artDao()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentArtListBinding.inflate(layoutInflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getFromSQL()
    }

     fun getFromSQL(){
         compositeDisposable.add(
             artDao.getArtWithNameAndId()
                 .subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe(this::handleResponse)
         )

     }
    private fun handleResponse(artList :List<Art>){
        binding.recyclerView.layoutManager =LinearLayoutManager(requireContext())
        artAdapter = ArtAdapter(artList)
       binding.recyclerView.adapter = artAdapter

    }


}