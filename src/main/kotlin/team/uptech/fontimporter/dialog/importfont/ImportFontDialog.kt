package team.uptech.fontimporter.dialog.importfont

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.ui.layout.LayoutBuilder
import team.uptech.fontimporter.dialog.core.BaseDialog
import team.uptech.fontimporter.dialog.ui.FontsChooserRow
import team.uptech.fontimporter.dialog.ui.FontsTextArea
import team.uptech.fontimporter.dialog.ui.ModuleSelectionDropDown
import team.uptech.fontimporter.dialog.ui.core.CoreUi
import team.uptech.fontimporter.dialog.ui.core.DropDownUi
import team.uptech.fontimporter.dialog.ui.core.TextAreaUi
import team.uptech.fontimporter.localization.Localization
import team.uptech.fontimporter.utils.buildImportSuccessNotification
import team.uptech.fontimporter.utils.files.ResourcesUtil
import java.io.IOException


class ImportFontDialog(
    project: Project?,
    presenter: ImportFontAgreement.Presenter
) : BaseDialog<ImportFontAgreement.View, ImportFontAgreement.Presenter>(project, presenter), ImportFontAgreement.View {

    private val moduleSelectionDropDown: DropDownUi = ModuleSelectionDropDown(presenter.getModuleDropDownItems())
    private val fontsTextArea: TextAreaUi = FontsTextArea()
    private val fontsChooserRow: CoreUi = FontsChooserRow { openFileChooserDialog() }

    init {
        title = Localization.getString("dialog.title.import")
        setOKButtonText(Localization.getString("action.import"))
        setCancelButtonText(Localization.getString("action.cancel"))

        isOKActionEnabled = false
        init()

        presenter.bindView(this)
    }

    override fun buildBody(layoutBuilder: LayoutBuilder) {
        moduleSelectionDropDown.buildUi(layoutBuilder)
        fontsTextArea.buildUi(layoutBuilder)
        fontsChooserRow.buildUi(layoutBuilder)
    }

    private fun openFileChooserDialog() {
        val descriptor = FileChooserDescriptorFactory.createMultipleFilesNoJarsDescriptor()
        FileChooser.chooseFiles(descriptor, project, null) { selectedFonts ->
            presenter.saveSelectedFonts(selectedFonts)
        }
    }

    override fun updateDialogUi(fontsBeforeRenaming: String, fontsAfterRenaming: String) {
        fontsTextArea.setTexts(fontsBeforeRenaming, fontsAfterRenaming)
        isOKActionEnabled = true
    }

    override fun doOKAction() {
        super.doOKAction()
        try {
            val module = moduleSelectionDropDown.getDropDownSelectedItem() ?: ResourcesUtil.DEFAULT_MODULE
            presenter.importSelectedFontToModule(module)
        } catch (error: IOException) {
            showError(project, error.message)
        }
    }

    override fun showNoResFolderError(module: String) {
        showError(project, Localization.getString("exception.message.no_res_folder", module))
    }

    override fun showNoFontFolderError(module: String) {
        showError(project, Localization.getString("exception.message.no_font_folder", module))
    }

    override fun showImportSuccess(filesCount: Int, replacedFilesCount: Int, path: String) {
        buildImportSuccessNotification(project, filesCount, replacedFilesCount, path)
    }

    override fun dialogWidth(): Int {
        return 800
    }

    override fun dialogHeight(): Int {
        return 350
    }
}