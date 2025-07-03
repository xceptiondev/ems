package com.ems.ui.components

import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.upload.Upload
import com.ems.domain.Employee
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
import com.vaadin.flow.component.html.H5
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.progressbar.ProgressBar
import com.vaadin.flow.component.textfield.EmailField
import com.vaadin.flow.component.textfield.NumberField
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.validator.EmailValidator
import com.vaadin.flow.server.streams.UploadHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.util.Base64

class EmployeeFormDialog(
    private val employeeService: MyEmployeeService,
    private val onSave: suspend (Employee) -> Unit,
    private val onDelete: ((Employee) -> Unit)? = null
) : Dialog() {

    // State
    private var isLoading = false
    private var currentEmployee: Employee? = null
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
                val isAvailable = employeeService.isEmailAvailable(email, currentEmployee?.id)
                ui.access {
                    this@EmployeeFormDialog.email.isInvalid = !isAvailable
                    this@EmployeeFormDialog.email.errorMessage =
                        if (!isAvailable) "Email already registered" else null
                    isCheckingEmail = false
                    updateSaveButtonState()
                }
            } catch (e: Exception) {
                ui.access {
                    this@EmployeeFormDialog.email.isInvalid = true
                    this@EmployeeFormDialog.email.errorMessage = "Error checking email availability"
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
    private val binder = Binder<Employee>(Employee::class.java).apply {
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
            .bind(Employee::firstName, Employee::firstName::set)

        // Last Name
        forField(lastName)
            .asRequired("Last name is required")
            .withValidator(
                { it.isNotBlank() && it.length >= 2 },
                "Must be at least 2 characters"
            )
            .bind(Employee::lastName, Employee::lastName::set)

        // Email
        forField(email)
            .asRequired("Email is required")
            .withValidator(EmailValidator("Invalid email format"))
            .withValidator({ value ->
                runBlocking {
                    try {
                        employeeService.isEmailAvailable(value, currentEmployee?.id)
                    } catch (e: Exception) {
                        false
                    }
                }
            }, "Email already registered")
            .bind(Employee::email, Employee::email::set)

        // Position
        forField(position)
            .withValidator(
                { it.isNotBlank() },
                "Position cannot be empty"
            )
            .bind(Employee::position, Employee::position::set)

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
            .bind(Employee::department, Employee::department::set)

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
            .bind(Employee::salary, Employee::salary::set)

        // Hire Date
        forField(hireDate)
            .withValidator(
                { it != null && !it.isAfter(LocalDate.now()) },
                "Hire date cannot be in the future"
            )
            .bind(Employee::hireDate, Employee::hireDate::set)

        // Phone Number
        forField(phoneNumber)
            .withValidator(
                { it.isNullOrBlank() || it.matches(Regex("^[+\\d\\s-]{10,}\$")) },
                "Invalid phone number format"
            )
            .bind(Employee::phoneNumber, Employee::phoneNumber::set)

        // Address
        forField(address)
            .withValidator(
                { it.isNullOrBlank() || it.length >= 5 },
                "Address too short"
            )
            .bind(Employee::address, Employee::address::set)

        addStatusChangeListener { event -> saveButton.isEnabled = event.binder.isValid && !isLoading }

        writeBeanIfValid(Employee()) // Initialize with empty employee
    }

    private var uploadedPassportBytes: ByteArray? = null
    private val passportPreview = Image().apply {
        height = "120px"
        isVisible = false
        style["border"] = "1px dashed #ccc"
    }

    // Create in-memory upload handler
    private val passportUploadHandler = UploadHandler.inMemory { metadata, data ->
        uploadedPassportBytes = data
        currentEmployee?.passportPhoto = data

        UI.getCurrent().access {
            passportPreview.src = "data:${metadata.contentType};base64," +
                    Base64.getEncoder().encodeToString(data)
            passportPreview.isVisible = true
            clearUploadButton.isVisible = true
            Notification.show("Passport photo uploaded", 2000, Notification.Position.MIDDLE)
        }
    }

    // Upload component
    private val upload = Upload(passportUploadHandler).apply {
        setAcceptedFileTypes("image/jpeg", "image/png")
        maxFileSize = 2 * 1024 * 1024 // 2MB limit
        isDropAllowed = true
        width = "100%"
        height = "100px"

        addFileRejectedListener { event ->
            Notification.show(event.errorMessage, 3000, Notification.Position.MIDDLE)
        }
    }
    private val clearUploadButton = Button("Remove", VaadinIcon.TRASH.create()).apply {
        addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL)
        isVisible = false
        addClickListener {
            uploadedPassportBytes = null
            currentEmployee?.passportPhoto = null
            passportPreview.isVisible = false
            isVisible = false
        }
    }
    private fun buildUploadSection() = VerticalLayout().apply {
        add(H5("Passport Photo"), upload, passportPreview, clearUploadButton)
        width = "100%"
        alignItems = FlexComponent.Alignment.CENTER
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

        currentEmployee?.passportPhoto?.let { bytes ->
            uploadedPassportBytes = bytes
            passportPreview.src = "data:image/jpeg;base64,${Base64.getEncoder().encodeToString(bytes)}"
            passportPreview.isVisible = true
            clearUploadButton.isVisible = true
        } ?: run {
            // Clear photo state if no photo exists
            passportPreview.isVisible = false
            clearUploadButton.isVisible = false
            uploadedPassportBytes = null
        }

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
            val passportSection = buildUploadSection()
            add(passportSection, firstName, lastName, email, position, department, salary, hireDate, phoneNumber, address)
            setResponsiveSteps(FormLayout.ResponsiveStep("0", 2))
            setColspan(passportSection, 2)
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