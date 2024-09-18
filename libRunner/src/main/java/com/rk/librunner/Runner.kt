package com.rk.librunner

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rk.librunner.runners.jvm.beanshell.BeanshellRunner
import com.rk.librunner.runners.web.html.HtmlRunner
import com.rk.librunner.runners.web.markdown.MarkDownRunner
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

abstract class RunnerImpl(runnableFileExt:List<String>){
    init {
      runnableFileExt.forEach { ext ->
          val x = Runner.registry[ext]
          if(x != null){
              x.add(this)
          }else{
              Runner.registry[ext] = mutableListOf(this)
          }
      }
    }
    abstract fun run(file: File, context: Context)
    abstract fun getName(): String
    abstract fun getDescription(): String
    abstract fun getIcon(context: Context): Drawable?
}

object Runner {
    val registry = HashMap<String, MutableList<RunnerImpl>>()
    
    fun isRunnable(file: File): Boolean {
        val ext = file.name.substringAfterLast('.', "")
        return registry.keys.any { it == ext }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun run(file: File, context: Context) {
        GlobalScope.launch(Dispatchers.Default) {
            if (isRunnable(file)) {
                val ext = file.name.substringAfterLast('.', "")
                val runners = registry[ext]
                if (runners?.size!! == 0) {
                    return@launch
                }
                if (runners.size == 1) {
                    runners[0].run(file, context)
                } else {
                    withContext(Dispatchers.Main){
                        showRunnerSelectionDialog(context, runners) { selectedRunner ->
                            selectedRunner.run(file, context)
                        }
                    }

                }
            }
        }

    }

    private fun showRunnerSelectionDialog(
        context: Context,
        runners: List<RunnerImpl>,
        onRunnerSelected: (RunnerImpl) -> Unit
    ) {
        val dialogView =
            LayoutInflater.from(context).inflate(R.layout.dialog_runner_selection, null)
        val recyclerView: RecyclerView = dialogView.findViewById(R.id.runner_recycler_view)

        recyclerView.layoutManager = LinearLayoutManager(context)

        val dialog =
            MaterialAlertDialogBuilder(context).setTitle("Choose Runtime").setView(dialogView)
                .setNegativeButton("Cancel", null).show()

        recyclerView.adapter = RunnerAdapter(runners, dialog, onRunnerSelected)
    }

}