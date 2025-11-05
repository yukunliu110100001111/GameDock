package com.example.gamedock.domain.usecase

import com.example.gamedock.data.repo.DealsRepository
import com.example.gamedock.domain.model.Game

/**
 * Retrieves the currently available freebies from the repository.
 */
class GetFreebiesUseCase(
    private val repository: DealsRepository
) {
    suspend operator fun invoke(): List<Game> = repository.getFreebies()
}
