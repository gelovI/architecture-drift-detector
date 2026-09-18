package dev.archdrift.analyzer.kotlin

import dev.archdrift.core.Dependency
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import dev.archdrift.core.SourceLocation
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtFile

class KotlinSourceAnalyzer {

    fun analyze(
        source: String,
        sourceFile: String? = null,
    ): List<Dependency> {
        val normalizedSource = source
            .replace("\r\n", "\n")
            .replace("\r", "\n")

        return KotlinPsiParser().use { parser ->
            val file = parser.parse(normalizedSource)

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

                    val fullyQualifiedTypeReferences = sourceClass
                        .collectDescendantsOfType<KtTypeReference>()
                        .filter { typeReference ->
                            typeReference.getParentOfType<KtClassOrObject>(strict = true) == sourceClass
                        }
                        .filter { "." in it.text }

                    val importedDependencies = file.importDirectives
                        .mapNotNull { importDirective ->
                            val importedFqName = importDirective.importedFqName
                                ?.asString()
                                ?: return@mapNotNull null

                            val referencedName = importDirective.aliasName
                                ?: importedFqName.substringAfterLast(".")

                            val reference = sourceClass
                                .collectDescendantsOfType<KtNameReferenceExpression>()
                                .firstOrNull { candidate ->
                                    candidate.getParentOfType<KtClassOrObject>(strict = true) == sourceClass &&
                                            candidate.getReferencedName() == referencedName
                                }
                                ?: return@mapNotNull null

                            Dependency(
                                source = qualifiedClassName,
                                target = importedFqName,
                                location = sourceLocation(
                                    file = file,
                                    element = reference,
                                    sourceFile = sourceFile,
                                ),
                            )
                        }

                    val fullyQualifiedDependencies = fullyQualifiedTypeReferences
                        .map { typeReference ->
                            Dependency(
                                source = qualifiedClassName,
                                target = typeReference.text,
                                location = sourceLocation(
                                    file = file,
                                    element = typeReference,
                                    sourceFile = sourceFile,
                                ),
                            )
                        }

                    (importedDependencies + fullyQualifiedDependencies).distinct()
                }
        }
    }

    private fun sourceLocation(
        file: KtFile,
        element: PsiElement,
        sourceFile: String?,
    ): SourceLocation? =
        sourceFile?.let {
            SourceLocation(
                file = it,
                line = file
                    .viewProvider
                    .document
                    ?.getLineNumber(element.textOffset)
                    ?.plus(1)
                    ?: 1,
            )
        }
    }