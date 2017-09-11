package com.simple.refresh

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_recyclerview.*
import org.jetbrains.anko.*

class RecyclerViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview)
        recyclerView.adapter = Adapter(ctx)
        btnStart.onClick{
           // ttRefreshLayout.setRefreshing()
        }
        btnStop.onClick{
         //   ttRefreshLayout.endRefresh()
        }
    }
    class Adapter(private val context: Context): RecyclerView.Adapter<Adapter.AdapterViewHolder>(){
        override fun onBindViewHolder(holder: AdapterViewHolder?, position: Int) {

        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) =
        AdapterViewHolder(context.linearLayout {
            lparams(width = matchParent,height = context.dip(50))
            gravity = Gravity.CENTER
            textView(arrayOf("梦想：此生不被祭天","我：这就尴尬了",
                    "我：我觉得很OK","我：我觉得很普通","无freeStyle")[(Math.random()*5).toInt()]){
                textSize = 16f

            }
        })

        override fun getItemCount() = 50

        class AdapterViewHolder(itemView:View):RecyclerView.ViewHolder(itemView)
    }

}
