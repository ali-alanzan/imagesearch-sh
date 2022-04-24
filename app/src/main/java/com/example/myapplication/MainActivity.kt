package com.example.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import java.io.ByteArrayOutputStream
import java.io.File


class MainActivity : AppCompatActivity() {
    private lateinit var mService: HelloService
    private var mBound: Boolean = false
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as HelloService.HelloServiceBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }
    private lateinit var fragmentManager: FragmentManager
    private var photoInfo = ArrayList<PhotoInfo>()
    lateinit var imagelink:String;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        photoInfo = PhotoInfoTester.createRandomphotos(10)
        Log.i(Globals.TAG, "Activity 1 onCreate")
        Toast.makeText(this, "Activity onCreate", Toast.LENGTH_SHORT).show()


    }

    fun switchFragment(v: View) {
        Toast.makeText(
            this,
            "Activity switchFragment. Tag" + v.getTag().toString(),
            Toast.LENGTH_SHORT
        ).show()

        fragmentManager = supportFragmentManager

        if (Integer.parseInt(v.getTag().toString()) == 1) {
            fragmentManager
                .beginTransaction()
                .replace(
                    R.id.myframe,
                    SelectFragment(),
                    "Fragment1"
                )
                .commit()
         //   formData()

        }
        if (Integer.parseInt(v.getTag().toString()) == 2) {
            fragmentManager
                .beginTransaction()
                .replace(
                    R.id.myframe,
                    resoultFragment(photoInfo),
                    "Fragment2"
                )
                .commit()
            getMethod()
        }
        if (Integer.parseInt(v.getTag().toString()) == 3) {
            fragmentManager
                .beginTransaction()
                .replace(
                    R.id.myframe,
                    SavedFragment(),
                    "Fragment3"
                )
                .commit()
        }

    }

            fun submit(view: View) {
                   var nameViewText = (fragmentManager.findFragmentByTag("Fragment1") as SelectFragment).photoName.text.toString()
                   var imageUri = (fragmentManager.findFragmentByTag("Fragment1") as SelectFragment).imageUri
                   var imageRect = (fragmentManager.findFragmentByTag("Fragment1") as SelectFragment).imageView.actualCropRect
                    val newPhoto : PhotoInfo = PhotoInfo(nameViewText,imageUri,imageRect.left.toInt(),imageRect.top.toInt()
                              ,imageRect.width().toInt(),imageRect.height().toInt())
                        photoInfo.add(newPhoto)
                Toast.makeText(this, "Added New Student", Toast.LENGTH_SHORT).show()
            }



    /////////////////////////////the full mode//////////////////////
    fun formData(bitmap:Bitmap) {

        val imgOutputString= ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG,50,imgOutputString)

            Thread(Runnable {
                // Create Retrofit
                val retrofit = Retrofit.Builder()
                    .baseUrl("http://api-edu.gtl.ai")
                    .build()

                // Create Service
                val service = retrofit.create(APIService::class.java)

                // List of all MIME Types you can upload: https://www.freeformatter.com/mime-types-list.html

                // Get file from assets folder
               //  val file =getFileFromAssets(this, "car.png")
                val mediaType= "image/png".toMediaTypeOrNull()
                val fields: HashMap<String?, RequestBody?> = HashMap()

              //  fields["email"] = ("jack@email.com").toRequestBody("text/plain".toMediaTypeOrNull())
                fields["image\"; filename=\"test.png\" "] = (imgOutputString.toByteArray()).toRequestBody(mediaType)
               // RequestBody.create(mediaType,imgOutputString.toByteArray())



               // var requestFile = file.asRequestBody("image/png".toMediaTypeOrNull())
              //  val req=MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("image","test.png",
                //    imgOutputString.toByteArray().toRequestBody(mediaType, 0)
               // ).build()

               // var body = MultipartBody.Part.createFormData("image", "test.png", imgOutputString.toByteArray().toRequestBody(mediaType))



                CoroutineScope(Dispatchers.IO).launch {

                    // Do the POST request and get response


                    val response = service.uploadEmployeeData(fields)





                    //some method here

                    // val response = service.uploadEmployeeData(body)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {

                            imagelink = response.body()?.string().toString();


                            // Convert raw JSON to pretty JSON using GSON library

                            /* val gson = GsonBuilder().setPrettyPrinting().create()
                               val prettyJson = gson.toJson(
                                   JsonParser.parseString(
                                       response.body()
                                           ?.string() // About this thread blocking annotation : https://github.com/square/retrofit/issues/3255
                                   )
                               )
           */
                            Log.d("Pretty Printed JSON :", imagelink)


                        } else {

                            Log.e("RETROFIT_ERROR", response.code().toString())

                        }
                    }



                }

            }).start()



    }
    fun getFileFromAssets(context: Context, fileName: String): File =
        File(context.cacheDir, fileName)
            .also {
                if (!it.exists()) {
                    it.outputStream().use { cache ->
                        context.assets.open(fileName).use { inputStream ->
                            inputStream.copyTo(cache)
                        }
                    }
                }
            }

    fun getMethod() {
        Thread(Runnable {

        // Create Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://api-edu.gtl.ai")
            .build()

        // Create Service
        val service = retrofit.create(APIService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            /*
             * For @Query: You need to replace the following line with val response = service.getEmployees(2)
             * For @Path: You need to replace the following line with val response = service.getEmployee(53)
             */

            // Do the GET request and get response
            val response = service.getFromBing(imagelink)
            //val response = service.getFromGoogle(imagelink)
           // val response = service.getFromTineye(imagelink)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {

                    // Convert raw JSON to pretty JSON using GSON library
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val prettyJson = gson.toJson(
                        JsonParser.parseString(
                            response.body()
                                ?.string() // About this thread blocking annotation : https://github.com/square/retrofit/issues/3255
                        )
                    )

                    Log.d("Pretty Printed JSON :", prettyJson)

                } else {

                    Log.e("RETROFIT_ERROR", response.code().toString())

                }
            }
        }
    }).start()




    }



    override fun onStart() {
        super.onStart()
        Log.i(Globals.TAG, "Activity 1 onStart")
        Toast.makeText(this, "Activity onStart", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        Log.i(Globals.TAG, "Activity 1 onResume")
        Toast.makeText(this, "Activity onResume", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        Log.i(Globals.TAG, "Activity 1 onPause")
        Toast.makeText(this, "Activity onPause", Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
        Log.i(Globals.TAG, "Activity 1 onStop")
        Toast.makeText(this, "Activity onStop", Toast.LENGTH_SHORT).show()
    }

    override fun onRestart() {
        super.onRestart()
        Log.i(Globals.TAG, "Activity 1 onRestart")
        Toast.makeText(this, "Activity onRestart", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(Globals.TAG, "Activity 1 onDestroy")
        Toast.makeText(this, "Activity onDestroy", Toast.LENGTH_SHORT).show()
    }








}



