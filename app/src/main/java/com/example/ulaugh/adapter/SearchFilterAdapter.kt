package com.example.ulaugh.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ulaugh.R
import com.example.ulaugh.model.Friend
import com.example.ulaugh.model.SuggestFriends
import java.util.*
import kotlin.collections.ArrayList

class SearchFilterAdapter(
    private var context: Context,
    private val list: ArrayList<Friend>
) : RecyclerView.Adapter<SearchFilterAdapter.MyViewHolder>()   {
    private var city: String=""
    var state: String =""
    private var country: String =""
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchFilterAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_fragment_nearby_list_layout, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SearchFilterAdapter.MyViewHolder, position: Int) {
        val listVar = list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }
    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var carMakeText: TextView = view.findViewById(R.id.car_make_text)
        var carYearText: TextView = view.findViewById(R.id.car_year_text)
        var carModelText: TextView = view.findViewById(R.id.car_model_text)
        var fuelTypeText: TextView = view.findViewById(R.id.fuel_type_text)
        var carMileageText: TextView = view.findViewById(R.id.car_mileage_text)
        var conditionText: TextView = view.findViewById(R.id.condition_text)
        var carPriceText: TextView = view.findViewById(R.id.car_price_text)
        var locationText: TextView = view.findViewById(R.id.details_location_text)
        var dateText: TextView = view.findViewById(R.id.car_date_text)
        var phoneButton: Button = view.findViewById(R.id.contact_seller_button)
        var viewsText: TextView = view.findViewById(R.id.views_text_views)
        var adsImage: ImageView =view.findViewById(R.id.ads_image)
        var carConditionDetailsText:TextView = view.findViewById(R.id.car_condition_details_text)
        var sideLineView: View =view.findViewById(R.id.divider2)
    }

}