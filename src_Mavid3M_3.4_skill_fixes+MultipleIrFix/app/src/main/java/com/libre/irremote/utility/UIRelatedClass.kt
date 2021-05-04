package com.libre.irremote.utility

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.danimahardhika.cafebar.CafeBar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.libre.irremote.R
import com.libre.irremote.adapters.IRPopularOptionsSelectionBottomSheetAdapter
import com.libre.irremote.models.ModelRemoteDetails
import com.libre.irremote.models.ModelRemoteSubAndMacDetils

class UIRelatedClass {

    fun buildSnackBarWithoutButton(context: Context, view: View, message: String) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        snackbar.view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
        snackbar.show()
    }


    fun buidCustomSnackBarWithButton(context: Context, textViewMessage: String, btnMessage: String, appCompatActivity: AppCompatActivity) {

        val builder = CafeBar.builder(context)
        builder.autoDismiss(false)
        builder.customView(R.layout.custom_snackbar_with_button_layout)
        var cafeBar: CafeBar? = builder.build();

        if (cafeBar != null) {
            val tvMessage: AppCompatTextView = cafeBar?.cafeBarView!!.findViewById(R.id.tvMessage)
            tvMessage.text = textViewMessage

            val btnOK: AppCompatButton = cafeBar?.cafeBarView.findViewById(R.id.btnOk)
            btnOK.setOnClickListener {

                appCompatActivity.finish()
            }
            cafeBar.show()
        }
    }


    fun showCustomDialogForUUIDMismatch(appCompatActivity: AppCompatActivity, onButtonClickCallback: OnButtonClickCallback) {
        val builder: AlertDialog.Builder =
                AlertDialog.Builder(appCompatActivity)

        val viewGroup: ViewGroup = appCompatActivity.findViewById(android.R.id.content)

        val dialogView: View =
                LayoutInflater.from(appCompatActivity)
                        .inflate(R.layout.custom_alert_uuid_mismatch_layout, viewGroup, false)

        builder.setView(dialogView);

        builder.setCancelable(false)

        val alertDialog: AlertDialog = builder.create()


        val btnProceed: AppCompatButton = dialogView.findViewById(R.id.btnProceed)


        btnProceed.setOnClickListener {
            alertDialog.dismiss()
            onButtonClickCallback.onClick(true)
        }

        alertDialog.show()
    }


    fun showUserCustomDialogForPrevSelectedRemote(appCompatActivity: AppCompatActivity,
                                                  onButtonClickCallback: OnButtonClickCallback) {
        if (!appCompatActivity.isFinishing) {

            var alert = Dialog(appCompatActivity)

            alert.requestWindowFeature(Window.FEATURE_NO_TITLE)

            alert.setContentView(R.layout.custom_single_button_layout)

            alert.setCancelable(false)

            val tv_alert_title: AppCompatTextView = alert.findViewById(R.id.tv_alert_title)

            val tv_alert_message: AppCompatTextView = alert.findViewById(R.id.tv_alert_message)

            val btn_ok: AppCompatButton = alert.findViewById(R.id.btn_ok)

            tv_alert_title.text = "Remote is already selected"

            tv_alert_message.text = "The remote for the appliance you have  selected  is already configured\nPlease go back  select another remote."

            btn_ok.setOnClickListener {
                alert.dismiss()
                onButtonClickCallback.onClick(true)
            }

            alert!!.show()
        }
    }


    fun showCustomAlertToEditCustomName(appCompatActivity: AppCompatActivity, onButtonClickCallbackWithStringParams: OnButtonClickCallbackWithStringParams) {
        if (!appCompatActivity.isFinishing) {
            var builder = AlertDialog.Builder(appCompatActivity)

            var customLayout = appCompatActivity.layoutInflater.inflate(R.layout.aler_dialog_edit_custom_name_for_appliance, null)
            builder.setView(customLayout)

            var etCustomName: AppCompatEditText = customLayout.findViewById(R.id.etCustomName)

            var btnConfirm: AppCompatButton = customLayout.findViewById(R.id.btnConfirm)

            val dialog = builder.create()

            btnConfirm.setOnClickListener {
                onButtonClickCallbackWithStringParams
                dialog.dismiss()
            }
            // create and show
            // the alert dialog
            dialog.show()
        }
    }

    fun showBottomDialogForAddingCustomName(appCompatActivity: AppCompatActivity,
                                            onButtonClickCallbackWithStringParams: OnButtonClickCallbackWithStringParams,
                                            popularOptionsHashMap: HashMap<String, String>, userAlreadyUsedCustomNamesHashMap: HashMap<String, String>,
                                            selectedAppliance: String, brandName: String, macId: String) {

        // Getting Collection of values from HashMap
        val values: Collection<String> = popularOptionsHashMap.keys

        // Creating an ArrayList of values
        val popularOptionsList = ArrayList(values)


        val view: View = appCompatActivity.layoutInflater.inflate(R.layout.bottom_sheet_edit_custom_name_layout, null)

        val bottomSheetDialog: BottomSheetDialog? = BottomSheetDialog(appCompatActivity)
        val gvPopularOptions: GridView = view.findViewById(R.id.gvPopularOptions)
        val btnConfirm: AppCompatButton = view.findViewById(R.id.btnConfirm)


        val etCustomName: AppCompatEditText = view.findViewById(R.id.etCustomName)


        val ivCloseSheet: AppCompatImageView = view.findViewById(R.id.ivCloseSheet)

        ivCloseSheet.setOnClickListener {
            bottomSheetDialog?.dismiss()
            appCompatActivity.finish()
        }

        etCustomName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                btnConfirm.isEnabled = etCustomName.text.toString().isNotEmpty()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })

        var defaultText = ""
        if (selectedAppliance == "1" || selectedAppliance == "TV") {
            defaultText = setDefaultCustomName(popularOptionsHashMap, "TV", brandName)
        } else if (selectedAppliance == "2" || selectedAppliance == "TVP") {
            defaultText = setDefaultCustomName(popularOptionsHashMap, "TVP", brandName)
        } else {
            //for ac
            defaultText = setDefaultCustomName(popularOptionsHashMap, "AC", brandName)
        }

        etCustomName.setText(defaultText)

        bottomSheetDialog!!.setContentView(view)
        bottomSheetDialog.setCancelable(false)

        val adapter = IRPopularOptionsSelectionBottomSheetAdapter(appCompatActivity, popularOptionsList)
        gvPopularOptions.adapter = adapter


        gvPopularOptions.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            etCustomName.setText(popularOptionsList[position])
        }

        btnConfirm.setOnClickListener {
            if (checkIFUserIsUsingDIffNamesForTVorTVorAc(appCompatActivity, selectedAppliance, etCustomName.text.toString(), macId)) {
                if (checkIfAParticluarOptionsIsAlreadyPresent(userAlreadyUsedCustomNamesHashMap, etCustomName.text.toString().toUpperCase(), macId)) {
                    bottomSheetDialog.dismiss()
                    onButtonClickCallbackWithStringParams.onUserClicked(etCustomName.text.toString().toUpperCase())
                } else {
                    //show a snackbar error
//                buidCustomSnackBarWithButton(appCompatActivity,"","OK",appCompatActivity)
                    Toast.makeText(appCompatActivity, "This name for your appliance is already used , please use a different one", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(appCompatActivity, "This name for your appliance is already used , please use a different one", Toast.LENGTH_LONG).show()
            }
        }

        bottomSheetDialog.show()

    }


    fun checkIFUserIsUsingDIffNamesForTVorTVorAc(appCompatActivity: AppCompatActivity, userSelectedApplianceType: String,
                                                 userEditedName: String, macId: String): Boolean {
        var sharedPreferences = appCompatActivity.getSharedPreferences("Mavid", Context.MODE_PRIVATE)

        val gson = Gson()

        var modelRemoteDetailsString = sharedPreferences?.getString("applianceInfoList", "")

        var modelRemoteSubAndMacDetils = ModelRemoteSubAndMacDetils()

        if (modelRemoteDetailsString != null) {

            if (modelRemoteDetailsString!!.isNotEmpty()) {

                modelRemoteSubAndMacDetils = gson?.fromJson<ModelRemoteSubAndMacDetils>(modelRemoteDetailsString,
                        ModelRemoteSubAndMacDetils::class.java) as ModelRemoteSubAndMacDetils

                if (modelRemoteSubAndMacDetils.mac == macId) {
                    when (userSelectedApplianceType) {
                        "1",
                        "TV" -> {
                            val tvpCustomName = getTVPCustomName(modelRemoteSubAndMacDetils.modelRemoteDetailsList)
                            if (tvpCustomName.isNotEmpty() && tvpCustomName.equals(userEditedName, true)) {
                                return false
                            }
                        }
                        "2",
                        "TVP" -> {
                            val tvCustomName = getTVCustomName(modelRemoteSubAndMacDetils.modelRemoteDetailsList)
                            if (tvCustomName.isNotEmpty() && tvCustomName.equals(userEditedName, true)) {
                                return false
                            }
                        }
                        "3",
                        "AC" -> {
                            val acCustomName = getTVCustomName(modelRemoteSubAndMacDetils.modelRemoteDetailsList)
                            if (acCustomName.isNotEmpty() && acCustomName.equals(userEditedName, true)) {
                                return false
                            }
                        }
                    }
                }
            }
        }
        return true
    }

    fun getTVCustomName(modelRemoteDetailsList: MutableList<ModelRemoteDetails>): String {
        for (modelRemoteDetails: ModelRemoteDetails in modelRemoteDetailsList) {
            if (modelRemoteDetails.selectedAppliance == "1" || modelRemoteDetails.selectedAppliance == "TV") {
                return modelRemoteDetails.customName
            }
        }
        return ""
    }

    fun getTVPCustomName(modelRemoteDetailsList: MutableList<ModelRemoteDetails>): String {
        for (modelRemoteDetails: ModelRemoteDetails in modelRemoteDetailsList) {
            if (modelRemoteDetails.selectedAppliance == "2" || modelRemoteDetails.selectedAppliance == "TVP") {
                return modelRemoteDetails.customName
            }
        }
        return ""
    }

    fun checkIfAParticluarOptionsIsAlreadyPresent(userAlreadyUsedCustomNamesHashMap: HashMap<String, String>,
                                                  userEditedCustomName: String, macId: String): Boolean {

        for (preDefinedHashMapObject: Map.Entry<String, String> in userAlreadyUsedCustomNamesHashMap) {

            if (preDefinedHashMapObject.value != macId) {
                //check if the mac is diff from the ones present in the
                if (preDefinedHashMapObject.key.equals(userEditedCustomName, true)) {
                    //ie then the user that name for the appliance ie tv.tvp
                    //for a diffrent device then show an error to the user
                    return false
                }
            }
        }
        return true
    }

    fun setDefaultCustomName(popularOptionsHashMap: HashMap<String, String>, selectedAppliance: String, brandName: String): String {
        var defaultCustomName = ""

        when (selectedAppliance) {
            "TV" -> {
                defaultCustomName = when {

                    popularOptionsHashMap.containsKey("TV") -> {
                        "TV"
                    }
                    popularOptionsHashMap.containsKey("${brandName.toUpperCase()} $selectedAppliance") -> {
                        //ie LG TV / Samsung TV
                        "${brandName.toUpperCase()} $selectedAppliance"
                    }
                    else -> {
                        ""
                    }
                }
            }
            "TVP" -> {

                if (popularOptionsHashMap.containsKey("SET TOP BOX")) {
                    defaultCustomName = "SET TOP BOX"
                } else if (popularOptionsHashMap.containsKey("${brandName.toUpperCase()} SET TOP BOX")) {
                    //ie Airtel Set top box,Tata Sky Set top box
                    defaultCustomName = "${brandName.toUpperCase()} SET TOP BOX"
                } else {
                    defaultCustomName = ""
                }
            }
            "AC" -> {

            }
        }

        return defaultCustomName
    }

    fun showCustomAlertDialogForDeleteConfirmation(appCompatActivity: AppCompatActivity,
                                                   onButtonClickCallback: OnButtonClickCallback) {

        if (!appCompatActivity.isFinishing) {

            var alert = Dialog(appCompatActivity)

            alert.requestWindowFeature(Window.FEATURE_NO_TITLE)

            alert.setContentView(R.layout.custom_alert_two_buttons_layout)

            alert.setCancelable(false)

            val tv_alert_title: AppCompatTextView = alert.findViewById(R.id.tv_alert_title)

            val tv_alert_message: AppCompatTextView = alert.findViewById(R.id.tv_alert_message)

            val btn_ok: AppCompatButton = alert.findViewById(R.id.btn_ok)

            val btn_cancel: AppCompatButton = alert.findViewById(R.id.btn_cancel)

            tv_alert_title.text = "Are you sure?"

            tv_alert_message.text = "This will delete  your already configured appliance. "

            btn_cancel.setOnClickListener {
                alert.dismiss()
                onButtonClickCallback.onClick(false)
            }

            btn_ok.setOnClickListener {
                alert.dismiss()
                onButtonClickCallback.onClick(true)
            }


            alert!!.show()
        }
    }
}