package com.example.thesis_trading_app.utils

import androidx.recyclerview.widget.DiffUtil
import com.example.thesis_trading_app.models.Product


class ContactDiffCallback(oldList: List<Product>, newList: List<Product>) :
    DiffUtil.Callback() {
    private val mOldList: List<Product> = oldList
    private val mNewList: List<Product> = newList
    override fun getOldListSize(): Int {
        return mOldList.size
    }

    override fun getNewListSize(): Int {
        return mNewList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // add a unique ID property on Product
        return mOldList[oldItemPosition].product_id === mNewList[newItemPosition].product_id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldProduct: Product = mOldList[oldItemPosition]
        val newProduct: Product = mNewList[newItemPosition]
        return oldProduct.product_name == newProduct.product_name
    }

}