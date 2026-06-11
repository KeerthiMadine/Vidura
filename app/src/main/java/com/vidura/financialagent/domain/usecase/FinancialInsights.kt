package com.vidura.financialagent.domain.usecase

import com.vidura.financialagent.data.database.Transaction
import java.time.LocalDateTime
import java.time.ZoneId

class FinancialInsights {
    fun generateInsights(transactions: List<Transaction>): List<Insight> {
        val insights = mutableListOf<Insight>()

        if (transactions.isEmpty()) return insights

        // Calculate spending by category
        val spendingByCategory = transactions
            .groupBy { it.category }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
            .toList()
            .sortByDescending { it.second }

        // Top spending category insight
        if (spendingByCategory.isNotEmpty()) {
            val topCategory = spendingByCategory[0]
            insights.add(
                Insight(
                    type = "TOP_CATEGORY",
                    title = "Top Spending Category",
                    message = "You spent the most on ${topCategory.first} (₹${String.format("%.2f", topCategory.second)})",
                    priority = "info"
                )
            )
        }

        // Spending trend
        val totalSpending = transactions.sumOf { it.amount }
        val avgSpending = totalSpending / transactions.size
        val highSpendTransactions = transactions.count { it.amount > avgSpending * 1.5 }

        if (highSpendTransactions > 0) {
            insights.add(
                Insight(
                    type = "HIGH_SPENDING",
                    title = "Unusual Spending Detected",
                    message = "You have $highSpendTransactions transactions significantly above your average (₹${String.format("%.2f", avgSpending)})",
                    priority = "warning"
                )
            )
        }

        // Daily average
        val uniqueDays = transactions
            .map { it.timestamp / (1000 * 60 * 60 * 24) }
            .toSet()
            .size

        val dailyAverage = if (uniqueDays > 0) totalSpending / uniqueDays else 0.0
        insights.add(
            Insight(
                type = "DAILY_AVERAGE",
                title = "Daily Average Spending",
                message = "Your daily average spending is ₹${String.format("%.2f", dailyAverage)}",
                priority = "info"
            )
        )

        // Budget recommendations
        val monthlyAverage = dailyAverage * 30
        insights.add(
            Insight(
                type = "BUDGET_RECOMMENDATION",
                title = "Suggested Monthly Budget",
                message = "Based on your spending pattern, a monthly budget of ₹${String.format("%.2f", monthlyAverage)} would be comfortable",
                priority = "info"
            )
        )

        return insights
    }

    fun getSpendingByCategory(transactions: List<Transaction>): Map<String, Double> {
        return transactions
            .groupBy { it.category }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
    }

    data class Insight(
        val type: String,
        val title: String,
        val message: String,
        val priority: String // "info", "warning", "critical"
    )
}
