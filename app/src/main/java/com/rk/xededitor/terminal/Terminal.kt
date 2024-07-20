package com.rk.xededitor.terminal

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.KeyboardUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rk.xededitor.BaseActivity
import com.rk.xededitor.MainActivity.StaticData
import com.rk.xededitor.R
import com.rk.xededitor.databinding.ActivityTerminalBinding
import com.rk.xededitor.rkUtils
import com.rk.xededitor.terminal.virtualkeys.VirtualKeysConstants
import com.rk.xededitor.terminal.virtualkeys.VirtualKeysInfo
import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalSession
import com.termux.view.TerminalView


class Terminal : BaseActivity() {
  private lateinit var terminal: TerminalView
  private lateinit var binding: ActivityTerminalBinding
  private lateinit var session: TerminalSession
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityTerminalBinding.inflate(layoutInflater)
    setContentView(binding.getRoot())
    terminal = TerminalView(this,null)
    terminal.setTerminalViewClient(TerminalClient(terminal, this))
    terminal.keepScreenOn = true
    terminal.setTextSize(rkUtils.dpToPx(13.5f, this))
    session = createSession()
    terminal.attachSession(session)
    terminal.setBackgroundColor(Color.BLACK)
    val params = LinearLayout.LayoutParams(-1, 0)
    params.weight = 1f
    binding.root.addView(terminal, 0, params)
    binding.extraKeys.reload(VirtualKeysInfo(VIRTUAL_KEYS, "", VirtualKeysConstants.CONTROL_CHARS_ALIASES))
    
    window.statusBarColor = ContextCompat.getColor(this, R.color.dark)
    window.decorView.systemUiVisibility = 0
    window.navigationBarColor = ContextCompat.getColor(this, R.color.dark)
    
    
  }
  
  val VIRTUAL_KEYS =
    ("[" +
        "\n  [" +
        "\n    \"ESC\"," +
        "\n    {" +
        "\n      \"key\": \"/\"," +
        "\n      \"popup\": \"\\\\\"" +
        "\n    }," +
        "\n    {" +
        "\n      \"key\": \"-\"," +
        "\n      \"popup\": \"|\"" +
        "\n    }," +
        "\n    \"HOME\"," +
        "\n    \"UP\"," +
        "\n    \"END\"," +
        "\n    \"PGUP\"" +
        "\n  ]," +
        "\n  [" +
        "\n    \"TAB\"," +
        "\n    \"CTRL\"," +
        "\n    \"ALT\"," +
        "\n    \"LEFT\"," +
        "\n    \"DOWN\"," +
        "\n    \"RIGHT\"," +
        "\n    \"PGDN\"" +
        "\n  ]" +
        "\n]")
  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    terminal.onKeyDown(keyCode, event)
    return super.onKeyDown(keyCode, event)
  }
  
  override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
    terminal.updateSize()
    terminal.onScreenUpdated()
    return super.onKeyUp(keyCode, event)
  }
  
  private fun createSession(): TerminalSession {
    val workingDir = filesDir.absolutePath
    val shell = "/system/bin/sh"
    val args = arrayOf("")
    val env = arrayOf(
      "HOME=" + filesDir.absolutePath,
      "PUBLIC_HOME=" + getExternalFilesDir(null)?.absolutePath,
      "SHELL=$shell",
      "COLORTERM=truecolor",
      "TERM=xterm-256color"
    )
    return TerminalSession(
      shell,
      workingDir,
      args,
      env,
      TerminalEmulator.DEFAULT_TERMINAL_TRANSCRIPT_ROWS,
      TerminalSessionClient(terminal, this)
    )
  }
}