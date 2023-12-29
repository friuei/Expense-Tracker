package com.example.expensetracker

import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView


class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val toolbar = findViewById<Toolbar>(R.id.my_toolbar)
        toolbar.title = "Expense Tracker"
        setSupportActionBar(toolbar)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navigationview = findViewById<NavigationView>(R.id.naView)
        navigationview.setNavigationItemSelectedListener(this)

        val bottomnavigationview = findViewById<BottomNavigationView>(R.id.bottomBar)
        val frameLayout = findViewById<FrameLayout>(R.id.fragment_container)

        val incomeFragment = IncomeFragment()
        val expenseFragment = ExpenseFragment()
        val dashboardFragment = DashboardFragment()
        setFragment(dashboardFragment)

        bottomnavigationview.setOnNavigationItemSelectedListener { item ->
            when (item.itemId){
                R.id.income -> {
                    setFragment(incomeFragment)
                    true
                }
                R.id.expense -> {
                    setFragment(expenseFragment)
                    true
                }
                R.id.dashboard -> {
                    setFragment(dashboardFragment)
                }
            }
            false
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        displaySelectedListener(item.itemId)
        return true
    }


    override fun onBackPressed() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        if(drawerLayout.isDrawerOpen(GravityCompat.END)){
            drawerLayout.closeDrawer(GravityCompat.END)
        }
        else{
            super.onBackPressed()
        }

    }

    private fun displaySelectedListener(itemId : Int){
        var fragment : Fragment? = null

        when(itemId){
            R.id.income -> {
                fragment = IncomeFragment()
            }
            R.id.expense -> {
                fragment = ExpenseFragment()
            }
            R.id.dashboard -> {
                fragment = DashboardFragment()
            }
        }
        fragment?.let{
            val ft : FragmentTransaction = supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragment_container, fragment)
            ft.commit()
        }

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
    }
    private fun setFragment(fragment : Fragment){
        val fragmentTransaction : FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }
}