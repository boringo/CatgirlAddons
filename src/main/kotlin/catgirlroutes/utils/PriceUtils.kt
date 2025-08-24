package catgirlroutes.utils

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagInt
import kotlin.math.pow

object PriceUtils {

    private val masterStars = listOf(
        "FIRST_MASTER_STAR",
        "SECOND_MASTER_STAR",
        "THIRD_MASTER_STAR",
        "FOURTH_MASTER_STAR",
        "FIFTH_MASTER_STAR"
    )

    private val stackingEnchants = listOf(
        "champion",
        "cultivating",
        "toxophilite",
        "compact",
        "hecatomb",
        "expertise"
    )

    private val promisingTools = listOf(
        "PROMISING_PICKAXE",
        "PROMISING_SPADE",
        "PROMISING_AXE",
        "PROMISING_HOE"
    )

    private fun getPrice(skyblockID: String): Double {
        return NeuRepo.getItemFromID(skyblockID)?.price ?: 0.0
    }

    private val ItemStack.price get() = getPrice(this.skyblockID)

    private fun getBookPriceFromT1s(itemId: String): Double {
        val match = Regex("""^(.+?)(\d+)$""").find(itemId) ?: return 0.0
        val (enchantBase, tierStr) = match.destructured
        val tier = tierStr.toIntOrNull() ?: return 0.0

        val tier1Id = "${enchantBase}1"
        val tier1Price = getPrice(tier1Id)

        return tier1Price * 2.0.pow(tier - 1)
    }

    // ty bloom
    fun getItemValue(item: ItemStack): Pair<Double, ItemValueBreakdown?> { // todo: gems shit

        val sbId = item.skyblockID.ifEmpty { return 0.0 to null }

        val baseValue = item.price

        val breakdown = ItemValueBreakdown()
        breakdown.addText("Base", NeuRepo.getItemFromID(sbId)?.name, baseValue)

        val extra = item.getSubCompound("ExtraAttributes", false) ?: return breakdown.total to breakdown

        if (extra.hasKey("modifier")) { // todo multiline
            val reforge = extra.getString("modifier")
            val reforgeData = NeuRepo.getReforgeFromName(reforge)
            val rarity = item.getRarityOrNull()
            if (reforgeData != null && rarity != null) {
                breakdown.addText("Reforge", NeuRepo.getItemFromID(reforgeData.skyblockID)?.name, reforgeData.costs[rarity])
            }
        }

        if (extra.getInteger("rarity_upgrades") == 1) {
            breakdown.add("Recombobulator", getPrice("RECOMBOBULATOR_3000"))
        }

        if (extra.hasKey("art_of_war_count")) {
            breakdown.add("Art of War", getPrice("THE_ART_OF_WAR"))
        }

        // todo aop

        if (extra.hasKey("talisman_enrichment")) {
            val enrichmentType = extra.getString("talisman_enrichment").uppercase()
            val enrichmentId = "TALISMAN_ENRICHMENT_$enrichmentType"
            breakdown.addText("Enrichment", NeuRepo.getItemFromID(enrichmentId)?.name, getPrice(enrichmentId))
        }

        if (extra.hasKey("ability_scroll")) {
            val scrollsList = extra.getTagList("ability_scroll", 8)

            val scrollValues = (0 until scrollsList.tagCount()).associate { i ->
                val scrollId = scrollsList.getStringTagAt(i)
                NeuRepo.getItemFromID(scrollId)!!.name to (getPrice(scrollId))
            }

            breakdown.addMulti("Scrolls", scrollValues)
        }

        if (extra.hasKey("ethermerge")) {
            breakdown.add("Etherwarp", getPrice("ETHERWARP_MERGER") + getPrice("ETHERWARP_CONDUIT"))
        }

        if (extra.hasKey("tuned_transmission")) {
            val count = extra.getInteger("tuned_transmission")
            breakdown.addText("Transmission tuners", "§e$count§7/§e4", getPrice("TRANSMISSION_TUNER") * count)
        }

        if (extra.hasKey("dungeon_item_conversion_cost")) {
            breakdown.add("Dungeon conversion", getPrice(extra.getString("dungeon_item_conversion_cost")))
        }

        if (extra.hasKey("upgrade_level")) {
            val essenceData = NeuRepo.getEssenceDataFromID(sbId)

            val maxUpgrades = essenceData?.upgrades?.lastOrNull()?.star ?: 0
            val upgradeLvl = extra.getInteger("upgrade_level")

            val essenceUpgrades = essenceData?.upgrades?.take(minOf(upgradeLvl, maxUpgrades))

            val count = essenceUpgrades?.size
            val price = essenceUpgrades?.sumOf { upgrade ->
                getPrice(essenceData.essenceID) * upgrade.cost
            }

            breakdown.addText("Stars", "§e$count§7/§e$maxUpgrades", price)

            if (upgradeLvl > maxUpgrades && upgradeLvl - maxUpgrades <= 5) {
                val mStars = masterStars.take(upgradeLvl - maxUpgrades)
                val mStarsPrice = mStars.sumOf { getPrice(it) }
                breakdown.addText("Master stars", "§e${mStars.size}§7/§e5", mStarsPrice)
            }
        }

        if (extra.hasKey("hot_potato_count")) {
            val hpbCount = extra.getInteger("hot_potato_count")
            breakdown.apply {
                val regularHPBs = minOf(hpbCount, 10)
                addText("HPBs", "§e$regularHPBs§7/§e10", regularHPBs * getPrice("HOT_POTATO_BOOK"))

                if (hpbCount > 10) {
                    val fumingCount = hpbCount - 10
                    addText("Fumings", "§e$fumingCount§7/§e5", fumingCount * getPrice("FUMING_POTATO_BOOK"))
                }
            }
        }

        // todo rune

        if (extra.hasKey("dye_item")) {
            val dyeId = extra.getString("dye_item")
            breakdown.addText("Dye", NeuRepo.getItemFromID(dyeId)?.name, getPrice(dyeId))
        }

        if (extra.hasKey("gems")) { // todo unlock cost; fix showing only 1 gem
            val gemNbt = extra.getCompoundTag("gems")
            val gemTypes = gemNbt.keySet
                .mapNotNull { key ->
                    Regex("^(\\w+_\\d+)_gem$").find(key)?.let { match ->
                        match.groupValues[1] to gemNbt.getString(key)
                    }
                }.toMap()

            val gemstoneValues = gemNbt.keySet.mapNotNull { key ->
                val baseType = Regex("""^(\w+)_\d+$""").find(key)?.groupValues?.get(1) ?: return@mapNotNull null
                val gemType = gemTypes[key] ?: baseType

                val quality = when (val value = gemNbt.getTag(key)) {
                    is NBTTagCompound -> value.getString("quality")?.takeIf { it.isNotEmpty() } ?: "PERFECT"
                    else -> value.toString().replace("\"", "")
                }

                val gemId = "${quality}_${gemType}_GEM"
                getPrice(gemId).let { NeuRepo.getItemFromID(gemId)!!.name to it }
            }.toMap()

            breakdown.addMulti("Gemstones", gemstoneValues)
        }

        if (extra.hasKey("enchantments")) {
            val enchantments = extra.getCompoundTag("enchantments")
            val enchantmentValue = enchantments.keySet.mapNotNull { enchantment ->
                val levelTag = enchantments.getTag(enchantment)
                val baseLevel = (levelTag as? NBTTagInt)?.int ?: return@mapNotNull null

                if (enchantment == "efficiency" && baseLevel > 5 && !promisingTools.contains(sbId)) {
                    val silexes = baseLevel - 5
                    breakdown.addText("Silexes", "§e${silexes}§7/§e5", getPrice("SIL_EX") * silexes)
                }

                val tier = if (stackingEnchants.contains(enchantment)) 1 else baseLevel
                val enchantId = "ENCHANTMENT_${enchantment.uppercase()}_$tier"

                val value = getPrice(enchantId).takeIf { it != 0.0 }
                    ?: getBookPriceFromT1s(enchantId).takeIf { it != 0.0 }
                    ?: return@mapNotNull null

                enchantment.formatEnchantment(tier) to value
            }.toMap(mutableMapOf())

            breakdown.addMulti("Enchantments", enchantmentValue)
        }

        return breakdown.total to breakdown
    }

