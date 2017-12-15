package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat

class EscolherCommand : AbstractCommand("escolher", listOf("choose")) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.ESCOLHER_DESCRIPTION.msgFormat()
	}

	override fun getExample(): List<String> {
		return listOf("Sonic, Tails, Knuckles", "Asriel Dreemurr, Chara Dreemurr", "Shantae, Risky Boots");
	}
	override fun getCategory(): CommandCategory {
		return CommandCategory.MISC;
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			var joined = context.args.joinToString(separator = " "); // Vamos juntar tudo em uma string
			var split = joined.split(","); // E vamos separar!

			// Hora de escolher algo aleatório!
			var chosen = split[Loritta.RANDOM.nextInt(split.size)];
			context.reply(
					LoriReply(
							message = "${context.locale["ESCOLHER_RESULT", chosen]}",
							prefix = "\uD83E\uDD14"
					)
			)
		} else {
			context.explain()
		}
	}
}