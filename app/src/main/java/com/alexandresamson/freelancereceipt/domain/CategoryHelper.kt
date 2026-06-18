package com.alexandresamson.freelancereceipt.domain

import android.content.Context
import com.alexandresamson.freelancereceipt.R

enum class Category(val dbKey: String, val displayNameResId: Int) {
    GROCERIES("groceries", R.string.category_groceries),
    TRAVEL("travel", R.string.category_travel),
    OFFICE("office", R.string.category_office),
    RESTAURANT("restaurant", R.string.category_restaurant),
    SOFTWARE("software", R.string.category_software),
    OTHER("other", R.string.category_other);

    fun displayName(context: Context): String = context.getString(displayNameResId)

    companion object {
        fun fromDbKey(key: String): Category =
            entries.firstOrNull { it.dbKey == key } ?: OTHER

        fun allDbKeys(): List<String> = entries.map { it.dbKey }

        fun displayNames(context: Context): List<String> =
            entries.map { it.displayName(context) }

        fun dbKeyToDisplayName(context: Context, dbKey: String): String =
            fromDbKey(dbKey).displayName(context)

        fun displayNameToDbKey(context: Context, displayName: String): String =
            entries.firstOrNull { it.displayName(context) == displayName }?.dbKey ?: OTHER.dbKey
    }
}
