package dev.archdrift.analyzer.kotlin

import dev.archdrift.core.Dependency
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

class KotlinSourceAnalyzer {

    fun analyze(source: String): List<Dependency> =
        KotlinPsiParser().use { parser ->
            val file = parser.parse(source)

            val packageName = file.packageFqName.asString()

            file.collectDescendantsOfType<KtClassOrObject>()
                .flatMap { sourceClass ->
                    val className = sourceClass.name
                        ?: return@flatMap emptyList()

                    val enclosingClassNames = generateSequence(
                        sourceClass.getParentOfType<KtClassOrObject>(strict = true),
                    ) { enclosingClass ->
                        enclosingClass.getParentOfType<KtClassOrObject>(strict = true)
                    }
                        .mapNotNull { it.name }
                        .toList()
                        .asReversed()

                    val relativeClassName = (enclosingClassNames + className)
                        .joinToString(".")

                    val qualifiedClassName = if (packageName.isEmpty()) {
                        relativeClassName
                    } else {
                        "$packageName.$relativeClassName"
                    }

                    val referencedNames = sourceClass
                        .collectDescendantsOfType<KtNameReferenceExpression>()
                        .filter { reference ->
                            reference.getParentOfType<KtClassOrObject>(strict = true) == sourceClass
                        }
                        .map { it.getReferencedName() }
                        .toSet()

                    val fullyQualifiedTypeNames = sourceClass
                        .collectDescendantsOfType<KtTypeReference>()
                        .filter { typeReference ->
                            typeReference.getParentOfType<KtClassOrObject>(strict = true) == sourceClass
                        }
                        .map { it.text }
                        .filter { "." in it }
                        .toSet()

                    val importedDependencies = file.importDirectives
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

                    val fullyQualifiedDependencies = fullyQualifiedTypeNames
                        .map { typeName ->
                            Dependency(
                                source = qualifiedClassName,
                                target = typeName,
                            )
                        }

                    (importedDependencies + fullyQualifiedDependencies).distinct()
                }
        }
}