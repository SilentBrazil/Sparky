package com.silent.sparky.features.profile.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.ilustris.animations.fadeIn
import com.ilustris.animations.fadeOut
import com.ilustris.animations.slideInBottom
import com.ilustris.ui.extensions.gone
import com.silent.core.flow.data.FlowProfile
import com.silent.core.users.User
import com.silent.ilustriscore.core.model.ViewModelBaseState
import com.silent.sparky.R
import com.silent.sparky.databinding.FlowLinkAlertBinding
import com.silent.sparky.features.profile.viewmodel.ProfileState
import com.silent.sparky.features.profile.viewmodel.ProfileViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class FlowLinkDialog(private val user: User) : DialogFragment() {

    private var flowLinkAlertBinding: FlowLinkAlertBinding? = null

    private val profileViewModel: ProfileViewModel by sharedViewModel()
    private fun View.configure() {
        flowLinkAlertBinding = FlowLinkAlertBinding.bind(this).apply {
            flowAccountName.editText?.addTextChangedListener {
                flowAccountName.error = ""
                if (searchAccount.isGone) {
                    if (it.isNullOrEmpty()) {
                        searchAccount.gone()
                    } else {
                        searchAccount.slideInBottom()
                    }
                }
            }
            searchAccount.setOnClickListener {
                flowAccountName.editText?.let {
                    toggleAccountSearchLoading()
                    profileViewModel.getFlowProfile(it.text.toString())
                }
            }
        }
        observeViewModel()
    }

    private fun FlowLinkAlertBinding.toggleAccountSearchLoading() {
        loading.fadeIn()
        title.fadeOut()
        subtitle.fadeOut()
        flowAccountName.fadeOut()
        searchAccount.fadeOut()
    }

    private fun FlowLinkAlertBinding.toggleFullLoading() {
        loading.fadeIn()
        title.fadeOut()
        subtitle.fadeOut()
        flowAccountName.fadeOut()
        searchAccount.fadeOut()
        flowProfile.root.fadeOut()
        saveLinkButton.fadeOut()
    }

    private fun FlowLinkAlertBinding.disableAccountSearchLoading() {
        loading.fadeOut()
        title.fadeIn()
        subtitle.fadeIn()
        flowAccountName.fadeIn()
        searchAccount.fadeIn()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.flow_link_alert, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            DialogFragment.STYLE_NO_FRAME,
            android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.configure()
    }


    private fun observeViewModel() {
        profileViewModel.profileState.observe(viewLifecycleOwner) {
            when (it) {
                is ProfileState.FlowUserRetrieve -> {
                    setupFlowUser(it.flowProfile)
                }
                ProfileState.FlowUserError -> {
                    flowLinkAlertBinding?.flowAccountName?.error =
                        "Usuário não encontrado, tente novamente"

                    flowLinkAlertBinding?.disableAccountSearchLoading()

                }
            }
        }
        profileViewModel.viewModelState.observe(viewLifecycleOwner) {
            when (it) {
                is ViewModelBaseState.DataUpdateState -> {
                    dismiss()
                }
                else -> {}
            }
        }
    }

    private fun setupFlowUser(fProfile: FlowProfile) {
        flowLinkAlertBinding?.run {
            loading.fadeOut()
            flowProfile.root.fadeIn()
            title.fadeIn()
            subtitle.fadeIn()
            subtitle.text = "Verifique os dados de sua conta para continuar"
            flowLinkAlertBinding?.saveLinkButton?.fadeIn()
            Glide.with(requireContext())
                .load(fProfile.profile_picture)
                .error(R.drawable.ic_iconmonstr_connection_1)
                .into(flowProfile.profilePic)
            flowProfile.username.text = fProfile.username
            flowProfile.realName.text = fProfile.bio
            saveLinkButton.setOnClickListener {
                saveLinkButton.text = ""
                toggleFullLoading()
                user.flowUserName = fProfile.username
                user.profilePic = fProfile.profile_picture
                profileViewModel.editData(user)
            }
        }

    }


}