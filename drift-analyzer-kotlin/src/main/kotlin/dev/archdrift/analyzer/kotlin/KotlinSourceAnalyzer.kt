package dev.archdrift.analyzer.kotlin

import dev.archdrift.core.Dependency
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

class KotlinSourceAnalyzer {

    fun analyze(source: String): List<Dependency> =
        KotlinPsiParser().use { parser ->
            val file = parser.parse(source)

            val packageName = file.packageFqName.asString()

            file.declarations
                .filterIsInstance<KtClassOrObject>()
                .flatMap { sourceClass ->
                    val className = sourceClass.name
                        ?: return@flatMap emptyList()

                    val qualifiedClassName = if (packageName.isEmpty()) {
                        className
                    } else {
                        "$packageName.$className"
                    }

                    val referencedNames = sourceClass
                        .collectDescendantsOfType<KtNameReferenceExpression>()
                        .map { it.getReferencedName() }
                        .toSet()

                    file.importDirectives
                        .mapNotNull { importDirective ->
                            val importedFqName = importDirective.importedFqName
                                ?.asString()
                                ?: return@mapNotNull null

                            val referencedName = importDirective.aliasName
                                ?: importedFqName.substringAfterLast(".")

                            importedFqName to referencedName
                        }
                        .filter { (_, referencedName) ->
                            referencedName in referencedNames
                        }
                        .map { (importedFqName, _) ->
                            Dependency(
                                source = qualifiedClassName,
                                target = importedFqName,
                            )
                        }
                }
        }
}