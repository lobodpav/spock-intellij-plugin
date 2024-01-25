package io.github.lobodpav.spock.extension.testCreator.dialog

import io.github.lobodpav.spock.extension.fileType.Specification

data class CreateSpecificationDialogModel(
    var className: String,
    var destinationModule: ModuleSelectorItem,
    var destinationPackage: String,
    var superClass: SpecificationParent = Specification.parent,
)
