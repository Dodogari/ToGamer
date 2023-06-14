package com.example.to_gammer_app

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.to_gammer_app.databinding.ActivitySettingProfileBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.File
import java.io.FileOutputStream
import java.util.*

class SettingProfileActivity : AppCompatActivity() {
    private lateinit var settingProfileBinding: ActivitySettingProfileBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var imageUri: Uri

    //set profile image
    private var imgFlag = 0
    private var fileAbsolutePath: String? = null
    private var bitmap: Bitmap? = null
    private var imgState = 0

    //firebase storage
    private lateinit var storageRef: StorageReference
    private lateinit var profilePicRef: StorageReference

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingProfileBinding = ActivitySettingProfileBinding.inflate(layoutInflater)
        setContentView(settingProfileBinding.root)

        //load image
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == RESULT_OK) {
                //camera
                if(imgFlag == 0) {
                    val file = File(fileAbsolutePath!!)

                    //picture decoding -> bitmap
                    val decode = ImageDecoder.createSource(this.contentResolver,
                        Uri.fromFile(file.absoluteFile))
                    bitmap = ImageDecoder.decodeBitmap(decode)

                    //show image
                    settingProfileBinding.imgProfile.setImageBitmap(bitmap)
                    if (bitmap != null) {
                        saveImageFile(file.name, getExtension(file.name), bitmap!!)
                    }

                    imgState = 0
                }
                //gallery
                else if(imgFlag == 1) {
                    result.data?.data?.let { uri ->
                        imageUri = result.data?.data!!
                        //image exists
                        Glide.with(applicationContext).load(imageUri).override(500, 500)
                            .into(settingProfileBinding.imgProfile)
                    }
                    imgState = 1
                }
            }
        }


        //click camera
        settingProfileBinding.buttonCamera.setOnClickListener {
            checkCamAuthority()
        }

        //click gallery
        settingProfileBinding.buttonGallery.setOnClickListener {
            checkGallAuthority()
        }

        //click basic
        settingProfileBinding.buttonBasic.setOnClickListener {
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.profile_basic)
            settingProfileBinding.imgProfile.setImageBitmap(bitmap)
            imgState = 2
        }

        //upload to firebase
        //instance of storage

        auth = FirebaseAuth.getInstance()

        val uid = auth.currentUser!!.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        //upload button
        settingProfileBinding.buttonSignUp.setOnClickListener {
            val userName = settingProfileBinding.username.text.toString()

            val profile = ProfileDTO(userName)

            databaseReference.child(uid).setValue(profile).addOnCompleteListener {
                if(it.isSuccessful)
                    uploadProfileImg()
            }
        }
        /*storageRef = FirebaseStorage.getInstance().reference

        val fileName = "IMAGE_${SimpleDateFormat("yyyymmdd_HHmmss").format(Date())}_.png"
        val imageRef = storageRef.child("images/").child(fileName)

        imageRef.putFile(imageUri).continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask imageRef.downloadUrl
        }.addOnSuccessListener {
            var profileDTO = ProfileDTO()
            profileDTO.profileImageUrl = it.toString()
        }

        settingProfileBinding.username.text = fire


        val currentUser = FirebaseAuth.getInstance().currentUser

        profilePicRef = storageRef.child("images/" + UUID.randomUUID().toString() + ".jpg")
        profilePicRef.downloadUrl.addOnSuccessListener {
            Glide.with(this).load(imageUri).into(settingProfileBinding.imgProfile)
        }.addOnFailureListener {
            Toast.makeText(this, "프로필 사진을 업로드하지 못했습니다.", Toast.LENGTH_LONG).show()
        }*/
    }

    private fun uploadProfileImg() {
        //if profile image is basic -> local uri
        if(imgState == 2) {
            imageUri = Uri.parse("android.resources://$packageName/${R.drawable.profile_basic}")
            storageRef = FirebaseStorage.getInstance().getReference("Users/" + auth.currentUser?.uid)
            storageRef.putFile(imageUri).addOnSuccessListener {
                Toast.makeText(this, "프로필 저장 성공", Toast.LENGTH_SHORT).show()
                successSignUp()
            }.addOnFailureListener {
                Toast.makeText(this, "프로필 저장 실패", Toast.LENGTH_SHORT).show()
            }
        }

        else {
            storageRef = FirebaseStorage.getInstance().reference

            val fileName = "IMAGE_${SimpleDateFormat("yyyymmdd_HHmmss").format(Date())}_.png"
            val imageRef = storageRef.child("images/").child(fileName)

            imageRef.putFile(imageUri).continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                return@continueWithTask imageRef.downloadUrl
            }.addOnSuccessListener {
                Toast.makeText(this, "프로필 저장 성공", Toast.LENGTH_SHORT).show()
                successSignUp()
            }.addOnFailureListener {
                Toast.makeText(this, "프로필 저장 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun successSignUp() {
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
        if(!isFinishing) finish()
    }

    //gallery authority check
    private fun checkGallAuthority() {
        val galleryPermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)

        when (galleryPermission) {
            PackageManager.PERMISSION_GRANTED -> {
                requestGallery()
            }
            PackageManager.PERMISSION_DENIED -> {
                val dlg = AlertDialog.Builder(this)
                dlg.setTitle("갤러리 접근 권한")
                dlg.setMessage("갤러리 사진을 첨부하려면 외부 저장소 권한이 필요합니다.")
                dlg.setPositiveButton("확인") { dialog, which ->
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_READ_EXTERNAL_STORAGE)
                }
                dlg.setNegativeButton("취소", null)
                dlg.show()
            }
            else -> {
                //request authority
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_EXTERNAL_STORAGE)
            }
        }
    }

    //camera authority check
    private fun checkCamAuthority() {
        val cameraPermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA)

        when (cameraPermission) {
            PackageManager.PERMISSION_GRANTED -> {
                requestCamera()
            }
            PackageManager.PERMISSION_DENIED -> {
                val dlg = AlertDialog.Builder(this)
                dlg.setTitle("카메라 접근 권한")
                dlg.setMessage("카메라를 사용하기 위해선 외부 저장소 권한이 필요합니다.")
                dlg.setPositiveButton("확인") { dialog, which ->
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.CAMERA),
                        REQUEST_CAMERA)
                }
                dlg.setNegativeButton("취소", null)
                dlg.show()
            }
            else -> {
                //request authority
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CAMERA)
            }
        }
    }

    private fun saveImageFile(filename: String, mimeType: String, bitmap: Bitmap): Uri? {
        val values = ContentValues()
        //contentValues's name, type
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            //ignore request from others
            values.put(MediaStore.Images.Media.IS_PENDING, 1)

        // MediaStore에 파일 등록
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            //file descriptor
            val descriptor = contentResolver.openFileDescriptor(uri, "w")
            if (descriptor != null) {
                //save bitmap
                val fos = FileOutputStream(descriptor.fileDescriptor)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.close()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    //release ignore
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(uri, values, null, null)
                }
            }
        }
        return uri
    }


    @SuppressLint("SimpleDateFormat")
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        //set uri
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            //절대경로 변수에 저장
            fileAbsolutePath = absolutePath
        }
    }

    //image extension
    private fun getExtension(fileStr: String): String = fileStr.substring(fileStr.lastIndexOf(".") + 1, fileStr.length)

    private fun requestGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        resultLauncher.launch(intent)
        //1번 -> 갤러리
        imgFlag = 1
    }

    //camera
    private fun requestCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                //image to file
                val photoFile: File = createImageFile()

                //File to Content
                photoFile.also {
                    val photoURI: Uri = FileProvider.getUriForFile(this,
                        "com.example.to_gammer_app.fileprovider", it)

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    resultLauncher.launch(takePictureIntent)
                }
            }
            //2번 -> 카메라
            imgFlag = 0
        }
    }

    companion object {
        const val REQUEST_READ_EXTERNAL_STORAGE = 1000
        const val REQUEST_CAMERA = 0
    }
}