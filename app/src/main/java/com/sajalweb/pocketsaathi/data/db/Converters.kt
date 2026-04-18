package com.sajalweb.pocketsaathi.data.db

import androidx.room.TypeConverter
import com.sajalweb.pocketsaathi.data.model.Category

class Converters {
    @TypeConverter fun fromCategory(value: String): Category = Category.valueOf(value)
    @TypeConverter fun toCategory(category: Category): String = category.name
}