    sealed class Component {
        abstract val value: Double
        abstract fun format(): List<String>
    }

    data class SimpleComponent(
        val name: String,
        override val value: Double
    ) : Component() {
        override fun format() = listOf("§7$name: §6${value.formatCoins()}")
    }

    data class TextComponent(
        val name: String,
        val text: String,
        override val value: Double
    ) : Component() {
        override fun format() = listOf("§7$name: $text §7(§6${value.formatCoins()}§7)")
    }

    data class MultiComponent(
        val name: String,
        override val value: Double,
        val items: Map<String, Double>?
    ) : Component() {
        override fun format() = buildList {
            add("§7$name: §6${value.formatCoins()}")
            items?.entries
                ?.sortedByDescending { it.value }
                ?.forEach { (k, v) ->
                    add("  §7$k: §7(§6${v.formatCoins()}§7)")
                }
        }
    }

    data class ItemValueBreakdown(
        val components: MutableList<Component> = mutableListOf()
    ) {
        val total: Double
            get() = components.sumOf { it.value }

        fun format(): List<String> = buildList {
            add("§aTotal Value: §6${total.format()} coins")
            components.filter { it.value > 0 }
                .flatMap { it.format() }
                .forEach(::add)
        }

        private fun addComponent(component: Component): Double {
            components.add(component)
            return component.value
        }

        fun add(name: String, price: Double?) {
            price?.takeIf { it > 0 }?.let {
                addComponent(SimpleComponent(name, it))
            }
        }

        fun addText(name: String, text: String?, price: Double?) {
            price?.takeIf { it > 0 }?.let {
                addComponent(TextComponent(name, text ?: return, it))
            }
        }

        fun addMulti(name: String, items: Map<String, Double>?) {
            items?.takeIf { it.isNotEmpty() }?.let {
                addComponent(MultiComponent(name, it.values.sum(), it))
            }
        }
    }
}