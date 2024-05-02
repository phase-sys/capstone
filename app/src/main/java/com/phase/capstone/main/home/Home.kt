package com.phase.capstone.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.phase.capstone.databinding.FragmentHomeBinding
import com.phase.capstone.repo.NetworkHandler
import com.phase.capstone.viewmodels.GroupViewModel
import com.phase.capstone.viewmodels.MapsViewModel
import kotlinx.coroutines.launch

class Home : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val groupSharedViewModel: GroupViewModel by activityViewModels()
    private val mapsSharedViewModel: MapsViewModel by activityViewModels()

    private val networkHandler = NetworkHandler()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
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

        binding.apply {
            loadHome(refreshHome)

            refreshHome.setOnRefreshListener {
                loadHome(refreshHome)
            }

            btnCreateGroup.setOnClickListener {
                mapsSharedViewModel.homeLocation.value = null
                val action =
                    HomeDirections.actionNavigationHomeToHomeConnection(connectionState = "Create", "")
                findNavController().navigate(action)
            }

            btnJoinGroup.setOnClickListener {
                val action =
                    HomeDirections.actionNavigationHomeToHomeConnection(connectionState = "Join", "")
                findNavController().navigate(action)
            }
        }
    }

    private fun loadHome(refreshHome: SwipeRefreshLayout) {
        lifecycleScope.launch {
            groupSharedViewModel.checkUserGroup(requireContext()).run {
                observeInGroup(refreshHome)
            }
        }
    }

    private fun observeInGroup(refreshHome: SwipeRefreshLayout) {
        groupSharedViewModel.isGrouped.observe(viewLifecycleOwner) { isGrouped ->
            if (isGrouped == true) {
                val action = HomeDirections.actionNavigationHomeToHomeMain()
                findNavController().navigate(action)
            }
            refreshHome.isRefreshing = false
        }
    }

}