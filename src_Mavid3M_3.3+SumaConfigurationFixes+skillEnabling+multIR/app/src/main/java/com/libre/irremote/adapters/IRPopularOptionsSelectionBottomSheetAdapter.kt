package com.libre.irremote.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.widget.AppCompatTextView
import com.libre.irremote.R

internal class IRPopularOptionsSelectionBottomSheetAdapter(
        private val context: Context,
        private val popularOptionsList: MutableList<String>) : BaseAdapter() {
    private var layoutInflater: LayoutInflater? = null

    lateinit var tvPopularOptionName: AppCompatTextView

    override fun getCount(): Int {
        return popularOptionsList.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(
            position: Int,
            convertView: View?,
            parent: ViewGroup): View? {

        var convertView = convertView
        if (layoutInflater == null) {
            layoutInflater =
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }
        if (convertView == null) {
            convertView = layoutInflater!!.inflate(R.layout.adaapter_popular_options_bottom_sheet, null)
            tvPopularOptionName = convertView.findViewById(R.id.tvPopularOptionName)
        }



        tvPopularOptionName.text = popularOptionsList[position]
        return convertView
    }
}