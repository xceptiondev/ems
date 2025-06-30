package com.ems.ui.components

import com.ems.domain.MyEmployee
import com.ems.services.MyEmployeeService
import com.vaadin.flow.component.*
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.progressbar.ProgressBar
import com.vaadin.flow.component.textfield.EmailField
import com.vaadin.flow.component.textfield.NumberField
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.validator.EmailValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class MyEmployeeFormDialog(
    private val myEmployeeService: MyEmployeeService,
    private val onSave: suspend (MyEmployee) -> Unit,
    private val onDelete: ((MyEmployee) -> Unit)? = null
) : Dialog() {

    // State
    private var isLoading = false
    private var currentEmployee: MyEmployee? = null
    val ui = UI.getCurrent()

    // Form Components
    private val header = H2("Employee Form").apply {
        style["margin-top"] = "0"
    }
    private val progressBar = ProgressBar().apply {
        isIndeterminate = true
        isVisible = false
    }
    // Form fields (declare these as class properties)
    private val firstName = TextField("First Name").apply {
        setRequiredIndicatorVisible(true)
        setWidthFull()
    }

    private val lastName = TextField("Last Name").apply {
        setRequiredIndicatorVisible(true)
        setWidthFull()
    }

    private val email = EmailField("Email").apply {
        setRequiredIndicatorVisible(true)
        setWidthFull()
        addValueChangeListener {
            if (it.value.isNotBlank() && it.value != currentEmployee?.email) {
                checkEmailAvailability(it.value)
            }
        }
    }
    private var isCheckingEmail = false

    private fun checkEmailAvailability(email: String) {
        isCheckingEmail = true
        this.email.isInvalid = false
        this.email.errorMessage = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isAvailable = myEmployeeService.isEmailAvailable(email, currentEmployee?.id)
                ui.access {
                    this@MyEmployeeFormDialog.email.isInvalid = !isAvailable
                    this@MyEmployeeFormDialog.email.errorMessage =
                        if (!isAvailable) "Email already registered" else null
                    isCheckingEmail = false
                    updateSaveButtonState()
                }
            } catch (e: Exception) {
                ui.access {
                    this@MyEmployeeFormDialog.email.isInvalid = true
                    this@MyEmployeeFormDialog.email.errorMessage = "Error checking email availability"
                    isCheckingEmail = false
                    updateSaveButtonState()
                }
            }
        }
    }
    private fun updateSaveButtonState() {
        saveButton.isEnabled = binder.isValid && !isLoading && !isCheckingEmail
    }
    private val position = TextField("Position").apply {
        setWidthFull()
        addValueChangeListener { validateSalaryForManager() }
    }

    private val department = ComboBox<String>("Department").apply {
        setItems("HR", "Engineering", "Finance", "Operations")
        setWidthFull()
    }

    private val salary = NumberField("Salary").apply {
        setPrefixComponent(Span("\$"))
        setWidthFull()
    }

    private val hireDate = DatePicker("Hire Date").apply {
        setWidthFull()
    }

    private val phoneNumber = TextField("Phone").apply {
        setPattern("^[+\\d\\s-]*\$") // Live validation
        setWidthFull()
    }

    private val address = TextArea("Address").apply {
        setWidthFull()
        setMaxLength(200)
    }

    // Action Buttons
    private val saveButton = Button("Save", VaadinIcon.CHECK.create()).apply {
        addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        isDisableOnClick = true
        isEnabled = false
    }
    private val cancelButton = Button("Cancel")
    private val deleteButton = Button("Delete", VaadinIcon.TRASH.create()).apply {
        addThemeVariants(ButtonVariant.LUMO_ERROR)
        isVisible = false
    }

    // Binder with advanced validation
    private val binder = Binder<MyEmployee>(MyEmployee::class.java).apply {
        // First Name
        forField(firstName)
            .asRequired("First name is required")
            .withValidator(
                { it.isNotBlank() && it.length >= 2 },
                "Must be at least 2 characters"
            )
            .withValidationStatusHandler { status ->
                firstName.isInvalid = status.isError
                firstName.errorMessage = status.message.orElse(null)
            }
            .bind({ it.firstName }, { obj, value -> obj.copy(firstName = value) })

        // Last Name
        forField(lastName)
            .asRequired("Last name is required")
            .withValidator(
                { it.isNotBlank() && it.length >= 2 },
                "Must be at least 2 characters"
            )
            .bind(
                { it.lastName },
                { obj, value -> obj.copy(lastName = value) }
            )

        // Email
        forField(email)
            .asRequired("Email is required")
            .withValidator(EmailValidator("Invalid email format"))
            .withValidator({ value ->
                runBlocking {
                    try {
                        myEmployeeService.isEmailAvailable(value, currentEmployee?.id)
                    } catch (e: Exception) {
                        false
                    }
                }
            }, "Email already registered")
            .bind(
                { it.email },
                { obj, value -> obj.copy(email = value) }
            )

        // Position
        forField(position)
            .withValidator(
                { it.isNotBlank() },
                "Position cannot be empty"
            )
            .bind({ it.position },{ obj, value -> obj.copy(position = value) })

        // Department
        forField(department)
            .withConverter(
                String::trim,
                { it }
            )
            .withValidator(
                { it.isNotBlank() },
                "Please select a department"
            )
            .bind({ it.department },{ obj, value -> obj.copy(department = value) })

        // Salary
        forField(salary)
            .withConverter(
                { it ?: 0.0 },
                { it.toDouble() }
            )
            .withValidator(
                { it >= 0 },
                "Salary cannot be negative"
            )
            .bind({ it.salary },{ obj, value -> obj.copy(salary = value) })

        // Hire Date
        forField(hireDate)
            .withValidator(
                { it != null && !it.isAfter(LocalDate.now()) },
                "Hire date cannot be in the future"
            )
            .bind({ it.hireDate },{ obj, value -> obj.copy(hireDate = value) })

        // Phone Number
        forField(phoneNumber)
            .withValidator(
                { it.isNullOrBlank() || it.matches(Regex("^[+\\d\\s-]{10,}\$")) },
                "Invalid phone number format"
            )
            .bind({ it.phoneNumber },{ obj, value -> obj.copy(phoneNumber = value) })

        // Address
        forField(address)
            .withValidator(
                { it.isNullOrBlank() || it.length >= 5 },
                "Address too short"
            )
            .bind({ it.address },{ obj, value -> obj.copy(address = value) })

        addStatusChangeListener { event -> saveButton.isEnabled = event.binder.isValid && !isLoading }

        //writeBeanIfValid(MyEmployee()) // Initialize with empty employee
    }

    init {
        configureDialog()
        buildLayout()
        setupEventHandlers()
    }

    fun open(employee: MyEmployee? = null) {
        currentEmployee = employee?.copy() ?: MyEmployee()
        binder.readBean(currentEmployee)
        deleteButton.isVisible = (employee != null && onDelete != null)
        open()
    }
    private fun validateSalaryForManager() {
        val isManager = position.value.equals("Manager", ignoreCase = true)
        val isSalaryInvalid = (salary.value ?: 0.0) < 5000

        salary.isInvalid = isManager && isSalaryInvalid
        salary.errorMessage = if (salary.isInvalid) "Managers need ≥ 5000" else null
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
            add(firstName, lastName, email, position, department, salary, hireDate, phoneNumber, address)
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
        val employee = currentEmployee ?: Employee()
        if(binder.writeBeanIfValid(employee)){
            setLoading(true)
            CoroutineScope(Dispatchers.IO).launch{
                onSave(employee)
                ui.access{
                    setLoading(false)
                    close()
                }
            }
        }

    }

    private fun handleDelete() {
        currentEmployee?.let { employee ->
            val deleteDialog = ConfirmDialog(
                "Confirm Delete",
                "Delete ${employee.firstName} ${employee.lastName}?",
                "Delete", {
                    it.source.close()
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