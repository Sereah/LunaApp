package com.lunacattus.app.presentation.view.feature.one

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.lunacattus.app.presentation.view.R
import com.lunacattus.app.presentation.view.base.BaseDialogFragment
import com.lunacattus.app.presentation.view.databinding.DialogEditDataBinding
import com.lunacattus.app.presentation.view.feature.one.mvi.OneUIState
import com.lunacattus.app.presentation.view.feature.one.mvi.OneUiEffect
import com.lunacattus.app.presentation.view.feature.one.mvi.OneUiIntent
import com.lunacattus.app.presentation.view.feature.one.mvi.OneViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditDataDialog :
    BaseDialogFragment<DialogEditDataBinding, OneUiIntent, OneUIState, OneUiEffect, OneViewModel>(
        DialogEditDataBinding::inflate
    ) {

    override val viewModel: OneViewModel by hiltNavGraphViewModels(R.id.one_navigation)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.confirm.setOnClickListener {
            dispatchUiIntent(OneUiIntent.AddData(binding.edit.text.toString()))
            dismiss()
        }
        binding.cancel.setOnClickListener {
            dismiss()
        }
    }
}