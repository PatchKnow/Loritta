package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.EmbedBuilder
import java.util.*

class AvatarCommand : AbstractCommand("avatar") {
	override fun getDescription(locale: BaseLocale): String {
		return locale.AVATAR_DESCRIPTION.msgFormat()
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.DISCORD
	}

	override fun getUsage(): String {
		return "nome do usuário"
	}

	override fun getExample(): List<String> {
		return Arrays.asList("@Loritta")
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var getAvatar = LorittaUtils.getUserFromContext(context, 0)

		if (getAvatar == null) {
			getAvatar = context.userHandle
		}

		var embed = EmbedBuilder();
		embed.setColor(Constants.DISCORD_BURPLE) // Cor do embed (Cor padrão do Discord)
		embed.setDescription("**${context.locale["AVATAR_CLICKHERE", getAvatar.effectiveAvatarUrl + "?size=2048"]}**" + if (getAvatar.id == Loritta.config.clientId) "\n*${context.locale["AVATAR_LORITTACUTE"]}* \uD83D\uDE0A" else "");
		embed.setTitle("\uD83D\uDDBC ${getAvatar.name}")
		embed.setImage(getAvatar.effectiveAvatarUrl + if (!getAvatar.effectiveAvatarUrl.endsWith(".gif")) "?size=2048" else "")
		context.sendMessage(embed.build());
	}
}