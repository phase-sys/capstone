package com.phase.capstone.auth

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.isDigitsOnly
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.phase.capstone.R
import com.phase.capstone.databinding.FragmentRegisterBinding
import com.phase.capstone.repo.User
import com.phase.capstone.viewmodels.UserViewModel
import kotlinx.coroutines.launch

class Register : Fragment() {
    private val sharedViewModel: UserViewModel by activityViewModels()

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val args: RegisterArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(args.isEdit) {
            binding.apply {
                tvSignUpTitle.text = "Update Info"
                btnSignUp.text = "Update"

                etSignUpEmail.isGone = true
                tvEmailHint.isGone = true
                spcEmail.isGone = true

                tvOldPasswordHint.isGone = false
                etSignUpOldPassword.isGone = false

                etSignUpOldPassword.hint = "Input your current password"
                etSignUpPassword.hint = "Input more than 8 characters"

                lifecycleScope.launch{
                    sharedViewModel.getUserProfile(requireContext(), "")
                    sharedViewModel.getUserState(requireContext(), "")
                    observeViewModel()
                }
            }
        }

        binding.apply {
            btnSignUp.setOnClickListener {
                if (!etContactNumber.text.isDigitsOnly()){
                    etContactNumber.text.clear()
                }
                sharedViewModel.signUpUser(
                    args.isEdit,
                    requireActivity(),
                    requireContext(), view,
                    etNickname.text.toString(),
                    checkRadioButton(),
                    User(
                        etAddress.text.toString(),
                        etContactNumber.text.toString(),
                        etSignUpEmail.text.toString(),
                        etEyeColor.text.toString(),
                        etFirstName.text.toString(),
                        etGender.text.toString(),
                        etHairColor.text.toString(),
                        etLastName.text.toString(),
                        etMarks.text.toString(),
                        etMedicalStatus.text.toString(),
                        etMiddleName.text.toString(),
                        etScars.text.toString(),
                        etTattoos.text.toString()
                    ),
                    etSignUpPassword.text.toString(),
                    etSignUpOldPassword.text.toString()
                )
            }

            switchShowPassword.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    etSignUpOldPassword.transformationMethod = null
                    etSignUpPassword.transformationMethod = null
                } else {
                    etSignUpOldPassword.transformationMethod = PasswordTransformationMethod()
                    etSignUpPassword.transformationMethod = PasswordTransformationMethod()
                }
            }
        }
    }

    private fun observeViewModel() {
        sharedViewModel.userState.observe(viewLifecycleOwner){ userState ->
            binding.apply {
                etNickname.setText(userState.nickname)
            }
        }

        sharedViewModel.userInfo.observe(viewLifecycleOwner){ userInfo ->
            binding.apply {
                etFirstName.setText(userInfo.firstName)
                etMiddleName.setText(userInfo.middleName)
                etLastName.setText(userInfo.lastName)
                etGender.setText(userInfo.gender)
                etContactNumber.setText(userInfo.contactNumber)
                etAddress.setText(userInfo.address)
                etHairColor.setText(userInfo.hairColor)
                etEyeColor.setText(userInfo.eyeColor)
                etScars.setText(userInfo.scars)
                etMarks.setText(userInfo.marks)
                etTattoos.setText(userInfo.tattoos)
                etMedicalStatus.setText(userInfo.medicalStatus)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val actualFragment =  childFragmentManager.findFragmentById(R.id.register)
        childFragmentManager.fragments.remove(actualFragment)

    }

    private fun checkRadioButton(): Boolean {
        return binding.radGroup.checkedRadioButtonId == binding.radGuardian.id
    }
}