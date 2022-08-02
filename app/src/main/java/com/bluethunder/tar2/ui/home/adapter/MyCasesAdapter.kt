package com.bluethunder.tar2.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluethunder.tar2.databinding.MyCasesItemBinding
import com.bluethunder.tar2.ui.edit_case.model.CaseModel

class MyCasesAdapter(
    val interaction: MyCasesInteractions
) :
    RecyclerView.Adapter<MyCasesAdapter.ViewHolder>() {
    private val TAG = "NotificationCenterAdapt"

    var myCases: MutableList<CaseModel> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            MyCasesItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


    }

    override fun getItemCount(): Int {
        return myCases.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(binding: MyCasesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var bindingView: MyCasesItemBinding = binding
    }

    fun addNewData(notificationsList: MutableList<CaseModel>) {
        this.myCases.addAll(notificationsList)
        notifyDataSetChanged()
    }

    fun clearData() {
        this.myCases.clear()
        notifyDataSetChanged()
    }

    interface MyCasesInteractions {
        fun onNotificationClicked(caseModel: CaseModel)
    }

}