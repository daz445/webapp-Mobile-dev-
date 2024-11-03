package ru.dmitryzyrynov.cw

import ApiClient
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import io.ktor.client.statement.bodyAsText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.graphics.Color
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var productAdapter: ProductAdapter
    private lateinit var products: MutableList<Product>
    var openNow = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        lifecycleScope.launch {

            try {
                val isOpen = withContext(Dispatchers.IO) {
                    ApiClient.isOpen(getString(R.string.host),getString(R.string.apiKey))
                }
                if (isOpen && openNow){
                    starttrain()
                    openNow = false
                }
                findViewById<Button>(R.id.brepeat).setOnClickListener {
                    repeattrain()
                }


            }
            catch (e: Exception) {

                val button = findViewById<Button>(R.id.button)

                button.setOnClickListener {
                    starttrain()
                }

                findViewById<Button>(R.id.brepeat).setOnClickListener {
                    repeattrain()
                }

                }
            }



    }



    private fun loadItems(onItemsLoaded: (List<Map<String, Any>>) -> Unit) {
        lifecycleScope.launch {
            try {
                val itemsJson = withContext(Dispatchers.IO) {

                    ApiClient.fetchItems(getString(R.string.host),getString(R.string.apiKey))
                }

                val jsonString = itemsJson.bodyAsText()
                val gson = Gson()
                val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
                val items: List<Map<String, Any>> = gson.fromJson(jsonString, listType)


                onItemsLoaded(items)

            } catch (e: Exception) {

                Log.e("MainActivity", "Error loading items ", e)
            }
        }
    }
    private fun loadItemsnew(onItemsLoaded: (List<Map<String, Any>>) -> Unit) {
        lifecycleScope.launch {
            try {
                val itemsJson = withContext(Dispatchers.IO) {

                    ApiClient.fetchItemsNew(getString(R.string.host),getString(R.string.apiKey))
                }

                val jsonString = itemsJson.bodyAsText()
                val gson = Gson()
                val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
                val items: List<Map<String, Any>> = gson.fromJson(jsonString, listType)


                onItemsLoaded(items)

            } catch (e: Exception) {

                Log.e("MainActivity", "Error loading items ", e)
            }
        }
    }

    private fun generate(onGenerated: (List<Product>) -> Unit) {
        loadItems { items ->
            val productList = items.mapNotNull {

                try {
                    val id = it["id"].toString().toDouble().toInt()
                    val isFinish = it["isFinish"].toString().toDouble().toInt()
                    val name = it["name"].toString()
                    val repeat = "${it["repeat"]}"
                    val img = getResources().getIdentifier(it["img"].toString(),"drawable",getPackageName())

                    Product(id, isFinish,name,repeat, img)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error parsing item", e)
                    null
                }
            }
            onGenerated(productList)
        }
    }

    private fun generatenew(onGenerated: (List<Product>) -> Unit) {
        loadItemsnew { items ->
            val productList = items.mapNotNull {

                try {
                    val id = it["id"].toString().toDouble().toInt()
                    val isFinish = it["isFinish"].toString().toDouble().toInt()
                    val name = it["name"].toString()
                    val repeat = "${it["repeat"]}"
                    val img = getResources().getIdentifier(it["img"].toString(),"drawable",getPackageName())

                    Product(id,isFinish,name,repeat, img)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error parsing item", e)
                    null
                }
            }
            onGenerated(productList)
        }
    }







    private fun createNewItem(name: String) {
        lifecycleScope.launch {
            try {
                val newItem = withContext(Dispatchers.IO) {
                    ApiClient.createItem(name)
                }
                Log.d("MainActivity", "Создал $newItem")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error! ", e)
            }
        }
    }


private fun starttrain(){
    var startbutton = findViewById<Button>(R.id.button)
    var main_train_view = findViewById<LinearLayout>(R.id.main_train_view)
    var brepeat = findViewById<Button>(R.id.brepeat)
    var progress_bar = findViewById<ProgressBar>(R.id.progress_bar)

    startbutton.visibility = View.GONE
    main_train_view.visibility = View.VISIBLE
    progress_bar.visibility = View.VISIBLE
    brepeat.visibility = View.VISIBLE

    generate { products ->
        lateinit var recyclerView: RecyclerView
        lateinit var productAdapter: ProductAdapter

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        productAdapter = ProductAdapter(products) { product, isAdded  ->
            if (isAdded) {

                val clMain = findViewById<ConstraintLayout>(R.id.main)
                val sb = Snackbar.make(clMain, "Выполнено: ${product.name}", Snackbar.LENGTH_SHORT)
                sb.setBackgroundTint(Color.GREEN)

                sb.show()


            }






        }
        recyclerView.adapter = productAdapter
        progress_bar.visibility = View.GONE




    }}
    private fun repeattrain(){

        var progress_bar = findViewById<ProgressBar>(R.id.progress_bar)


        progress_bar.visibility = View.VISIBLE


        generatenew { products ->
            lateinit var recyclerView: RecyclerView
            lateinit var productAdapter: ProductAdapter

            recyclerView = findViewById(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(this)

            productAdapter = ProductAdapter(products) { product, isAdded  ->
                if (isAdded) {

                    product.isFinish = 1
                    lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        ApiClient.editEl(
                            getString(R.string.host),
                            getString(R.string.apiKey),
                            product.isFinish,
                            product.id
                        )
                    }}

                    val clMain = findViewById<ConstraintLayout>(R.id.main)
                    val sb = Snackbar.make(clMain, "Выполнено: ${product.name}", Snackbar.LENGTH_SHORT)
                    sb.setBackgroundTint(Color.GREEN)


                    sb.show()


                }
                else{
                    product.isFinish = 0
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            ApiClient.editEl(
                                getString(R.string.host),
                                getString(R.string.apiKey),
                                product.isFinish,
                                product.id
                            )
                        }}
                }

            }
            recyclerView.adapter = productAdapter
            progress_bar.visibility = View.GONE




        }}


}














