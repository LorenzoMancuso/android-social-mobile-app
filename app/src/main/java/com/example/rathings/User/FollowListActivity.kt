package com.example.rathings.User

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rathings.FirebaseUtils
import com.example.rathings.R
import java.util.*

class FollowListActivity : AppCompatActivity(), Observer {

    var allUsersObs = FirebaseUtils.getUsers()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_list)

        /**SET TOOLBAR OPTION*/
        val toolbar = findViewById<View>(R.id.toolbar) as androidx.appcompat.widget.Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        /**-----------------*/

        allUsersObs.addObserver(this)

    }

    override fun update(observableObj: Observable?, data: Any?) {
        when(observableObj) {
            allUsersObs -> {
                var requestType = intent.getStringExtra("requestType")
                var followList = intent.getStringArrayListExtra("followList")
                var allUsers = allUsersObs.getValue() as ArrayList<User>

                findViewById<TextView>(R.id.request_type).text = this.getString(R.string.followed_followers_size, requestType, followList.size)

                var filteredList = allUsers.filter{ followList.contains(it.id) } as ArrayList<User>

                var cardRecyclerView = findViewById<RecyclerView>(R.id.recycler_users)
                val mLayoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL,false)
                cardRecyclerView?.layoutManager = mLayoutManager
                var userAdapter = UserAdapter(filteredList)
                cardRecyclerView?.adapter = userAdapter
            }
            else -> Log.d("[SEARCH FRAGMENT]", "observable not recognized $data")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        allUsersObs.deleteObserver(this)
    }
}
