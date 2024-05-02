package com.phase.capstone.main.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.phase.capstone.databinding.FragmentHomeMainBinding
import com.phase.capstone.recyclerview.UserListRecyclerViewAdapter
import com.phase.capstone.repo.NetworkHandler
import com.phase.capstone.viewmodels.GroupViewModel
import kotlinx.coroutines.launch

class HomeMain : Fragment() {

    private val groupSharedViewModel: GroupViewModel by activityViewModels()

    private var _binding: FragmentHomeMainBinding? = null
    private val binding get() = _binding!!

    private val networkHandler = NetworkHandler()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        networkHandler.checkNetwork(requireContext())

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().finish()
        }

        binding.apply{
            loadHomeMain(refreshHomeMain)

            refreshHomeMain.setOnRefreshListener {
                loadHomeMain(refreshHomeMain)
            }

            btnEditHome.setOnClickListener {
                val action = HomeMainDirections.actionHomeMainToHomeConnection(
                    connectionState = "Edit",
                    tvHomeIdView.text.toString()
                )
                findNavController().navigate(action)
            }

            btnLeaveHome.setOnClickListener {
                groupSharedViewModel.isGrouped.value = false
                groupSharedViewModel.leaveGroup(requireContext(), view)
            }

            switchShowSecret.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    tvSecretKeyView.transformationMethod = null
                    tvSecretKeyView.transformationMethod = null
                } else {
                    tvSecretKeyView.transformationMethod = PasswordTransformationMethod()
                    tvSecretKeyView.transformationMethod = PasswordTransformationMethod()
                }
            }
        }
    }

    private fun loadHomeMain(refreshHome: SwipeRefreshLayout) {
        lifecycleScope.launch {
            groupSharedViewModel.getGroupMeta(requireContext()).run {
                observeGroupViewModel()
            }
            groupSharedViewModel.getMemberList(requireContext()).run {
                if (!groupSharedViewModel.memberList.hasObservers()){
                    groupSharedViewModel.memberList.observe(viewLifecycleOwner){ memberList ->
                        groupSharedViewModel.getUserList(requireContext(), memberList).run {
                            observeGroupMemberViewModel()
                        }
                    }
                }
            }
            refreshHome.isRefreshing = false
        }
    }

    private fun observeGroupViewModel() {
        if (!groupSharedViewModel.groupMeta.hasObservers()){
            groupSharedViewModel.groupMeta.observe(viewLifecycleOwner){ group ->
                binding.apply {
                    tvHomeNameView.text = group.name
                    tvHomeIdView.text = group.groupId
                    tvSecretKeyView.text = group.secretKey
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeGroupMemberViewModel() {
        if (!groupSharedViewModel.userList.hasObservers()){
            groupSharedViewModel.userList.observe(viewLifecycleOwner){ user ->
                binding.apply {
                    rvUserList.layoutManager = LinearLayoutManager(requireContext())
                    rvUserList.post {
                        rvUserList.adapter = UserListRecyclerViewAdapter(user, requireActivity())
                    }
                }
            }
        }
    }
}