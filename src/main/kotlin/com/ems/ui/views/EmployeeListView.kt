package com.ems.ui.views

import com.ems.domain.Employee
import com.ems.services.EmployeeService
import com.ems.ui.components.EmployeeFormDialog
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Route(value = "employees", layout = MainView::class)
class EmployeeListView(
    private val employeeService: EmployeeService,
) : VerticalLayout() {

    private val grid = Grid<Employee>()
    private val filter = TextField()
    private val addButton = Button("Add Employee", VaadinIcon.PLUS.create())

    private val employeeDialog = EmployeeFormDialog(
        onSave = { employee ->
            CoroutineScope(Dispatchers.IO).launch {
                employeeService.save(employee)
                updateList()
            }
        },
        onDelete = { employee ->
            CoroutineScope(Dispatchers.IO).launch {
                employeeService.delete(employee.id!!)
            }
            updateList()
        }
    )

    init {
        configureGrid()
        buildLayout()
        updateList()
    }

    private fun buildLayout() {
        addButton.addClickListener {
            employeeDialog.open()
        }

        add(
            HorizontalLayout(H2("Employee Management"), addButton),
            filter,
            grid
        )
    }

    private fun configureGrid() {
        grid.addColumn(Employee::firstName).setHeader("First Name")
        grid.addColumn(Employee::lastName).setHeader("Last Name")

        grid.addComponentColumn { employee ->
            Button(VaadinIcon.EDIT.create()) {
                employeeDialog.open(employee)
            }.apply {
                addThemeVariants(ButtonVariant.LUMO_SMALL)
                setTooltipText("Edit employee")
            }
        }.setHeader("Actions")
    }

    private fun updateList() {
        CoroutineScope(Dispatchers.IO).launch {
            val employees = employeeService.findAll()
            ui.get().access {
                grid.setItems (employees)
            }
        }
    }
}