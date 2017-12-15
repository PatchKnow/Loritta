package com.mrpowergamerbr.loritta.utils.debug

import com.mrpowergamerbr.loritta.threads.AminoRepostThread
import com.mrpowergamerbr.loritta.threads.NewLivestreamThread
import com.mrpowergamerbr.loritta.threads.NewRssFeedThread
import com.mrpowergamerbr.loritta.threads.NewYouTubeVideosThread
import com.mrpowergamerbr.loritta.utils.LORITTA_SHARDS
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.debug.DebugLog.subscribedDebugTypes
import com.mrpowergamerbr.loritta.utils.loritta
import net.pocketdreams.loriplugins.cleverbot.commands.CleverbotCommand
import java.util.concurrent.ThreadPoolExecutor
import kotlin.concurrent.thread

object DebugLog {
	val subscribedDebugTypes = mutableListOf<DebugType>()

	fun startCommandListenerThread() {
		thread {
			commandLoop@ while (true) {
				try {
					val line = readLine()!!
					handleLine(line)
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
		}
	}

	fun handleLine(line: String) {
		val args = line.split(" ").toMutableList()
		val command = args[0]
		args.removeAt(0)

		when (command) {
			"debug" -> {
				if (args.isNotEmpty()) {
					val todo = args[0]

					if (todo == "all") {
						subscribedDebugTypes.addAll(DebugType.values())
						return
					}
					if (todo == "none") {
						subscribedDebugTypes.clear()
						return
					}

					val type = args[1]

					if (todo == "add") {
						subscribedDebugTypes.add(DebugType.valueOf(type))

						println("$type added to the subscription list")
						return
					}
					if (todo == "remove") {
						subscribedDebugTypes.remove(DebugType.valueOf(type))

						println("$type removed from the subscription list")
						return
					}
				}
				println("Subscribed Debug Types: ${subscribedDebugTypes.joinToString(", ", transform = { it.name })}")
			}
			"info" -> {
				val mb = 1024 * 1024
				val runtime = Runtime.getRuntime()
				println("===[ INFO ]===")
				println("Shards: ${LORITTA_SHARDS.shards.size}")
				println("Total Servers: ${LORITTA_SHARDS.getGuilds().size}")
				println("Users: ${LORITTA_SHARDS.getUsers().size}")
				//Print used memory
				println("Used Memory:"
						+ (runtime.totalMemory() - runtime.freeMemory()) / mb);

				//Print free memory
				println("Free Memory:"
						+ runtime.freeMemory() / mb);

				//Print total available memory
				println("Total Memory:" + runtime.totalMemory() / mb);

				//Print Maximum available memory
				println("Max Memory:" + runtime.maxMemory() / mb);
			}
			"extendedinfo" -> {
				println("===[ EXTENDED INFO ]===")
				println("commandManager.commandMap.size: ${loritta.commandManager.commandMap.size}")
				println("commandManager.defaultCmdOptions.size: ${loritta.commandManager.defaultCmdOptions.size}")
				println("dummyServerConfig.userData.size: ${loritta.dummyServerConfig.userData.size}")
				println("messageContextCache.size: ${loritta.messageContextCache.size}")
				println("messageInteractionCache.size: ${loritta.messageInteractionCache.size}")
				println("rawServersFanClub.size: ${loritta.rawServersFanClub.size}")
				println("serversFanClub.size: ${loritta.serversFanClub.size}")
				println("locales.size: ${loritta.locales.size}")
				println("ignoreIds.size: ${loritta.ignoreIds.size}")
				println("userCooldown.size: ${loritta.userCooldown.size}")
				println("southAmericaMemesPageCache.size: ${loritta.southAmericaMemesPageCache.size}")
				println("southAmericaMemesGroupCache.size: ${loritta.southAmericaMemesGroupCache.size}")
				println("musicManagers.size: ${loritta.musicManagers.size}")
				println("songThrottle.size: ${loritta.songThrottle.size}")
				println("youTubeKeys.size: ${loritta.youtubeKeys.size}")
				println("famousGuilds.size: ${loritta.famousGuilds.size}")
				println("randomFamousGuilds.size: ${loritta.randomFamousGuilds.size}")
				println("isPatreon.size: ${loritta.isPatreon.size}")
				println("isDonator.size: ${loritta.isDonator.size}")
				println("youTubeKeys.size: ${loritta.youtubeKeys.size}")
				println("fanArts.size: ${loritta.fanArts.size}")
				println("cleverbots.size: ${CleverbotCommand.cleverbots.size}")
				println("commandQueue.size: ${LorittaUtilsKotlin.commandQueue.size}")
				println("storedLastIds.size: ${AminoRepostThread.storedLastIds.size}")
				println("gameInfoCache.size: ${NewLivestreamThread.gameInfoCache.size}")
				println("isLivestreaming.size: ${NewLivestreamThread.isLivestreaming.size}")
				println("displayNameCache.size: ${NewLivestreamThread.displayNameCache.size}")
				println("lastItemTime.size: ${NewRssFeedThread.lastItemTime.size}")
				println("channelPlaylistIdCache.size: ${NewYouTubeVideosThread.channelPlaylistIdCache.size}")
				println("doNotReverify.size: ${NewYouTubeVideosThread.doNotReverify.size}")
				println("youTubeVideoCache.size: ${NewYouTubeVideosThread.youTubeVideoCache.size}")
			}
			"threads" -> {
				println("===[ ACTIVE THREADS ]===")
				println("eventLogExecutors: ${(loritta.eventLogExecutors as ThreadPoolExecutor).activeCount}")
				println("messageExecutors: ${(loritta.messageExecutors as ThreadPoolExecutor).activeCount}")
				println("executor: ${(loritta.executor as ThreadPoolExecutor).activeCount}")
			}
		}
	}
}

enum class DebugType {
	MESSAGE_RECEIVED, REACTION_RECEIVED, COMMAND_EXECUTED, TWITCH_THREAD
}

fun debug(type: DebugType, message: Any?) {
	if (subscribedDebugTypes.contains(type)) {
		System.out.println("[${type.name}] $message")
	}
}