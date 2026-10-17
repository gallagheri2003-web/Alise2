package com.example.data.model

data class BtcOracleTelemetry(
    val btcUsd: Double = 300000.0,
    val vix1moPercent: Double = 4.31,
    val ethGasGweiMedian: Int = 25,
    val poolDepthBtc: Double = 20000.0,
    val effectiveYieldApy: Double = 5.35496,
    val arbitrageActive: Boolean = true,
    val lastArbProfitBtc: Double = 0.0,
    val taxRatePercentBtc: Double = 2.15
)
