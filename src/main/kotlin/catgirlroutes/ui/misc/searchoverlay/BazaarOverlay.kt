package catgirlroutes.ui.misc.searchoverlay

import catgirlroutes.module.impl.misc.Inventory.bzHistory
import catgirlroutes.utils.NeuRepo
import catgirlroutes.utils.RepoItem
import net.minecraft.tileentity.TileEntitySign

class BazaarOverlay(sign: TileEntitySign? = null) : SearchOverlay(sign) {

    override var history: MutableList<String>
        get() = bzHistory
        set(value) {
            bzHistory = value
        }

    override var commandPrefix: String = "bz"

    override fun filterItems(query: String): List<RepoItem> {
        return NeuRepo.repoItems.filter { item ->
            item.bazaar &&
            (item.name.contains(query, true) ||
            item.lore.any { it.contains(query, true) })
        }
    }
}