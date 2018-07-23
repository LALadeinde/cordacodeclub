package com.property.contract

import com.property.state.DividendState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

/**
 * A implementation of a basic smart contract in Corda.
 *
 * This contract enforces rules regarding the creation of a valid [DividendState], which in turn encapsulates an [Dividend].
 *
 * For a new [Dividend] to be issued onto the ledger, a transaction is required which takes:
 * - Zero input states.
 * - One output state: the new [Dividend].
 * - An Create() command with the public keys of both the fundManager and the shareholders.
 *
 * All contracts must sub-class the [Contract] interface.
 */
open class IssueDividendContract : Contract {
    companion object {
        @JvmStatic
        val ISSUE_DIVIDEND_CONTRACT_ID = "com.property.contract.DividendContract"
    }

    /**
     * The verify() function of all the states' contracts must not throw an exception for a transaction to be
     * considered valid.
     */
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands.Issue>()
        requireThat {
            // Generic constraints around the Dividend transaction.
            "No inputs should be consumed when issuing an Dividend." using (tx.inputs.isEmpty())
            "Only one output state should be created." using (tx.outputs.size == 1)
            val out = tx.outputsOfType<DividendState>().single()
            //"The fundManager and the shareholders cannot be the same entity." using (out.fundName!= out.out.shareholders)
            // NOTE TO DO LIST CONSTRAIT: The FundState ( with a Name ) must exist in order to paid of dividend
            "All of the participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))

            // Dividend-specific constraints.
            "The Dividend's value must be non-negative." using (out.value > 0)
            "The Dividend's value must not be greater than a 10 million." using (out.value < 10000000)
        }
    }

    interface Commands : CommandData {
        fun verify(tx: LedgerTransaction, signers: List<PublicKey>)

        class Issue : Commands {
            companion object {
                val CONTRACT_RULE_INPUTS = "Zero inputs should be consumed when issuing an invoice."
                val CONTRACT_RULE_OUTPUTS = "Only one output should be created when issuing an invoice."
                val CONTRACT_RULE_SIGNERS = "All participants are required to sign when issuing an invoice."
            }

            override fun verify(tx: LedgerTransaction, signers: List<PublicKey>) {
                // Transaction Rules
                CONTRACT_RULE_INPUTS using (tx.inputs.isEmpty())
                CONTRACT_RULE_OUTPUTS using (tx.outputs.size == 1)

                // State Rules
                val outputState = tx.outputsOfType<DividendState>().single()
                CONTRACT_RULE_SIGNERS using (signers.containsAll(outputState.participants.map { it.owningKey }))
            }
        }

        class ChangeOwner : Commands {
            companion object {
                val CONTRACT_RULE_INPUTS = "At least one input should be consumed when changing ownership of an invoice."
                val CONTRACT_RULE_OUTPUTS = "At least one output should be created when changing ownership of an invoice."
                val CONTRACT_RULE_SIGNERS = "All participants are required to sign when changing ownership of an invoice."
            }

            override fun verify(tx: LedgerTransaction, signers: List<PublicKey>) {
                // Transaction Rules
                CONTRACT_RULE_INPUTS using (tx.inputs.isNotEmpty())
                CONTRACT_RULE_OUTPUTS using (tx.outputs.isNotEmpty())

                // State Rules
                val keys = tx.outputsOfType<DividendState>()
                        .flatMap { it.participants }
                        .map { it.owningKey }
                        .distinct()

                CONTRACT_RULE_SIGNERS using signers.containsAll(keys)
            }
        }

        class Amend : Commands {
            companion object {
                val CONTRACT_RULE_INPUTS = "Only one input should be consumed when amending an invoice."
                val CONTRACT_RULE_OUTPUTS = "Only one output should be created when amending an invoice."
            }

            override fun verify(tx: LedgerTransaction, signers: List<PublicKey>) {
                // Transaction Rules
                CONTRACT_RULE_INPUTS using (tx.inputs.size == 1)
                CONTRACT_RULE_OUTPUTS using (tx.outputs.size == 1)
            }
        }

        class Cancel : Commands {
            companion object {
                val CONTRACT_RULE_INPUTS = "Only one input should be consumed when cancelling an invoice."
                val CONTRACT_RULE_OUTPUTS = "Zero outputs should be created when cancelling an invoice."
            }

            override fun verify(tx: LedgerTransaction, signers: List<PublicKey>) {
                // Transaction Rules
                CONTRACT_RULE_INPUTS using (tx.inputs.size == 1)
                CONTRACT_RULE_OUTPUTS using (tx.outputs.isEmpty())
            }
        }
    }
}
