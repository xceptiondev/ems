package com.ems.ui.views

import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.applayout.DrawerToggle
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.sidenav.SideNav
import com.vaadin.flow.component.sidenav.SideNavItem
import com.vaadin.flow.router.Route
import com.vaadin.flow.theme.lumo.LumoUtility

@Route("")
class MainView : AppLayout() {
    init {
        createHeader()
        createDrawer()
    }

    private fun createHeader() {
        val logo = H1("Employee Management")
        logo.addClassNames(
            LumoUtility.FontSize.LARGE,
            LumoUtility.Margin.MEDIUM
        )

        val header = HorizontalLayout(DrawerToggle(), logo)
        header.defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        header.expand(logo)
        header.setWidthFull()
        header.addClassNames(
            LumoUtility.Padding.Vertical.NONE,
            LumoUtility.Padding.Horizontal.MEDIUM
        )

        addToNavbar(header)
    }

    private fun createDrawer() {
        val employeeNav = SideNav().apply {
            addItem(
                SideNavItem("Employee List", EmployeeListView::class.java, VaadinIcon.LIST.create())
            )
        }

        addToDrawer(VerticalLayout(employeeNav))
    }
}