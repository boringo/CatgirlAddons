package catgirlroutes.ui.misc.customtriggers

import catgirlroutes.ui.Screen
import catgirlroutes.ui.clickgui.util.MouseUtils.mouseX
import catgirlroutes.ui.clickgui.util.MouseUtils.mouseY
import catgirlroutes.ui.misc.elements.impl.*
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedBorderedRect

class TriggerGUI : Screen() { // todo figure auroraui and use it for all guis cuz this is pmo

    var guiWidth = 0.0
    var guiHeight = 0.0

    var triggerPanelWidth = 0.0
    var mainPanelWidth = 0.0

    val padding = 8.0
    val rad = 10.0

    lateinit var searchBar: MiscElementTextField
    lateinit var addTriggerButton: MiscElementButton

    var x = 0.0
    var y = 0.0

    val theme = DarkTheme

//    private lateinit var triggerScrollPanel: MiscElementScrollPanel

    override fun onInit() {

        guiWidth = (width * 0.7).coerceAtMost(720.0)
        guiHeight = (height * 0.7).coerceAtMost(420.0)

        triggerPanelWidth = guiWidth * 0.25
        mainPanelWidth = guiWidth - triggerPanelWidth

        x = getX(guiWidth)
        y = getY(guiHeight)

        val searchWidth = triggerPanelWidth - padding * 2
        val searchHeight = (guiHeight * 0.06).coerceAtMost(25.0)
        val searchX = x + padding
        val searchY = y + padding

        searchBar = textField {
            at(searchX, searchY)
            size(searchWidth, searchHeight)
            placeholder = "Search..."
            textColour = theme.textPrimary
            colour = theme.card
            outlineColour = theme.border
            outlineHoverColour = theme.border
            thickness = 0.5
            radius = rad
        }

        addTriggerButton = button {
            text = "Add Trigger"
            at(searchX, searchY + searchHeight + padding)
            size(searchWidth, searchHeight)
            textColour = theme.textPrimary
            colour = theme.card
            outlineColour = theme.border
            hoverColour = theme.hoverCard
            outlineHoverColour = theme.hoverCard
            thickness = 0.5
            radius = rad
        }

        updateTriggers()
    }


    override fun draw() {
        drawRoundedBorderedRect(x - padding, y - padding, guiWidth + padding * 2 + padding / 2, guiHeight + padding * 2, rad, 0.5, theme.background, theme.border)
        drawRoundedBorderedRect(x, y, triggerPanelWidth, guiHeight, rad, 1.0, theme.panel, theme.border)

        drawRoundedBorderedRect(x + triggerPanelWidth + padding / 2.0, y, mainPanelWidth, guiHeight, rad, 0.5, theme.panel, theme.border)
        searchBar.draw(mouseX, mouseY)
        addTriggerButton.draw(mouseX, mouseY)
//        triggerScrollPanel.draw(mouseX, mouseY)
    }

    override fun onMouseClick(mouseButton: Int) {
//        triggerScrollPanel.onMouseClick(mouseX, mouseY, mouseButton)
    }

    override fun onScroll(amount: Int) {
//        triggerScrollPanel.onScroll(mouseX, mouseY, amount)
    }

    override fun onGuiClosed() {
        searchBar.isFocused = false
//        TriggerManager.save()
    }

    private fun updateTriggers() {

    }
}