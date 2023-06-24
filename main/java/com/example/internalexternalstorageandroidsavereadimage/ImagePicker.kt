package com.example.internalexternalstorageandroidsavereadimage

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.example.internalexternalstorageandroidsavereadimage.databinding.ActivityImagePickerBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class ImagePicker : AppCompatActivity() {

    lateinit var binding:ActivityImagePickerBinding

   val storagePermissionContract=registerForActivityResult(ActivityResultContracts.RequestPermission()){
       if (it){
           pickImageFromgallery()
       }else{
           Toast.makeText(this, "Allow permission", Toast.LENGTH_SHORT).show()
       }
   }

    var galleryContract=registerForActivityResult(ActivityResultContracts.GetContent()){
        if (it!=null ){
            imageUri=it
            binding.ivThumbnail.setImageURI(imageUri)
        }
    }

    var cameraContract=registerForActivityResult(ActivityResultContracts.TakePicture()){
        if(it){
            binding.ivThumbnail.setImageURI(imageUri)
        }
    }
    var imageUri:Uri?=null

    private fun pickImageFromgallery() {
        var intent=Intent()
        intent.type="image/*"
      //  intent.action=Intent.ACTION_GET_CONTENT
        intent.action=Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,100)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityImagePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageUri=createImageURI()

        binding.ivThumbnail.setOnClickListener{
            showOptionDialog()
        }
        binding.btnSaveImage.setOnClickListener {
            if (imageUri !=null){
                var bitmap= uriToBitmap(imageUri!!)
                bitmap?.let{
                    saveImageToInternalStorage(this,it)
                }
            }
        }
           binding.btnGetImage.setOnClickListener {
                var file=File(filesDir,"images/IMG_1681898720719.png")
                var bitmap=fileToBitmap(file)
                bitmap?.let{
                    binding.ivThumbnail2.setImageBitmap(it)
                }
            }


    }

   private fun fileToBitmap(file: File): Bitmap? {
        try {
            return BitmapFactory.decodeFile(file.path)

        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }
        return null
    }

    private fun showOptionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Image")
            .setItems(
                arrayOf("Take Photo","From Gallery"),
                DialogInterface.OnClickListener { dialogInterface, i ->
                    if (i==0){
                        //Camera
                        cameraContract.launch(imageUri)
                    } else {
                        //storagePermissionContract.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        galleryContract.launch("image/*")
                    }
                }).show()
    }

    private fun uriToBitmap(imageUri: Uri): Bitmap? {
        var inputStream:InputStream?=null
        try {
            inputStream=contentResolver.openInputStream(imageUri)

            return BitmapFactory.decodeStream(inputStream)
        }catch (e:IOException){
            e.printStackTrace()
        }finally {
            inputStream!!.close()
        }
            return null
    }

    private fun saveImageToInternalStorage(context:Context,bitmap:Bitmap) {
        var filename="IMG_${System.currentTimeMillis()}.png"
        var directory=File(filesDir,"images")

        if (!directory.exists()){
            directory.mkdir()
        }
        try {
            var file =File(directory,filename)
            var fout=FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG,100,fout)
            fout.close()

            Toast.makeText(this, "File saved", Toast.LENGTH_SHORT).show()

        }catch (e:Exception){
            e.printStackTrace()
        }

    }

    private fun createImageURI(): Uri? {
        var filename="${System.currentTimeMillis()}.png"
        var file=File(filesDir,filename)
        return FileProvider.getUriForFile(
            this,
            "com.example.internalexternalstorageandroidsavereadimage.fileprovider",
            file
        )

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==100 && resultCode== RESULT_OK){
            data?.let{
                binding.ivThumbnail.setImageURI(it.data)
            }
        }
    }
}