package com.octawizard.server.input

/**
 * Examples:
 * { "op": "ADD", "path": "/player", "value": "rob@email.com" } to let the user join the match,
 * { "op": "REMOVE", "path": "/player", value: "rob@email.com" } to let the user leave the match
 */
data class PatchMatchInput(val op: OpType, val value: String)

enum class OpType {
    REMOVE, ADD
}


