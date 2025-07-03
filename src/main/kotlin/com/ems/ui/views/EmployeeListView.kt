package com.ems.ui.views

import com.ems.domain.Employee
import com.ems.services.EmployeeService
import com.ems.services.MyEmployeeService
import com.ems.ui.components.EmployeeFormDialog
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Route(value = "employees", layout = MainView::class)
class EmployeeListView(
    private val employeeService: MyEmployeeService,
) : VerticalLayout() {

    val ui = UI.getCurrent()
    private val grid = Grid<Employee>()
    private val filter = TextField().apply {
        placeholder = "Filter by name or email"
        setPrefixComponent(VaadinIcon.SEARCH.create())
        setWidth("300px")
        setClearButtonVisible(true)

        // For debouncing the input
        var filterJob: Job? = null
        val coroutineScope = CoroutineScope(Dispatchers.Default)

        addValueChangeListener { event ->
            filterJob?.cancel() // Cancel previous request if still pending
            filterJob = coroutineScope.launch {
                delay(300) // Wait for 300ms of inactivity
                updateList(event.value)
            }
        }

        // Clear the job when component is detached
        addDetachListener {
            filterJob?.cancel()
        }
    }
    private val addButton = Button("Add Employee", VaadinIcon.PLUS.create()){
        employeeDialog.open(null)
    }

    private val employeeDialog = EmployeeFormDialog(
        employeeService = employeeService,
        onSave = { employee ->
            CoroutineScope(Dispatchers.IO).launch{
                employeeService.save(employee)
                updateList()
            }
        },
        onDelete = { employee ->
            CoroutineScope(Dispatchers.IO).launch {
                employeeService.delete(employee.id!!)
                updateList()
            }
        }
    )

    init {
        configureGrid()
        buildLayout()
        updateList()

        grid.setSizeFull()
        //grid.setHeightByRows(true)
        grid.setPageSize(20)
        grid.setMultiSort(true)
    }

    private fun buildLayout() {
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)

        val header = HorizontalLayout(
            H2("Employee Management").apply { style["margin"] = "0" },
            addButton
        ).apply {
            justifyContentMode = FlexComponent.JustifyContentMode.BETWEEN
            alignItems = FlexComponent.Alignment.CENTER
            setWidthFull()
        }

        val filterLayout = HorizontalLayout(filter).apply {
            justifyContentMode = FlexComponent.JustifyContentMode.CENTER
            setWidthFull()
        }

        add(header, filterLayout, grid)
        setSizeFull()
        expand(grid)
    }

    private fun configureGrid() {
        grid.setSelectionMode(Grid.SelectionMode.SINGLE)
        grid.addColumn(Employee::firstName).setHeader("First Name").setSortable(true)
        grid.addColumn(Employee::lastName).setHeader("Last Name").setSortable(true)
        grid.addColumn(Employee::email).setHeader("Email").setSortable(true)
        grid.addColumn(Employee::position).setHeader("Position")

        // Enhanced action buttons
        grid.addComponentColumn { employee ->
            HorizontalLayout().apply {
                spacing = "true"
                add(
                    Button(VaadinIcon.EDIT.create()) {
                        employeeDialog.open(employee)
                    }.apply {
                        addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY)
                        setTooltipText("Edit employee")
                    },
                    Button(VaadinIcon.TRASH.create()) {
                        showDeleteConfirmation(employee)
                    }.apply {
                        addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR)
                        setTooltipText("Delete employee")
                    }
                )
            }
        }.setHeader("Actions").setFlexGrow(0)

        // Add double-click to edit
        grid.addItemClickListener { event ->
            if (event.clickCount == 2) {
                event.item?.let { employeeDialog.open(it) }
            }
        }
    }
    private fun showDeleteConfirmation(employee: Employee) {
        ConfirmDialog(
            "Confirm Delete",
            "Are you sure you want to delete ${employee.firstName} ${employee.lastName}?",
            "Delete", {
                CoroutineScope(Dispatchers.IO).launch {
                    employeeService.delete(employee.id!!)
                    ui.access { updateList() }
                }
            },
            "Cancel", {}
        ).open()
    }
    private fun updateList(filter: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            val employees = employeeService.findAll().filter { employee ->
                filter.isNullOrBlank() ||
                        employee.firstName.contains(filter, ignoreCase = true) ||
                        employee.lastName.contains(filter, ignoreCase = true) ||
                        employee.email.contains(filter, ignoreCase = true)
            }
            ui.access {
                grid.setItems(employees)
            }
        }
    }
}