//package catgirlroutes.module.impl.misc
//
//import catgirlroutes.module.Category
//import catgirlroutes.module.Module
//import catgirlroutes.utils.ConfigSystem
//import catgirlroutes.utils.Island
//import com.google.gson.reflect.TypeToken
//import net.minecraft.util.Vec3
//import java.io.File
//
//object Walker : Module(
//    name = "Walker",
//    category = Category.MISC
//)  {
//
//    private val walkerFile = File("config/catgirlroutes/walker.json")
//
//    var walkPoints: MutableList<WalkerClass> = loadConfig()
//
//    private fun loadConfig(): MutableList<WalkerClass> {
//        return ConfigSystem.loadConfig(walkerFile, object : TypeToken<MutableList<WalkerClass>>() {}.type) ?: mutableListOf()
//    }
//
//    private fun saveConfig() {
//        ConfigSystem.saveConfig(walkerFile, walkPoints)
//    }
//
//
//}
//
//data class WalkerClass(var pos: Vec3, var action: String, var yaw: Float, var pitch: Float, var area: Island)