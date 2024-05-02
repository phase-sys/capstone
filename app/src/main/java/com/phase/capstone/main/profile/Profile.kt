package com.phase.capstone.main.profile

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.phase.capstone.R
import com.phase.capstone.databinding.FragmentProfileBinding
import com.phase.capstone.repo.NetworkHandler
import com.phase.capstone.service.MessageHandler
import com.phase.capstone.viewmodels.GroupViewModel
import com.phase.capstone.viewmodels.UserViewModel
import kotlinx.coroutines.launch

class Profile : Fragment() {
    private val userSharedViewModel: UserViewModel by activityViewModels()
    private val groupSharedViewModel: GroupViewModel by activityViewModels()

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val args: ProfileArgs by navArgs()

    private val networkHandler = NetworkHandler()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        networkHandler.checkNetwork(requireContext())

        var userId = ""
        var nickname = ""
        var allowEditProfile: Boolean

        binding.apply {
            if (!(args.userId.isNullOrBlank() || args.userId.isNullOrEmpty())) {
                userId = args.userId.toString()
                nickname = args.userNickname.toString()
                allowEditProfile = false

                btnSignOut.isGone = true
                grpMessage.isGone = false

                switchTrackUser.isGone = false

                groupSharedViewModel.getUserGroupUnique(requireContext()).run {
                    groupSharedViewModel.userGroupUnique.observe(viewLifecycleOwner) { userGroupUnique ->
                        groupSharedViewModel.getToTrackUser(
                            requireContext(),
                            userId,
                            userGroupUnique
                        ).run {
                            groupSharedViewModel.isTracked.observe(viewLifecycleOwner) { isTracked ->
                                switchTrackUser.isChecked = isTracked
                            }
                        }

                        switchTrackUser.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                groupSharedViewModel.setToTrackUser(
                                    requireContext(),
                                    userId,
                                    userGroupUnique
                                )
                            } else {
                                groupSharedViewModel.removeToTrackUser(
                                    requireContext(),
                                    userId,
                                    userGroupUnique
                                )
                            }
                        }

                    }
                }

                btnSendMessage.setOnClickListener {
                    val message = etMessage.text.toString()
                    if (message.isNotBlank()) {
                        messageUser(userId, message, etMessage)
                    } else {
                        Toast.makeText(requireContext(), "Empty Message", Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                allowEditProfile = true

                val pickMedia =
                    registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                        if (uri != null) {
                            binding.ivProfilePic.setImageURI(uri)
                            userSharedViewModel.uploadImage(requireContext(), uri)
                        } else {
                            Toast.makeText(requireContext(), "No Media Selected", Toast.LENGTH_LONG)
                                .show()
                        }
                    }

                ivProfilePic.setOnClickListener {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }

                btnSignOut.setOnClickListener {
                    userSharedViewModel.signOutUser(requireActivity(), view)
                }

                userSharedViewModel.getUserState(requireContext(), nickname).run {
                    userSharedViewModel.userState.observe(viewLifecycleOwner) { userState ->
                        userId = userState.userId
                        nickname = userState.nickname
                        binding.tvNicknameTitle.text = nickname
                    }
                }
            }

            loadUser(refreshProfile, userId, nickname)

            refreshProfile.setOnRefreshListener {
                loadUser(refreshProfile, userId, nickname)
            }

            ibLocate.setOnClickListener {
                val action = ProfileDirections.actionNavigationProfileToNavigationMaps(userId, nickname)
                findNavController().navigate(action)
            }

            ibShare.setOnClickListener {
                userSharedViewModel.shareUser(requireActivity(), requireContext(), userId)
            }

            btnProfileDetails.setOnClickListener {
                val action = ProfileDirections.actionNavigationProfileToProfileDetails(userId, nickname, allowEditProfile)
                Navigation.findNavController(view).navigate(action)
            }

            val mediaPlayer = MediaPlayer.create(context, R.raw.help)
            ibPanic.setOnClickListener {
                if (!(args.userId.isNullOrBlank() || args.userId.isNullOrEmpty())) {
                    messageUser(userId, "I'm lost.", etMessage)
                    Log.i("TAG", "onViewCreated: RUNMESSAGE")
                }

                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                }
            }
        }
    }

    private fun loadUser(refreshProfile: SwipeRefreshLayout, userId: String, nickname: String) {
        lifecycleScope.launch {
            binding.tvNicknameTitle.text = nickname
            userSharedViewModel.getImage(requireContext(), binding.ivProfilePic, userId)
            refreshProfile.isRefreshing = false
        }
    }

    private fun messageUser(userId: String, message: String, etMessage: EditText) {
        if (!userSharedViewModel.messageToken.hasObservers()) {
            userSharedViewModel.getToken(requireContext(), userId).run {
                userSharedViewModel.messageToken.observe(viewLifecycleOwner) { messageMeta ->
                    MessageHandler().sendMessage(
                        requireContext(),
                        messageMeta["token"].toString(),
                        messageMeta["nickname"].toString(),
                        message, etMessage
                    )
                }
            }
        }

        val messageMeta = userSharedViewModel.messageToken.value
        MessageHandler().sendMessage(
            requireContext(),
            messageMeta?.get("token").toString(),
            messageMeta?.get("nickname").toString(),
            message, etMessage
        )
    }
}