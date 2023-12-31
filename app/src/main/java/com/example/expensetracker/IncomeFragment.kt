package com.example.expensetracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.model.Data
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerAdapter_LifecycleAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [IncomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class IncomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mIncomeDatabase: DatabaseReference

    private lateinit var recyclerView : RecyclerView
    private lateinit var adapter : FirebaseRecyclerAdapter<Data, MyViewHolder>
    private lateinit var incomeTotalSum : TextView

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
        val myView : View = inflater.inflate(R.layout.fragment_income, container, false)
        mAuth = FirebaseAuth.getInstance()
        val mUser : FirebaseUser? = mAuth.currentUser
        val uid : String? = mUser?.uid
        mIncomeDatabase = FirebaseDatabase.getInstance("https://expense-tracker-31a94-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("IncomeData").child(uid!!)
        recyclerView = myView.findViewById(R.id.recycler_id_income);
        incomeTotalSum = myView.findViewById(R.id.income_txt_result)
        val layoutManager : LinearLayoutManager = LinearLayoutManager(activity)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager

        mIncomeDatabase.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot){
                var totalvalue: Int = 0

                for(mySnapshot: DataSnapshot in dataSnapshot.children){
                    val data: Data? = mySnapshot.getValue(Data::class.java)
                    totalvalue += data!!.amount
                    var stTotalValue: String = totalvalue.toString()
                    incomeTotalSum.setText(stTotalValue)
                }
            }
            override fun onCancelled(databaseError: DatabaseError){

            }
        })

        return myView
    }

    override fun onStart(){
        super.onStart()

        val recyclerOptions : FirebaseRecyclerOptions<Data> = FirebaseRecyclerOptions.Builder<Data>().setQuery(mIncomeDatabase, Data::class.java).build()
        adapter = object : FirebaseRecyclerAdapter<Data, MyViewHolder>(recyclerOptions){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder{
                val view = LayoutInflater.from(parent.context).inflate(R.layout.income_recycler_data, parent, false)
                return MyViewHolder(view)
            }
            override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: Data) {
                holder.setType(model.type)
                holder.setDate(model.date)
                holder.setAmount(model.amount)
            }
        }
        recyclerView.adapter = adapter
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment IncomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IncomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

internal class MyViewHolder(var mView: View) : RecyclerView.ViewHolder(
    mView
) {
    fun setType(type: String?) {
        val mType = mView.findViewById<TextView>(R.id.type_txt_income)
        mType.text = type
    }

    fun setDate(date: String?) {
        val mDate = mView.findViewById<TextView>(R.id.date_txt_income)
        mDate.text = date
    }

    fun setAmount(amount: Int) {
        val mAmount = mView.findViewById<TextView>(R.id.amount_txt_income)
        val stamount = amount.toString()
        mAmount.text = stamount
    }
}