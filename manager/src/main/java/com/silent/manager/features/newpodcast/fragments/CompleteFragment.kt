package com.silent.manager.features.newpodcast.fragments

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ilustris.animations.fadeIn
import com.silent.ilustriscore.core.model.ViewModelBaseState
import com.silent.ilustriscore.core.utilities.delayedFunction
import com.silent.manager.R
import com.silent.manager.databinding.FragmentCreateCompleteBinding
import com.silent.manager.features.newpodcast.NewPodcastViewModel

class CompleteFragment : Fragment() {

    private val newPodcastViewModel: NewPodcastViewModel by lazy {
        ViewModelProvider(requireActivity())[NewPodcastViewModel::class.java]
    }

    private val completeFragmentBinding by lazy {
        view?.let { FragmentCreateCompleteBinding.bind(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_complete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        newPodcastViewModel.saveData(newPodcastViewModel.podcast)
    }

    private fun FragmentCreateCompleteBinding.showSaveSuccess() {
        sucessMessage.fadeIn()
        animation.playAnimation()
        animation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                sucessMessage.text = "Programa salvo com sucesso!"
                delayedFunction(2000) {
                    requireActivity().finish()
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }

        })
    }

    private fun FragmentCreateCompleteBinding.showError() {
        sucessMessage.fadeIn()
        sucessMessage.text = "Ocorreu um erro inesperado ao salvar :("
    }

    private fun observeViewModel() {
        newPodcastViewModel.viewModelState.observe(this, {
            when (it) {
                is ViewModelBaseState.DataSavedState -> {
                    completeFragmentBinding?.showSaveSuccess()
                }
                is ViewModelBaseState.ErrorState -> {
                    completeFragmentBinding?.showError()
                }
                else -> {
                    //DO NOTHING}
                }
            }
        })
    }
}