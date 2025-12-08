package com.swadratna.swadratna_admin.data.model

import com.google.gson.annotations.SerializedName

data class WithdrawalResponse(
    val count: Int,
    val withdrawals: List<Withdrawal>?
)

data class Withdrawal(
    @SerializedName("cash_amount")
    val cashAmount: Double,
    @SerializedName("created_at")
    val createdAt: String,
    val id: Long,
    @SerializedName("payment_details")
    val paymentDetails: PaymentDetails?,
    @SerializedName("payment_method")
    val paymentMethod: String,
    val points: Int,
    @SerializedName("processed_at")
    val processedAt: String?,
    @SerializedName("rejection_reason")
    val rejectionReason: String?,
    val remarks: String?,
    val status: String,
    @SerializedName("transaction_ref")
    val transactionRef: String?,
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("user_name")
    val userName: String?,
    @SerializedName("user_phone")
    val userPhone: String?
)

data class PaymentDetails(
    val method: String
)

data class WithdrawalStatusUpdateRequest(
    @SerializedName("transaction_ref")
    val transactionRef: String? = null,
    val remarks: String? = null,
    val reason: String? = null
)

data class WithdrawalStatusUpdateResponse(
    val message: String,
    @SerializedName("wallet_balance")
    val walletBalance: Int,
    val withdrawal: Withdrawal
)
