package com.property.state

import com.property.schema.DividendSchemaV1
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

/**
 * The state object recording A dividend payment agreements between two parties.
 *
 * A state must implement [ContractState] or one of its descendants.
 *
 * @param value the value of the Dividend.
 * @param fundName the name of the fund who is receiving the Dividend.
 */
data class DividendState(val value: Int,
                         val fundName: Party,
                         override val linearId: UniqueIdentifier = UniqueIdentifier()):
        LinearState, QueryableState {
    /** The public keys of the involved parties. */
    override val participants: List<AbstractParty> get() = listOf(fundName)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is DividendSchemaV1 -> DividendSchemaV1.PersistentDividendState(
                    this.fundName.name.toString(),
                    this.value,
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(DividendSchemaV1)
}
