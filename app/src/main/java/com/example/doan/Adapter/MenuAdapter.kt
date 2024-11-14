package com.example.doan.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.doan.R
import com.example.doan.model.MenuItemData

class MenuAdapter(context: Context, private val items: List<MenuItemData>) :
    ArrayAdapter<MenuItemData>(context, R.layout.custom_menu_item, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.custom_menu_item, parent, false)
        val item = getItem(position)

        val icon = view.findViewById<ImageView>(R.id.menu_icon)
        val text = view.findViewById<TextView>(R.id.menu_text)

        item?.let {
            icon.setImageResource(it.iconResId)
            text.text = it.text
        }
        return view
    }
}