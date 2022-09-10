/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bluethunder.tar2

import android.app.Application
import com.bluethunder.tar2.cloud_db.CloudStorageWrapper
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig
import com.huawei.agconnect.AGConnectInstance
import com.huawei.hms.maps.MapsInitializer


class Tar2Application : Application() {
    override fun onCreate() {
        super.onCreate()
        AGConnectInstance.initialize(this)
        CloudStorageWrapper.initStorage(this)
        MapsInitializer.initialize(this)
        MapsInitializer.setApiKey("DAEDADPF5OJAG21jxCnUWAX0InV9vW76SXWUaSMiIv81YAXW8bfCDMkAKKZ3lMU9mC2GCv78cYTZgeOIZ8OJkKXPg4ynC/CyrCUuvQ==");
        val config = ImagePipelineConfig.newBuilder(this)
            .setProgressiveJpegConfig(SimpleProgressiveJpegConfig())
            .setResizeAndRotateEnabledForNetwork(true)
            .setDownsampleEnabled(true)
            .build()
        Fresco.initialize(this, config)
    }

}