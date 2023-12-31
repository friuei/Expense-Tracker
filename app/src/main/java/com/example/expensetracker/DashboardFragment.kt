package com.example.expensetracker

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.expensetracker.model.Data
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.DateFormat
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DashboardFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    //Floating Button

    private var fab_main_btn : FloatingActionButton? = null
    private var fab_income_btn : FloatingActionButton? = null
    private var fab_expense_btn : FloatingActionButton? = null

    private var fab_income_txt : TextView? = null
    private var fab_expense_txt : TextView? = null

    private var isOpen : Boolean = false

    private var fadeOpen : Animation? = null
    private var fadeClose : Animation? = null

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mIncomeDatabase: DatabaseReference
    private lateinit var mExpenseDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val myview : View = inflater.inflate(R.layout.fragment_dashboard, container, false)
        mAuth = FirebaseAuth.getInstance()
        var mUser : FirebaseUser? = mAuth.currentUser
        var uid : String? = mUser?.uid
        mIncomeDatabase = FirebaseDatabase.getInstance("https://expense-tracker-31a94-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("IncomeData").child(uid!!)
        mExpenseDatabase = FirebaseDatabase.getInstance("https://expense-tracker-31a94-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("ExpenseData").child(uid!!)
        fab_main_btn = myview.findViewById(R.id.fb_main_plus_btn)
        fab_income_btn = myview.findViewById(R.id.income_ft_btn)
        fab_expense_btn = myview.findViewById(R.id.expense_ft_btn)

        fab_income_txt = myview.findViewById(R.id.income_ft_text)
        fab_expense_txt = myview.findViewById(R.id.expense_ft_text)

        fadeOpen = AnimationUtils.loadAnimation(activity, R.anim.fade_open)
        fadeClose = AnimationUtils.loadAnimation(activity, R.anim.fade_close)

        fab_main_btn?.setOnClickListener {
            addData()

            if(isOpen){
                fab_income_btn?.startAnimation(fadeClose)
                fab_expense_btn?.startAnimation(fadeClose)
                fab_income_btn?.isClickable = false
                fab_expense_btn?.isClickable = false
                fab_income_txt?.startAnimation(fadeClose)
                fab_expense_txt?.startAnimation(fadeClose)
                fab_income_txt?.isClickable = false
                fab_expense_txt?.isClickable = false
                isOpen = false
            }
            else{
                fab_income_btn?.startAnimation(fadeOpen)
                fab_expense_btn?.startAnimation(fadeOpen)
                fab_income_btn?.isClickable = true
                fab_expense_btn?.isClickable = true
                fab_income_txt?.startAnimation(fadeOpen)
                fab_expense_txt?.startAnimation(fadeOpen)
                fab_income_txt?.isClickable = false
                fab_expense_txt?.isClickable = false
                isOpen = true
            }
        }

        return myview
    }

    private fun addData(){
        fab_income_btn?.setOnClickListener{
            incomeDataInsert()
        }

        fab_expense_btn?.setOnClickListener{
            expenseDataInsert()
        }
    }

    fun incomeDataInsert(){
        val mydialog = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val myviewm : View = inflater.inflate(R.layout.custom_layout_for_insert_data, null)
        mydialog.setView(myviewm)
        val dialog : AlertDialog = mydialog.create()

        val edtAmount : EditText = myviewm.findViewById(R.id.amount_edt)
        val edtType : EditText = myviewm.findViewById(R.id.type_edt)

        val btnSave : Button = myviewm.findViewById(R.id.btnSave)
        val btnCancel : Button = myviewm.findViewById(R.id.btnCancel)

        btnSave.setOnClickListener{
            var type : String = edtType.text.toString().trim()
            var amount : String = edtAmount.text.toString().trim()

            if(TextUtils.isEmpty(type)){
                edtType.setError("Required field..")
                return@setOnClickListener
            }
            if(TextUtils.isEmpty(amount)){
                edtAmount.setError("Required field..")
                return@setOnClickListener
            }
            var ouramountint : Int = Integer.parseInt(amount)
            var id : String = mIncomeDatabase.push().key.toString()
            val mDate : String = DateFormat.getDateInstance().format(Date()).toString()
            var data : Data = Data(ouramountint, type, id, mDate)
            mIncomeDatabase.child(id).setValue(data)
            Toast.makeText(activity, "Data Added", Toast.LENGTH_SHORT).show()

            dialog.dismiss()
        }

        btnCancel.setOnClickListener{
            dialog.dismiss()
        }
        dialog.show()
    }

    fun expenseDataInsert(){
        val mydialog = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val myview : View = inflater.inflate(R.layout.custom_layout_for_insert_data, null)
        mydialog.setView(myview)
        val dialog : AlertDialog = mydialog.create()

        val edtAmount : EditText = myview.findViewById(R.id.amount_edt)
        val edtType : EditText = myview.findViewById(R.id.type_edt)

        val btnSave : Button = myview.findViewById(R.id.btnSave)
        val btnCancel : Button = myview.findViewById(R.id.btnCancel)

        btnSave.setOnClickListener{
            var type : String = edtType.text.toString().trim()
            var amount : String = edtAmount.text.toString().trim()

            if(TextUtils.isEmpty(type)){
                edtType.setError("Required field..")
                return@setOnClickListener
            }
            if(TextUtils.isEmpty(amount)){
                edtAmount.setError("Required field..")
                return@setOnClickListener
            }
            var ouramountint : Int = Integer.parseInt(amount)
            var id : String = mExpenseDatabase.push().key.toString()
            val mDate : String = DateFormat.getDateInstance().format(Date()).toString()
            var data : Data = Data(ouramountint, type, id, mDate)
            mExpenseDatabase.child(id).setValue(data)
            Toast.makeText(activity, "Data Added", Toast.LENGTH_SHORT).show()

            dialog.dismiss()
        }

        btnCancel.setOnClickListener{
            dialog.dismiss()
        }
        dialog.show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DashboardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DashboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}