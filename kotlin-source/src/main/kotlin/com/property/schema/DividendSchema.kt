package com.property.schema

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * The family of schemas for DividendState.
 */
object DividendSchema

/**
 * An DividendState schema.
 */
object DividendSchemaV1 : MappedSchema(
        schemaFamily = DividendSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentDividendState::class.java)) {
    @Entity
    @Table(name = "DividendState")
    class PersistentDividendState(
            @Column(name = "fundName")
            var fundName: String,

            @Column(name = "value")
            var value: Int,

            @Column(name = "linear_id")
            var linearId: UUID
    ) : PersistentState() {
        // Default constructor required by hibernate.
        constructor(): this("", 0,  UUID.randomUUID())
    }
}