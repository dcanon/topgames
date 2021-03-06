package br.com.bancopan.topgames.main.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableBoolean
import android.databinding.ObservableInt
import android.view.View
import br.com.bancopan.topgames.App
import br.com.bancopan.topgames.core.CoreVM
import br.com.bancopan.topgames.main.EndlessScrollHelper
import br.com.bancopan.topgames.repository.GameRepository
import br.com.bancopan.topgames.repository.data.Game
import br.com.bancopan.topgames.repository.data.ServiceErrorModel
import br.com.bancopan.topgames.utils.Constants.API_LIMIT
import timber.log.Timber
import javax.inject.Inject

class GameListVM : CoreVM() {
    @Inject
    lateinit var repository: GameRepository
    var isLoading: ObservableBoolean
    var progressVisibility: ObservableInt
    var labelErrorVisibility: ObservableInt
    var serviceSuccess: MutableLiveData<List<Game>>
    var serviceFailure: MutableLiveData<ServiceErrorModel>
    val scrollHelper: EndlessScrollHelper

    init {
        isLoading = ObservableBoolean()
        progressVisibility = ObservableInt()
        labelErrorVisibility = ObservableInt()
        serviceSuccess = MutableLiveData()
        serviceFailure = MutableLiveData()
        scrollHelper = EndlessScrollHelper(API_LIMIT)
        App.instance.repositoryComponent.inject(this)

//        repository.listener = this
    }

    fun requestData() {
        progressVisibility.set(View.VISIBLE)
        labelErrorVisibility.set(View.GONE)

        repository.getTopGames(scrollHelper.nextOffset,
                { success -> handleSuccess(success) },
                { error -> handleError(error) })

    }

    fun onRefresh() {
        isLoading.set(true)
        requestData()
    }

    fun handleSuccess(response: List<Game>) {
        serviceSuccess.postValue(response)
        configureUIForSuccess()

        if (scrollHelper.totalPages == 1) {
            scrollHelper.updateTotalPage(repository!!.totalItems)
        }

        scrollHelper.updatePageIndex()
    }

    fun handleError(errorModel: ServiceErrorModel) {
        serviceFailure.postValue(errorModel)
        scrollHelper.enable = false
        Timber.d("Failed to load data from cloud")
    }

    private fun configureUIForError() {
        progressVisibility.set(View.GONE)
        labelErrorVisibility.set(View.VISIBLE)
    }

    private fun configureUIForSuccess() {
        progressVisibility.set(View.GONE)
        labelErrorVisibility.set(View.GONE)
    }

}

