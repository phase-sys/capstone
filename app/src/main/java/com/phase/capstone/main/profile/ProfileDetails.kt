package com.phase.capstone.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.phase.capstone.databinding.FragmentProfileDetailsBinding
import com.phase.capstone.viewmodels.UserViewModel

class ProfileDetails : Fragment() {
    private val userSharedViewModel: UserViewModel by activityViewModels()

    private val args: ProfileDetailsArgs by navArgs()

    private var _binding: FragmentProfileDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = args.userId
        val nickname = args.userNickname

        if(args.allowEditProfile) {
            binding.btnEditProfile.setOnClickListener {
                val action = ProfileDetailsDirections.actionProfileDetailsToProfileEdit(isEdit = true)
                Navigation.findNavController(view).navigate(action)
            }
        } else {
            binding.btnEditProfile.isGone = true
        }

        if(!userId.isNullOrBlank()) {
            userSharedViewModel.getUserProfile(requireContext(), userId).run {
                observeViewModel()
                binding.tvNicknameProfileDetail.text = nickname
            }
        }
    }

    private fun observeViewModel(){
        userSharedViewModel.userInfo.observe(viewLifecycleOwner) { userInfo ->
            binding.apply {
                tvAddressProfileDetail.text = userInfo.address
                tvContactProfileDetail.text = userInfo.contactNumber
                tvEmailProfileDetail.text = userInfo.email
                tvEyeProfileDetail.text = userInfo.eyeColor
                tvFirstNameProfileDetail.text = userInfo.firstName
                tvGenderProfileDetail.text = userInfo.gender
                tvHairProfileDetail.text = userInfo.hairColor
                tvLastNameProfileDetail.text = userInfo.lastName
                tvMarksProfileDetail.text = userInfo.marks
                tvMedicalStatusProfileDetail.text = userInfo.medicalStatus
                tvMiddleNameProfileDetail.text = userInfo.middleName
                tvScarsProfileDetail.text = userInfo.scars
                tvTattoosProfileDetail.text = userInfo.tattoos
            }
        }
    }
}