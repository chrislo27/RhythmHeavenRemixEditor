package chrislo27.rhre.oauth2.discord

import spark.kotlin.Http
import java.net.URI
import java.net.URLEncoder

object DiscordOAuth2 {

	const val AUTHORIZE_URL: String = "https://discordapp.com/api/oauth2/authorize"
	const val TOKEN_URL: String = "https://discordapp.com/api/oauth2/token"
	const val REVOKATION_URL: String = "https://discordapp.com/api/oauth2/token/revoke"
	const val CLIENT_ID: String = "278329593012682754"
	const val PORT: Int = 5000
	const val REDIRECT_URI: String = "http://localhost:$PORT/discordcallback"

	const val JAVASCRIPT_TOKEN_EXTRACTOR: String =
"""
<!DOCTYPE html>
<html>
<head>
<script type="text/javascript">
function onLoad() {
	document.body.innerHTML = "Working...";
	if (window.location.hash) {
		var hash = window.location.hash.substring(1)
		var xhr = new XMLHttpRequest();
		xhr.open('POST', 'http://localhost:$PORT/discordcallbacktoken' + '?token=' + hash.substring(13, hash.indexOf('&')), true);
		xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
		xhr.send();
		document.body.innerHTML = "You may now close this tab and return to RHRE2.";
    } else {
		document.body.innerHTML = "Error: no token received";
	}
}
</script>
<title>RHRE2 Discord OAuth2</title>
</head>

<body onload="onLoad()">
<h1>You need JavaScript enabled for this to work.</h1>
</body>
</html>
"""

	fun createImplicitURI(scopes: String): URI {
		return URI.create(
				"$AUTHORIZE_URL?client_id=$CLIENT_ID&scope=$scopes&response_type=token&redirect_uri=" +
						URLEncoder.encode(REDIRECT_URI, "UTF-8"))
	}

	fun revokeToken(token: String) {
		khttp.post("$REVOKATION_URL?token=$token")
	}

	fun requestToken(scopes: String) {
		val http: Http = spark.kotlin.ignite().port(PORT)

		http.get("/discordcallback") response@{
			status(200)
			return@response JAVASCRIPT_TOKEN_EXTRACTOR
		}
		http.post("/discordcallbacktoken") {
			val token = request.queryString().substring(6)

			println(khttp.get("https://discordapp.com/api/users/@me/guilds", headers =
			mapOf("Authorization" to "Bearer $token",
				  "User-Agent" to "DiscordBot (https://github.com/chrislo27/RhythmHeavenRemixEditor2, ${ionium.templates.Main.version})")).text)

			revokeToken(token)
			http.stop()
		}

		println(createImplicitURI(scopes))
	}

}
