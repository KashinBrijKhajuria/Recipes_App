package my.app.recipe.view.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.ScriptGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.karumi.dexter.Dexter
import my.app.recipe.R
import my.app.recipe.databinding.ActivityAddUpdateDishBinding
import my.app.recipe.databinding.DialogCustomImageSelectionBinding
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.audiofx.BassBoost
import android.net.Uri
import android.provider.LiveFolders.INTENT
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import my.app.recipe.application.RecipeApplication
import my.app.recipe.databinding.DialogCustomListBinding
import my.app.recipe.model.entities.Recipe
import my.app.recipe.utils.Constants
import my.app.recipe.view.adapters.CustomListItemAdapter
import my.app.recipe.viewModel.RecipeViewModel
import my.app.recipe.viewModel.RecipeViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

@Suppress("DEPRECATION")
class AddUpdateDish : AppCompatActivity() , View.OnClickListener{

    private lateinit var mBinding : ActivityAddUpdateDishBinding
    private var mImagePath : String = ""
    private lateinit var customListDialog : Dialog
    private var mFavDishDetails: Recipe? = null

    private val mRecipeViewModel: RecipeViewModel by viewModels {
        RecipeViewModelFactory((application as RecipeApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityAddUpdateDishBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        if (intent.hasExtra(Constants.EXTRA_DISH_DETAILS)) {
            mFavDishDetails = intent.getParcelableExtra(Constants.EXTRA_DISH_DETAILS)
        }

        setupActionBar()

        mFavDishDetails?.let {
            if (it.id != 0) {
                mImagePath = it.image

                // Load the dish image in the ImageView.
                Glide.with(this@AddUpdateDish)
                    .load(mImagePath)
                    .centerCrop()
                    .into(mBinding.ivDishImage)

                mBinding.etTitle.setText(it.title)
                mBinding.etType.setText(it.type)
                mBinding.etCategory.setText(it.category)
                mBinding.etIngredients.setText(it.ingredients)
                mBinding.etCookingTime.setText(it.cookingTime)
                mBinding.etDirectionToCook.setText(it.directionToCook)

                mBinding.btnAddDish.text = resources.getString(R.string.lbl_update_dish)
            }
        }

        mBinding.ivAddDishImage.setOnClickListener(this)
        mBinding.etType.setOnClickListener(this)
        mBinding.etCategory.setOnClickListener(this)
        mBinding.etCookingTime.setOnClickListener(this)
        mBinding.btnAddDish.setOnClickListener(this)

    }

    private fun setupActionBar() {
        setSupportActionBar(mBinding.toolbarAddDishActivity)
        if (mFavDishDetails != null && mFavDishDetails!!.id != 0) {
            supportActionBar?.let {
                it.title = resources.getString(R.string.title_edit_dish)
            }
        } else {
            supportActionBar?.let {
                it.title = resources.getString(R.string.title_add_dish)
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)


        mBinding.toolbarAddDishActivity.setNavigationOnClickListener{
            onBackPressed()
        }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when(v.id){
                R.id.iv_add_dish_image->{
                    customImageSelectionDialogue()
                    return
                }
                R.id.et_type->{
                    customItemDialog( resources.getString(R.string.title_select_dish_type) , Constants.dishTypes() , Constants.DISH_TYPE)
                    return
                }
                R.id.et_category->{
                    customItemDialog( resources.getString(R.string.title_select_dish_category) , Constants.dishCategories() , Constants.DISH_CATEGORY)
                    return
                }
                R.id.et_cooking_time->{
                    customItemDialog( resources.getString(R.string.title_select_dish_cooking_time) , Constants.dishCookTime() , Constants.DISH_COOKING_TIME)
                    return
                }

                R.id.btn_add_dish -> {

                    // Define the local variables and get the EditText values.
                    // For Dish Image we have the global variable defined already.

                    val title = mBinding.etTitle.text.toString().trim { it <= ' ' }
                    val type = mBinding.etType.text.toString().trim { it <= ' ' }
                    val category = mBinding.etCategory.text.toString().trim { it <= ' ' }
                    val ingredients = mBinding.etIngredients.text.toString().trim { it <= ' ' }
                    val cookingTimeInMinutes = mBinding.etCookingTime.text.toString().trim { it <= ' ' }
                    val cookingDirection = mBinding.etDirectionToCook.text.toString().trim { it <= ' ' }

                    when {

                        TextUtils.isEmpty(mImagePath) -> {
                            Toast.makeText(
                                    this@AddUpdateDish,
                                    resources.getString(R.string.err_msg_select_dish_image),
                                    Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(title) -> {
                            Toast.makeText(
                                    this@AddUpdateDish,
                                    resources.getString(R.string.err_msg_enter_dish_title),
                                    Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(type) -> {
                            Toast.makeText(
                                    this@AddUpdateDish,
                                    resources.getString(R.string.err_msg_select_dish_type),
                                    Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(category) -> {
                            Toast.makeText(
                                    this@AddUpdateDish,
                                    resources.getString(R.string.err_msg_select_dish_category),
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
                        TextUtils.isEmpty(ingredients) -> {
                            Toast.makeText(
                                    this@AddUpdateDish,
                                    resources.getString(R.string.err_msg_enter_dish_ingredients),
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
                        TextUtils.isEmpty(cookingTimeInMinutes) -> {
                            Toast.makeText(
                                    this@AddUpdateDish,
                                    resources.getString(R.string.err_msg_select_dish_cooking_time),
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
                        TextUtils.isEmpty(cookingDirection) -> {
                            Toast.makeText(
                                    this@AddUpdateDish,
                                    resources.getString(R.string.err_msg_enter_dish_cooking_instructions),
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            var dishID = 0
                            var imageSource = Constants.DISH_IMAGE_SOURCE_LOCAL
                            var favoriteDish = false

                            mFavDishDetails?.let {
                                if (it.id != 0) {
                                    dishID = it.id
                                    imageSource = it.imageSource
                                    favoriteDish = it.favoriteDish
                                }
                            }

                            val recipeDetails: Recipe = Recipe(
                                mImagePath,
                                Constants.DISH_IMAGE_SOURCE_LOCAL,
                                title,
                                type,
                                category,
                                ingredients,
                                cookingTimeInMinutes,
                                cookingDirection,
                                favoriteDish,
                                dishID
                            )

                            if (dishID == 0) {
                                mRecipeViewModel.insert(recipeDetails)

                            } else {
                                mRecipeViewModel.update(recipeDetails)

                            }
                            finish()

                        }
                    }}
            }
        }}

    private fun customImageSelectionDialogue() {
        val dialog = Dialog(this)
        val binding: DialogCustomImageSelectionBinding =
            DialogCustomImageSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.show()

        binding.tvCamera.setOnClickListener {
            Dexter.withContext(this@AddUpdateDish).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?. let {
                        if(report.areAllPermissionsGranted()){
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            startActivityForResult(intent,CAMERA)
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                    //showRationaleDialogForPermission()
                }

            }).onSameThread().check()
            dialog.dismiss()
        }

        binding.tvGallery.setOnClickListener {
            Dexter.withContext(this).withPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,

            ).withListener(object : PermissionListener {
//

                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {

                    val galleryIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent , GALLERY)
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

            }).onSameThread().check()
            dialog.dismiss()
        }
    }
//        fun showRationaleDialogForPermission(){
//            AlertDialog.Builder(this).setMessage(" Go to settings to give permission ")
//                .setPositiveButton("GO TO SETTINGS"){
//                    _,_ ->
//                    try {
//                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                        val uri = Uri.fromParts("package",packageName, null)
//                        intent.data = uri
//                        startActivity(intent)
//
//                    }catch (e:ActivityNotFoundException){
//                        e.printStackTrace()
//                    }
//                }
//                .setNegativeButton("CANCEL"){
//                    dialog,_->
//                    dialog.dismiss()
//                }.show()
//        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == CAMERA){
                data?.extras?. let{
                    val thumbnail  : Bitmap = data.extras!!.get("data") as Bitmap
//                    mBinding.ivDishImage.setImageBitmap(thumbnail)
//                    val thumbnail = data.extras!!.get("data")




                    Glide.with(this)
                            .load(thumbnail)
                            .centerCrop()
                            .into(mBinding.ivDishImage)
                    mImagePath = saveImageToInternalStorage(thumbnail)


                    mBinding.ivAddDishImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_vector_edit))
                }
            }
            if(requestCode == GALLERY){
                data?. let{
                    val selectedImage   = data.data


                    Glide.with(this)
                            .load(selectedImage)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .listener(object: RequestListener<Drawable>{
                                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                    return false
                                }

                                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                    resource?.let{
                                        val bitmap : Bitmap = resource.toBitmap()
                                        mImagePath = saveImageToInternalStorage(bitmap)
                                    }
                                    return false
                                }

                            })
                            .into(mBinding.ivDishImage)

                    mBinding.ivAddDishImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_vector_edit))
                }
            }

        }
    }

    private fun saveImageToInternalStorage( bitmap : Bitmap) : String{
        val wrapper = ContextWrapper(this)
        var file = wrapper.getDir(IMAGE_DIRECTORY , MODE_PRIVATE)
        file = File( file , "${UUID.randomUUID()}.jpg" )

        try {
            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG , 100 , stream)
            stream.flush()
            stream.close()

        }catch ( e : IOException){
            e.printStackTrace()
        }
        return file.absolutePath
    }

    private fun customItemDialog( title: String , itemList : List<String> , selection : String ){
        customListDialog = Dialog(this)
        val binding : DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)
        customListDialog.setContentView(binding.root)


        binding.tvTitle.text = title
        binding.rvList.layoutManager = LinearLayoutManager(this)
        val adapter = CustomListItemAdapter(this ,  null , itemList , selection)
        binding.rvList.adapter = adapter
        customListDialog.show()




    }

    fun selectedListItem(item: String, selection: String) {

        when (selection) {

            Constants.DISH_TYPE -> {
                customListDialog.dismiss()
                mBinding.etType.setText(item)
            }

            Constants.DISH_CATEGORY -> {
                customListDialog.dismiss()
                mBinding.etCategory.setText(item)
            }
            else -> {
                customListDialog.dismiss()
                mBinding.etCookingTime.setText(item)
            }
        }
    }


    companion object {
        private const val CAMERA = 1
        private const val GALLERY = 2
        private const val IMAGE_DIRECTORY = "Recipe Images"
    }


}