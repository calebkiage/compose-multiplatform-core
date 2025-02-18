/*
 * Copyright 2021 The Android Open Source Project
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

package androidx.window.demo

import android.content.res.Configuration
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.window.demo.common.EdgeToEdgeActivity
import androidx.window.demo.common.infolog.InfoLogAdapter
import androidx.window.layout.WindowMetricsCalculator

class WindowMetricsActivity : EdgeToEdgeActivity() {

    private val adapter = InfoLogAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_window_metrics)
        findViewById<RecyclerView>(R.id.recycler_view).adapter = adapter
        adapter.append("onCreate", "triggered")

        updateMetrics()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateMetrics()
    }

    private fun updateMetrics() {
        val windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)
        val width = windowMetrics.bounds.width()
        val height = windowMetrics.bounds.height()
        adapter.append(
            "WindowMetrics update",
            "width: $width, height: $height, " + "density: ${windowMetrics.density}"
        )
        adapter.notifyDataSetChanged()
    }
}
