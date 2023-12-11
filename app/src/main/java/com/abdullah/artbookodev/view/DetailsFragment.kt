package com.abdullah.artbookodev.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.abdullah.artbookodev.R
import com.abdullah.artbookodev.databinding.FragmentDetailsBinding
import com.abdullah.artbookodev.model.Art
import com.abdullah.artbookodev.roomdb.ArtDao
import com.abdullah.artbookodev.roomdb.ArtDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


import java.io.ByteArrayOutputStream


class DetailsFragment : Fragment() {
  private lateinit var binding : FragmentDetailsBinding
  private  lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
  private  lateinit var permissiLauncher: ActivityResultLauncher<String>
  var selectedBitmap : Bitmap? = null // resimin işlenip kullanıcıya gösterileceği son hali bu değişkene kaydedilir.Ayrıca kayıt ve delete işlemleri için global olarak kullanılabilir.
  private lateinit var artDatabase: ArtDatabase
  private lateinit var artDao: ArtDao
  var artFromMain : Art? = null
  private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            registerLauncher()
        artDatabase = Room.databaseBuilder(requireContext(),ArtDatabase::class.java,"Arts").build()
        artDao = artDatabase.artDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailsBinding.inflate(layoutInflater,container,false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageView.setOnClickListener { selectedImage(view) }
        binding.button1.setOnClickListener { save(view) }
        binding.button2.setOnClickListener { delete(view) }

        arguments?.let {
            val info = DetailsFragmentArgs.fromBundle(it).info
            if(info == "new"){
                binding.artName.setText("")
                binding.artistName.setText("")
                binding.year.setText("")
                binding.button1.visibility = View.VISIBLE
                binding.button2.visibility = View.GONE
                val selectedImageBackground = BitmapFactory.decodeResource(context?.resources,R.drawable.selectimage)
                binding.imageView.setImageBitmap(selectedImageBackground)
            }else{
                binding.button1.visibility = View.GONE
                binding.button2.visibility = View.VISIBLE
                val selectedId =DetailsFragmentArgs.fromBundle(it).id
                compositeDisposable.add(
                    artDao.getById(selectedId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponseOldArt)
                )


            }
        }
    }

    private fun handleResponseOldArt(art:Art){
        artFromMain = art
        binding.artName.setText(art.artName)
        binding.artistName.setText(art.artistName)
        binding.year.setText(art.year)
        art.image?.let {
            val bitmap = BitmapFactory.decodeByteArray(it,0,it.size)
            binding.imageView.setImageBitmap(bitmap)
        }
    }





    fun selectedImage(view:View){

        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(requireActivity().applicationContext,Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                //rationale
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)) {
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",
                        View.OnClickListener {
                            //request permission
                            permissiLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()
                } else{
                    //request permission
                    permissiLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)

                }

            }else{
                val intentToGallery = Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }

        }else{
            if(ContextCompat.checkSelfPermission(requireActivity().applicationContext,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //rationale
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",
                        View.OnClickListener {
                            //request permission
                            permissiLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()
                } else{
                    //request permission
                    permissiLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                }

            }else{
                val intentToGallery = Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }

        }



    }

    fun save(view:View){

        val artName = binding.artName.text.toString()
        val artistName = binding.artistName.text.toString()
        val year = binding.year.text.toString()

        if(selectedBitmap != null){
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!,300)
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()

            val art = Art(artName,artistName,year,byteArray)

            compositeDisposable.add(artDao.insert(art)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse))

        }else{
            Toast.makeText(requireContext(),"Enter complete information",Toast.LENGTH_SHORT).show()
        }
    }

    fun delete(view:View){
        artFromMain.let {
            compositeDisposable.add(
                artDao.delete(it!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)
            )
        }

    }

    private fun handleResponse(){
            val action = DetailsFragmentDirections.actionDetailsFragmentToArtListFragment()
            Navigation.findNavController(requireView()).navigate(action)
    }

    private fun makeSmallerBitmap(image:Bitmap,maximumSize:Int):Bitmap{ // resimleri veritabanlarına kaydetmeden önce küçülten fonksiyon
        var width = image.width // resmin gerçek genişliği
        var height = image.height // resmin gerçek uzunluğu

        val bitmapRatio :Double = width.toDouble() / height.toDouble()

        if(bitmapRatio > 1){ // bitmap ratio birden büyükse yatay resimdir
            width = maximumSize
            val scaledHeight = width / bitmapRatio // alınan max size bitmap ratioya bölünerek aynmı oranda uzunluk belirlenir
            height = scaledHeight.toInt() // ve uzunluğa eşitlenir
        }else{ // değilse dikey resimdir çünkü genişliği uzunluğa bölüyoruz
            height = maximumSize
            val scaledWidth = height * bitmapRatio // bitmapRatio 0 dan küçük olduğu için uzunlukla orantılı genişlik çıkacak
            width = scaledWidth.toInt()

        }

        return Bitmap.createScaledBitmap(image,width,height,true)
    }


    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ // galeriden bir sonuç için intent döner yani veri ile
            result ->
            if(result.resultCode == RESULT_OK){ // başarılı bir şekilde döndü mü diye kontrol edilir
                val intentFromResult = result.data // getIntent yapılır nullable döner
                if(intentFromResult != null){ // eğer intentFromResult null değilse bir veri dönüyor aşağıdaki scope'da bu alınır.
                    val imageData = intentFromResult.data // resim verisinin galeride ki urisini veriyor ve bunu da nullable veriyor

                    if(imageData != null){
                        try {   // eldeki uri'yi normal resime döndereceğimiz için try-catch kullanılır.

                            if(Build.VERSION.SDK_INT >= 28){ // API 28 ve büyükse bu yöntem ile yapılır
                                val source = ImageDecoder.createSource(requireActivity().contentResolver,imageData)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)

                            }else{ // API 28 den küçükse kullanıcının telefonunda bu kodlar çalışır
                                selectedBitmap = Media.getBitmap(requireActivity().contentResolver,imageData)
                                binding.imageView.setImageBitmap(selectedBitmap)

                            }

                        }catch (e:Exception){
                            e.stackTrace
                        }
                    }
                }

            }
        }
        permissiLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            result->
            if(result){
                // permission granted
                val intentToGallery = Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                // permission denied
                Toast.makeText(requireActivity().applicationContext,"Give Permissionaaa",Toast.LENGTH_SHORT).show()
            }
        }

    }

}