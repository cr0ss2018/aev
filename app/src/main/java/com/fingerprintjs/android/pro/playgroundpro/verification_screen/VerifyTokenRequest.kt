package com.fingerprintjs.android.pro.playgroundpro.verification_screen


import com.fingerprintjs.android.pro.fingerprint.transport.Request
import com.fingerprintjs.android.pro.fingerprint.transport.RequestResultType
import com.fingerprintjs.android.pro.fingerprint.transport.TypedRequestResult
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap


class Verdict(
    val description: String
)

class VerificationResult(
    val requestId: String,
    val deviceId: String,
    val verdicts: List<Verdict>
)

class VerifyTokenResponse(
    type: RequestResultType,
    rawResponse: ByteArray?
) : TypedRequestResult<VerificationResult>(type, rawResponse) {
    override fun typedResult(): VerificationResult? {
        val errorResponse = VerificationResult("", "", emptyList())
        val body = rawResponse?.toString(Charsets.UTF_8) ?: return errorResponse
        return try {
            val jsonBody = JSONObject(body)
            val requestId = jsonBody.getString(REQUEST_ID_KEY)
            val deviceId = jsonBody.getString(DEVICE_ID_KEY)
            val verdict = jsonBody.getJSONObject(VERDICT_KEY)
            val analyzers = verdict.getJSONObject(ANALYZERS_KEY)
            val verdictList = LinkedList<Verdict>()
            analyzers.keys().forEach {
                verdictList.add(Verdict("$it: ${analyzers.getJSONObject(it).toString(2)}"))
            }
            VerificationResult(requestId, deviceId, verdictList)
        } catch (exception: Exception) {
            errorResponse
        }
    }
}

class VerifyTokenRequest(
    endpointUrl: String,
    autorizationToken: String,
    private val securityToken: String
) : Request {
    override val url = "$endpointUrl/api/v1/request"
    override val type = "POST"
    override val headers = mapOf(
        "Content-Type" to "application/json",
        "Authorization" to autorizationToken
    )

    override fun bodyAsMap(): Map<String, Any> {
        val resultMap = HashMap<String, Any>()
        resultMap["token"] = securityToken
        return resultMap
    }
}

private const val REQUEST_ID_KEY = "requestId"
private const val DEVICE_ID_KEY = "deviceId"
private const val VERDICT_KEY = "verdict"
private const val ANALYZERS_KEY = "analysers"