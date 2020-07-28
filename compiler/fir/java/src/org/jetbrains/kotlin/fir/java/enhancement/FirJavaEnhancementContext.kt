/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.java.enhancement

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirAnnotationCall
import org.jetbrains.kotlin.load.java.AnnotationTypeQualifierResolver
import org.jetbrains.kotlin.load.java.lazy.JavaDefaultQualifiers
import org.jetbrains.kotlin.load.java.lazy.JavaTypeQualifiersByElementType
import org.jetbrains.kotlin.load.java.lazy.QualifierByApplicabilityType
import org.jetbrains.kotlin.utils.Jsr305State

class FirJavaEnhancementContext private constructor(
    val session: FirSession,
    delegateForDefaultTypeQualifiers: Lazy<JavaTypeQualifiersByElementType?>
) {
    constructor(session: FirSession, typeQualifiersComputation: () -> JavaTypeQualifiersByElementType?) :
            this(session, lazy(LazyThreadSafetyMode.NONE, typeQualifiersComputation))

    val defaultTypeQualifiers: JavaTypeQualifiersByElementType? by delegateForDefaultTypeQualifiers

    val moduleInfo get() = session.moduleInfo
}

fun extractDefaultNullabilityQualifier(
    typeQualifierResolver: FirAnnotationTypeQualifierResolver,
    jsr305State: Jsr305State,
    annotationCall: FirAnnotationCall
): JavaDefaultQualifiers? {
    typeQualifierResolver.resolveQualifierBuiltInDefaultAnnotation(annotationCall)?.let { return it }

    val (typeQualifier, applicability) =
        typeQualifierResolver.resolveTypeQualifierDefaultAnnotation(annotationCall)
            ?: return null

    val jsr305ReportLevel = with(typeQualifierResolver) {
        resolveJsr305CustomLevel(annotationCall) ?: resolveJsr305ReportLevel(typeQualifier)
    }

    if (jsr305ReportLevel.isIgnore) {
        return null
    }

    val nullabilityQualifier = typeQualifier.extractNullability(
        typeQualifierResolver, jsr305State
    )?.copy(isForWarningOnly = jsr305ReportLevel.isWarning) ?: return null

    return JavaDefaultQualifiers(nullabilityQualifier, applicability)
}

fun FirJavaEnhancementContext.computeNewDefaultTypeQualifiers(
    typeQualifierResolver: FirAnnotationTypeQualifierResolver,
    jsr305State: Jsr305State,
    additionalAnnotations: List<FirAnnotationCall>
): JavaTypeQualifiersByElementType? {
    if (typeQualifierResolver.disabled) return defaultTypeQualifiers

    val defaultQualifiers =
        additionalAnnotations.mapNotNull { annotationCall ->
            extractDefaultNullabilityQualifier(
                typeQualifierResolver,
                jsr305State,
                annotationCall
            )
        }

    if (defaultQualifiers.isEmpty()) return defaultTypeQualifiers

    val defaultQualifiersByType =
        defaultTypeQualifiers?.defaultQualifiers?.let(::QualifierByApplicabilityType)
            ?: QualifierByApplicabilityType(AnnotationTypeQualifierResolver.QualifierApplicabilityType::class.java)

    var wasUpdate = false
    for (qualifier in defaultQualifiers) {
        for (applicabilityType in qualifier.qualifierApplicabilityTypes) {
            defaultQualifiersByType[applicabilityType] = qualifier
            wasUpdate = true
        }
    }

    return if (!wasUpdate) defaultTypeQualifiers else JavaTypeQualifiersByElementType(defaultQualifiersByType)
}

fun FirJavaEnhancementContext.copyWithNewDefaultTypeQualifiers(
    typeQualifierResolver: FirAnnotationTypeQualifierResolver,
    jsr305State: Jsr305State,
    additionalAnnotations: List<FirAnnotationCall>
): FirJavaEnhancementContext =
    when {
        additionalAnnotations.isEmpty() -> this
        else -> FirJavaEnhancementContext(session) {
            computeNewDefaultTypeQualifiers(typeQualifierResolver, jsr305State, additionalAnnotations)
        }
    }
