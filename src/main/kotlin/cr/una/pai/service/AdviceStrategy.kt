package cr.una.pai.service

import cr.una.pai.dto.AIContextData
import cr.una.pai.dto.AIAdvisorResponse

interface AdviceStrategy {
    fun generarConsejo(contexto: AIContextData): AIAdvisorResponse
}

