package com.example.to_gammer_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.example.to_gammer_app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var mainBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //MainActivity Pre-Setting
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        //BottomNavigation Icon Color Remove -> Show Preset Image
        mainBinding.navi.itemIconTintList = null

        loadFragment(HomeFragment())
        mainBinding.navi.selectedItemId = R.id.icon_home

        //네비게이션 바 메뉴 누를 시 액션
        mainBinding.navi.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.icon_home -> {                         //피드 메뉴를 누르면 피드 메뉴로 넘어감
                    loadFragment(HomeFragment())
                    true
                }
                R.id.icon_profile -> {                       //마이페이지 아이콘을 누르면 마이페이지
                    loadFragment(ProfileFragment())
                    true
                }
                else -> {
                    true
                }
            }
        }
    }

    // 프래그먼트 불러오는 함수
    private fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.mainFrame, fragment)
        transaction.commit()
    }
}