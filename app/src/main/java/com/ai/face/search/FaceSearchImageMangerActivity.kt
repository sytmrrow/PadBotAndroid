package com.ai.face.search

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ai.face.FaceApplication.Companion.CACHE_SEARCH_FACE_DIR
import com.example.myapplication.R
import com.ai.face.addFaceImage.AddFaceImageActivity
import com.ai.face.faceSearch.search.FaceSearchImagesManger
import com.ai.face.search.SearchNaviActivity.Companion.copyManyTestFaceImages
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.myapplication.databinding.ActivityFaceImageMangerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Arrays
import java.util.Locale

/**
 * 增删改 编辑人脸图片,演示怎样使用SDK API进行人脸的增删改查
 * FaceSearchImagesManger.IL1Iii.getInstance(application)
 *                     ?.insertOrUpdateFaceImage
 *
 * 需要使用SDK 的API 操作增删改，不能直接插入目录就以为可以搜索
 *
 */
class FaceSearchImageMangerActivity : AppCompatActivity() {

    private val faceImageList: MutableList<String> = ArrayList()
    private lateinit var faceImageListAdapter: FaceImageListAdapter
    private lateinit var binding: ActivityFaceImageMangerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFaceImageMangerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 不显示菜单栏上默认出现的APP名

        var num = 3
        val mConfiguration: Configuration = this.resources.configuration //获取设置的配置信息
        val ori: Int = mConfiguration.orientation //获取屏幕方向
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            num = 5
        }

        val gridLayoutManager: LinearLayoutManager = GridLayoutManager(this, num)
        binding.recyclerView.layoutManager = gridLayoutManager
        loadImageList()

        faceImageListAdapter = FaceImageListAdapter(faceImageList)
        binding.recyclerView.adapter = faceImageListAdapter
        faceImageListAdapter.setOnItemLongClickListener { _, _, i ->
            AlertDialog.Builder(this@FaceSearchImageMangerActivity)
                .setTitle("确定要删除" + File(faceImageList[i]).name)
                .setMessage("删除后对应的人将无法被程序识别")
                .setPositiveButton("确定") { _: DialogInterface?, _: Int ->
                    //删除一张照片
                    FaceSearchImagesManger.Companion().getInstance(application)
                        ?.deleteFaceImage(faceImageList[i])

                    loadImageList()
                    faceImageListAdapter.notifyDataSetChanged()
                }
                .setNegativeButton("取消") { _: DialogInterface?, _: Int -> }
                .show()
            false
        }

        faceImageListAdapter.setEmptyView(R.layout.empty_layout)

        /*faceImageListAdapter.emptyLayout?.setOnClickListener { v: View? ->
            Toast.makeText(baseContext, "复制中...", Toast.LENGTH_LONG).show()
            CoroutineScope(Dispatchers.IO).launch {
                copyManyTestFaceImages(application)
                delay(800)
                MainScope().launch {
                    //Kotlin 混淆操作后协程操作失效了，因为是异步操作只能等一下
                    loadImageList()
                    faceImageListAdapter.notifyDataSetChanged()
                }
            }
        }*/

        //直接添加照片
        if (intent.extras?.getBoolean("isAdd") == true) {
            startActivityForResult(
                Intent(baseContext, AddFaceImageActivity::class.java), REQUEST_ADD_FACE_IMAGE
            )
        }

    }

    /**
     * 加载图片
     */
    private fun loadImageList() {
        faceImageList.clear()
        val file = File(CACHE_SEARCH_FACE_DIR)
        val subFaceFiles = file.listFiles()
        if (subFaceFiles != null) {
            Arrays.sort(subFaceFiles, object : Comparator<File> {
                override fun compare(f1: File, f2: File): Int {
                    val diff = f1.lastModified() - f2.lastModified()
                    return if (diff > 0) -1 else if (diff == 0L) 0 else 1
                }

                override fun equals(obj: Any?): Boolean {
                    return true
                }
            })
            for (index in subFaceFiles.indices) {
                // 判断是否为文件夹
                if (!subFaceFiles[index].isDirectory) {
                    val filename = subFaceFiles[index].name
                    val lowerCaseName = filename.trim { it <= ' ' }.lowercase(Locale.getDefault())
                    if (lowerCaseName.endsWith(".jpg")
                        || lowerCaseName.endsWith(".png")
                        || lowerCaseName.endsWith(".jpeg")
                    ) {
                        faceImageList.add(subFaceFiles[index].path)
                    }
                }
            }
        }
    }


    class FaceImageListAdapter(results: MutableList<String>) :
        BaseQuickAdapter<String, BaseViewHolder>(R.layout.adapter_face_image_list_item, results) {
        override fun convert(helper: BaseViewHolder, item: String) {
            Glide.with(context).load(item)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into((helper.getView<View>(R.id.image) as ImageView))
        }
    }


    /**
     * 录入人脸返回了
     *
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD_FACE_IMAGE && resultCode == RESULT_OK) {

            val bis = data?.getByteArrayExtra("picture_data")
            val faceName = data?.getStringExtra("picture_name") + ".jpg"
            val bitmap = BitmapFactory.decodeByteArray(bis, 0, bis!!.size)

            CoroutineScope(Dispatchers.IO).launch {
                FaceSearchImagesManger.Companion().getInstance(application)
                    ?.insertOrUpdateFaceImage(
                        bitmap,
                        CACHE_SEARCH_FACE_DIR + File.separatorChar + faceName
                    )
                delay(300)
                MainScope().launch {
                    loadImageList()
                    faceImageListAdapter.notifyDataSetChanged()
                }
            }

        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                startActivityForResult(
                    Intent(baseContext, AddFaceImageActivity::class.java), REQUEST_ADD_FACE_IMAGE
                )
                true
            }

            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }


    companion object {
        const val REQUEST_ADD_FACE_IMAGE = 10086
    }

}