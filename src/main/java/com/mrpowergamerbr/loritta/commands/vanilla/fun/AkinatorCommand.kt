package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.perfectdreams.loritta.api.commands.CommandCategory
import org.json.XML
import java.awt.Color
import kotlin.collections.set

class AkinatorCommand : AbstractCommand("akinator", category = CommandCategory.FUN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.get("AKINATOR_DESCRIPTION")
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_MANAGE)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	fun getApiEndpoint(localeId: String): String {
		return when (localeId) {
			"default", "pt-pt", "pt-funk" -> "https://srv11.akinator.com:9174"
			"tr-tr" -> "https://srv3.akinator.com:9211"
			"pl-pl" -> "https://srv7.akinator.com:9143"
			"ru-ru" -> "https://srv12.akinator.com:9190"
			"nl-nl" -> "https://srv9.akinator.com:9215"
			"kr-kr" -> "https://srv2.akinator.com:9156"
			"ja-jp" -> "https://srv11.akinator.com:9172"
			"it-it" -> "https://srv9.akinator.com:9214"
			"he-il" -> "https://srv12.akinator.com:9189"
			"fr-fr" -> "https://srv3.akinator.com:9217"
			"es-es" -> "https://srv6.akinator.com:9127"
			"ar-sa" -> "https://srv2.akinator.com:9155"
			"ch-ch" -> "https://srv11.akinator.com:9150"
			else -> "https://srv2.akinator.com:9157"
		}
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val apiEndpoint = getApiEndpoint(context.config.localeId)
		val response = HttpRequest.get("$apiEndpoint/ws/new_session.php?base=0&partner=410&premium=0&player=Android-Phone&uid=6fe3a92130c49446&do_geoloc=1&prio=0&constraint=ETAT%3C%3E'AV'&channel=0&only_minibase=0")
				.body()

		val xmlJSONObj = XML.toJSONObject(response)

		val jsonPrettyPrintString = xmlJSONObj.toString(4)

		val jsonSession = jsonParser.parse(jsonPrettyPrintString).obj["RESULT"]

		val identification = jsonSession["PARAMETERS"]["IDENTIFICATION"].obj

		val channel = identification["CHANNEL"].long
		val session = identification["SESSION"].long
		val signature = identification["SIGNATURE"].long

		val stepInfo = jsonSession["PARAMETERS"]["STEP_INFORMATION"]

		val question = stepInfo["QUESTION"].string
		val progression = stepInfo["PROGRESSION"].double
		val step = stepInfo["STEP"].int
		val answers = stepInfo["ANSWERS"]["ANSWER"].array

		var text = "[`"
		for (i in 0..100 step 10) {
			if (progression >= i) {
				text += "█"
			} else {
				text += "."
			}
		}
		text += "`]"

		var reactionInfo = ""

		for ((idx, answer) in answers.withIndex()) {
			reactionInfo += Constants.INDEXES[idx] + " ${answer.string}\n"
		}

		val builder = EmbedBuilder().apply {
			setTitle("<:akinator:383613256939470849> Akinator (${context.handle.effectiveName})")
			setThumbnail("${Loritta.config.websiteUrl}assets/img/akinator_embed.png")
			setDescription(question + "\n\n$progression% $text\n\n$reactionInfo")
			setColor(Color(20, 158, 255))
		}

		context.metadata["channel"] = channel
		context.metadata["session"] = session
		context.metadata["signature"] = signature
		context.metadata["step"] = step

		val message = context.sendMessage(context.getAsMention(true), builder.build())

		message.onReactionAddByAuthor(context) {
			val apiEndpoint = getApiEndpoint(context.config.localeId)

			it.reaction.removeReaction(context.userHandle).queue()
			if (context.metadata.contains("channel")) {
				val channel = context.metadata["channel"] as Long
				val session = context.metadata["session"] as Long
				val signature = context.metadata["signature"] as Long
				var step = context.metadata["step"] as Int
				val answer = when {
					it.reactionEmote.name == "1⃣" -> 0
					it.reactionEmote.name == "2⃣" -> 1
					it.reactionEmote.name == "3⃣" -> 2
					it.reactionEmote.name == "4⃣" -> 3
					it.reactionEmote.name == "5⃣" -> 4
					else -> 0
				}

				val response = if (it.reactionEmote.name == "⏪") {
					HttpRequest.get("$apiEndpoint/ws/cancel_answer.php?base=0&channel=$channel&session=$session&signature=$signature&step=$step")
							.body()
				} else {
					HttpRequest.get("$apiEndpoint/ws/answer.php?base=0&channel=$channel&session=$session&signature=$signature&step=$step&answer=$answer")
							.body()
				}

				val xmlJSONObj = XML.toJSONObject(response)

				val jsonPrettyPrintString = xmlJSONObj.toString(4)

				val jsonResult = jsonParser.parse(jsonPrettyPrintString).obj["RESULT"]

				if (jsonResult["COMPLETION"].string == "KO - TIMEOUT") {
					val builder = EmbedBuilder().apply {
						setTitle("<:akinator:383613256939470849> Akinator")
						setDescription(context.legacyLocale.get("AKINATOR_TIMEOUT"))
						setColor(Color(20, 158, 255))
					}

					context.metadata.remove("channel")
					context.metadata.remove("signature")
					context.metadata.remove("session")
					context.metadata.remove("step")

					message.clearReactions().queue()
					message.editMessage(builder.build()).queue()
					return@onReactionAddByAuthor
				}

				if (jsonResult["COMPLETION"].string == "WARN - NO QUESTION") {
					val builder = EmbedBuilder().apply {
						setTitle("<:akinator:383613256939470849> Akinator")
						setDescription(context.legacyLocale.get("AKINATOR_NoQuestion"))
						setColor(Color(20, 158, 255))
					}

					context.metadata.remove("channel")
					context.metadata.remove("signature")
					context.metadata.remove("session")
					context.metadata.remove("step")

					message.clearReactions().queue()
					message.editMessage(builder.build()).queue()
					return@onReactionAddByAuthor
				}

				try {
					val jsonAnswer = jsonResult["PARAMETERS"]

					val question = jsonAnswer["QUESTION"].string
					val progression = jsonAnswer["PROGRESSION"].double
					step = jsonAnswer["STEP"].int
					val answers = jsonAnswer["ANSWERS"]["ANSWER"].array

					if (95 >= progression) {
						var text = "[`"
						for (i in 0..100 step 10) {
							if (progression >= i) {
								text += "█"
							} else {
								text += "."
							}
						}
						text += "`]"

						var reactionInfo = ""

						for ((idx, answer) in answers.withIndex()) {
							reactionInfo += Constants.INDEXES[idx] + " ${answer.string}\n"
						}

						val builder = EmbedBuilder().apply {
							setTitle("<:akinator:383613256939470849> Akinator (${context.handle.effectiveName})")
							setThumbnail("https://loritta.website/assets/img/akinator_embed.png")
							setDescription(question + "\n\n$progression% $text\n\n$reactionInfo")
							setColor(Color(20, 158, 255))
						}

						context.metadata["channel"] = channel
						context.metadata["session"] = session
						context.metadata["signature"] = signature
						context.metadata["step"] = step

						message.editMessage(builder.build()).queue()

						if (message.reactions.filter { it.reactionEmote.name == "⏪" }.count() == 0) {
							if (step > 0) {
								message.addReaction("⏪").queue()
							}
						} else {
							if (step == 0) {
								message.reactions.forEach {
									if (it.reactionEmote.name == "⏪") {
										it.removeReaction(context.userHandle).queue()
									}
								}
							}
						}
					} else {
						val response = HttpRequest.get("$apiEndpoint/ws/list.php?base=0&channel=$channel&session=$session&signature=$signature&step=$step&size=1&max_pic_width=360&max_pic_height=640&mode_question=0")
								.body()

						val xmlJSONObj = XML.toJSONObject(response)

						val jsonPrettyPrintString = xmlJSONObj.toString(4)

						val jsonAnswer = jsonParser.parse(jsonPrettyPrintString).obj["RESULT"]["PARAMETERS"]["ELEMENTS"]["ELEMENT"]

						val builder = EmbedBuilder().apply {
							setTitle("<:akinator:383613256939470849> ${jsonAnswer["NAME"].string}")
							setImage(jsonAnswer["ABSOLUTE_PICTURE_PATH"].string)
							setDescription(jsonAnswer["DESCRIPTION"].string)
							addField("Ranking", "#${jsonAnswer["RANKING"].string}", false)
							setColor(Color(20, 158, 255))
						}

						context.metadata.remove("channel")
						context.metadata.remove("signature")
						context.metadata.remove("session")
						context.metadata.remove("step")

						message.clearReactions().queue()
						message.editMessage(builder.build()).queue()
					}
				} catch (e: Exception) {
					logger.error(response, e)
				}
			}
		}

		for (emote in Constants.INDEXES.subList(0, 5)) {
			message.addReaction(emote).queue()
		}
	}
}