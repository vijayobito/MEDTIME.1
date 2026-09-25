package com.example.util

import kotlin.math.*

data class VisitFeeResult(
    val distanceKm: Double,
    val billableKm: Int,
    val baseFee: Double,
    val additionalFee: Double,
    val totalFee: Double
)

object LocationUtils {

    /**
     * Calculates the great-circle distance between two points on the Earth
     * using the Haversine formula (returned in kilometers rounded to 1 decimal place).
     */
    fun calculateDistanceKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = earthRadiusKm * c

        // Round to 1 decimal place
        return (distance * 10.0).roundToInt() / 10.0
    }

    /**
     * Calculates visit fee based on the exact project pricing rules:
     * - 1 km to 3 km: ₹30 fixed base fee
     * - Above 3 km: ₹30 base + ₹10 for every additional kilometre
     * - Fractional/decimal distances are rounded UP to the next whole kilometre (e.g. 3.2 km -> 4 km -> ₹40)
     */
    fun calculateVisitFee(distanceKm: Double): VisitFeeResult {
        val positiveDistance = maxOf(0.1, distanceKm)
        val billableKm = maxOf(1, ceil(positiveDistance).toInt())
        val baseFee = 30.0
        val additionalFee = if (billableKm > 3) {
            ((billableKm - 3) * 10).toDouble()
        } else {
            0.0
        }
        val totalFee = baseFee + additionalFee

        return VisitFeeResult(
            distanceKm = (positiveDistance * 10.0).roundToInt() / 10.0,
            billableKm = billableKm,
            baseFee = baseFee,
            additionalFee = additionalFee,
            totalFee = totalFee
        )
    }

    /**
     * Backward-compatible helper returning Triple(baseCharge, additionalDistanceCharge, totalCharge)
     */
    fun calculateVisitPricing(distanceKm: Double): Triple<Double, Double, Double> {
        val result = calculateVisitFee(distanceKm)
        return Triple(result.baseFee, result.additionalFee, result.totalFee)
    }
}

