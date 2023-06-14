package com.example.to_gammer_app

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.to_gammer_app.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private lateinit var profileBinding: FragmentProfileBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        profileBinding = FragmentProfileBinding.inflate(inflater, container,false)
        return profileBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //BottomNavigation Icon Color Remove -> Show Preset Image
        profileBinding.buttonEdit.backgroundTintList = null

        //resultLauncher 초기화
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == AppCompatActivity.RESULT_OK) {
            }
        }

        profileBinding.buttonEdit.setOnClickListener {
            val editIntent = Intent(activity, SettingProfileActivity::class.java)
            resultLauncher.launch(editIntent)
        }
    }
}