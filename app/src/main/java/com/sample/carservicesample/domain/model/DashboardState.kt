package com.sample.carservicesample.domain.model

data class DashboardState(
    val speed: Float = 0f,
    val gear: Int = 0,
    val soc: Int = 0,
    val outsideTemp: Float = 0f,
    val doorOpen: Boolean = false
)
