package com.lunacattus.app.presentation.view.feature.one

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lunacattus.app.domain.model.Data
import com.lunacattus.app.presentation.view.R
import com.lunacattus.app.presentation.view.base.BaseFragment
import com.lunacattus.app.presentation.view.base.StateCollectorDelegate
import com.lunacattus.app.presentation.view.databinding.FragmentOneBinding
import com.lunacattus.app.presentation.view.feature.one.mvi.OneUIState
import com.lunacattus.app.presentation.view.feature.one.mvi.OneUiEffect
import com.lunacattus.app.presentation.view.feature.one.mvi.OneUiIntent
import com.lunacattus.app.presentation.view.feature.one.mvi.OneViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OneFragment :
    BaseFragment<FragmentOneBinding, OneUiIntent, OneUIState, OneUiEffect, OneViewModel>(
        FragmentOneBinding::inflate
    ) {

    private val dataAdapter by lazy { DataListAdapter() }

    override val viewModel: OneViewModel by hiltNavGraphViewModels(R.id.one_navigation)

    override fun handleSideEffect(effect: OneUiEffect) {
        when (effect) {
            is OneUiEffect.ShowToast -> {
                Toast.makeText(requireContext(), effect.msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.text.setOnClickListener {
            findNavController().navigate(
                resId = R.id.dest_dialog
            )
        }
        binding.dataList.apply {
            adapter = dataAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        collectState()
    }

    @SuppressLint("SetTextI18n")
    private fun collectState() {
        stateCollector.collectState<OneUIState.Loading> {
            binding.text.text = "状态：Loading"
            binding.text.isEnabled = false
            binding.dataList.visibility = View.GONE
        }

        stateCollector.collectState<OneUIState.Success> {
            binding.text.text = "点击拉起dialog插入数据"
            binding.text.isEnabled = true
            binding.dataList.visibility = View.VISIBLE
        }

        stateCollector.collectState(
            config = StateCollectorDelegate.CollectConfig<OneUIState.Success, List<Data>>(
                mapFn = { it.dataList }
            )
        ) {
            dataAdapter.submitList(it)
        }
    }

}