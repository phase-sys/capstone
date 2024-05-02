package com.phase.capstone.auth

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.phase.capstone.R
import com.phase.capstone.databinding.FragmentLoginBinding
import com.phase.capstone.viewmodels.UserViewModel

class Login : Fragment() {
    private val sharedViewModel: UserViewModel by activityViewModels()

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity()
        val context = requireContext()

        if(sharedViewModel.userIsSignedIn()){
            Navigation.findNavController(view).navigate(R.id.action_navigation_login_to_mainActivity)
            activity.finish()
        }

        binding.btnSignIn.setOnClickListener{
            binding.apply {
                sharedViewModel.signInUser(
                    activity,
                    context,
                    view,
                    etEmail.text.toString(),
                    etPassword.text.toString()
                )
            }
        }

        binding.btnToSignUp.setOnClickListener {
            val action = LoginDirections.actionNavigationLoginToRegister(isEdit = false)
            Navigation.findNavController(view).navigate(action)
        }

        binding.apply {
            switchShowPassword.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    etPassword.transformationMethod = null
                } else {
                    etPassword.transformationMethod = PasswordTransformationMethod()
                }
            }
        }
    }
}