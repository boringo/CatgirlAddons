package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.ChatPacket
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import kotlin.concurrent.schedule

object Party {

    val members = mutableListOf<String>()
    var leader: String? = null

    private val memberJoined = listOf(
        Regex("^(.+) joined the party.$"),
        Regex("^You have joined (.+)'s? party!$"),
        Regex("^Party Finder > (.+) joined the dungeon group! (.+)$")
    )

    private val memberLeft = listOf(
        Regex("^(.+) has been removed from the party.$"),
        Regex("^(.+) has left the party.$"),
        Regex("^(.+) was removed from your party because they disconnected.$"),
        Regex("^Kicked (.+) because they were offline.$")
    )

    private val partyDisbanded = listOf(
        Regex("^.+ has disbanded the party!$"),
        Regex("^The party was disbanded because all invites expired and the party was empty$"),
        Regex("^You left the party.$"),
//        Regex("^Party Members \\(\\d+\\)$"),
        Regex("^You are not currently in a party\\.$"),
        Regex("^You have been kicked from the party by .+$"),
        Regex("^The party was disbanded because the party leader disconnected\\.$")
    )

    private val leaderMessages = listOf(
        Regex("^Party Leader: (.+) ●$"),
        Regex("^You have joined (.+)'s* party!$"),
        Regex("^The party was transferred to (.+) by .+$")
    )

    @SubscribeEvent
    fun onChatPacket(event: ChatPacket) {
        val msg = event.message
        val formatted = event.formatted

        memberJoined.forEach { regex ->
            regex.find(msg)?.groupValues?.get(1)?.let { addMember(it) }
        }

        memberLeft.forEach { regex ->
            regex.find(msg)?.groupValues?.get(1)?.let { removeMember(it) }
        }

        leaderMessages.forEach { regex ->
            regex.find(msg)?.groupValues?.get(1)?.let { makeLeader(it) }
        }

        partyDisbanded.forEach { regex ->
            if (regex.containsMatchIn(msg)) {
                disbandParty()
            }
        }

        // Joined a party
        Regex("§eYou'll be partying with: .+").find(formatted)?.let { match ->
            match.value.split("§e, ").forEach { player ->
                addMember(player)
            }
        }

        // Party List shown in chat
        Regex("^§eParty .+: (.+)").find(formatted)?.let { match ->
            match.groupValues[1].split(Regex("§r§a ● §r|§r§c ● §r| §r§a●§r| §r§c●§r"))
                .filter { it.replace(" ", "").isNotEmpty() }
                .forEach { player -> addMember(player) }
        }

        // You make a party in party finder
        if (msg == "Party Finder > Your party has been queued in the dungeon finder!") {
            Timer().schedule(1250) {
                hidePartySpam(1000)
                ChatUtils.sendChat("/pl")
            }
        }

        // Creating a party
        if (members.isEmpty()) {
            Regex("(.+) §einvited §r.+ §r§eto the party! They have §r§c60 §r§eseconds to accept.§r")
                .find(formatted)?.let { match ->
                    makeLeader(match.groupValues[1])
                }
        }

        // Joining a party
        if (Regex("§eYou have joined §r.+'s §r§eparty!§r").containsMatchIn(formatted)) {
            Timer().schedule(1300) {
                hidePartySpam(750)
                ChatUtils.sendChat("/pl")
            }
        }

        Regex("§eThe party was transferred to §r(.+) §r§ebecause §r(.+) §r§eleft§r")
            .find(formatted)?.let { match ->
                val newLeader = match.groupValues[1]
                val oldLeader = match.groupValues[2]

                if (oldLeader.stripRank == mc.thePlayer.name) {
                    disbandParty()
                } else {
                    makeLeader(newLeader)
                    removeMember(oldLeader)
                }
            }
    }

    private var hidingPartySpam = false

    private val partySpamMessages = listOf(
        Regex(".+ has disbanded the party!"),
        Regex("(.+) invited (.+) to the party! They have 60 seconds to accept."),
        Regex("-----------------------------------------------------"),
        Regex("Party [Members|Leader:|Members:]+.+"),
    )

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (!hidingPartySpam) return
        partySpamMessages.forEach {
            if (event.message.unformattedText.matches(it)) event.isCanceled = true
        }
        if (event.message.unformattedText.noControlCodes.isEmpty()) event.isCanceled = true
    }

    private fun hidePartySpam(delay: Long) {
        hidingPartySpam = true

        Timer().schedule(delay) { hidingPartySpam = false }
    }


    private fun addMember(player: String) {
        val playerNoRank = player.stripRank
        if (members.contains(playerNoRank) || playerNoRank == mc.thePlayer.name) return
        if (playerNoRank.contains("You'll be partying")) return // cba to fix regex
        members.add(playerNoRank)
    }

    private fun removeMember(player: String) {
        members.remove(player.stripRank)
    }

    private fun makeLeader(player: String) {
        leader = player.stripRank
    }

    private fun disbandParty() {
        members.clear()
        leader = null
    }
}