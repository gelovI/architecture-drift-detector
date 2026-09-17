package dev.archdrift.analyzer.kotlin

import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory

class KotlinPsiParser : AutoCloseable {

    private val disposable: Disposable = Disposer.newDisposable()

    private val environment: KotlinCoreEnvironment

    init {
        setIdeaIoUseFallback()

        environment = KotlinCoreEnvironment.createForProduction(
            disposable,
            CompilerConfiguration(),
            EnvironmentConfigFiles.JVM_CONFIG_FILES,
        )
    }

    private val psiFactory = KtPsiFactory(
        environment.project,
        false,
    )

    fun parse(source: String): KtFile =
        psiFactory.createFile(source)

    override fun close() {
        Disposer.dispose(disposable)
    }
}