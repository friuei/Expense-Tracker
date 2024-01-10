package com.example.expensetracker

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
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
    companion object {
        const val CHANNEL_ID: String = "my_channel"
    }
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        scheduleDailyReminder(this)
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Expense Tracker"
            val descriptionText = "Alert"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    fun scheduleDailyReminder(context: Context) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val timeToNotify = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (timeToNotify.before(Calendar.getInstance())) {
            timeToNotify.add(Calendar.DATE, 1)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            timeToNotify.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}