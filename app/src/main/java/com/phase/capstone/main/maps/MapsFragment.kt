package com.phase.capstone.main.maps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.phase.capstone.R
import com.phase.capstone.databinding.FragmentMapsBinding
import com.phase.capstone.repo.GroupMeta
import com.phase.capstone.repo.LocationGetMeta
import com.phase.capstone.repo.NetworkHandler
import com.phase.capstone.viewmodels.GroupViewModel
import com.phase.capstone.viewmodels.MapsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MapsFragment : Fragment() {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private val args: MapsFragmentArgs by navArgs()

    private val groupSharedViewModel: GroupViewModel by activityViewModels()
    private val mapsSharedViewModel: MapsViewModel by activityViewModels()
    private var isViewOnly = true
    private var homeMarker: Marker? = null

    private val networkHandler = NetworkHandler()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        networkHandler.checkNetwork(requireContext())

        groupSharedViewModel.checkUserGroup(requireContext())

        isViewOnly = groupSharedViewModel.isViewOnly.value ?: true

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private val callback = OnMapReadyCallback { googleMap ->
        if (!isViewOnly) {
            editMap(googleMap)
        } else {
            viewMap(googleMap)
        }
    }

    private fun editMap(googleMap: GoogleMap) {
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    12.8797,
                    121.7740
                ), 4f
            )
        )

        if (mapsSharedViewModel.homeLocation.value != null) {
            homeMarker = googleMap.addMarker(
                MarkerOptions().position(mapsSharedViewModel.homeLocation.value!!)
                    .title("Home Location")
            )
        }

        googleMap.setOnMapClickListener {
            mapsSharedViewModel.homeLocation.value = it
            if (homeMarker != null) {
                homeMarker?.remove()
            }
            homeMarker = googleMap.addMarker(
                MarkerOptions().position(mapsSharedViewModel.homeLocation.value!!)
                    .title("Home Location")
            )
        }
    }

    private fun viewMap(googleMap: GoogleMap) {
        googleMap.clear()
        if (groupSharedViewModel.isGrouped.value == true) {
            groupSharedViewModel.getGroupMeta(requireContext()).run {
                groupSharedViewModel.groupMeta.observe(viewLifecycleOwner) { group ->
                    if (args.userId.isNullOrBlank() || args.userId.isNullOrEmpty()) {
                        groupLocate(googleMap, group)
                    } else {
                        userLocate(googleMap, group)
                    }
                }
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Please join a HOME to see location",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun observeMemberLocation(googleMap: GoogleMap) {
        mapsSharedViewModel.memberLocation.observe(viewLifecycleOwner) { location ->
            location.forEach {
                googleMap.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            it.latitude,
                            it.longitude
                        )
                    ).title(it.nickname).snippet(it.timestamp.toString())
                )
            }
        }
    }

    private fun groupLocate(googleMap: GoogleMap, group: GroupMeta) {
        lifecycleScope.launch {
            val groupLat = group.latitude!!
            val groupLong = group.longitude!!

            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        groupLat,
                        groupLong
                    ), 6f
                )
            )

            while (true) {
                googleMap.clear()

                googleMap.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            groupLat,
                            groupLong
                        )
                    ).title(group.name).snippet(group.groupId)
                )
                mapsSharedViewModel.getLocationFromDB(requireContext()).run {
                    observeMemberLocation(googleMap)
                }
                delay(10000)
                googleMap.clear()
            }
        }
    }


    private fun userLocate(googleMap: GoogleMap, group: GroupMeta) {
        lifecycleScope.launch {

            var userLocation: List<LocationGetMeta> = mutableListOf()
            mapsSharedViewModel.getUserLocationFromDB(requireContext(), args.userId!!)

            mapsSharedViewModel.userLocation.observe(viewLifecycleOwner) {
                userLocation = it
            }

            userLocation.forEach {
                if (args.userNickname!! == it.nickname) {
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.latitude, it.longitude), 20f
                        )
                    )
                }
            }

            while (true) {
                googleMap.clear()

                googleMap.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            group.latitude!!,
                            group.longitude!!
                        )
                    ).title(group.name).snippet(group.groupId)
                )

                mapsSharedViewModel.getUserLocationFromDB(requireContext(), args.userId!!).run {
                    mapsSharedViewModel.userLocation.observe(viewLifecycleOwner) {
                        it.forEach { userLocation ->
                            googleMap.addMarker(
                                MarkerOptions().position(
                                    LatLng(
                                        userLocation.latitude,
                                        userLocation.longitude
                                    )
                                ).title(userLocation.nickname)
                                    .snippet(userLocation.timestamp.toString())
                            )
                        }
                    }
                }

                delay(10000)
                googleMap.clear()
            }
        }
    }
}