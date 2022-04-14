package com.daon.airbnb_part3_07

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.daon.airbnb_part3_07.HouseModel
import com.daon.airbnb_part3_07.R

class HouseViewPagerAdapter(
    val itemClickListener : (HouseModel) -> Unit
) : ListAdapter<HouseModel, HouseViewPagerAdapter.HouseViewHolder>(diffUtil) {

    inner class HouseViewHolder(
        private val view : View
    ) : RecyclerView.ViewHolder(view) {

        fun bind (houseModel: HouseModel) {
            val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
            val priceTextView = view.findViewById<TextView>(R.id.priceTextView)
            val thumbnailImageView = view.findViewById<ImageView>(R.id.thumbnailImageView)

            titleTextView.text = houseModel.title
            priceTextView.text = houseModel.price
            Glide.with(thumbnailImageView.context)
                .load(houseModel.imgUrl)
                .into(thumbnailImageView)

            view.setOnClickListener { itemClickListener(houseModel) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HouseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return HouseViewHolder(inflater.inflate(R.layout.item_house_detail_for_viewpager, parent, false))
    }

    override fun onBindViewHolder(holder: HouseViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<HouseModel>() {
            override fun areItemsTheSame(oldItem: HouseModel, newItem: HouseModel)
                    = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: HouseModel, newItem: HouseModel)
                    = oldItem == newItem
        }
    }
}