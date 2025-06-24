package com.ems.ui.components

import com.ems.domain.Employee
import com.vaadin.flow.component.*
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.progressbar.ProgressBar
import com.vaadin.flow.component.textfield.EmailField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.ValidationException
import com.vaadin.flow.data.validator.EmailValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class EmployeeFormDialog(
    private val onSave: suspend (Employee) -> Unit,
    private val onDelete: ((Employee) -> Unit)? = null
) : Dialog() {

    // State
    private var isLoading = false
    private var currentEmployee: Employee? = null

    // Form Components
    private val header = H2("Employee Form").apply {
        style["margin-top"] = "0"
    }
    private val progressBar = ProgressBar().apply {
        isIndeterminate = true
        isVisible = false
    }
    private val firstName = TextField("First Name").apply {
        isRequired = true
        setRequiredIndicatorVisible(true)
    }
    private val lastName = TextField("Last Name").apply {
        isRequired = true
        setRequiredIndicatorVisible(true)
    }
    private val email = EmailField("Email").apply {
        isRequired = true
        setRequiredIndicatorVisible(true)
    }
    private val position = TextField("Position")
    private val department = TextField("Department")

    // Action Buttons
    private val saveButton = Button("Save", VaadinIcon.CHECK.create()).apply {
        addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        isDisableOnClick = true
    }
    private val cancelButton = Button("Cancel")
    private val deleteButton = Button("Delete", VaadinIcon.TRASH.create()).apply {
        addThemeVariants(ButtonVariant.LUMO_ERROR)
        isVisible = false
    }

    // Binder with advanced validation
    private val binder = Binder<Employee>(Employee::class.java).apply {
        forField(firstName)
            .asRequired("Required")
            .withValidator({ it.length >= 2 }, "Minimum 2 characters")
            .bind(Employee::firstName, Employee::firstName::set)

        forField(email)
            .asRequired("Required")
            .withValidator(EmailValidator("Invalid email"))
            .bind(Employee::email, Employee::email::set)

        // Other field bindings...
    }

    init {
        configureDialog()
        buildLayout()
        setupEventHandlers()
    }

    fun open(employee: Employee? = null) {
        currentEmployee = employee?.copy() ?: Employee()
        binder.readBean(currentEmployee)
        deleteButton.isVisible = (employee != null && onDelete != null)
        open()
    }

    private fun configureDialog() {
        isCloseOnEsc = true
        isCloseOnOutsideClick = false
        setWidth("600px")
        setDraggable(true)
        setResizable(true)
    }

    private fun buildLayout() {
        val formLayout = FormLayout().apply {
            add(firstName, lastName, email, position, department)
            setResponsiveSteps(FormLayout.ResponsiveStep("0", 2))
        }

        val buttonLayout = HorizontalLayout(saveButton, deleteButton, cancelButton).apply {
            justifyContentMode = FlexComponent.JustifyContentMode.END
        }

        add(header, progressBar, formLayout, buttonLayout)
    }

    private fun setupEventHandlers() {
        saveButton.addClickListener { handleSave() }
        cancelButton.addClickListener { close() }
        deleteButton.addClickListener { handleDelete() }
    }

    private fun handleSave() {
        if (isLoading) return

        try {
            val employee = currentEmployee ?: Employee()
            binder.writeBean(employee) // Validate form
            close()
            CoroutineScope(Dispatchers.IO).launch {
                onSave(employee)
//                ui.get().access {
//
//                }
            }
            setLoading(true)
        } catch (e: ValidationException) {
            Notification.show("Fix validation errors", 3000, Notification.Position.MIDDLE)
        }

    }

    // Required extension property
    private val Dialog.isOpened: Boolean
        get() = element.isVisible

    private fun handleDelete() {
        currentEmployee?.let { employee ->
            ConfirmDialog(
                "Confirm Delete",
                "Delete ${employee.firstName} ${employee.lastName}?",
                "Delete", {
                    onDelete?.invoke(employee)
                    close()
                },
                "Cancel", {}
            ).open()
        }
    }

    private fun setLoading(loading: Boolean) {
        isLoading = loading
        progressBar.isVisible = loading
        saveButton.isEnabled = !loading
        deleteButton.isEnabled = !loading
        cancelButton.isEnabled = !loading
    }

    // Custom Events
    class SaveEvent(source: Component, val employee: Employee) : ComponentEvent<Component>(source, false)
    class DeleteEvent(source: Component, val employee: Employee) : ComponentEvent<Component>(source, false)
}