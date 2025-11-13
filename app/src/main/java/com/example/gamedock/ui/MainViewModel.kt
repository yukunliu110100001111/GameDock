package com.example.gamedock.ui

import androidx.lifecycle.ViewModel
import com.example.gamedock.data.repository.DealsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * DealRepository Owner.
 * Because it is difficult to modify all UI code to implement dependency injection for dealRepository in a short time,
 * we directly inject DealsRepository into MainViewModel to access it where needed.
 * This is a short-term expedient, not a long-term solution.
 *
 * DealRepository 的持有者。
 * 由于短时间难以修改所有 UI 代码以实现 dealRepository 的依赖注入，故直接将 DealsRepository 注入到 MainViewModel 中，
 * 以便在需要的地方访问它。这是短期内的权益之计，并非长期解决方案。
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    val dealsRepository: DealsRepository
) : ViewModel()