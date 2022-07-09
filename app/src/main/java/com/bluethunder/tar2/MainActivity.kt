package com.bluethunder.tar2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val query = CloudDBZoneQuery.where(BookInfo::class.java).equalTo (BookEditFields.BOOK_NAME, "Zuozhuan")


    }
}