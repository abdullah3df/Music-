package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.viewmodel.MusicViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Aura Music", appName)
  }

  @Test
  fun `create MusicViewModel successfully`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    try {
      val factory = MusicViewModel.Factory(context)
      val viewModel = factory.create(MusicViewModel::class.java)
      assertNotNull(viewModel)
    } catch (t: Throwable) {
      t.printStackTrace()
      throw t
    }
  }
}
