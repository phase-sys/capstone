package com.phase.capstone.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.phase.capstone.databinding.FragmentHomeConnectionBinding
import com.phase.capstone.repo.GroupMeta
import com.phase.capstone.viewmodels.GroupViewModel
import com.phase.capstone.viewmodels.MapsViewModel

class HomeConnection : Fragment() {

    private val args: HomeConnectionArgs by navArgs()
    private val groupSharedViewModel: GroupViewModel by activityViewModels()
    private val mapsSharedViewModel: MapsViewModel by activityViewModels()

    private var _binding: FragmentHomeConnectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeConnectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        groupSharedViewModel.isViewOnly.value = true
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            when (args.connectionState) {
                "Join" -> {
                    groupSharedViewModel.isViewOnly.value = false

                    tvHomeConnectionTitle.text = "Join Home"
                    btnNext.text = "Join Home"
                    tvHomeLocation.text = "Home Location"

                    tvHomeName.isGone = true
                    etGroupName.isGone = true

                    tvHomeLocation.isGone = true
                    fcvGroupMaps.isGone = true
                }
                "Create" -> {
                    groupSharedViewModel.isViewOnly.value = false

                    tvHomeConnectionTitle.text = "Create Home"
                    btnNext.text = "Create Home"
                    tvHomeLocation.text = "Set Home Location"
                }
                else -> {
                    loadGroup()
                    groupSharedViewModel.isViewOnly.value = false

                    tvHomeConnectionTitle.text = "Edit Home"
                    btnNext.text = "Edit Home"
                    tvHomeLocation.text = "Set Home Location"
                }
            }
        }

        binding.btnNext.setOnClickListener {
            when (args.connectionState) {
                "Join" -> {
                    groupSharedViewModel.joinGroup(
                        requireContext(), view, binding.etGroupId.text.toString().lowercase(),
                        binding.etSecretKey.text.toString()
                    )
                }
                "Edit" -> {
                    groupSharedViewModel.editGroup(
                        requireContext(), view,
                        GroupMeta(
                            mapsSharedViewModel.homeLocation.value?.longitude,
                            mapsSharedViewModel.homeLocation.value?.latitude,
                            binding.etGroupId.text.toString().lowercase(),
                            binding.etGroupName.text.toString(),
                            binding.etSecretKey.text.toString(),
                        ), args.groupId
                    )
                }
                else -> {
                    groupSharedViewModel.createGroup(
                        requireContext(), view,
                        GroupMeta(
                            mapsSharedViewModel.homeLocation.value?.longitude,
                            mapsSharedViewModel.homeLocation.value?.latitude,
                            binding.etGroupId.text.toString().lowercase(),
                            binding.etGroupName.text.toString(),
                            binding.etSecretKey.text.toString()
                        )
                    )
                }
            }
        }
    }

    private fun loadGroup() {
        groupSharedViewModel.groupMeta.observe(viewLifecycleOwner){ group ->
            binding.apply {
                etGroupId.setText(group.groupId)
                etGroupName.setText(group.name)
                etSecretKey.setText(group.secretKey)
            }
        }
    }
}