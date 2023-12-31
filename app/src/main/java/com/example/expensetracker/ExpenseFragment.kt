package com.example.expensetracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.model.Data
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ExpenseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExpenseFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mExpenseDatabase: DatabaseReference

    private lateinit var recyclerView : RecyclerView
    private lateinit var adapter : FirebaseRecyclerAdapter<Data, MyVieweHolder>
    private lateinit var expenseTotalSum : TextView

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
        val myView : View = inflater.inflate(R.layout.fragment_expense, container, false)
        mAuth = FirebaseAuth.getInstance()
        val mUser : FirebaseUser? = mAuth.currentUser
        val uid : String? = mUser?.uid
        mExpenseDatabase = FirebaseDatabase.getInstance("https://expense-tracker-31a94-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("ExpenseData").child(uid!!)
        recyclerView = myView.findViewById(R.id.recycler_id_expense)
        expenseTotalSum = myView.findViewById(R.id.expense_txt_result)
        val layoutManager : LinearLayoutManager = LinearLayoutManager(activity)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager;

        mExpenseDatabase.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot){
                var exptotalvalue: Int = 0

                for(mySnapshot: DataSnapshot in dataSnapshot.children){
                    val data: Data? = mySnapshot.getValue(Data::class.java)
                    exptotalvalue += data!!.amount
                    val stExpTotalValue: String = exptotalvalue.toString()
                    expenseTotalSum.setText(stExpTotalValue)
                }
            }
            override fun onCancelled(databaseError: DatabaseError){

            }
        })

        return myView
    }

    override fun onStart(){
        super.onStart()

        val recyclerOptions : FirebaseRecyclerOptions<Data> = FirebaseRecyclerOptions.Builder<Data>().setQuery(mExpenseDatabase, Data::class.java).build()
        adapter = object: FirebaseRecyclerAdapter<Data, MyVieweHolder>(recyclerOptions){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyVieweHolder{
                val view = LayoutInflater.from(parent.context).inflate(R.layout.expense_recycler_data, parent, false)
                return MyVieweHolder(view)
            }
            override fun onBindViewHolder(holder: MyVieweHolder, position: Int, model: Data) {
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
         * @return A new instance of fragment ExpenseFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ExpenseFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

internal class MyVieweHolder(var mView: View) : RecyclerView.ViewHolder(
    mView
) {
    fun setType(type: String?) {
        val mType = mView.findViewById<TextView>(R.id.type_txt_expense)
        mType.text = type
    }

    fun setDate(date: String?) {
        val mDate = mView.findViewById<TextView>(R.id.date_txt_expense)
        mDate.text = date
    }

    fun setAmount(amount: Int) {
        val mAmount = mView.findViewById<TextView>(R.id.amount_txt_expense)
        val stamount = amount.toString()
        mAmount.text = stamount
    }
}