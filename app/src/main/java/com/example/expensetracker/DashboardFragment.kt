package com.example.expensetracker

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.expensetracker.model.Data
import com.example.expensetracker.model.SavingsThreshold
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DateFormat
import java.util.Calendar
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
    private var fab_threshold_btn : FloatingActionButton? = null

    private var fab_income_txt : TextView? = null
    private var fab_expense_txt : TextView? = null
    private var fab_threshold_txt : TextView? = null

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
        fab_threshold_btn = myview.findViewById(R.id.threshold_ft_btn)
        fab_income_txt = myview.findViewById(R.id.income_ft_text)
        fab_expense_txt = myview.findViewById(R.id.expense_ft_text)
        fab_threshold_txt = myview.findViewById(R.id.threshold_ft_text)

        fadeOpen = AnimationUtils.loadAnimation(activity, R.anim.fade_open)
        fadeClose = AnimationUtils.loadAnimation(activity, R.anim.fade_close)

        fab_main_btn?.setOnClickListener {
            addData()

            if(isOpen){
                fab_income_btn?.startAnimation(fadeClose)
                fab_expense_btn?.startAnimation(fadeClose)
                fab_threshold_btn?.startAnimation(fadeClose)
                fab_income_btn?.isClickable = false
                fab_expense_btn?.isClickable = false
                fab_threshold_btn?.isClickable = false
                fab_income_txt?.startAnimation(fadeClose)
                fab_expense_txt?.startAnimation(fadeClose)
                fab_threshold_txt?.startAnimation(fadeClose)
                fab_income_txt?.isClickable = false
                fab_expense_txt?.isClickable = false
                fab_threshold_txt?.isClickable = false
                isOpen = false
            }
            else{
                fab_income_btn?.startAnimation(fadeOpen)
                fab_expense_btn?.startAnimation(fadeOpen)
                fab_threshold_btn?.startAnimation(fadeOpen)
                fab_income_btn?.isClickable = true
                fab_expense_btn?.isClickable = true
                fab_threshold_btn?.isClickable = true
                fab_income_txt?.startAnimation(fadeOpen)
                fab_expense_txt?.startAnimation(fadeOpen)
                fab_threshold_txt?.startAnimation(fadeOpen)
                fab_income_txt?.isClickable = false
                fab_expense_txt?.isClickable = false
                fab_threshold_txt?.isClickable = false
                isOpen = true
            }
        }

        return myview
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addMonthlyBoxes()
        calculateMonthlySavings()
    }

    private fun addData(){
        fab_income_btn?.setOnClickListener{
            incomeDataInsert()
        }

        fab_expense_btn?.setOnClickListener{
            expenseDataInsert()
        }
        fab_threshold_btn?.setOnClickListener {
            showSetThresholdDialog("jan")
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
            val type : String = edtType.text.toString().trim()
            val amount : String = edtAmount.text.toString().trim()

            if(TextUtils.isEmpty(type)){
                edtType.setError("Required field..")
                return@setOnClickListener
            }
            if(TextUtils.isEmpty(amount)){
                edtAmount.setError("Required field..")
                return@setOnClickListener
            }
            val ouramountint : Int = Integer.parseInt(amount)
            val id : String = mExpenseDatabase.push().key.toString()
            val mDate : String = DateFormat.getDateInstance().format(Date()).toString()
            val timestamp: Long = System.currentTimeMillis()
            val data : Data = Data(ouramountint, type, id, mDate, timestamp)
            mExpenseDatabase.child(id).setValue(data)
            Toast.makeText(activity, "Data Added", Toast.LENGTH_SHORT).show()

            dialog.dismiss()
        }

        btnCancel.setOnClickListener{
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showSetThresholdDialog(month: String) {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setTitle("Set Savings Threshold for $month")

        val input = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "Enter threshold amount"
        }

        dialogBuilder.setView(input)

        dialogBuilder.setPositiveButton("Save") { _, _ ->
            val thresholdAmount = input.text.toString()
            if (thresholdAmount.isNotEmpty()) {
                saveThresholdToFirebase(month, thresholdAmount.toInt())
            }
        }

        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        dialogBuilder.create().show()
    }

    private fun saveThresholdToFirebase(month: String, threshold: Int) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let {
            val thresholdRef = FirebaseDatabase.getInstance("https://expense-tracker-31a94-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("UserThresholds")
                .child(it)

            thresholdRef.child(month).setValue(threshold).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("DashboardFragment", "Threshold updated for $month: $threshold")
                } else {
                    Log.e("DashboardFragment", "Failed to update threshold for $month", it.exception)
                }
            }
        }
    }

    private fun calculateMonthlySavings() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let { userId ->
            val currentMonthIndex = getCurrentMonthIndex()
            val currentMonthCalendar = Calendar.getInstance()

            val startOfMonth = getStartOfMonthTimestamp(currentMonthCalendar, currentMonthIndex).toDouble()
            val endOfMonth = getEndOfMonthTimestamp(currentMonthCalendar, currentMonthIndex).toDouble()

            val query = mExpenseDatabase.child(userId)
                .orderByChild("timestamp")
                .startAt(startOfMonth)
                .endAt(endOfMonth)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var totalExpense = 0
                    for (snapshot in dataSnapshot.children) {
                        val data = snapshot.getValue(Data::class.java)
                        totalExpense += data?.amount ?: 0
                    }
                    calculateAndFetchMonthlyStatus(userId) {monthStatus ->
                        Log.d("DashboardFragment", "Month status: $monthStatus")
                        updateMonthlyBoxColors(monthStatus)}
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                }
            })
        }
    }

    private fun calculateAndFetchMonthlyStatus(userId: String, callback: (Map<String, Boolean>) -> Unit) {
        val thresholdRef = FirebaseDatabase.getInstance("https://expense-tracker-31a94-default-rtdb.europe-west1.firebasedatabase.app/").getReference("UserThresholds").child(userId)

        thresholdRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(thresholdSnapshot: DataSnapshot) {
                val monthStatus = mutableMapOf<String, Boolean>()
                val months = arrayOf("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec")

                months.forEachIndexed { index, month ->
                    val monthlyThreshold = thresholdSnapshot.child(month).getValue(Int::class.java) ?: 0
                    val startEndTimestamps = getMonthStartEndTimestamps(months.indexOf(month))

                    mExpenseDatabase.child(userId)
                        .orderByChild("timestamp")
                        .startAt(startEndTimestamps.first.toDouble())
                        .endAt(startEndTimestamps.second.toDouble())
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(expenseSnapshot: DataSnapshot) {
                                val totalExpense = expenseSnapshot.children.sumOf { it.getValue(Data::class.java)?.amount ?: 0 }
                                val isWithinThreshold = totalExpense <= monthlyThreshold
                                monthStatus[month] = isWithinThreshold

                                if (monthStatus.size == months.size) {
                                    callback(monthStatus)
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Handle errors
                            }
                        })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }

    private fun getMonthStartEndTimestamps(monthIndex: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR)) // Set to current year
        val startOfMonth = getStartOfMonthTimestamp(calendar, monthIndex)
        val endOfMonth = getEndOfMonthTimestamp(calendar, monthIndex)
        return Pair(startOfMonth, endOfMonth)
    }

    private fun getCurrentMonthIndex(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.MONTH)
    }

    private fun getStartOfMonthTimestamp(calendar: Calendar, monthIndex: Int): Long {
        calendar.set(Calendar.MONTH, monthIndex)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfMonthTimestamp(calendar: Calendar, monthIndex: Int): Long {
        calendar.set(Calendar.MONTH, monthIndex)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        return calendar.timeInMillis
    }

    private fun addMonthlyBoxes() {
        Log.d("DashboardFragment", "Adding monthly boxes")
        val months = arrayOf("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec")
        val container = view?.findViewById<LinearLayout>(R.id.monthly_boxes_container)

        months.forEach { month ->
            val box = TextView(context).apply {
                text = month
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 8
                }
                setPadding(10, 10, 10, 10)
                gravity = Gravity.CENTER
                setBackgroundResource(R.drawable.month_box_background)
            }
            container?.addView(box)
            Log.d("DashboardFragment", "Added box for $month")
        }
    }

    private fun updateMonthlyBoxColors(monthStatus: Map<String, Boolean>) {
        val container = view?.findViewById<LinearLayout>(R.id.monthly_boxes_container)
        for (i in 0 until (container?.childCount ?: 0)) {
            val box = container?.getChildAt(i) as? TextView
            val month = box?.text.toString()
            val isWithinThreshold = monthStatus[month] ?: false

            Log.d("DashboardFragment", "Month: $month, Within Threshold: $isWithinThreshold")

            box?.setBackgroundResource(
                if (isWithinThreshold) R.drawable.month_box_success
                else R.drawable.month_box_failure
            )
        }
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