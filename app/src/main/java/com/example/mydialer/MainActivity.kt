package com.example.mydialer

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val URL = "https://drive.google.com/u/0/uc?id=1-KO-9GA3NzSgIc1dkAsNm8Dqw0fuPxcR&export=download"
    private val okHttpClient : OkHttpClient = OkHttpClient()
    private var links : MutableList<Contact> = mutableListOf()
    private var rviewContacts : MutableList<Contact> = mutableListOf()
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.rView)

        val myButton: Button = findViewById(R.id.btn_search)
        val editText: EditText = findViewById(R.id.et_search)
        getGSONFromserver()

        myButton.setOnClickListener() {
            if (editText.text.toString() != "") {
                links.clear()

                for (i in 0 .. rviewContacts.size - 1) {
                    if (rviewContacts[i].name.contains(editText.text.toString(), ignoreCase = true) ||
                        rviewContacts[i].phone.contains(editText.text.toString(), ignoreCase = true) ||
                        rviewContacts[i].type.contains(editText.text.toString(), ignoreCase = true)) {

                        links.add(rviewContacts[i])
                    }
                }

                recyclerView.adapter?.notifyDataSetChanged()
            } else {
                links.clear()

                for (i in 0 .. rviewContacts.size - 1) {
                    links.add(rviewContacts[i])
                }
                recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }
    private fun getGSONFromserver(){
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(URL)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                TODO("Not yet implemented")
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                parseJSON(json)
            }

        })
    }


    private fun parseJSON(jsonFromServer: String?) {
        val result = Gson().fromJson(jsonFromServer, Array<Contact> :: class.java)
        for (i in 0 .. result.size - 1){
            links += result[i]

            Timber.d("Contact name", links[i].name)
            Timber.d("Contact phone", links[i].phone)
            Timber.d("Contact type", links[i].type)
        }
        runOnUiThread{
            recyclerView.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            recyclerView.adapter = MyRecyclerAdapter(this, links)
        }

    }
}
data class Contact(
    val name: String = "",
    val phone: String = "",
    val type: String = "",
)
data class Wrapper(
    val contacts : ArrayList<Contact>
)
class MyRecyclerAdapter (private val context: Context, private val contacts: MutableList<Contact>) : RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder>(){
    class  ViewHolder (itemView : View):RecyclerView.ViewHolder (itemView){
        val name : TextView = itemView.findViewById(R.id.textName)
        val phone : TextView = itemView.findViewById(R.id.textPhone)
        val type : TextView = itemView.findViewById(R.id.textType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rview_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = contacts[position]

        holder.name.text = data.name
        holder.phone.text = data.phone
        holder.type.text = data.type
    }
}

