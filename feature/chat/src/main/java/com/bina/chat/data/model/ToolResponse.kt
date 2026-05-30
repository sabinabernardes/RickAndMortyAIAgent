package com.bina.chat.data.model

data class ToolResponse(
    val text: String?,
    val functionCalls: List<FunctionCallData>
)

data class FunctionCallData(
    val name: String,
    val args: Map<String, String?>
)
